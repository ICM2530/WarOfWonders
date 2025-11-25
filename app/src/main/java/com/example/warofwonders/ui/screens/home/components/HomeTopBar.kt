package com.example.warofwonders.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.warofwonders.R
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


@Composable
fun HomeTopBar(navController: NavHostController) {
    var userName by remember { mutableStateOf("Jugador") }
    var team by remember { mutableStateOf("Sin equipo") }
    var coins by remember { mutableIntStateOf(0) }
    var xp by remember { mutableIntStateOf(0) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    val uid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        val ref = FirebaseDatabase.getInstance().reference.child("users").child(uid)
        ref.get().addOnSuccessListener { snap ->
            userName = snap.child("name").getValue(String::class.java)
                ?: snap.child("userName").getValue(String::class.java)
                        ?: snap.child("firstName").getValue(String::class.java)
                        ?: snap.child("lastName").getValue(String::class.java)
                        ?: snap.child("email").getValue(String::class.java)?.substringBefore("@")
                        ?: "Jugador"
            team = snap.child("clanid").getValue(String::class.java) ?: "Sin equipo"
            coins = (snap.child("coins").getValue(Long::class.java) ?: 0).toInt()
            xp = (snap.child("xp").getValue(Long::class.java) ?: 0).toInt()
            profileImageUrl = snap.child("profileImageUrl").getValue(String::class.java)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 12.dp, top = 5.dp, end = 12.dp, bottom = 8.dp),

                verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen de perfil con contorno
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clickable { navController.navigate(AppScreens.Camera.name) },
                contentAlignment = Alignment.Center
            ) {
                val painter = if (profileImageUrl.isNullOrBlank()) {
                    painterResource(id = R.drawable.profile_user)
                } else {
                    rememberAsyncImagePainter(profileImageUrl)
                }
                Image(
                    painter = painter,
                    contentDescription = "Imagen de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.dp, color = Color(0xFFA17745), CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre y equipo
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = team,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1
                )


                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp) // separa XP y coins
                ) {
                    // XP
                    Box(
                        contentAlignment = Alignment.Center, // centra el texto sobre la imagen
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.xp_bar),
                            contentDescription = "Barra de XP",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = xp.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Coins
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(60.dp)
                            .height(20.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.bracket_coin),
                            contentDescription = "Monedas",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = coins.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(R.drawable.shop),
                    contentDescription = "Tienda",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(AppScreens.Shop.name) }
                )
                Image(
                    painter = painterResource(R.drawable.iconcontactos),
                    contentDescription = "Contactos",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(AppScreens.Contacts.name) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Image(
                    painter = painterResource(R.drawable.configbutton),
                    contentDescription = "Configuración",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(AppScreens.Settings.name) }
                )
                Image(
                    painter = painterResource(R.drawable.bell_icon),
                    contentDescription = "Notificaciones",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { }
                )
            }
        }
    }
}

