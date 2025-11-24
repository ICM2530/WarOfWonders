package com.example.warofwonders.ui.screens.home.components

import androidx.compose.foundation.Image
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        // Columna principal: Perfil + info
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
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
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column {
                        Text(
                            text = userName,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = team,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.xp_bar),
                            contentDescription = "Barra de experiencia",
                            modifier = Modifier
                                .height(28.dp)
                                .width(80.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = xp.toString(),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 40.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.bracket_coin),
                            contentDescription = "Monedas",
                            modifier = Modifier
                                .height(28.dp)
                                .width(80.dp),
                            contentScale = ContentScale.FillBounds
                        )
                        Text(
                            text = coins.toString(),
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 40.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }


        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp), // margen desde la esquina
            verticalArrangement = Arrangement.spacedBy(12.dp) // espacio entre filas
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp) // espacio entre iconos
            ) {
                Image(
                    painter = painterResource(R.drawable.shop),
                    contentDescription = "Tienda",
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { navController.navigate(AppScreens.Shop.name) }
                )
                Image(
                    painter = painterResource(R.drawable.iconcontactos),
                    contentDescription = "Contactos",
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { navController.navigate(AppScreens.Contacts.name) }
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.configbutton),
                    contentDescription = "Configuración",
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { navController.navigate(AppScreens.Settings.name) }
                )
                Image(
                    painter = painterResource(R.drawable.bell_icon),
                    contentDescription = "Notificaciones",
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { }
                )
            }
        }



    }
}
