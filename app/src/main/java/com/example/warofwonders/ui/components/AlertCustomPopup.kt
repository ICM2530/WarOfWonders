package com.example.warofwonders.ui.components



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlertCustomPopup(
    title: String,
    message: String,
    imageRes: Int? = null,
    onAccept: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFC79E63),
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .widthIn(min = 260.dp, max = 320.dp)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 18.sp
                )

                Text(
                    text = message,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 14.sp
                )

                // Imagen opcional
                imageRes?.let { res ->
                    Image(
                        painter = painterResource(res),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(140.dp)
                    )
                }

                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF65451A),
                        contentColor = Color.White
                    )
                ) {
                    Text("Aceptar")
                }

            }
        }
    }
}
