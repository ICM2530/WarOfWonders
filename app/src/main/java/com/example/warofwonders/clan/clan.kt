package com.example.warofwonders.clan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R

@Composable
fun ClanScreen(
    onClose: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("Información") }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.clanmainbox),
                contentDescription = "Clan box",
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 20.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TabItem("Información", selectedTab == "Información") {
                        selectedTab = "Información"
                    }
                    TabItem("Historial", selectedTab == "Historial") {
                        selectedTab = "Historial"
                    }
                    TabItem("Miembros", selectedTab == "Miembros") {
                        selectedTab = "Miembros"
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (selectedTab) {
                    "Información" -> ClanInfoTab()
                    "Historial" -> ClanHistoryTab()
                    "Miembros" -> ClanMembersTab()
                }
            }

            Image(
                painter = painterResource(id = R.drawable.closechat),
                contentDescription = "Cerrar",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clickable { onClose() }
            )
        }
    }
}

@Composable
fun ClanHistoryTab() {
    val battleHistory = listOf(
        BattleRecord("Batalla vs Bogotá Alan", "Victoria", "Hace 2 días"),
        BattleRecord("Batalla vs Los Piratas", "Derrota", "Hace 5 días"),
        BattleRecord("Batalla vs Dragones", "Victoria", "Hace 1 semana"),
        BattleRecord("Batalla vs Guerreros", "Victoria", "Hace 2 semanas"),
        BattleRecord("Batalla vs Titanes", "Derrota", "Hace 3 semanas")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        items(battleHistory) { battle ->
            BattleHistoryItem(battle)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BattleHistoryItem(battle: BattleRecord) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.chatframe),
            contentDescription = "Battle history item",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = battle.battleName,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = battle.date,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (battle.result == "Victoria") colorResource(id = R.color.green)
                        else colorResource(id = R.color.red)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = battle.result,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class BattleRecord(
    val battleName: String,
    val result: String,
    val date: String
)

@Composable
fun TabItem(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val tabRes = if (isSelected) R.drawable.clancurrenttab else R.drawable.clanothertab
    Box(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = tabRes),
            contentDescription = name,
            modifier = Modifier.height(40.dp)
        )
        Text(
            text = name,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun ClanInfoTab() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.shield),
            contentDescription = "Shield",
            modifier = Modifier.size(60.dp)
        )

        Column {
            Text(
                text = "teusaquillo amigos",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = "Crown",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "8",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Clan creado para compartir y divertirnos con amigos de Teusaquillo.",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ClanMembersTab() {
    val clanMembers = listOf(
        ClanMember("Jaime", "Líder", "M 20", R.drawable.profile1),
        ClanMember("Pablo", "Colder", "M 11", R.drawable.profile2),
        ClanMember("Ana", "Miembro", "F 15", R.drawable.profile1),
        ClanMember("Carlos", "Miembro", "M 18", R.drawable.profile2),
        ClanMember("Laura", "Miembro", "F 22", R.drawable.profile1)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        items(clanMembers) { member ->
            ClanMemberItem(member)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ClanMemberItem(member: ClanMember) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
    ) {
        Image(
            painter = painterResource(id = R.drawable.chatframe),
            contentDescription = "Member item",
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = member.profileImage),
                contentDescription = "Profile",
                modifier = Modifier.size(50.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = member.role,
                    color = Color.LightGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = member.details,
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }

            if (member.role == "Líder") {
                Image(
                    painter = painterResource(id = R.drawable.trophy),
                    contentDescription = "Trophy",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}