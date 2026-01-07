package br.com.codecacto.locadora.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import br.com.codecacto.locadora.features.auth.presentation.login.LoginScreen
import br.com.codecacto.locadora.features.auth.presentation.register.RegisterScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

sealed class RootRoute(val route: String) {
    data object Auth : RootRoute("auth")
    data object Main : RootRoute("main")
}

@Composable
fun RootNavigation() {
    val authRepository: AuthRepository = koinInject()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var isCheckingAuth by remember { mutableStateOf(true) }
    var isAuthenticated by remember { mutableStateOf(false) }

    // Observe auth state
    LaunchedEffect(Unit) {
        authRepository.observeAuthState().collect { user ->
            isAuthenticated = user != null
            isCheckingAuth = false
        }
    }

    // Navigate based on auth state
    LaunchedEffect(isAuthenticated, isCheckingAuth) {
        if (!isCheckingAuth) {
            val targetRoute = if (isAuthenticated) RootRoute.Main.route else RootRoute.Auth.route
            val currentRoute = navController.currentDestination?.route

            if (currentRoute != targetRoute && currentRoute != "login" && currentRoute != "register") {
                navController.navigate(targetRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    if (isCheckingAuth) {
        // Show loading while checking auth state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        NavHost(
            navController = navController,
            startDestination = if (isAuthenticated) RootRoute.Main.route else RootRoute.Auth.route
        ) {
            // Auth Flow
            composable(RootRoute.Auth.route) {
                AuthNavigation(
                    onNavigateToMain = {
                        navController.navigate(RootRoute.Main.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Main App
            composable(RootRoute.Main.route) {
                MainScreen(
                    onLogout = {
                        scope.launch {
                            authRepository.logout()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AuthNavigation(
    onNavigateToMain: () -> Unit
) {
    val authNavController = rememberNavController()

    NavHost(
        navController = authNavController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToHome = onNavigateToMain,
                onNavigateToRegister = {
                    authNavController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToHome = onNavigateToMain,
                onNavigateToLogin = {
                    authNavController.popBackStack()
                }
            )
        }
    }
}
