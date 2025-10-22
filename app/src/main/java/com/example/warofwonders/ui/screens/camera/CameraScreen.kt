package com.example.warofwonders.ui.screens.camera

import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Lanzador de la galería
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { it ->
        imageUri = it
        if (it != null) {
            val imageDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "WarOfWonders"
            )
            if (!imageDir.exists()) imageDir.mkdirs()
            saveCameraImageToGallery(context, it, imageDir)
            Log.i("ImageApp", "🖼️ Imagen de galería guardada en WarOfWonders")
        }
    }

    // Archivo temporal para la cámara
    val cameraUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.file_provider",
        File(context.filesDir, "${System.currentTimeMillis()}_cameraPic.jpg")
    )

    // Lanzador de la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { it ->
        if (it) {
            imageUri = cameraUri

            // ✅ Guardar la foto tomada en la carpeta WarOfWonders
            val imageDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "WarOfWonders"
            )
            if (!imageDir.exists()) imageDir.mkdirs()

            saveCameraImageToGallery(context, cameraUri, imageDir)
            Log.i("ImageApp", "📸 Foto guardada en carpeta WarOfWonders")
        }
    }

    // Interfaz visual
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(imageUri),
                    contentDescription = "Imagen",
                    modifier = Modifier.size(500.dp)
                )
            } else {
                Text(
                    text = "Cargando imagen...",
                    textAlign = TextAlign.Center
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ) {
            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.width(125.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Galería")
            }
            Button(
                onClick = { cameraLauncher.launch(cameraUri) },
                modifier = Modifier.width(125.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue)
            ) {
                Text("Cámara")
            }
        }
    }
}

/**
 * Copia una imagen (de cámara o galería) en la carpeta interna del app:
 * /Android/data/com.example.warofwonders/files/Pictures/WarOfWonders/
 */
fun saveCameraImageToGallery(context: Context, uri: Uri, destDir: File) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val newFile = File(destDir, "IMG_$timeStamp.jpg")

        val outputStream = FileOutputStream(newFile)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        // 🔄 Escanear para que aparezca inmediatamente si usas un visor externo
        MediaScannerConnection.scanFile(context, arrayOf(newFile.absolutePath), null, null)

        Log.i("ImageApp", "✅ Imagen copiada a: ${newFile.absolutePath}")
    } catch (e: Exception) {
        Log.e("ImageApp", "❌ Error al copiar imagen: ${e.message}")
    }
}
