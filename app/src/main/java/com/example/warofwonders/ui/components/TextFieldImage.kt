package com.example.warofwonders.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.warofwonders.R
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun TextFieldImage(
    modifier: Modifier = Modifier,
    value: String,
    placeholderText: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier.defaultMinSize(minHeight = 56.dp)
    ) {
        Image(
            modifier = Modifier.matchParentSize(),
            painter = painterResource(id = R.drawable.textfield_image),
            contentDescription = "TextField background",
            contentScale = ContentScale.FillBounds
        )

        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                if(value.isEmpty()) {
                    Text(
                        text = placeholderText,
                        color = Color.Gray,
                        fontSize = 20.sp
                    )
                }
            },
            textStyle = TextStyle( fontSize = 20.sp ),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.matchParentSize().background(Color.Transparent)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TextFieldImagePreview() {
    WarOfWondersTheme {
        TextFieldImage(
            value = "3D2YArley",
            placeholderText = "Usuario",
            onValueChange = { },
            modifier = Modifier.width(250.dp).height(56.dp)
        )
    }
}