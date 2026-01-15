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
import br.com.codecacto.locadora.domain.repository.PurchaseRepository
import br.com.codecacto.locadora.features.auth.data.repository.AuthRepository
import br.com.codecacto.locadora.features.auth.presentation.login.LoginScreen
import br.com.codecacto.locadora.features.auth.presentation.register.RegisterScreen
import br.com.codecacto.locadora.features.subscription.presentation.SubscriptionScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

sealed class RootRoute(val route: String) {
    data object Auth : RootRoute("auth")
    data object Subscription : RootRoute("subscription")
    data object Main : RootRoute("main")
}

@Composable
fun RootNavigation() {
    val authRepository: AuthRepository = koinInject()
    val purchaseRepository: PurchaseRepository = koinInject()
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    var isCheckingAuth by remember { mutableStateOf(true) }
    var isCheckingSubscription by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var isPremium by remember { mutableStateOf(false) }

    // Observe auth state
    LaunchedEffect(Unit) {
        authRepository.observeAuthState().collect { user ->
            isAuthenticated = user != null
            isCheckingAuth = false

            // Se autenticado, verificar assinatura
            if (user != null) {
                isCheckingSubscription = true
                isPremium = purchaseRepository.isPremium()
                isCheckingSubscription = false
            }
        }
    }

    // Observe subscription state
    LaunchedEffect(Unit) {
        purchaseRepository.subscriptionState.collect { subscriptionInfo ->
            isPremium = subscriptionInfo.isActive
        }
    }

    // Navigate based on auth and subscription state
    LaunchedEffect(isAuthenticated, isPremium, isCheckingAuth, isCheckingSubscription) {
        if (!isCheckingAuth && !isCheckingSubscription) {
            val targetRoute = when {
                !isAuthenticated -> RootRoute.Auth.route
                !isPremium -> RootRoute.Subscription.route
                else -> RootRoute.Main.route
            }
            val currentRoute = navController.currentDestination?.route

            val shouldNavigate = currentRoute != targetRoute &&
                currentRoute != "login" &&
                currentRoute != "register"

            if (shouldNavigate) {
                navController.navigate(targetRoute) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    if (isCheckingAuth || isCheckingSubscription) {
        // Show loading while checking auth/subscription state
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        val startDestination = when {
            !isAuthenticated -> RootRoute.Auth.route
            !isPremium -> RootRoute.Subscription.route
            else -> RootRoute.Main.route
        }

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            // Auth Flow
            composable(RootRoute.Auth.route) {
                AuthNavigation(
                    onNavigateToMain = {
                        // Após login, verificar subscription
                        scope.launch {
                            val hasPremium = purchaseRepository.isPremium()
                            if (hasPremium) {
                                navController.navigate(RootRoute.Main.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            } else {
                                navController.navigate(RootRoute.Subscription.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    }
                )
            }

            // Subscription Screen
            composable(RootRoute.Subscription.route) {
                SubscriptionScreen(
                    onSubscriptionSuccess = {
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
