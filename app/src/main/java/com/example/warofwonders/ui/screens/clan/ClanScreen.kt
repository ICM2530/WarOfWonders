package com.example.warofwonders.ui.screens.clan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.warofwonders.ui.model.Clan
import com.example.warofwonders.ui.model.MyUserState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ClanScreen(
    viewModel: ClanViewModel,
    onClanSelected: () -> Unit,
    onBack: () -> Unit
) {
    val clanes by viewModel.clanes.collectAsState()
    val mensaje by viewModel.mensaje.collectAsState()


    val user by viewModel.currentUser.collectAsState()

    var creandoClan by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.listenCurrentUserRealtime()
        viewModel.cargarClanes()
    }


    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val u = user!!   // usuario ya listo

    // -----------------------------------------------------------------
    // Crear Clan
    // -----------------------------------------------------------------
    if (creandoClan) {
        CreateClanScreen(
            user = u,
            viewModel = viewModel,
            onClanCreated = {
                creandoClan = false
                viewModel.cargarClanes()
            },
            onBack = { creandoClan = false }
        )
        return
    }

    // -----------------------------------------------------------------
    // Pantalla principal
    // -----------------------------------------------------------------
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (mensaje.isNotEmpty()) {
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // -----------------------------------------------------------------
        // NO TIENE CLAN
        // -----------------------------------------------------------------
        if (u.clanid.isEmpty()) {

            Text(
                "No perteneces a un clan",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = { creandoClan = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Crear Clan")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                items(clanes) { clan ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {

                            Text(
                                clan.nombre,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                clan.descripcion,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Miembros: ${clan.miembros.size}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { viewModel.unirseAClan(clan, u) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Unirse")
                            }
                        }
                    }
                }
            }
        } else {
            // Buscar el clan en la lista de clanes cargada en el viewModel
            val clan = clanes.find { it.id == u.clanid }

            if (clan != null) {
                ClanDetailScreen(
                    clan = clan,
                    currentUser = u,
                    onBack = onBack
                )


            } else {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Clan no encontrado", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    }

