package com.example.warofwonders.ui.screens.map.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TextFieldSearch(
    modifier: Modifier = Modifier,
    place: String,
    placeholderText: String,
    onPlaceChange: (String) -> Unit
) {
    TextField(
        modifier = modifier,
        value = place,
        onValueChange = onPlaceChange,
        placeholder = {
            if (place.isEmpty()) {
                Text(
                    text = placeholderText,
                    color = Color.Gray,
                    fontSize = 18.sp
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )
        },
        textStyle = TextStyle(fontSize = 18.sp),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}
