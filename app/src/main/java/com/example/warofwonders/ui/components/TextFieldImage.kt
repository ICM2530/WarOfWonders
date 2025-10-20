package com.example.warofwonders.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.VisualTransformation
import com.example.warofwonders.R

@Composable
fun TextFieldImage(
    modifier: Modifier = Modifier,
    value: String,
    placeholderText: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    supportingText: (@Composable (() -> Unit))? = null
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
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            modifier = Modifier.matchParentSize().background(Color.Transparent)
        )
        if (supportingText != null) {
            Box(modifier = Modifier.padding(start = 16.dp, top = 2.dp)) {
                supportingText()
            }
        }
    }
}