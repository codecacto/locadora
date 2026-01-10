package br.com.codecacto.locadora.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import br.com.codecacto.locadora.features.locacoes.presentation.NovaLocacaoViewModel
import br.com.codecacto.locadora.features.locacoes.presentation.NovaLocacaoContract
import br.com.codecacto.locadora.features.locacoes.presentation.DetalhesLocacaoScreen
import br.com.codecacto.locadora.features.locacoes.presentation.SelecionarClienteScreen
import br.com.codecacto.locadora.features.locacoes.presentation.SelecionarEquipamentoScreen
import org.koin.compose.viewmodel.koinViewModel
import br.com.codecacto.locadora.features.entregas.presentation.EntregasScreen
import br.com.codecacto.locadora.features.recebimentos.presentation.RecebimentosScreen
import br.com.codecacto.locadora.features.recebimentos.presentation.RecebimentosLocacaoScreen
import br.com.codecacto.locadora.features.clientes.presentation.ClientesScreen
import br.com.codecacto.locadora.features.clientes.presentation.ClienteFormScreen
import br.com.codecacto.locadora.features.equipamentos.presentation.EquipamentosScreen
import br.com.codecacto.locadora.features.equipamentos.presentation.EquipamentoFormScreen
import br.com.codecacto.locadora.features.settings.presentation.SettingsScreen
import br.com.codecacto.locadora.features.settings.presentation.ChangePasswordScreen
import br.com.codecacto.locadora.features.settings.presentation.ChangeEmailScreen
import br.com.codecacto.locadora.features.settings.presentation.ChangeProfileScreen
import br.com.codecacto.locadora.features.settings.presentation.DataPrivacyScreen
import br.com.codecacto.locadora.features.settings.presentation.DadosEmpresaScreen
import br.com.codecacto.locadora.features.settings.presentation.HorarioNotificacaoScreen
import br.com.codecacto.locadora.features.feedback.presentation.FeedbackScreen
import br.com.codecacto.locadora.features.notifications.presentation.NotificationsScreen
import br.com.codecacto.locadora.core.ui.components.NotificationBadge
import br.com.codecacto.locadora.data.repository.NotificacaoRepository
import br.com.codecacto.locadora.getAppVersion
import org.koin.compose.koinInject

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

private enum class NovaLocacaoSubScreen {
    NONE,
    SELECIONAR_CLIENTE,
    SELECIONAR_EQUIPAMENTO,
    ADICIONAR_CLIENTE,
    ADICIONAR_EQUIPAMENTO
}

@Composable
fun MainScreen(
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showNovaLocacaoSheet by remember { mutableStateOf(false) }
    var novaLocacaoSubScreen by remember { mutableStateOf(NovaLocacaoSubScreen.NONE) }
    var currentMenuScreen by remember { mutableStateOf<String?>(null) }
    var editingClienteId by remember { mutableStateOf<String?>(null) }
    var editingEquipamentoId by remember { mutableStateOf<String?>(null) }

    // ViewModel compartilhado para Nova Locacao
    val novaLocacaoViewModel: NovaLocacaoViewModel = koinViewModel()

    // Notification state
    val notificacaoRepository: NotificacaoRepository = koinInject()
    val unreadNotifications by notificacaoRepository.getUnreadCount().collectAsState(initial = 0)

    val bottomNavItems = listOf(
        BottomNavItem.Locacoes,
        BottomNavItem.Entregas,
        BottomNavItem.Recebimentos,
        BottomNavItem.Menu
    )

    // Hide bottom bar on certain routes
    val hideBottomBar = currentDestination?.route in listOf("notifications", "detalhes_locacao/{locacaoId}", "recebimentos_locacao/{locacaoId}")

    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
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
                    },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    unreadNotifications = unreadNotifications
                )
            }
            composable(BottomNavItem.Entregas.route) {
                EntregasScreen(
                    onNavigateToDetalhes = { locacaoId ->
                        navController.navigate("detalhes_locacao/$locacaoId")
                    },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    unreadNotifications = unreadNotifications
                )
            }
            composable(BottomNavItem.Recebimentos.route) {
                RecebimentosScreen(
                    onNavigateToDetalhes = { locacaoId ->
                        navController.navigate("detalhes_locacao/$locacaoId")
                    },
                    onNavigateToNotifications = { navController.navigate("notifications") },
                    unreadNotifications = unreadNotifications
                )
            }
            composable(
                route = "detalhes_locacao/{locacaoId}",
                arguments = listOf(navArgument("locacaoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val locacaoId = backStackEntry.arguments?.getString("locacaoId") ?: return@composable
                DetalhesLocacaoScreen(
                    locacaoId = locacaoId,
                    onBack = { navController.popBackStack() },
                    onNavigateToRecebimentos = { locId ->
                        navController.navigate("recebimentos_locacao/$locId")
                    }
                )
            }
            composable("notifications") {
                NotificationsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "recebimentos_locacao/{locacaoId}",
                arguments = listOf(navArgument("locacaoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val locacaoId = backStackEntry.arguments?.getString("locacaoId") ?: return@composable
                RecebimentosLocacaoScreen(
                    locacaoId = locacaoId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(BottomNavItem.Menu.route) {
                when (currentMenuScreen) {
                    "clientes" -> ClientesScreen(
                        onBack = { currentMenuScreen = null },
                        onNavigateToForm = { clienteId ->
                            editingClienteId = clienteId
                            currentMenuScreen = "cliente_form"
                        }
                    )
                    "cliente_form" -> ClienteFormScreen(
                        clienteId = editingClienteId,
                        onBack = {
                            editingClienteId = null
                            currentMenuScreen = "clientes"
                        }
                    )
                    "equipamentos" -> EquipamentosScreen(
                        onBack = { currentMenuScreen = null },
                        onNavigateToForm = { equipamentoId ->
                            editingEquipamentoId = equipamentoId
                            currentMenuScreen = "equipamento_form"
                        }
                    )
                    "equipamento_form" -> EquipamentoFormScreen(
                        equipamentoId = editingEquipamentoId,
                        onBack = {
                            editingEquipamentoId = null
                            currentMenuScreen = "equipamentos"
                        }
                    )
                    "settings" -> SettingsScreen(
                        onBack = { currentMenuScreen = null },
                        onNavigateToChangePassword = { currentMenuScreen = "change_password" },
                        onNavigateToChangeEmail = { currentMenuScreen = "change_email" }
                    )
                    "change_password" -> ChangePasswordScreen(
                        onBack = { currentMenuScreen = "settings" }
                    )
                    "change_email" -> ChangeEmailScreen(
                        onBack = { currentMenuScreen = "settings" }
                    )
                    "profile" -> ChangeProfileScreen(
                        onBack = { currentMenuScreen = null }
                    )
                    "data_privacy" -> DataPrivacyScreen(
                        onBack = { currentMenuScreen = null },
                        onAccountDeleted = onLogout
                    )
                    "feedback" -> FeedbackScreen(
                        onBack = { currentMenuScreen = null }
                    )
                    "dados_empresa" -> DadosEmpresaScreen(
                        onBack = { currentMenuScreen = null }
                    )
                    "horario_notificacao" -> HorarioNotificacaoScreen(
                        onBack = { currentMenuScreen = null }
                    )
                    else -> MenuScreen(
                        onNavigateToClientes = { currentMenuScreen = "clientes" },
                        onNavigateToEquipamentos = { currentMenuScreen = "equipamentos" },
                        onNavigateToSettings = { currentMenuScreen = "settings" },
                        onNavigateToProfile = { currentMenuScreen = "profile" },
                        onNavigateToFeedback = { currentMenuScreen = "feedback" },
                        onNavigateToDataPrivacy = { currentMenuScreen = "data_privacy" },
                        onNavigateToDadosEmpresa = { currentMenuScreen = "dados_empresa" },
                        onNavigateToHorarioNotificacao = { currentMenuScreen = "horario_notificacao" },
                        onNavigateToNotifications = { navController.navigate("notifications") },
                        unreadNotifications = unreadNotifications,
                        onLogout = onLogout
                    )
                }
            }
        }
    }

    // Nova Locação Bottom Sheet
    if (showNovaLocacaoSheet) {
        NovaLocacaoBottomSheet(
            onDismiss = {
                showNovaLocacaoSheet = false
                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
            },
            onSuccess = {
                showNovaLocacaoSheet = false
                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
                // Navegar para a aba Locações após criar com sucesso
                navController.navigate(BottomNavItem.Locacoes.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            onNavigateToSelecionarCliente = {
                novaLocacaoSubScreen = NovaLocacaoSubScreen.SELECIONAR_CLIENTE
            },
            onNavigateToSelecionarEquipamento = {
                novaLocacaoSubScreen = NovaLocacaoSubScreen.SELECIONAR_EQUIPAMENTO
            },
            viewModel = novaLocacaoViewModel
        )
    }

    // Telas de selecao como overlay (por cima de tudo usando Dialog)
    val novaLocacaoState by novaLocacaoViewModel.state.collectAsState()

    if (novaLocacaoSubScreen != NovaLocacaoSubScreen.NONE) {
        Dialog(
            onDismissRequest = {
                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                when (novaLocacaoSubScreen) {
                    NovaLocacaoSubScreen.SELECIONAR_CLIENTE -> {
                        SelecionarClienteScreen(
                            clientes = novaLocacaoState.clientes,
                            onSelect = { cliente ->
                                novaLocacaoViewModel.dispatch(NovaLocacaoContract.Action.SelectCliente(cliente))
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
                            },
                            onAddNew = {
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.ADICIONAR_CLIENTE
                            },
                            onDismiss = {
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
                            }
                        )
                    }
                    NovaLocacaoSubScreen.SELECIONAR_EQUIPAMENTO -> {
                        SelecionarEquipamentoScreen(
                            equipamentos = novaLocacaoState.equipamentosDisponiveis,
                            onSelect = { equipamento ->
                                novaLocacaoViewModel.dispatch(NovaLocacaoContract.Action.SelectEquipamento(equipamento))
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
                            },
                            onAddNew = {
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.ADICIONAR_EQUIPAMENTO
                            },
                            onDismiss = {
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.NONE
                            }
                        )
                    }
                    NovaLocacaoSubScreen.ADICIONAR_CLIENTE -> {
                        ClienteFormScreen(
                            clienteId = null,
                            onBack = {
                                novaLocacaoViewModel.dispatch(NovaLocacaoContract.Action.ReloadData)
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.SELECIONAR_CLIENTE
                            }
                        )
                    }
                    NovaLocacaoSubScreen.ADICIONAR_EQUIPAMENTO -> {
                        EquipamentoFormScreen(
                            equipamentoId = null,
                            onBack = {
                                novaLocacaoViewModel.dispatch(NovaLocacaoContract.Action.ReloadData)
                                novaLocacaoSubScreen = NovaLocacaoSubScreen.SELECIONAR_EQUIPAMENTO
                            }
                        )
                    }
                    NovaLocacaoSubScreen.NONE -> { /* Nao deveria chegar aqui */ }
                }
            }
        }
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
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToDataPrivacy: () -> Unit,
    onNavigateToDadosEmpresa: () -> Unit,
    onNavigateToHorarioNotificacao: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    unreadNotifications: Int,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val appVersion = remember { getAppVersion() }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

                NotificationBadge(
                    count = unreadNotifications,
                    onClick = onNavigateToNotifications
                )
            }
        }

        // Menu Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
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
            MenuItemCard(
                icon = Icons.Default.Description,
                title = Strings.MENU_DADOS_COMPROVANTE,
                subtitle = Strings.MENU_DADOS_COMPROVANTE_SUBTITLE,
                backgroundColor = AppColors.Emerald100,
                iconColor = AppColors.Emerald600,
                onClick = onNavigateToDadosEmpresa
            )
            MenuItemCard(
                icon = Icons.Default.Notifications,
                title = Strings.MENU_HORARIO_NOTIFICACAO,
                subtitle = Strings.MENU_HORARIO_NOTIFICACAO_SUBTITLE,
                backgroundColor = AppColors.Amber100,
                iconColor = AppColors.Amber500,
                onClick = onNavigateToHorarioNotificacao
            )
            MenuItemCard(
                icon = Icons.Default.Settings,
                title = Strings.MENU_CONFIGURACOES,
                subtitle = Strings.MENU_CONFIGURACOES_SUBTITLE,
                backgroundColor = AppColors.Slate100,
                iconColor = AppColors.Slate600,
                onClick = onNavigateToSettings
            )
            MenuItemCard(
                icon = Icons.Default.Person,
                title = Strings.MENU_PERFIL,
                subtitle = Strings.MENU_PERFIL_SUBTITLE,
                backgroundColor = AppColors.Orange100,
                iconColor = AppColors.Orange500,
                onClick = onNavigateToProfile
            )
            MenuItemCard(
                icon = Icons.Default.Feedback,
                title = Strings.MENU_FEEDBACK,
                subtitle = Strings.MENU_FEEDBACK_SUBTITLE,
                backgroundColor = AppColors.Violet100,
                iconColor = AppColors.Violet600,
                onClick = onNavigateToFeedback
            )
            MenuItemCard(
                icon = Icons.Default.Security,
                title = Strings.MENU_DADOS_PRIVACIDADE,
                subtitle = Strings.MENU_DADOS_PRIVACIDADE_SUBTITLE,
                backgroundColor = AppColors.Green100,
                iconColor = AppColors.Green600,
                onClick = onNavigateToDataPrivacy
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
                onClick = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App Info
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                            text = appVersion.displayVersion,
                            fontSize = 12.sp,
                            color = AppColors.Slate500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Dialog de confirmação de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Sair",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Deseja realmente sair do aplicativo?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text(Strings.COMMON_CONFIRMAR)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(Strings.COMMON_CANCELAR)
                }
            }
        )
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
    onSuccess: () -> Unit,
    onNavigateToSelecionarCliente: () -> Unit,
    onNavigateToSelecionarEquipamento: () -> Unit,
    viewModel: NovaLocacaoViewModel
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        NovaLocacaoScreen(
            onDismiss = onDismiss,
            onSuccess = onSuccess,
            onNavigateToSelecionarCliente = onNavigateToSelecionarCliente,
            onNavigateToSelecionarEquipamento = onNavigateToSelecionarEquipamento,
            viewModel = viewModel
        )
    }
}
