package com.example.warofwonders.ui.screens.gallery

import android.os.Environment
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.navigation.AppScreens
import java.io.File

@Composable
fun GalleryScreen(navController: NavHostController) {
    val context = LocalContext.current
    val imageDir = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "WarOfWonders"
    )
    var images by remember { mutableStateOf(listOf<File>()) }

    // Cargar imágenes desde la carpeta
    LaunchedEffect(Unit) {
        if (imageDir.exists()) {
            images = imageDir.listFiles()?.sortedByDescending { it.lastModified() }?.toList() ?: emptyList()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF9ACD32))
            .padding(top = 48.dp),

        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .size(width = 220.dp, height = 70.dp),
                contentAlignment = Alignment.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.chatframe),
                    contentDescription = "Fondo encabezado",
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )


                Text(
                    text = "GALERÍA",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }


            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(images) { file ->
                    Image(
                        painter = rememberAsyncImagePainter(file),
                        contentDescription = "Imagen guardada",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }


        ImageButton(
            imageRes = R.drawable.closechat,
            contentDescription = "Cerrar galería",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(48.dp),
            onClick = { navController.navigate(AppScreens.Inventory.name) }
        )
    }
}
