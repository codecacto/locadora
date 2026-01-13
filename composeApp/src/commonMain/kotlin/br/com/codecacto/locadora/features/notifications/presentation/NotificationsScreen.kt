package br.com.codecacto.locadora.features.notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.Notificacao
import br.com.codecacto.locadora.core.model.NotificacaoTipo
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.currentTimeMillis
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NotificationsContract.Effect.NavigateBack -> onBack()
                is NotificationsContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is NotificationsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = Strings.COMMON_VOLTAR,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = Strings.NOTIFICATIONS_TITLE,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.unreadCount > 0)
                                    "${state.unreadCount} nao lidas"
                                else
                                    Strings.NOTIFICATIONS_SUBTITLE,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Menu de ações
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(Strings.NOTIFICATIONS_MARCAR_TODAS_LIDAS) },
                                onClick = {
                                    viewModel.dispatch(NotificationsContract.Action.OnMarcarTodasComoLidas)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DoneAll, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(Strings.NOTIFICATIONS_LIMPAR_LIDAS) },
                                onClick = {
                                    viewModel.dispatch(NotificationsContract.Action.OnLimparLidas)
                                    showMenu = false
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            // Content
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.dispatch(NotificationsContract.Action.Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Blue600)
                    }
                } else if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.RedLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = AppColors.Red
                                )
                            }
                            Text(
                                text = "Erro ao carregar",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Slate700
                            )
                            Text(
                                text = "Nao foi possivel carregar as notificacoes. Tente novamente.",
                                fontSize = 14.sp,
                                color = AppColors.Slate500
                            )
                        }
                    }
                } else if (state.notificacoes.isEmpty()) {
                    // Empty State
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(AppColors.Slate100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = AppColors.Slate400
                                )
                            }
                            Text(
                                text = Strings.NOTIFICATIONS_EMPTY_TITLE,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Slate700
                            )
                            Text(
                                text = Strings.NOTIFICATIONS_EMPTY_SUBTITLE,
                                fontSize = 14.sp,
                                color = AppColors.Slate500
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = state.notificacoes,
                            key = { it.id }
                        ) { notificacao ->
                            NotificacaoItem(
                                notificacao = notificacao,
                                onClick = {
                                    viewModel.dispatch(NotificationsContract.Action.OnNotificacaoClick(notificacao))
                                },
                                onDelete = {
                                    viewModel.dispatch(NotificationsContract.Action.OnExcluir(notificacao.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificacaoItem(
    notificacao: Notificacao,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, iconColor, bgColor) = getNotificacaoStyle(notificacao.tipo)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (notificacao.lida) Color.White else AppColors.Blue100.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notificacao.lida) 1.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notificacao.titulo,
                        fontWeight = if (notificacao.lida) FontWeight.Normal else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = AppColors.Slate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (!notificacao.lida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppColors.Blue600)
                        )
                    }
                }

                Text(
                    text = notificacao.mensagem,
                    fontSize = 13.sp,
                    color = AppColors.Slate600,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimeAgo(notificacao.criadoEm),
                    fontSize = 11.sp,
                    color = AppColors.Slate400
                )
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = Strings.NOTIFICATIONS_EXCLUIR,
                    tint = AppColors.Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getNotificacaoStyle(tipo: String): Triple<ImageVector, Color, Color> {
    return when (tipo) {
        NotificacaoTipo.LOCACAO.valor -> Triple(
            Icons.Default.Home,
            AppColors.Violet600,
            AppColors.Violet100
        )
        NotificacaoTipo.ENTREGA.valor -> Triple(
            Icons.Default.LocalShipping,
            AppColors.Orange500,
            AppColors.Orange100
        )
        NotificacaoTipo.PAGAMENTO.valor -> Triple(
            Icons.Default.AttachMoney,
            AppColors.Green,
            AppColors.GreenLight
        )
        NotificacaoTipo.VENCIMENTO.valor -> Triple(
            Icons.Default.Warning,
            AppColors.Red,
            AppColors.RedLight
        )
        NotificacaoTipo.SISTEMA.valor -> Triple(
            Icons.Default.Settings,
            AppColors.Slate600,
            AppColors.Slate100
        )
        else -> Triple(
            Icons.Default.Info,
            AppColors.Blue600,
            AppColors.Blue100
        )
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val now = currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Agora"
        minutes < 60 -> "${minutes.toInt()} min atras"
        hours < 24 -> "${hours.toInt()}h atras"
        days < 2 -> "Ontem"
        days < 7 -> "${days.toInt()} dias atras"
        else -> "${days.toInt() / 7} semanas atras"
    }
}
