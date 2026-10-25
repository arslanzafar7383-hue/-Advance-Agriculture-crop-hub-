package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.repositories.CropHubRepository
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainAppContainer
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.CropHubTheme
import com.example.utils.PreferencesManager
import com.example.viewmodels.AuthViewModel
import com.example.viewmodels.CropHubViewModel

enum class AuthScreenState {
    SPLASH,
    LOGIN,
    REGISTER,
    MAIN
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferencesManager = PreferencesManager(applicationContext)
        val repository = CropHubRepository(preferencesManager)
        val localDatabase = com.example.data.local.AppDatabase.getDatabase(applicationContext)
        val localCartRepository = com.example.repositories.LocalCartRepository(localDatabase.cartDao())
        val authViewModel = AuthViewModel(repository, preferencesManager)
        val cropHubViewModel = CropHubViewModel(repository, localCartRepository, applicationContext, preferencesManager)
        val weatherRepository = com.example.repositories.WeatherRepositoryImpl(preferencesManager)
        val weatherViewModel = com.example.viewmodels.WeatherViewModel(weatherRepository)

        setContent {
            CropHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
                    val cropHubState by cropHubViewModel.uiState.collectAsStateWithLifecycle()
                    val weatherState by weatherViewModel.uiState.collectAsStateWithLifecycle()

                    var authScreen by remember { mutableStateOf(AuthScreenState.SPLASH) }

                    // Show error/success toasts from auth
                    LaunchedEffect(authState.errorMessage) {
                        authState.errorMessage?.let { msg ->
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            authViewModel.clearMessages()
                        }
                    }

                    LaunchedEffect(authState.successMessage) {
                        authState.successMessage?.let { msg ->
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                            authViewModel.clearMessages()
                        }
                    }

                    // Once logged in, move to main app
                    LaunchedEffect(authState.isLoggedIn) {
                        if (authState.isLoggedIn && authScreen != AuthScreenState.SPLASH) {
                            authScreen = AuthScreenState.MAIN
                        }
                    }

                    Crossfade(targetState = authScreen, label = "auth_screen_transition") { screen ->
                        when (screen) {
                            AuthScreenState.SPLASH -> {
                                SplashScreen(
                                    onTimeout = {
                                        authScreen = if (authState.isLoggedIn) {
                                            AuthScreenState.MAIN
                                        } else {
                                            AuthScreenState.LOGIN
                                        }
                                    }
                                )
                            }

                            AuthScreenState.LOGIN -> {
                                LoginScreen(
                                    uiState = authState,
                                    onLoginClick = { id, pass ->
                                        authViewModel.login(id, pass)
                                    },
                                    onNavigateRegister = {
                                        authScreen = AuthScreenState.REGISTER
                                    },
                                    onTestServer = { customUrl ->
                                        authViewModel.testServerConnection(customUrl)
                                    }
                                )
                            }

                            AuthScreenState.REGISTER -> {
                                RegisterScreen(
                                    uiState = authState,
                                    onRegisterClick = { name, email, phone, pass, type ->
                                        authViewModel.register(name, email, phone, pass, type)
                                    },
                                    onNavigateBack = {
                                        authScreen = AuthScreenState.LOGIN
                                    }
                                )
                            }

                            AuthScreenState.MAIN -> {
                                val targetScreen = when (intent?.getStringExtra(com.example.utils.NotificationHelper.EXTRA_DESTINATION)) {
                                    com.example.utils.NotificationHelper.DESTINATION_MARKETPLACE -> com.example.ui.screens.Screen.MARKETPLACE
                                    com.example.utils.NotificationHelper.DESTINATION_WEATHER -> com.example.ui.screens.Screen.WEATHER
                                    com.example.utils.NotificationHelper.DESTINATION_ORDERS -> com.example.ui.screens.Screen.PROFILE
                                    else -> com.example.ui.screens.Screen.HOME
                                }
                                MainAppContainer(
                                    authUiState = authState,
                                    cropHubUiState = cropHubState,
                                    cropHubViewModel = cropHubViewModel,
                                    weatherUiState = weatherState,
                                    weatherViewModel = weatherViewModel,
                                    preferencesManager = preferencesManager,
                                    initialScreen = targetScreen,
                                    onTestServer = { customUrl ->
                                        authViewModel.testServerConnection(customUrl)
                                    },
                                    onLogout = {
                                        authViewModel.logout()
                                        authScreen = AuthScreenState.LOGIN
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
