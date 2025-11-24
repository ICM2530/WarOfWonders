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
fun HomeTopBar(
    navController: NavHostController
) {
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
            userName =
                snap.child("name").getValue(String::class.java)
                    ?: snap.child("userName").getValue(String::class.java)
                            ?: snap.child("firstName").getValue(String::class.java)
                            ?: snap.child("lastName").getValue(String::class.java)
                            ?: snap.child("email").getValue(String::class.java)?.substringBefore("@")
                            ?: "Jugador"

            team = snap.child("team").getValue(String::class.java) ?: "Sin equipo"
            coins = (snap.child("coins").getValue(Long::class.java) ?: 0).toInt()
            xp = (snap.child("xp").getValue(Long::class.java) ?: 0).toInt()
            profileImageUrl = snap.child("profileImageUrl").getValue(String::class.java)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 8.dp, end = 12.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(180.dp)
                    .padding(end = 8.dp)
                    .clickable { navController.navigate(AppScreens.Camera.name) },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUrl.isNullOrBlank()) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_user),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(profileImageUrl),
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f),
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
                            .fillMaxWidth(0.85f),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = xp.toString(),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 60.dp),
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.bracket_coin),
                        contentDescription = "Barra de monedas",
                        modifier = Modifier
                            .height(28.dp)
                            .fillMaxWidth(0.85f),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = coins.toString(),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 60.dp),
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.bell_icon),
                contentDescription = "Notificaciones",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { }
            )

            Image(
                painter = painterResource(R.drawable.iconcontactos),
                contentDescription = "Contactos",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { navController.navigate(AppScreens.Contacts.name) }
            )

            Image(
                painter = painterResource(R.drawable.configbutton),
                contentDescription = "Configuración",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { navController.navigate(AppScreens.Settings.name) }
            )

            Image(
                painter = painterResource(R.drawable.shop),
                contentDescription = "Tienda",
                modifier = Modifier
                    .size(34.dp)
                    .clickable { navController.navigate(AppScreens.Shop.name) }
            )
        }
    }
}