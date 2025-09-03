package com.example.warofwonders.creatures

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.chat.ChatScreen

@Composable
fun CreaturesScreenContent() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2E7D32))
            .verticalScroll(scrollState)
            .padding(8.dp)
    ) {
        // Header Usuario
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Row(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.shield),
                    contentDescription = "Avatar",
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "user #11", fontWeight = FontWeight.Bold)
                    Text(text = "tus amigos")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Text("001")
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Star, contentDescription = null)
                        Text("10 xp")
                    }
                    Text("Lvl 1")
                }
            }
        }
    }
}

@Composable
fun CreaturesScreen(navController: NavController) {
    CreaturesScreenContent()
    // lógica de navegación aquí si se necesita
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreaturesScreenPreview() {
    val navcontroller= rememberNavController()
    CreaturesScreen(navcontroller)
}