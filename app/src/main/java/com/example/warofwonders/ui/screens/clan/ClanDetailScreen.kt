package com.example.warofwonders.ui.screens.clan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import com.example.warofwonders.ui.model.Clan
import com.example.warofwonders.ui.model.MyUserState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ClanDetailScreen(
    clan: Clan,
    currentUser: MyUserState,
    onBack: () -> Unit
) {
    val db = FirebaseDatabase.getInstance().reference
    val miembrosState = remember { mutableStateListOf<MyUserState>() }

    // Estado para el Dialog
    var dialogUsuario by remember { mutableStateOf<MyUserState?>(null) }
    var dialogAccion by remember { mutableStateOf<String?>(null) }

    // Cargar miembros de Firebase
    LaunchedEffect(clan.miembros) {
        miembrosState.clear()
        clan.miembros.keys.forEach { uid ->
            db.child("users").child(uid)
                .get().addOnSuccessListener { snapshot ->
                    val usuario = snapshot.getValue(MyUserState::class.java)
                    usuario?.let {
                        miembrosState.removeAll { it.id == uid }
                        miembrosState.add(it)
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // -----------------------------
        // Info del Clan
        // -----------------------------
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = clan.nombre,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = clan.descripcion,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fuerza: ${clan.fuerza}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------
        // Lista de Miembros
        // -----------------------------
        Text(
            text = "Miembros:",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(miembrosState) { usuario ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${usuario.name} ${usuario.lastName}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = usuario.clanRole,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Mostrar acciones según rol
                        if (currentUser.clanRole == "lider" && usuario.id != currentUser.id) {
                            Button(onClick = {
                                dialogUsuario = usuario
                                dialogAccion = "lider"
                            }) {
                                Text("Acciones")
                            }
                        } else if (currentUser.clanRole == "colider" && usuario.clanRole == "miembro") {
                            Button(onClick = {
                                dialogUsuario = usuario
                                dialogAccion = "colider"
                            }) {
                                Text("Ascender")
                            }
                        } else if (usuario.id == currentUser.id) {
                            Button(onClick = {
                                dialogUsuario = usuario
                                dialogAccion = "salir"
                            }) {
                                Text("Salir")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // -----------------------------
        // Botón Volver
        // -----------------------------
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Volver")
        }
    }

    // -----------------------------
    // Dialogs para acciones
    // -----------------------------
    if (dialogUsuario != null && dialogAccion != null) {
        AlertDialog(
            onDismissRequest = { dialogUsuario = null; dialogAccion = null },
            title = {
                Text(text = "Acciones para ${dialogUsuario!!.name}")
            },
            text = {
                when (dialogAccion) {
                    "lider" -> Text("Puedes ascender, descender o expulsar a este miembro.")
                    "colider" -> Text("Puedes ascender a este miembro a colider.")
                    "salir" -> Text("¿Deseas salir del clan?")
                }
            },
            confirmButton = {
                Button(onClick = {
                    dialogUsuario?.let { usuario ->
                        when (dialogAccion) {
                            "lider" -> {
                                // Aquí pones funciones de ascender, descender o expulsar
                            }
                            "colider" -> {
                                db.child("users").child(usuario.id).child("clanRole")
                                    .setValue("colider")
                            }
                            "salir" -> {
                                // Eliminar usuario del clan
                                db.child("clanes").child(clan.id).child("miembros")
                                    .child(usuario.id).removeValue()
                                db.child("users").child(usuario.id).child("clanid").setValue("")
                                db.child("users").child(usuario.id).child("clanRole").setValue("")
                            }
                        }
                    }
                    dialogUsuario = null
                    dialogAccion = null
                }) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                Button(onClick = { dialogUsuario = null; dialogAccion = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
