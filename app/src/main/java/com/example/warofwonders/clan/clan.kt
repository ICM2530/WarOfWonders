package com.example.warofwonders.clan

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.warofwonders.navigation.AppScreens

data class ClanMember(
    val avatar: Int,
    val name: String,
    val role: String,
    val level: Int
)

data class ClanHistory(
    val battle: String,
    val result: String
)

@Composable
fun ClanScreen(navController: NavController) {
    val members = listOf(
        ClanMember(R.drawable.profile2, "jaime", "Líder", 20),
        ClanMember(R.drawable.profile1, "pablo", "co-líder", 17),
        ClanMember(R.drawable.perfil3, "user #11", "miembro", 1),
    )

    val history = listOf(
        ClanHistory("Batalla vs bogota clan", "victoria"),
        ClanHistory("Batalla vs los piratas", "derrota")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1B3A1D))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ClanHeader(navController)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "miembros",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(members) { member ->
                ClanMemberItem(member)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "historial",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(history) { item ->
                ClanHistoryItem(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ClanHeader(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .clip(RoundedCornerShape(6.dp))
            .height(80.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.madera),
            contentDescription = "Fondo madera",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = "Escudo",
                    modifier = Modifier.size(40.dp)
                        .clickable(
                            onClick = { navController.navigate(AppScreens.Home.name) }
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "teusaquillo amigos",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = "Corona",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "8",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ClanMemberItem(member: ClanMember) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chatframe),
            contentDescription = "Fondo chatframe",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = member.avatar),
                contentDescription = member.name,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = member.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = member.role,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.trophy),
                    contentDescription = "Nivel",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "lvl ${member.level}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ClanHistoryItem(item: ClanHistory) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(50.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.chatframe),
            contentDescription = "Fondo chatframe",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.battle,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = item.result,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.result == "victoria") Color.Green else Color.Red
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClanScreenPreview() {
    ClanScreen(navController = rememberNavController())
}
