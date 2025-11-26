package com.example.warofwonders.ui.screens.friendlybattle

import android.widget.Toast
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Slider
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.warofwonders.R

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


    Box(
        modifier = Modifier
            .fillMaxWidth()

    ) {

        Image(
            painter = painterResource(id = R.drawable.fondotienda),
            contentDescription = "Escenario bosque",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }



    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {

        Text("Batalla amistosa", modifier = Modifier.padding(8.dp), color = Color.White)

        Text("Tus amigos activos:", color = Color.White)
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(friends) { (uid, user) ->
                val isSelected = selectedFriend == uid
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { selectedFriend = uid },
                    shape = RoundedCornerShape(10.dp),
                    border = if (isSelected) BorderStroke(2.dp, Color.Yellow) else null,
                    colors = CardDefaults.cardColors()
                ) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "${user.name} (lvl ${user.level})", modifier = Modifier.weight(1f))
                        Button(onClick = { selectedFriend = uid }, colors = ButtonDefaults.buttonColors()) {
                            Text(if (isSelected) "Seleccionado" else "Seleccionar")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Tu saldo: ${inventarioState.coins}", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))

        // Slider para seleccionar la apuesta
        val maxCoins = inventarioState.coins.coerceAtLeast(0)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text("Apuesta: $stakeCoins coins")
            Slider(
                value = stakeCoins.toFloat().coerceIn(0f, maxCoins.toFloat()),
                onValueChange = { stakeCoins = it.toInt() },
                valueRange = 0f..maxCoins.toFloat(),
                steps = if (maxCoins > 1) (maxCoins - 1) else 0
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val canSend = !(stakeCoins > inventarioState.coins || inventarioState.coins == 0)
            Button(onClick = {
                // enviar solicitud
                val toUid = selectedFriend
                if (toUid.isNullOrBlank()) {
                    Toast.makeText(context, "Seleccione un amigo", Toast.LENGTH_SHORT).show(); return@Button
                }
                sendBattleRequest(toUid, stakeCoins) { success ->
                    if (success) {
                        Toast.makeText(context, "Batalla iniciada", Toast.LENGTH_SHORT).show()
                        // Navegar directamente a combate sin esperar aceptación
                        navController.navigate("${com.example.warofwonders.ui.navigation.AppScreens.Combat.name}/$currentUid/$toUid")
                    } else Toast.makeText(context, "Error al iniciar batalla", Toast.LENGTH_SHORT).show()
                }
            }, enabled = canSend) { Text("Enviar reto (coins)") }

            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Cancelar") }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Funciones de utilidad para enviar solicitudes de batalla amistosa
/**
 * Enviar reto: crear encuentro inmediatamente y navegar directamente a combate.
 * No esperar confirmación del otro jugador.
 */
fun sendBattleRequest(toUid: String, stakeCoins: Int, onComplete: (Boolean) -> Unit) {
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: run { onComplete(false); return }
    if (currentUid == toUid) { onComplete(false); return }

    try {
        // Generar clave de encounter (mismo algoritmo que CombatViewModel)
        val ids = listOf(currentUid, toUid).sorted()
        val encounterKey = "encounter_${ids[0]}_${ids[1]}"
        val encounterRef = database.getReference("encounters").child(encounterKey)

        // Crear datos del encuentro
        val encounterData = hashMapOf<String, Any>(
            "attackerId" to currentUid,
            "defenderId" to toUid,
            "stake" to stakeCoins,
            "started" to false,
            "started_at" to ServerValue.TIMESTAMP,
            "started_by" to "system_auto_start"
        )

        // Escribir encuentro (crea nodo con todos los campos necesarios)
        encounterRef.setValue(encounterData).addOnSuccessListener {
            Log.d("FriendlyBattle", "Encounter created: $encounterKey")
            onComplete(true)
        }.addOnFailureListener { ex ->
            Log.e("FriendlyBattle", "Failed to create encounter for $currentUid -> $toUid: ${ex.message}")
            onComplete(false)
        }
    } catch (e: Exception) {
        Log.e("FriendlyBattle", "Exception in sendBattleRequest: ${e.message}")
        onComplete(false)
    }
}
