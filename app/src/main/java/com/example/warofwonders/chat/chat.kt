package com.example.warofwonders.chat

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R

@Composable
fun ChatScreen(navController: NavController) {
    val messages = listOf(
        ChatMessage("Jaime", "Líder", "Hola a todos"),
        ChatMessage("user #11", "miembro", "Saludos"),
        ChatMessage("pablo", "co-líder", "Bienvenidos"),
        ChatMessage("Jaime", "Líder", "Cómo están?"),
        ChatMessage("user #11", "miembro", "¡Listo para jugar!")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
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
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                //la parte de arribita paraa el clan
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
                                "teusaquillo",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "amigos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                    Image(
                        painter = painterResource(id = R.drawable.closechat),
                        contentDescription = "Close chat",
                        modifier = Modifier.size(32.dp).clickable {}
                    )
                }
            }

            // Lista de mensajesccon una lista de esas lazy como las de la f1
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
                        contentDescription = "caja mensaje",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.size(50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chatbutton),
                        contentDescription = "enviar",
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.FillBounds
                    )
                    Image(
                        painter = painterResource(id = R.drawable.pointerright),
                        contentDescription = "enviar icono",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isRight = message.user == "user #11"

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isRight) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 800.dp)  //esto para el futuro ajj
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            //la imagen de la burbujita
            Image(
                painter = painterResource( //esto tambn lo pusimos por q la burbuja puede ir a la izq o ala derecha
                    if (isRight) R.drawable.chatbubbletextinverted else R.drawable.chatbubbletext
                ),
                contentDescription = "burbuja",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.FillBounds
            )

            //modifiq el top y bottom pq no cabia ek texto
            Column(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 25.dp,
                        bottom = 50.dp
                    ), //aca una logica si esta a la izq o a la derecha
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



@Preview(showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    val navcontroller= rememberNavController()
    ChatScreen(navcontroller)
}
