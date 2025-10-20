package com.example.warofwonders.ui.screens.login.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.navigation.AppScreens
import com.example.warofwonders.ui.theme.WarOfWondersTheme

@Composable
fun SignUpScreen(navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    modifier = Modifier.size(280.dp, 220.dp),
                    painter = painterResource(id = R.drawable.tittle_post),
                    contentDescription = "Title"
                )

                TextFieldImage(
                    value = "",
                    placeholderText = "Nombre",
                    onValueChange = { },
                    modifier = Modifier.width(250.dp)
                )

                TextFieldImage(
                    value = "",
                    placeholderText = "Correo",
                    onValueChange = { },
                    modifier = Modifier.width(250.dp)
                )

                TextFieldImage(
                    value = "",
                    placeholderText = "Contraseña",
                    onValueChange = { },
                    modifier = Modifier.width(250.dp)
                )

                ImageButton(
                    imageRes = R.drawable.button_signup,
                    contentDescription = "Sign Up",
                    modifier = Modifier.width(120.dp),
                    onClick = { navController.navigate(AppScreens.LogIn.name) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    WarOfWondersTheme {
        SignUpScreen(navController = rememberNavController())
    }
}