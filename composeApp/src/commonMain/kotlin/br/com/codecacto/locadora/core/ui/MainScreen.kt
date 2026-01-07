package br.com.codecacto.locadora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import br.com.codecacto.locadora.core.navigation.*
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.features.locacoes.presentation.LocacoesScreen
import br.com.codecacto.locadora.features.locacoes.presentation.NovaLocacaoScreen
import br.com.codecacto.locadora.features.locacoes.presentation.DetalhesLocacaoScreen
import br.com.codecacto.locadora.features.entregas.presentation.EntregasScreen
import br.com.codecacto.locadora.features.recebimentos.presentation.RecebimentosScreen
import br.com.codecacto.locadora.features.clientes.presentation.ClientesScreen
import br.com.codecacto.locadora.features.equipamentos.presentation.EquipamentosScreen

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    data object Locacoes : BottomNavItem("locacoes", Icons.Default.Home, Strings.NAV_LOCACOES)
    data object Entregas : BottomNavItem("entregas", Icons.Default.CalendarMonth, Strings.NAV_ENTREGAS)
    data object Recebimentos : BottomNavItem("recebimentos", Icons.Default.AttachMoney, Strings.NAV_RECEBER)
    data object Menu : BottomNavItem("menu", Icons.Default.Menu, Strings.NAV_MENU)
}

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showNovaLocacaoSheet by remember { mutableStateOf(false) }
    var currentMenuScreen by remember { mutableStateOf<String?>(null) }

    val bottomNavItems = listOf(
        BottomNavItem.Locacoes,
        BottomNavItem.Entregas,
        BottomNavItem.Recebimentos,
        BottomNavItem.Menu
    )

    Scaffold(
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left items (Locações, Entregas)
                    bottomNavItems.take(2).forEach { item ->
                        BottomNavItemView(
                            item = item,
                            isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                currentMenuScreen = null
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }

                    // Center button (Locar)
                    FloatingActionButton(
                        onClick = { showNovaLocacaoSheet = true },
                        modifier = Modifier
                            .offset(y = (-16).dp)
                            .size(56.dp),
                        containerColor = Color.Transparent,
                        elevation = FloatingActionButtonDefaults.elevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(8.dp, RoundedCornerShape(16.dp))
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(AppColors.Violet600, AppColors.Purple600)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = Strings.NAV_LOCAR,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Right items (Receber, Menu)
                    bottomNavItems.takeLast(2).forEach { item ->
                        BottomNavItemView(
                            item = item,
                            isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                if (item == BottomNavItem.Menu) {
                                    currentMenuScreen = null
                                }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Locacoes.route,
            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable(BottomNavItem.Locacoes.route) {
                LocacoesScreen(
                    onNavigateToDetalhes = { locacaoId ->
                        navController.navigate("detalhes_locacao/$locacaoId")
                    }
                )
            }
            composable(BottomNavItem.Entregas.route) {
                EntregasScreen(
                    onNavigateToDetalhes = { locacaoId ->
                        navController.navigate("detalhes_locacao/$locacaoId")
                    }
                )
            }
            composable(BottomNavItem.Recebimentos.route) {
                RecebimentosScreen(
                    onNavigateToDetalhes = { locacaoId ->
                        navController.navigate("detalhes_locacao/$locacaoId")
                    }
                )
            }
            composable(
                route = "detalhes_locacao/{locacaoId}",
                arguments = listOf(navArgument("locacaoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val locacaoId = backStackEntry.arguments?.getString("locacaoId") ?: return@composable
                DetalhesLocacaoScreen(
                    locacaoId = locacaoId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Menu.route) {
                when (currentMenuScreen) {
                    "clientes" -> ClientesScreen(onBack = { currentMenuScreen = null })
                    "equipamentos" -> EquipamentosScreen(onBack = { currentMenuScreen = null })
                    else -> MenuScreen(
                        onNavigateToClientes = { currentMenuScreen = "clientes" },
                        onNavigateToEquipamentos = { currentMenuScreen = "equipamentos" },
                        onLogout = onLogout
                    )
                }
            }
        }
    }

    // Nova Locação Bottom Sheet
    if (showNovaLocacaoSheet) {
        NovaLocacaoBottomSheet(
            onDismiss = { showNovaLocacaoSheet = false },
            onSuccess = { showNovaLocacaoSheet = false }
        )
    }
}

@Composable
private fun BottomNavItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = if (isSelected) AppColors.Violet600 else AppColors.Slate500

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            modifier = Modifier.size(24.dp),
            tint = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = color
        )
    }
}

@Composable
private fun MenuScreen(
    onNavigateToClientes: () -> Unit,
    onNavigateToEquipamentos: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Slate50)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Blue600)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Column {
                Text(
                    text = Strings.MENU_TITLE,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = Strings.MENU_SUBTITLE,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MenuItemCard(
                icon = Icons.Default.People,
                title = Strings.MENU_CLIENTES,
                subtitle = Strings.MENU_CLIENTES_SUBTITLE,
                backgroundColor = AppColors.Blue100,
                iconColor = AppColors.Blue600,
                onClick = onNavigateToClientes
            )
            MenuItemCard(
                icon = Icons.Default.Inventory2,
                title = Strings.MENU_EQUIPAMENTOS,
                subtitle = Strings.MENU_EQUIPAMENTOS_SUBTITLE,
                backgroundColor = AppColors.Violet100,
                iconColor = AppColors.Violet600,
                onClick = onNavigateToEquipamentos
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = AppColors.Slate200
            )

            MenuItemCard(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = Strings.MENU_SAIR,
                subtitle = Strings.MENU_SAIR_SUBTITLE,
                backgroundColor = AppColors.RedLight,
                iconColor = AppColors.Red,
                onClick = onLogout
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // App Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Slate100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = AppColors.Slate600
                    )
                }
                Column {
                    Text(
                        text = Strings.APP_DESCRIPTION,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = Strings.APP_VERSION,
                        fontSize = 12.sp,
                        color = AppColors.Slate500
                    )
                }
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Slate900
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = AppColors.Slate500
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovaLocacaoBottomSheet(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        NovaLocacaoScreen(
            onDismiss = onDismiss,
            onSuccess = onSuccess
        )
    }
}
