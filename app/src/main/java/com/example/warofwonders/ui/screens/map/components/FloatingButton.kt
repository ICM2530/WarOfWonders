package com.example.warofwonders.ui.screens.map.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.warofwonders.R

@Composable
fun FloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    contentDescription: String,
    contentColor: Color,
    backgroundImage: Painter
) {
    Box(
        modifier = modifier
            .paint(
                painter = backgroundImage,
                contentScale = ContentScale.Crop
            )
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.matchParentSize(),
            containerColor = Color.Transparent,
            contentColor = contentColor,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}

@Composable
fun ImageIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,      // Permite controlar tamaño del botón
    backgroundImage: Painter,
    icon: ImageVector,
    iconTint: Color = Color.White,
    iconSize: Dp = 24.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onClick)
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(iconSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImageIconButtonPreview() {
    val backgroundPainter = painterResource(id = R.drawable.slots)
    val testIcon = Icons.Default.ToggleOff

    ImageIconButton(
        onClick = { /* Acción de prueba */ },
        modifier = Modifier.size(62.dp, 42.dp), // tamaño del botón
        backgroundImage = backgroundPainter,
        icon = testIcon,
        iconTint = Color.White,
        iconSize = 32.dp
    )
}