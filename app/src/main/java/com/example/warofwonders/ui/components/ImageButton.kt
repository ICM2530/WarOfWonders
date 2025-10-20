package com.example.warofwonders.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.warofwonders.R
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun ImageButton(
    imageRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier.defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .background(Color.Transparent),
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ImageButtonPreview() {
    WarOfWondersTheme {
        ImageButton(
            imageRes = R.drawable.button_login,
            contentDescription = "LogIn",
            modifier = Modifier.width(120.dp).height(50.dp),
            onClick = { },
            enabled = true
        )
    }
}
