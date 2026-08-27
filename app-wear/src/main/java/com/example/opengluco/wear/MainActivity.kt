package com.example.opengluco.wear

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.opengluco.core.data.OpenGlucoRepository
import com.example.opengluco.core.data.UserPreferencesRepository
import com.example.opengluco.wear.notification.WearAlarmNotificationHelper
import com.example.opengluco.wear.service.WearBluetoothRfcommService
import com.example.opengluco.wear.ui.auth.WearLoginScreen
import com.example.opengluco.wear.ui.auth.WearLoginViewModel
import com.example.opengluco.wear.ui.auth.WearQrLoginScreen
import com.example.opengluco.wear.ui.dashboard.WearDashboardScreen
import com.example.opengluco.wear.ui.dashboard.WearDashboardViewModel
import com.example.opengluco.wear.ui.settings.WearSettingsScreen
import com.example.opengluco.wear.ui.theme.LibreWearTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            checkAndStartRfcommService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WearAlarmNotificationHelper.createChannels(this)

        val preferencesRepository = UserPreferencesRepository(applicationContext)
        val repository = OpenGlucoRepository()

        requestBluetoothPermissions()

        setContent {
            LibreWearTheme {
                WearAppNavigation(
                    repository = repository,
                    preferencesRepository = preferencesRepository
                )
            }
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            if (connectPermission != PackageManager.PERMISSION_GRANTED) {
                bluetoothPermissionLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
                )
                return
            }
        }
        checkAndStartRfcommService()
    }

    private fun checkAndStartRfcommService() {
        lifecycleScope.launch {
            val userPrefs = UserPreferencesRepository(applicationContext)
            val settings = userPrefs.userSettingsFlow.first()
            if (settings.token.isNotBlank() && settings.userId.isNotBlank()) {
                WearBluetoothRfcommService.start(applicationContext)
            }
        }
    }
}

@Composable
fun WearAppNavigation(
    repository: OpenGlucoRepository,
    preferencesRepository: UserPreferencesRepository
) {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            val viewModel: WearDashboardViewModel = viewModel {
                WearDashboardViewModel(repository, preferencesRepository)
            }
            WearDashboardScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate("qr_login") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }

        composable("qr_login") {
            WearQrLoginScreen(
                preferencesRepository = preferencesRepository,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("qr_login") { inclusive = true }
                    }
                },
                onManualLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("login") {
            val viewModel: WearLoginViewModel = viewModel {
                WearLoginViewModel(repository, preferencesRepository)
            }
            WearLoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("settings") {
            WearSettingsScreen(
                preferencesRepository = preferencesRepository,
                onLogoutSuccess = {
                    navController.navigate("qr_login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }
    }
}
