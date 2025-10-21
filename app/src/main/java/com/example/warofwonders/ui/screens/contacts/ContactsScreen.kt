package com.example.warofwonders.ui.screens.contacts

import android.Manifest
import android.content.ContentResolver
import android.provider.ContactsContract
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R
import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.pathUsers
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.firebase.auth.FirebaseAuth

data class Contact (
    val id: String,
    val name: String,
    val phone: String
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val contactsPermissionState = rememberPermissionState(Manifest.permission.READ_CONTACTS)
    var matchedContacts by remember { mutableStateOf<List<Pair<String, Contact>>>(emptyList()) }

    Column (
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            contactsPermissionState.status.isGranted -> {
                val contacts = loadContacts(contentResolver)
                LaunchedEffect(Unit) {
                    findFriendsInFirebase(contacts) { matchedContacts = it }
                }
                DrawContacts(matchedContacts) { uid ->
                    addFriend(uid, context)
                }
            }

            contactsPermissionState.status.shouldShowRationale -> {
                Text(
                    "Por favor conceda el permiso para acceder correctamente a los contactos",
                    modifier = Modifier.padding(30.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                Button(
                    onClick = {contactsPermissionState.launchPermissionRequest()},
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Conceder permiso")
                }
            }

            else -> {
                Text(
                    "Por favor conceda permiso para agregar amigos",
                    modifier = Modifier.padding(30.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp
                )
                Button(
                    onClick = {contactsPermissionState.launchPermissionRequest()},
                    modifier = Modifier.fillMaxWidth().padding(30.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                ) {
                    Text("Conceder permiso")
                }
            }
        }
    }
}
@Composable
fun DrawContacts(contacts: List<Pair<String, Contact>>, onAddFriend: (String) -> Unit) {
    LazyColumn(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        items(contacts) { (uid, contact) ->
            ElevatedCard(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(15.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.shield),
                        contentDescription = "Contact",
                        modifier = Modifier.height(30.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(contact.name)
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        onClick = { onAddFriend(uid) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

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

        while(cursor.moveToNext()) {
            val id = cursor.getString(idColumn)
            val name = cursor.getString(nameColumn)
            val number = it.getString(numberColumn)
            contacts.add(Contact(id, name, number))
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
            if (phone != null) uid to phone else null
        }

        fun normalize(num: String) = num.filter { it.isDigit() }.takeLast(10)

        val matched = contacts.mapNotNull { contact ->
            val normalizedContact = normalize(contact.phone)
            val match = firebaseUsers.find { normalize(it.second) == normalizedContact }
            match?.let { it.first to contact }
        }

        onResult(matched)
    }
}

fun addFriend(uidFriend: String, context: android.content.Context) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = database.getReference(pathUsers)

    val currentUserFriendsRef = usersRef.child(currentUid).child("friends").child(uidFriend)

    currentUserFriendsRef.get().addOnSuccessListener { snapshot ->
        if (snapshot.exists()) {
            Toast.makeText(context, "Ya agregaste a este amigo", Toast.LENGTH_SHORT).show()
        } else {
            currentUserFriendsRef.setValue(true)
            usersRef.child(uidFriend).child("friends").child(currentUid).setValue(true)
            Toast.makeText(context, "Amigo agregado", Toast.LENGTH_SHORT).show()
        }
    }
}