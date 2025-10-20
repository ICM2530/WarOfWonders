package com.example.warofwonders.ui.screens.inventory.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun ProfileUser() {
    Box(
        modifier = Modifier.fillMaxWidth().height(190.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.madera),
            contentDescription = "Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(34.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.profile_user),
                    contentDescription = "Avatar del usuario",
                    modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Column(
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "World Recoverer",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                        )

                    Text(
                        text = "Teusaquillo amigos",
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    icon = R.drawable.shield,
                    contentDescription = "Monedas",
                    value = "2000",
                    tint = Color.Yellow
                )

                StatItem(
                    icon = R.drawable.shield,
                    contentDescription = "Nivel",
                    value = "20",
                    tint = Color.White
                )
                StatItem(
                    icon = R.drawable.shield,
                    contentDescription = "XP",
                    value = "20 xp",
                    tint = Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    @DrawableRes icon: Int,
    contentDescription: String,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier.width(50.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )

        Text(
            text = value,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileUserPreview() {
    WarOfWondersTheme {
        ProfileUser()
    }
}