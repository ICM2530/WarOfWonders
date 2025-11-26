package com.example.warofwonders.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.warofwonders.R
import com.example.warofwonders.data.model.ClanData
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

@Composable
fun ChatScreen(
    navController: NavHostController,
    currentUser: MyUserState,
    clanData: ClanData? = null
) {
    val clanId = remember(currentUser.clanid, clanData?.id) {
        when {
            currentUser.clanid.isNotBlank() -> currentUser.clanid
            !clanData?.id.isNullOrBlank() -> clanData.id
            else -> ""
        }
    }

    val clanName = remember(clanId, clanData?.nombre) {
        when {
            !clanData?.nombre.isNullOrBlank() -> clanData.nombre
            clanId.isNotBlank() -> clanId
            else -> "Clan"
        }
    }

    val clanSubName = remember(clanData?.descripcion) {
        if (!clanData?.descripcion.isNullOrBlank()) {
            clanData.descripcion
        } else {
            "Amigos"
        }
    }

    val currentUserName = remember(currentUser.name, currentUser.lastName) {
        listOf(currentUser.name, currentUser.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { "Jugador" }
    }

    val currentUserRole = remember(currentUser.clanRole) {
        currentUser.clanRole.ifBlank { "Miembro" }
    }

    ChatScreenInternal(
        navController = navController,
        clanId = clanId,
        clanName = clanName,
        clanSubName = clanSubName,
        currentUserName = currentUserName,
        currentUserRole = currentUserRole
    )
}

@Composable
private fun ChatScreenInternal(
    navController: NavHostController,
    clanId: String,
    clanName: String,
    clanSubName: String,
    currentUserName: String,
    currentUserRole: String
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    val messages = remember { mutableStateListOf<ChatMessage>() }

    var newMessageText by remember { mutableStateOf("") }

    val chatRef = remember(clanId) {
        FirebaseDatabase.getInstance()
            .getReference("clanes")
            .child(clanId)
            .child("chat")
    }

    DisposableEffect(clanId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        list.add(msg)
                    }
                }
                messages.clear()
                messages.addAll(list)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        chatRef.addValueEventListener(listener)

        onDispose {
            chatRef.removeEventListener(listener)
        }
    }

    fun sendMessage() {
        if (newMessageText.isBlank() || currentUserId.isBlank() || clanId.isBlank()) return

        val key = chatRef.push().key ?: return
        val message = ChatMessage(
            uid = currentUserId,
            user = currentUserName,
            role = currentUserRole,
            message = newMessageText.trim()
        )
        chatRef.child(key).setValue(message)
        newMessageText = ""
    }

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
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
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
                                text = clanName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = clanSubName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                    ChatBubble(
                        message = msg,
                        isCurrentUser = msg.uid == currentUserId
                    )
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

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = newMessageText,
                            onValueChange = { newText ->
                                if (newText.length <= 500) {
                                    newMessageText = newText
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.Black,
                                fontSize = 14.sp
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clickable { sendMessage() },
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
fun ChatBubble(
    message: ChatMessage,
    isCurrentUser: Boolean
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                // Ancho máximo razonable para que el texto se envuelva
                .widthIn(min = 120.dp, max = 280.dp)
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    if (isCurrentUser) R.drawable.chatbubbletextinverted
                    else R.drawable.chatbubbletext
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
                horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = message.user,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = message.role,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.message,
                    fontSize = 14.sp,
                    color = Color.Black,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}