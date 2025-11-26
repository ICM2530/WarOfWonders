package com.example.warofwonders.ui.screens.map.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import com.example.warofwonders.R
import com.example.warofwonders.ui.theme.White

@Composable
fun TextFieldSearch(
    place: String,
    modifier: Modifier = Modifier,
    placeholderText: String,
    onPlaceChange: (String) -> Unit,
    onSearchAction: (String) -> Unit,
    backgroundImage: Painter
) {
    Box(
        modifier = modifier
            .height(56.dp) // altura estándar de TextField
            .clip(RoundedCornerShape(16.dp))
    ) {
        Image(
            painter = backgroundImage,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        TextField(
            value = place,
            onValueChange = onPlaceChange,
            modifier = Modifier.matchParentSize(),
            placeholder = {
                if (place.isEmpty()) {
                    Text(
                        text = placeholderText,
                        color = White,
                        fontSize = 20.sp
                    )
                }
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "Search",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(48.dp).padding(start = 12.dp)
                )
            },
            textStyle = TextStyle(fontSize = 20.sp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearchAction(place) }
            )
        )
    }
}