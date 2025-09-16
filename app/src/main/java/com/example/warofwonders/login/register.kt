package com.example.warofwonders.login

import android.widget.Space
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.warofwonders.R
import com.example.warofwonders.navigation.AppScreens

@Composable
fun RegisterScreen(navController: NavController) {

    val padding = 16.dp

        Box(modifier = Modifier.fillMaxSize()) {

            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Image(
                    painter = painterResource(id = R.drawable.tittle),
                    contentDescription = "Logo del Juego",
                    modifier = Modifier.size(320.dp)
                    )

                    Spacer(modifier = Modifier.size(padding))

                    Image(
                        painter = painterResource(id = R.drawable.textfield),
                        contentDescription = "Espacio de texto correo",
                        modifier = Modifier.width(270.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    Spacer(modifier = Modifier.size(padding))

                    Image(
                        painter = painterResource(id = R.drawable.textfield),
                        contentDescription = "Espacio de texto nombre Usuario",
                        modifier = Modifier.width(270.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    Spacer(modifier = Modifier.size(padding))

                    Image(
                        painter = painterResource(id = R.drawable.textfield),
                        contentDescription = "Espacio de texto contraseña",
                        modifier = Modifier.width(270.dp),
                        contentScale = ContentScale.FillWidth
                    )

                    Spacer(modifier = Modifier.size(padding))

                    Image(
                        painter = painterResource(id = R.drawable.buttonr),
                        contentDescription = "Registrarse",
                        modifier = Modifier
                            .size(150.dp)
                            .clickable { navController.navigate(AppScreens.Home.name) }
                    )


                    }
                }
            }
        }

