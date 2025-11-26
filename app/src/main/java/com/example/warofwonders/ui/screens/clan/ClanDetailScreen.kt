package com.example.warofwonders.ui.screens.clan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.warofwonders.ui.model.Clan
import com.example.warofwonders.ui.model.MyUserState

@Composable
fun ClanDetailScreen(
    clan: Clan,
    currentUser: MyUserState?,
    viewModel: ClanViewModel,
    onBack: () -> Unit
) {
    val miembrosState = remember { mutableStateListOf<MyUserState>() }
    var dialogUsuario by remember { mutableStateOf<MyUserState?>(null) }
    var dialogAccion by remember { mutableStateOf<String?>(null) }

    // Escuchar cambios del usuario actual
    LaunchedEffect(Unit) { viewModel.listenCurrentUserRealtime() }

    // Cargar miembros
    LaunchedEffect(clan.miembros) {
        miembrosState.clear()
        for (uid in clan.miembros.keys) {
            val usuario = viewModel.getUsuarioAsync(uid)
            usuario?.let { miembrosState.add(it) }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ---------------- Info del Clan ----------------
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(6.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(clan.nombre, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(clan.descripcion, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text("Fuerza: ${clan.fuerza}", style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------------- Lista de Miembros ----------------
        Text("Miembros:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(miembrosState) { usuario ->
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${usuario.name} ${usuario.lastName}", style = MaterialTheme.typography.bodyLarge)
                            Text(usuario.clanRole, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (currentUser != null && usuario.id != currentUser.id) {
                                when (currentUser.clanRole) {
                                    "lider" -> {
                                        Button(onClick = { dialogUsuario = usuario; dialogAccion = "ascender" }) { Text("Ascender") }
                                        Button(onClick = { dialogUsuario = usuario; dialogAccion = "expulsar" }) { Text("Expulsar") }
                                    }
                                    "colider" -> {
                                        if (usuario.clanRole == "miembro") {
                                            Button(onClick = { dialogUsuario = usuario; dialogAccion = "ascender" }) { Text("Ascender") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------------- Botón Salir del Clan ----------------
        currentUser?.let {
            Button(
                onClick = { dialogUsuario = it; dialogAccion = "salir" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salir del clan")
            }
        }

        Spacer(Modifier.height(12.dp))

        // ---------------- Botón Volver ----------------
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Volver")
        }
    }

    // ---------------- Dialogos de Acciones ----------------
    if (dialogUsuario != null && dialogAccion != null) {
        AlertDialog(
            onDismissRequest = { dialogUsuario = null; dialogAccion = null },
            title = { Text("Acciones para ${dialogUsuario!!.name}") },
            text = {
                when (dialogAccion) {
                    "ascender" -> Text("¿Deseas ascender a este usuario?")
                    "expulsar" -> Text("¿Deseas expulsar a este usuario del clan?")
                    "salir" -> Text("¿Deseas salir del clan?")
                    else -> Text("")
                }
            },
            confirmButton = {
                Button(onClick = {
                    dialogUsuario?.let { usuario ->
                        when (dialogAccion) {
                            "ascender" -> viewModel.ascender(usuario, clan)
                            "expulsar" -> viewModel.expulsarUsuario(usuario, clan)
                            "salir" -> viewModel.salirse(usuario, clan)
                        }
                    }
                    dialogUsuario = null
                    dialogAccion = null
                }) { Text("Confirmar") }
            },
            dismissButton = { Button(onClick = { dialogUsuario = null; dialogAccion = null }) { Text("Cancelar") } }
        )
    }
}
