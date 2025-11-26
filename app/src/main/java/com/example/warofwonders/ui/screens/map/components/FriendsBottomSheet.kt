package com.example.warofwonders.ui.screens.map.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.warofwonders.ui.screens.map.MapUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsBottomSheet(
    uiState: MapUiState,
    onClose: () -> Unit,
    onShowFriendOnMap: (friendUid: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onClose() },
        sheetState = sheetState,
        containerColor = Color(0x81C57E52)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Amigos (${uiState.friendsList.size})",
                fontSize = 18.sp,
                style = androidx.compose.ui.text.TextStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                color = Color(0xFFCCBEAC)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.friendsList) { friend ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFC5B39C))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!friend.profileImage.isNullOrEmpty()) {
                                AsyncImage(
                                    model = friend.profileImage,
                                    contentDescription = "${friend.name} ${friend.lastname}",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        (friend.name.firstOrNull()?.uppercase() ?: "U") +
                                                (friend.lastname.firstOrNull()?.uppercase() ?: ""),
                                        color = Color.White,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${friend.name} ${friend.lastname}",
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(if (friend.active) Color(0xFF0D9A14) else Color(0xFFA4A4A4))
                                    )
                                }
                                Text(
                                    friend.email,
                                    fontSize = 12.sp,
                                    color = Color(0x81482501)
                                )
                            }

                            Button(
                                onClick = {
                                    if (friend.active) {
                                        onShowFriendOnMap(friend.uid)
                                        scope.launch { sheetState.hide() }
                                    }
                                },
                                enabled = friend.active,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (friend.active) Color(0xFFE37E4A) else Color(0x81BDA181)
                                )
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ver", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}
