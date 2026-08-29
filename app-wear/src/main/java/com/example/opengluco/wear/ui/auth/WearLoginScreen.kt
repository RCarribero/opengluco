package com.example.opengluco.wear.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import com.example.opengluco.wear.ui.theme.WearDarkBackground
import com.example.opengluco.wear.ui.theme.WearPrimary
import com.example.opengluco.wear.ui.theme.WearSurface

@Composable
fun WearLoginScreen(
    viewModel: WearLoginViewModel,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScalingLazyListState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is WearLoginUiState.Success) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(WearDarkBackground)
    ) {
        TimeText {
            time()
        }

        ScalingLazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Text(
                    text = "OpenGluco",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WearPrimary,
                    textAlign = TextAlign.Center
                )
            }

            // Campo Email
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 3.dp)) {
                    Text("Email", fontSize = 10.sp, color = com.example.opengluco.wear.ui.theme.ClinicalTextSecondary, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard)
                            .border(1.dp, com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        if (email.isEmpty()) {
                            Text("correo@ejemplo.com", fontSize = 11.sp, color = com.example.opengluco.wear.ui.theme.ClinicalTextMuted)
                        }
                        BasicTextField(
                            value = email,
                            onValueChange = { email = it },
                            textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            cursorBrush = SolidColor(com.example.opengluco.wear.ui.theme.ClinicalMint),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Campo Contraseña
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contraseña", fontSize = 10.sp, color = com.example.opengluco.wear.ui.theme.ClinicalTextSecondary)
                        Text(
                            text = if (isPasswordVisible) "Ocultar" else "Ver",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = com.example.opengluco.wear.ui.theme.ClinicalMint,
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(com.example.opengluco.wear.ui.theme.ClinicalSurfaceCard)
                            .border(1.dp, com.example.opengluco.wear.ui.theme.ClinicalSurfaceBorder, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        if (password.isEmpty()) {
                            Text("••••••••", fontSize = 11.sp, color = com.example.opengluco.wear.ui.theme.ClinicalTextMuted)
                        }
                        BasicTextField(
                            value = password,
                            onValueChange = { password = it },
                            visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            cursorBrush = SolidColor(com.example.opengluco.wear.ui.theme.ClinicalMint),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Mensaje de error
            if (uiState is WearLoginUiState.Error) {
                item {
                    Text(
                        text = (uiState as WearLoginUiState.Error).message,
                        color = com.example.opengluco.wear.ui.theme.ClinicalLowCoral,
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // Botón de Login
            item {
                Spacer(modifier = Modifier.height(4.dp))
                if (uiState is WearLoginUiState.Loading) {
                    CircularProgressIndicator(
                        progress = { 0.75f },
                        modifier = Modifier.size(26.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(14.dp))
                            .background(com.example.opengluco.wear.ui.theme.ClinicalMint)
                            .clickable { viewModel.login(email, password) }
                            .padding(vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Acceder", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
