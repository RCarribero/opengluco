package com.example.opengluco.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.core.data.UserSettings
import com.example.opengluco.mobile.notification.MobileAlarmNotificationHelper
import com.example.opengluco.mobile.ui.auth.MobileLoginScreen
import com.example.opengluco.mobile.ui.dashboard.MobileDashboardScreen
import com.example.opengluco.mobile.ui.qr.QrScannerScreen
import com.example.opengluco.mobile.ui.theme.LibreMobileTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear canales de notificacion al inicio
        MobileAlarmNotificationHelper.createChannels(applicationContext)

        val repository = OpenGlucoRepository()
        val preferencesRepository = UserPreferencesRepository(applicationContext)

        setContent {
            val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = UserSettings())
            val isDark = settings.isDarkMode

            // Solicitar permiso de notificaciones en Android 13+ (API 33+)
            val context = LocalContext.current
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* No-op callback */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!hasPermission) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            LibreMobileTheme(darkTheme = isDark) {
                MobileAppNavigation(
                    repository = repository,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }
}

@Composable
fun MobileAppNavigation(
    repository: OpenGlucoRepository,
    preferencesRepository: UserPreferencesRepository
) {
    val navController = rememberNavController()
    val settings by preferencesRepository.userSettingsFlow.collectAsState(initial = UserSettings())

    val startDestination = if (settings.token.isNotBlank()) "dashboard" else "login"

    if (settings.token.isNotBlank()) {
        repository.setSession(settings.token, settings.userId)
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            MobileLoginScreen(
                repository = repository,
                preferencesRepository = preferencesRepository,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            MobileDashboardScreen(
                repository = repository,
                preferencesRepository = preferencesRepository,
                onOpenQrScanner = { navController.navigate("qr_scanner") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("qr_scanner") {
            QrScannerScreen(
                userEmail = settings.email,
                userToken = settings.token,
                userId = settings.userId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
