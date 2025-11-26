package com.example.warofwonders.ui.screens.contacts

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.pathUsers
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.messaging.FirebaseMessaging

data class Contact(
    val id: String,
    val name: String,
    val phone: String
)

data class FriendRelations(
    val friends: List<Pair<String, Contact>>,
    val incomingRequests: List<Pair<String, Contact>>,
    val availableToRequest: List<Pair<String, Contact>>
)

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(2.dp)
            .background(Color.White.copy(alpha = 0.5f))
            .padding(vertical = 8.dp)
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)

    Log.d("ContactsScreen", "Composable creado")

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                Log.d("ContactsScreen", "POST_NOTIFICATIONS resultado: $isGranted")
            }
        )

    LaunchedEffect(Unit) {
        Log.d("ContactsScreen", "LaunchedEffect START")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Log.d("ContactsScreen", "Solicitando permiso POST_NOTIFICATIONS…")
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Log.d("ContactsScreen", "API < 33, no se pide POST_NOTIFICATIONS")
        }

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid
        Log.d("ContactsScreen", "currentUid: $currentUid")

        if (currentUid != null) {
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d("FCMToken", "Token obtenido en ContactsScreen: $token")

                        val usersRef = database.getReference(pathUsers)
                        val path = "$pathUsers$currentUid/fcmToken"
                        Log.d("FCMToken", "Guardando token en: $path")

                        usersRef.child(currentUid).child("fcmToken")
                            .setValue(token)
                            .addOnSuccessListener {
                                Log.d("FCMToken", "Token guardado correctamente")
                            }
                            .addOnFailureListener { e ->
                                Log.e("FCMToken", "Error guardando token", e)
                            }
                    } else {
                        Log.e("FCMToken", "Error getting token", task.exception)
                    }
                }
        } else {
            Log.d("FCMToken", "No hay usuario logueado, no se guarda token")
        }
    }

    var friends by remember { mutableStateOf<List<Pair<String, Contact>>>(emptyList()) }
    var incomingRequests by remember { mutableStateOf<List<Pair<String, Contact>>>(emptyList()) }
    var contactsToRequest by remember { mutableStateOf<List<Pair<String, Contact>>>(emptyList()) }
    var reloadKey by remember { mutableStateOf(0L) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.fondocontactos),
            contentDescription = "Fondo contactos",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Log.d(
                "ContactsScreen",
                "Estado permiso contactos: isGranted=${contactsPermissionState.status.isGranted}, shouldShowRationale=${contactsPermissionState.status.shouldShowRationale}"
            )

            when {
                contactsPermissionState.status.isGranted -> {
                    Log.d("ContactsScreen", "Permiso contactos concedido, cargando contactos")
                    val contacts = loadContacts(contentResolver)
                    Log.d("ContactsScreen", "Contactos leídos: ${contacts.size}")

                    LaunchedEffect(reloadKey) {
                        Log.d("ContactsScreen", "LaunchedEffect reloadKey=$reloadKey")
                        findFriendsInFirebase(contacts) { matched ->
                            Log.d(
                                "ContactsScreen",
                                "findFriendsInFirebase -> matched=${matched.size}"
                            )
                            loadFriendsAndRequests(matched) { relations ->
                                Log.d(
                                    "ContactsScreen",
                                    "loadFriendsAndRequests -> friends=${relations.friends.size}, incoming=${relations.incomingRequests.size}, avail=${relations.availableToRequest.size}"
                                )
                                friends = relations.friends
                                incomingRequests = relations.incomingRequests
                                contactsToRequest = relations.availableToRequest
                            }
                        }
                    }

                    ContactsSections(
                        friends = friends,
                        requests = incomingRequests,
                        availableToRequest = contactsToRequest,
                        onSendRequest = { uid ->
                            Log.d("FriendRequest", "onSendRequest a uid=$uid")
                            sendFriendRequest(uid, context) {
                                Log.d("FriendRequest", "onSendRequest completado, reloadKey++")
                                reloadKey++
                            }
                        },
                        onAcceptRequest = { uid ->
                            Log.d("FriendRequest", "onAcceptRequest desde uid=$uid")
                            respondToFriendRequest(uid, true, context) {
                                Log.d("FriendRequest", "onAcceptRequest completado, reloadKey++")
                                reloadKey++
                            }
                        },
                        onRejectRequest = { uid ->
                            Log.d("FriendRequest", "onRejectRequest desde uid=$uid")
                            respondToFriendRequest(uid, false, context) {
                                Log.d("FriendRequest", "onRejectRequest completado, reloadKey++")
                                reloadKey++
                            }
                        }
                    )
                }

                contactsPermissionState.status.shouldShowRationale -> {
                    Text(
                        "Por favor conceda el permiso para acceder correctamente a los contactos",
                        modifier = Modifier.padding(30.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Button(
                        onClick = {
                            Log.d("ContactsScreen", "Usuario pulsa 'Conceder permiso' (rationale)")
                            contactsPermissionState.launchPermissionRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
                    ) {
                        Text("Conceder permiso")
                    }
                }

                else -> {
                    Text(
                        "Por favor conceda permiso para agregar amigos",
                        modifier = Modifier.padding(30.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    Button(
                        onClick = {
                            Log.d("ContactsScreen", "Usuario pulsa 'Conceder permiso' (else)")
                            contactsPermissionState.launchPermissionRequest()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C3E50))
                    ) {
                        Text("Conceder permiso")
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsSections(
    friends: List<Pair<String, Contact>>,
    requests: List<Pair<String, Contact>>,
    availableToRequest: List<Pair<String, Contact>>,
    onSendRequest: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            SectionTitle("Solicitudes de amistad")
        }

        if (requests.isEmpty()) {
            item {
                Text(
                    "No tienes solicitudes pendientes",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(requests) { (uid, contact) ->
                FriendRequestCard(
                    contact = contact,
                    uid = uid,
                    onAccept = { onAcceptRequest(uid) },
                    onReject = { onRejectRequest(uid) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(25.dp))
            SectionDivider()
        }

        item {
            SectionTitle("Amigos actuales")
        }

        if (friends.isEmpty()) {
            item {
                Text(
                    "Aún no tienes amigos agregados",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(friends) { (uid, contact) ->
                DrawContactCard(
                    contact = contact,
                    uid = uid,
                    showAddButton = false,
                    onAddFriend = {}
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(25.dp))
            SectionDivider()
        }

        item {
            SectionTitle("Contactos en War of Wonders")
        }

        if (availableToRequest.isEmpty()) {
            item {
                Text(
                    "No hay contactos disponibles para enviar solicitud",
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            items(availableToRequest) { (uid, contact) ->
                DrawContactCard(
                    contact = contact,
                    uid = uid,
                    showAddButton = true,
                    onAddFriend = { onSendRequest(uid) }
                )
            }
        }
    }
}

@Composable
fun DrawContactCard(
    contact: Contact,
    uid: String,
    showAddButton: Boolean,
    onAddFriend: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .height(90.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.chatframe),
            contentDescription = "Fondo contenedor contacto",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()

        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ICONO
            Image(
                painter = painterResource(R.drawable.iconocontacto),
                contentDescription = "Contacto",
                modifier = Modifier
                    .weight(0.15f)
                    .height(40.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // NOMBRE
            Text(
                text = contact.name,
                color = Color.White,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.55f)
            )

            // BOTÓN
            if (showAddButton) {
                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(0.60f)
                        .aspectRatio(3f)
                ) {
                    ImageButton(
                        imageRes = R.drawable.solicitar,
                        contentDescription = "Enviar solicitud",
                        modifier = Modifier.fillMaxSize(),
                        onClick = { onAddFriend(uid) }
                    )
                }
            }


        }
    }
}


@Composable
fun FriendRequestCard(
    contact: Contact,
    uid: String,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .height(90.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.chatframe),
            contentDescription = "Fondo contenedor contacto",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // ICONO
            Image(
                painter = painterResource(R.drawable.iconocontacto),
                contentDescription = "Contacto",
                modifier = Modifier
                    .weight(0.15f)
                    .height(40.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // NOMBRE
            Text(
                text = contact.name,
                color = Color.White,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.45f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            // BOTONES ACEPTAR / RECHAZAR
            Row(
                modifier = Modifier.weight(0.40f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    ImageButton(
                        imageRes = R.drawable.aceptar,
                        contentDescription = "Aceptar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        onClick = onAccept
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    ImageButton(
                        imageRes = R.drawable.rechazar,
                        contentDescription = "Rechazar",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        onClick = onReject
                    )
                }
            }
        }
    }
}


private fun normalizePhone(num: String): String =
    num.filter { it.isDigit() }.takeLast(10)

fun loadContacts(contentResolver: ContentResolver): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val projection = arrayOf(
        ContactsContract.CommonDataKinds.Phone._ID,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
        ContactsContract.CommonDataKinds.Phone.NUMBER
    )

    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        projection,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.let {
        val idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID)
        val nameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberColumn = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val name = cursor.getString(nameColumn)
            val rawNumber = it.getString(numberColumn)
            val normalized = normalizePhone(rawNumber)
            contacts.add(Contact(id, name, normalized))
        }
    }
    cursor?.close()
    return contacts
}

fun findFriendsInFirebase(
    contacts: List<Contact>,
    onResult: (List<Pair<String, Contact>>) -> Unit
) {
    val usersRef = database.getReference(pathUsers)

    usersRef.get().addOnSuccessListener { snapshot ->
        val firebaseUsers = snapshot.children.mapNotNull { userSnap ->
            val uid = userSnap.key ?: return@mapNotNull null
            val phone = userSnap.child("phone").getValue(String::class.java)
            if (phone != null) uid to normalizePhone(phone) else null
        }

        val matched = contacts.mapNotNull { contact ->
            val normalizedContact = contact.phone
            val match = firebaseUsers.find { it.second == normalizedContact }
            match?.let { it.first to contact }
        }

        onResult(matched)
    }
}

fun loadFriendsAndRequests(
    matchedContacts: List<Pair<String, Contact>>,
    onResult: (FriendRelations) -> Unit
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = database.getReference(pathUsers)
    val friendsRef = usersRef.child(currentUid).child("friends")
    val requestsRef = database.getReference("friendRequests").child(currentUid)

    val uidToContactFromPhone = matchedContacts.toMap()

    friendsRef.get().addOnSuccessListener { friendsSnap ->
        requestsRef.get().addOnSuccessListener { reqSnap ->
            usersRef.get().addOnSuccessListener { usersSnap ->
                val usersMap = usersSnap.children.associateBy { it.key ?: "" }

                val friendUids = friendsSnap.children
                    .filter { it.getValue(Boolean::class.java) == true }
                    .mapNotNull { it.key }
                    .toSet()

                val incomingSet = reqSnap.children
                    .filter { it.child("status").getValue(String::class.java) == "pending" }
                    .mapNotNull { it.key }
                    .toSet()

                val friends = friendUids.mapNotNull { uid ->
                    val contactFromPhone = uidToContactFromPhone[uid]
                    val contact = contactFromPhone ?: run {
                        val node = usersMap[uid] ?: return@mapNotNull null
                        val name = node.child("name").getValue(String::class.java) ?: "Jugador"
                        val phone = node.child("phone").getValue(String::class.java) ?: ""
                        Contact(uid, name, normalizePhone(phone))
                    }
                    uid to contact
                }

                val incomingRequests = incomingSet.mapNotNull { uid ->
                    val contactFromPhone = uidToContactFromPhone[uid]
                    val contact = contactFromPhone ?: run {
                        val node = usersMap[uid] ?: return@mapNotNull null
                        val name = node.child("name").getValue(String::class.java) ?: "Jugador"
                        val phone = node.child("phone").getValue(String::class.java) ?: ""
                        Contact(uid, name, normalizePhone(phone))
                    }
                    uid to contact
                }

                val availableToRequest = matchedContacts.filter { (uid, _) ->
                    uid !in friendUids && uid !in incomingSet
                }

                onResult(
                    FriendRelations(
                        friends = friends,
                        incomingRequests = incomingRequests,
                        availableToRequest = availableToRequest
                    )
                )
            }
        }
    }
}

fun sendFriendRequest(
    toUid: String,
    context: android.content.Context,
    onComplete: (() -> Unit)? = null
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        onComplete?.invoke()
        return
    }
    if (currentUid == toUid) {
        Toast.makeText(context, "No puedes agregarte a ti mismo", Toast.LENGTH_SHORT).show()
        onComplete?.invoke()
        return
    }

    val requestsRef = database.getReference("friendRequests")
        .child(toUid)
        .child(currentUid)

    requestsRef.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            val status = snapshot.child("status").getValue(String::class.java)
            val msg = when (status) {
                "pending" -> "Ya enviaste una solicitud a este jugador"
                "accepted" -> "Ya son amigos"
                "rejected" -> "Tu solicitud fue rechazada"
                else -> "Ya existe una solicitud para este jugador"
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
        } else {
            val data = mapOf(
                "fromUid" to currentUid,
                "toUid" to toUid,
                "status" to "pending",
                "timestamp" to ServerValue.TIMESTAMP
            )
            requestsRef.setValue(data)
                .addOnSuccessListener {
                    Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
        onComplete?.invoke()
    }
}

fun respondToFriendRequest(
    fromUid: String,
    accept: Boolean,
    context: android.content.Context,
    onComplete: (() -> Unit)? = null
) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        onComplete?.invoke()
        return
    }
    val requestsRef = database.getReference("friendRequests")
        .child(currentUid)
        .child(fromUid)

    requestsRef.get().addOnSuccessListener { snapshot ->
        if (!snapshot.exists()) {
            Toast.makeText(context, "La solicitud ya no existe", Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
            return@addOnSuccessListener
        }

        if (accept) {
            val updates = hashMapOf<String, Any>(
                "$pathUsers$currentUid/friends/$fromUid" to true,
                "$pathUsers$fromUid/friends/$currentUid" to true,
                "friendRequests/$currentUid/$fromUid/status" to "accepted"
            )

            database.reference.updateChildren(updates)
                .addOnSuccessListener {
                    Toast.makeText(context, "Solicitud aceptada", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al aceptar solicitud", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
        } else {
            requestsRef.child("status").setValue("rejected")
                .addOnSuccessListener {
                    Toast.makeText(context, "Solicitud rechazada", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al rechazar solicitud", Toast.LENGTH_SHORT).show()
                    onComplete?.invoke()
                }
        }
    }.addOnFailureListener {
        Toast.makeText(context, "Error al procesar la solicitud", Toast.LENGTH_SHORT).show()
        onComplete?.invoke()
    }
}