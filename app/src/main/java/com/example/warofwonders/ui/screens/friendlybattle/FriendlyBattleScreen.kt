package com.example.warofwonders.ui.screens.friendlybattle

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.warofwonders.ui.model.database
import com.example.warofwonders.ui.model.pathUsers
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.Criatura
import com.example.warofwonders.ui.model.InventarioViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.launch

/*
 FriendlyBattlescreen:
 - Muestra amigos activos
 - Permite seleccionar un amigo y cantidad de apuesta
 - Envia solicitudes de batalla
 - Escucha solicitudes entrantes y muestra aceptar/negar
 - Si se acepta, ambos se van a la pantalla de batalla
*/

@Composable
fun FriendlyBattleScreen(navController: NavController) {
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var friends by remember { mutableStateOf<List<Pair<String, MyUserState>>>(emptyList()) }
    var selectedFriend by remember { mutableStateOf<String?>(null) }
    var stakeCoins by remember { mutableStateOf(0) }

    // solicitud entrante al usuario actual
    var incomingRequests by remember { mutableStateOf<List<Pair<String, Map<String, Any>>>>(emptyList()) }

    val inventarioVM: InventarioViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val inventarioState by inventarioVM.inventario.collectAsState()

    // Cargar la lista de amigos
    LaunchedEffect(Unit) {
        val ref = database.getReference(pathUsers)
        ref.get().addOnSuccessListener { snap ->
            val userNode = snap.children.associateBy { it.key ?: "" }
            // leer los amigos del usuario
            val currentNode = snap.children.find { it.key == currentUid }
            val friendsMap = currentNode?.child("friends")
            val friendUids = friendsMap?.children?.filter { it.getValue(Boolean::class.java) == true }?.mapNotNull { it.key } ?: emptyList()
            val activeFriends = friendUids.mapNotNull { uid ->
                val node = userNode[uid] ?: return@mapNotNull null
                val active = node.child("active").getValue(Boolean::class.java) ?: false
                if (!active) return@mapNotNull null
                val userState = MyUserState(
                    name = node.child("name").getValue(String::class.java) ?: "",
                    lastName = "",
                    phone = node.child("phone").getValue(String::class.java) ?: "",
                    email = node.child("email").getValue(String::class.java) ?: "",
                    password = "",
                    id = uid,
                    coins = (node.child("coins").getValue(Int::class.java) ?: 0),
                    level = (node.child("nivel").getValue(Int::class.java) ?: 1),
                    clanid = node.child("clan").getValue(String::class.java) ?: "",
                    profileImageUrl = node.child("profileImageUrl").getValue(String::class.java) ?: "",
                    criaturas = emptyList(),
                    recursos = emptyList()
                )
                uid to userState
            }
            friends = activeFriends
        }
    }

    // Escuchar solicitudes entrantes
    DisposableEffect(Unit) {
        val reqRef = database.getReference("battleRequests").child(currentUid)
        val listener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val list = mutableListOf<Pair<String, Map<String, Any>>>()
                snapshot.children.forEach { child ->
                    val fromUid = child.key ?: return@forEach
                    val map = child.value as? Map<String, Any> ?: return@forEach
                    list.add(fromUid to map)
                }
                incomingRequests = list
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        reqRef.addValueEventListener(listener)
        onDispose { reqRef.removeEventListener(listener) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Batalla amistosa", modifier = Modifier.padding(8.dp))

        Text("Tus amigos activos:")
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(friends) { (uid, user) ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${user.name} (lvl ${user.level})", modifier = Modifier.weight(1f))
                    Button(onClick = { selectedFriend = uid }, colors = ButtonDefaults.buttonColors()) {
                        Text("Seleccionar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Tu saldo: ${inventarioState.coins}")
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = {
                // enviar solicitud
                val toUid = selectedFriend
                if (toUid.isNullOrBlank()) {
                    Toast.makeText(context, "Seleccione un amigo", Toast.LENGTH_SHORT).show(); return@Button
                }
                sendBattleRequest(toUid, stakeCoins) { success ->
                    if (success) {
                        Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_SHORT).show()
                        // Si el objetivo es un NPC (fake_), asumir aceptación automática y navegar a combate
                        if (toUid.startsWith("fake_")) {
                            navController.navigate("${com.example.warofwonders.ui.navigation.AppScreens.Combat.name}/$currentUid/$toUid")
                        }
                    } else Toast.makeText(context, "Error al enviar solicitud", Toast.LENGTH_SHORT).show()
                }
            }) { Text("Enviar reto (coins)") }

            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Cancelar") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (incomingRequests.isNotEmpty()) {
            Text("Solicitudes entrantes:")
            incomingRequests.forEach { (fromUid, map) ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val fromName = map["fromName"] as? String ?: fromUid
                    val stake = (map["stake"] as? Long)?.toInt() ?: 0
                    Text(text = "$fromName apuesta $stake coins", modifier = Modifier.weight(1f))
                    Button(onClick = {
                        respondToBattleRequest(fromUid, accept = true) { ok ->
                            if (ok) navController.navigate("${com.example.warofwonders.ui.navigation.AppScreens.Combat.name}/$fromUid/$currentUid")
                        }
                    }) { Text("Aceptar") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { respondToBattleRequest(fromUid, accept = false) {} }) { Text("Rechazar") }
                }
            }
        }
    }
}

// Funciones de utilidad para enviar y responder solicitudes de batalla amistosa
fun sendBattleRequest(toUid: String, stakeCoins: Int, onComplete: (Boolean) -> Unit) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onComplete(false); return }
    if (currentUid == toUid) { onComplete(false); return }

    val requestsRef = database.getReference("battleRequests").child(toUid).child(currentUid)
    val sentRef = database.getReference("battleRequestsSent").child(currentUid).child(toUid)

    val data = hashMapOf<String, Any>(
        "fromUid" to currentUid,
        "toUid" to toUid,
        "fromName" to (FirebaseAuth.getInstance().currentUser?.displayName ?: "Jugador"),
        "stake" to stakeCoins,
        "status" to "pending",
        "timestamp" to ServerValue.TIMESTAMP
    )

    requestsRef.setValue(data).addOnSuccessListener {
        // tambien escribir para que el que envia pueda observar
        sentRef.setValue(data).addOnSuccessListener {
            // Si el receptor es un NPC falso, marcar como aceptado automáticamente
            if (toUid.startsWith("fake_")) {
                val updates = hashMapOf<String, Any>(
                    "battleRequests/${toUid}/$currentUid/status" to "accepted",
                    "battleRequestsSent/$currentUid/${toUid}/status" to "accepted"
                )
                database.reference.updateChildren(updates).addOnSuccessListener {
                    onComplete(true)
                }.addOnFailureListener {
                    // si falla la actualización, seguir reportando que la solicitud fue enviada
                    onComplete(true)
                }
            } else {
                onComplete(true)
            }
        }.addOnFailureListener { onComplete(false) }
    }.addOnFailureListener { onComplete(false) }
}


fun respondToBattleRequest(fromUid: String, accept: Boolean, onComplete: (Boolean) -> Unit) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onComplete(false); return }
    val reqRef = database.getReference("battleRequests").child(currentUid).child(fromUid)
    val sentRef = database.getReference("battleRequestsSent").child(fromUid).child(currentUid)

    reqRef.get().addOnSuccessListener { snap ->
        if (!snap.exists()) { onComplete(false); return@addOnSuccessListener }
        if (accept) {
            // Actualizar ambos nodos a aceptados
            val updates = hashMapOf<String, Any>(
                "battleRequests/$currentUid/$fromUid/status" to "accepted",
                "battleRequestsSent/$fromUid/$currentUid/status" to "accepted"
            )
            database.reference.updateChildren(updates).addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
        } else {
            reqRef.child("status").setValue("rejected").addOnSuccessListener {
                sentRef.child("status").setValue("rejected").addOnSuccessListener { onComplete(true) }.addOnFailureListener { onComplete(false) }
            }.addOnFailureListener { onComplete(false) }
        }
    }.addOnFailureListener { onComplete(false) }
}
