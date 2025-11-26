package com.example.warofwonders.ui.screens.signup

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.warofwonders.R
import com.example.warofwonders.ui.components.ImageButton
import com.example.warofwonders.ui.components.TextFieldImage
import com.example.warofwonders.ui.model.MyUserState
import com.example.warofwonders.ui.model.MyUserViewModel
import com.example.warofwonders.ui.model.SignUpViewModel
import com.example.warofwonders.ui.navigation.AppScreens
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(navController: NavController) {
    val context = LocalContext.current
    val signUpViewModel: SignUpViewModel = viewModel()
    val usersViewModel: MyUserViewModel = viewModel()
    val state by signUpViewModel.form.collectAsState()

    var confirmPassword by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) } // Si ya existe

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) profileImageUri = uri }

    val cameraImageUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.file_provider",
            java.io.File(context.filesDir, "${System.currentTimeMillis()}_profile.jpg")
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) profileImageUri = cameraImageUri }

    var showMenu by remember { mutableStateOf(false) }

    // Launcher para pedir permiso de ubicación en runtime
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // permiso concedido, obtener la última ubicación conocida
            getLastKnownLocation(context, signUpViewModel)
        } else {
            signUpViewModel.updateLocationError("Permiso de ubicación denegado")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = R.drawable.closechat),
            contentDescription = "Cerrar",
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .clickable {
                    navController.navigate(AppScreens.StartUp.name) {
                        popUpTo(AppScreens.SignUp.name) { inclusive = true }
                    }
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(16.dp))


                Image(
                    modifier = Modifier.size(280.dp, 220.dp),
                    painter = painterResource(id = R.drawable.tittle_post),
                    contentDescription = "Title"
                )

                Spacer(modifier = Modifier.height(16.dp))



                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(width = 3.dp, color = Color(0xFFA17745), shape = CircleShape)
                        .clickable { showMenu = true },
                    contentAlignment = Alignment.Center
                ) {
                    val painter = if (profileImageUri != null) {
                        rememberAsyncImagePainter(profileImageUri)
                    } else if (profileImageUrl.isNullOrBlank()) {
                        painterResource(id = R.drawable.profile_user)
                    } else {
                        rememberAsyncImagePainter(profileImageUrl)
                    }
                    Image(
                        painter = painter,
                        contentDescription = "Imagen de perfil",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )


                    androidx.compose.material3.DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Galería") },
                            onClick = {
                                galleryLauncher.launch("image/*")
                                showMenu = false
                            }
                        )
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Cámara") },
                            onClick = {
                                cameraLauncher.launch(cameraImageUri)
                                showMenu = false
                            }
                        )
                    }
                }



                Spacer(modifier = Modifier.height(16.dp))


                TextFieldImage(
                    value = state.name,
                    onValueChange = signUpViewModel::updateName,
                    placeholderText = "Nombre",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.nameError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.lastName,
                    onValueChange = signUpViewModel::updateLastName,
                    placeholderText = "Apellido",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.lastNameError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.phone,
                    onValueChange = signUpViewModel::updatePhone,
                    placeholderText = "Teléfono",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.phoneError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.email,
                    onValueChange = signUpViewModel::updateEmail,
                    placeholderText = "Correo electrónico",
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.emailError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = state.password,
                    onValueChange = signUpViewModel::updatePassword,
                    placeholderText = "Contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(state.passError)

                Spacer(modifier = Modifier.height(8.dp))

                TextFieldImage(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = ""
                    },
                    placeholderText = "Confirmar contraseña",
                    isPassword = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.width(260.dp)
                )
                AnimatedErrorText(confirmPasswordError)

                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Ubicación: muestra y botón ---
            Row(
                modifier = Modifier
                    .width(260.dp)
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(2f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.textfield_image),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        if (state.latitude != null && state.longitude != null) {
                            Text(
                                text = "Lat: ${"%.5f".format(state.latitude)}\nLng: ${"%.5f".format(state.longitude)}",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Location not obtained yet",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                        if (state.locationError.isNotEmpty()) {
                            Text(
                                text = state.locationError,
                                color = Color(0xFFFF6B6B),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED

                        if (fineGranted) {
                            getLastKnownLocation(context, signUpViewModel)
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    },
                    modifier = Modifier.size(50.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation, // Cambia el ícono aquí
                        contentDescription = "Get Location",
                        tint = Color(0xFFE49C6C),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                ImageButton(
                    imageRes = R.drawable.button_signup,
                    contentDescription = "Registrar",
                    modifier = Modifier.width(140.dp),
                    onClick = {
                        val valid = validateSignUpForm(signUpViewModel, state)
                        if (confirmPassword != state.password) {
                            confirmPasswordError = "Las contraseñas no coinciden"
                        }

                        if (valid && confirmPasswordError.isEmpty()) {
                            usersViewModel.registerUserWithFirebase(
                                state = state,
                                profileImageUri = profileImageUri,
                                onSuccess = {
                                    clearForm(signUpViewModel)
                                    confirmPassword = ""
                                    Toast.makeText(
                                        context,
                                        "Usuario registrado correctamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    navController.navigate(AppScreens.Home.name) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onError = { e ->
                                    Toast.makeText(
                                        context,
                                        "Error: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}

/** --- Helpers: obtener última ubicación conocida --- **/
@SuppressLint("MissingPermission")
private fun getLastKnownLocation(
    context: android.content.Context,
    signUpViewModel: SignUpViewModel
) {
    try {
        val fused = LocationServices.getFusedLocationProviderClient(context)

        fused.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    signUpViewModel.updateLatitude(location.latitude)
                    signUpViewModel.updateLongitude(location.longitude)
                    signUpViewModel.updateAltitude(location.altitude)
                    signUpViewModel.updateLocationError("")
                } else {
                    fused.getCurrentLocation(
                        com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    ).addOnSuccessListener { loc2 ->
                        if (loc2 != null) {
                            signUpViewModel.updateLatitude(loc2.latitude)
                            signUpViewModel.updateLongitude(loc2.longitude)
                            signUpViewModel.updateAltitude(loc2.altitude)
                            signUpViewModel.updateLocationError("")
                        } else {
                            signUpViewModel.updateLocationError(
                                "No se pudo obtener la ubicación. Intenta más tarde."
                            )
                        }
                    }.addOnFailureListener { ex ->
                        signUpViewModel.updateLocationError("Error: ${ex.message}")
                    }
                }
            }
            .addOnFailureListener { ex ->
                signUpViewModel.updateLocationError("Error: ${ex.message}")
            }

    } catch (e: Exception) {
        signUpViewModel.updateLocationError("Error: ${e.message}")
    }
}

@Composable
fun AnimatedErrorText(error: String) {
    androidx.compose.animation.AnimatedVisibility(
        visible = error.isNotEmpty(),
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically(),
        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkVertically()
    ) {
        Text(
            text = error,
            color = Color.White,
            modifier = Modifier
                .padding(top = 2.dp)
                .width(260.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

fun validateSignUpForm(signUpViewModel: SignUpViewModel, s: MyUserState): Boolean {
    var ok = true

    if (s.name.isEmpty()) {
        signUpViewModel.updateNameError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updateNameError("")

    if (s.lastName.isEmpty()) {
        signUpViewModel.updateLastNameError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updateLastNameError("")

    if (s.phone.isEmpty()) {
        signUpViewModel.updatePhoneError("Campo obligatorio")
        ok = false
    } else signUpViewModel.updatePhoneError("")

    if (s.email.isEmpty()) {
        signUpViewModel.updateEmailError("Campo obligatorio")
        ok = false
    } else if (!validEmailAddress(s.email)) {
        signUpViewModel.updateEmailError("Correo inválido")
        ok = false
    } else signUpViewModel.updateEmailError("")

    if (s.password.isEmpty()) {
        signUpViewModel.updatePassError("Campo obligatorio")
        ok = false
    } else if (s.password.length < 6) {
        signUpViewModel.updatePassError("Mínimo 6 caracteres")
        ok = false
    } else signUpViewModel.updatePassError("")

    return ok
}

fun clearForm(signUpViewModel: SignUpViewModel) {
    signUpViewModel.updateName("")
    signUpViewModel.updateLastName("")
    signUpViewModel.updatePhone("")
    signUpViewModel.updateEmail("")
    signUpViewModel.updatePassword("")
}

fun validEmailAddress(email: String): Boolean {
    val regex = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    return email.matches(regex.toRegex())
}