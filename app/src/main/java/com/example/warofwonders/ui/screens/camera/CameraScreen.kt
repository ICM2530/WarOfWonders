package com.example.warofwonders.ui.screens.camera

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(navController: NavHostController) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        if (uri != null) {
            val imageDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "WarOfWonders"
            )
            if (!imageDir.exists()) imageDir.mkdirs()
            saveCameraImageToGallery(context, uri, imageDir)
        }
    }

    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.file_provider",
            File(context.filesDir, "${System.currentTimeMillis()}_cameraPic.jpg")
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = cameraUri
            val imageDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "WarOfWonders"
            )
            if (!imageDir.exists()) imageDir.mkdirs()
            saveCameraImageToGallery(context, cameraUri, imageDir)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.fondocontactos),
            contentDescription = "Fondo de la pantalla de cámara",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Cámara",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(250.dp)
                        .height(500.dp)
                ) {
                    if (imageUri != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 3.dp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Imagen capturada o seleccionada",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Color.Black.copy(alpha = 0.45f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Toma una foto o elige una imagen de perfil",
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        ImageButton(
                            imageRes = R.drawable.button,
                            contentDescription = "Abrir galería",
                            modifier = Modifier
                                .width(130.dp)
                                .height(50.dp),
                            onClick = { galleryLauncher.launch("image/*") }
                        )

                        Text(
                            text = "Galería",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }

                    Box(contentAlignment = Alignment.Center) {
                        ImageButton(
                            imageRes = R.drawable.button,
                            contentDescription = "Abrir cámara",
                            modifier = Modifier
                                .width(130.dp)
                                .height(50.dp),
                            onClick = { cameraLauncher.launch(cameraUri) }
                        )

                        Text(
                            text = "Cámara",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        )
                    }
                }

                Box(contentAlignment = Alignment.Center) {
                    ImageButton(
                        imageRes = R.drawable.button,
                        contentDescription = "Confirmar imagen de perfil",
                        modifier = Modifier
                            .width(180.dp)
                            .height(50.dp),
                        onClick = {
                            val uri = imageUri
                            if (uri == null) {
                                Toast.makeText(
                                    context,
                                    "Primero selecciona o toma una imagen",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                uploadProfileImageToFirebase(
                                    imageUri = uri,
                                    onSuccess = {
                                        Toast.makeText(
                                            context,
                                            "Imagen de perfil actualizada",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        navController.navigate(AppScreens.Home.name) {
                                            popUpTo(AppScreens.Home.name) { inclusive = false }
                                        }
                                    },
                                    onError = { e ->
                                        Toast.makeText(
                                            context,
                                            "Error al actualizar imagen: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                )
                            }
                        }
                    )

                    Text(
                        text = "Confirmar",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

fun saveCameraImageToGallery(context: Context, uri: Uri, destDir: File) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val newFile = File(destDir, "IMG_$timeStamp.jpg")

        val outputStream = FileOutputStream(newFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        MediaScannerConnection.scanFile(context, arrayOf(newFile.absolutePath), null, null)
        Log.i("ImageApp", "Imagen copiada a: ${newFile.absolutePath}")
    } catch (e: Exception) {
        Log.e("ImageApp", "Error al copiar imagen: ${e.message}")
    }
}

fun uploadProfileImageToFirebase(
    imageUri: Uri,
    onSuccess: () -> Unit,
    onError: (Exception) -> Unit
) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid
        ?: return onError(Exception("Usuario no autenticado"))

    val storageRef = FirebaseStorage.getInstance()
        .reference.child("profile_images/$uid.jpg")

    storageRef.putFile(imageUri)
        .continueWithTask { storageRef.downloadUrl }
        .addOnSuccessListener { downloadUri ->
            FirebaseDatabase.getInstance().reference
                .child("users")
                .child(uid)
                .child("profileImageUrl")
                .setValue(downloadUri.toString())
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onError(e) }
        }
        .addOnFailureListener { e -> onError(e) }
}