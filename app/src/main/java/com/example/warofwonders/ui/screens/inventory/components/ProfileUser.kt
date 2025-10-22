package com.example.warofwonders.ui.screens.inventory.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavHostController
import com.example.warofwonders.R

import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.navigation.AppScreens


@Composable
fun ProfileUser(
    viewModel: MyUserViewModel = viewModel(),
    navController: NavHostController
) {
    val users by viewModel.users.collectAsState()
    val user = users.firstOrNull()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Fondo madera
        Image(
            painter = painterResource(id = R.drawable.madera),
            contentDescription = "Fondo madera",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── Fila superior ─────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar y nombre
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.profile_user),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(65.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = user?.name ?: "user #11",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user?.team ?: "teusaquillo amigos",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                // Monedas (ícono encima del número)

                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.bracket_coin),
                        contentDescription = "Monedas",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )

                    Text(
                        text = "${user?.coins ?: 1000}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

            }



            // ─── Fila inferior ─────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nivel (más grande)
                StatItem(
                    icon = R.drawable.trophy,
                    value = "lvl ${user?.level ?: 1}",
                    fontSize = 25.sp
                )

                // Experiencia (más grande)
                StatItem(
                    icon = R.drawable.exp,
                    value = "${user?.xp ?: 10} xp",
                    fontSize = 25.sp
                )

                // Botón archivo
                Image(
                    painter = painterResource(id = R.drawable.archivo),
                    contentDescription = "Archivos",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            navController.navigate(AppScreens.Contacts.name)
                        },
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    @DrawableRes icon: Int,
    value: String,
    fontSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = value,
            tint = Color.Unspecified,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = fontSize,
            fontWeight = FontWeight.SemiBold
        )
    }
}
