package com.example.warofwonders.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.ui.navigation.AppScreens

@Composable
fun ChatScreen(navController: NavHostController) {
    val messages = listOf(
        ChatMessage("Jaime", "Líder", "Hola a todos"),
        ChatMessage("Pedro", "Miembro", "Saludos"),
        ChatMessage("Pablo", "Co-líder", "Bienvenidos"),
        ChatMessage("Jaime", "Líder", "¿Cómo están?"),
        ChatMessage("Pedro", "Miembro", "¡Listo para jugar!")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.chatbackground),
            contentDescription = "Chat background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(70.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chatframe),
                    contentDescription = "Chat frame",
                    modifier = Modifier.fillMaxSize().clickable {
                        navController.navigate(AppScreens.Clan.name)
                    },
                    contentScale = ContentScale.FillBounds
                )

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.shield),
                            contentDescription = "Clan shield",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Teusaquillo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "Amigos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.closechat),
                        contentDescription = "Close chat",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable(
                                onClick = { navController.navigate(AppScreens.Home.name) }
                            )
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.roundedrectangle),
                        contentDescription = "Caja mensaje",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        ,
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chatbutton),
                        contentDescription = "Enviar",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Image(
                        painter = painterResource(id = R.drawable.pointerright),
                        contentDescription = "Enviar ícono",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isRight = message.user == "Pedro"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 800.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    if (isRight) R.drawable.chatbubbletextinverted else R.drawable.chatbubbletext
                ),
                contentDescription = "burbuja",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            Column(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 25.dp,
                        bottom = 50.dp
                    ),
                horizontalAlignment = if (isRight) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.user,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = message.role,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}