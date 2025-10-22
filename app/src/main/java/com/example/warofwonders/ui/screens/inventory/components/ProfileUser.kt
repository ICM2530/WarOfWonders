package com.example.warofwonders.ui.screens.inventory.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.warofwonders.R
import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.navigation.AppScreens
import java.io.File

@Composable
fun ProfileUser(
    viewModel: MyUserViewModel = viewModel(),
    navController: NavHostController
) {
    val user by viewModel.currentUser.collectAsState()
    val context = LocalContext.current

    // Cargar usuario actual al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    // --- Lanzadores ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            user?.email?.let { email ->
                viewModel.updateProfileImage(email, it)
            }
        }
    }

    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.file_provider",
            File(context.filesDir, "${System.currentTimeMillis()}_profile.jpg")
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraUri
            user?.email?.let { email ->
                viewModel.updateProfileImage(email, cameraUri)
            }
        }
    }

    // --- Diálogo para elegir foto ---
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("Elige una opción") },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    cameraLauncher.launch(cameraUri)
                }) { Text("Cámara") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("Galería") }
            }
        )
    }

    // --- UI del perfil ---
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Imagen de perfil
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val profilePainter = when {
                            imageUri != null -> rememberAsyncImagePainter(imageUri)
                            !user?.profileImageUrl.isNullOrEmpty() -> rememberAsyncImagePainter(user?.profileImageUrl)
                            else -> painterResource(id = R.drawable.profile_user)
                        }

                        Image(
                            painter = profilePainter,
                            contentDescription = "Foto de perfil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = user?.name ?: "Cargando...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = user?.team ?: "Sin equipo",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }

                // Monedas
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
                        text = "${user?.coins ?: 0}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Parte inferior
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(icon = R.drawable.trophy, value = "lvl ${user?.level ?: 1}", fontSize = 25.sp)
                StatItem(icon = R.drawable.exp, value = "${user?.xp ?: 0} xp", fontSize = 25.sp)

                Image(
                    painter = painterResource(id = R.drawable.archivo),
                    contentDescription = "Archivos",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { navController.navigate(AppScreens.Contacts.name) },
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
