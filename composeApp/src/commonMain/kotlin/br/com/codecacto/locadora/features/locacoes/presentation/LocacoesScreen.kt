package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.ui.components.SuccessToast
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.model.StatusPrazo
import br.com.codecacto.locadora.core.ui.components.NotificationBadge
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocacoesScreen(
    onNavigateToDetalhes: (String) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    unreadNotifications: Int = 0,
    viewModel: LocacoesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var locacaoToDelete by remember { mutableStateOf<Locacao?>(null) }
    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is LocacoesContract.Effect.NavigateToDetalhes -> {
                    onNavigateToDetalhes(effect.locacaoId)
                }
                is LocacoesContract.Effect.ShowError -> {
                    // Handle error
                }
                is LocacoesContract.Effect.ShowSuccess -> {
                    successMessage = effect.message
                    showSuccessToast = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Slate50)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.Violet600, AppColors.Purple600)
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = Strings.LOCACOES_TITLE,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.tabSelecionada == 0) {
                            Strings.locacoesAtivas(state.locacoesAtivas.size)
                        } else {
                            Strings.locacoesFinalizadas(state.locacoesFinalizadas.size)
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }

                NotificationBadge(
                    count = unreadNotifications,
                    onClick = onNavigateToNotifications
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = Strings.LOCACOES_TAB_ATIVOS,
                    isSelected = state.tabSelecionada == 0,
                    onClick = { viewModel.dispatch(LocacoesContract.Action.SelectTab(0)) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = Strings.LOCACOES_TAB_FINALIZADOS,
                    isSelected = state.tabSelecionada == 1,
                    onClick = { viewModel.dispatch(LocacoesContract.Action.SelectTab(1)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Content
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.dispatch(LocacoesContract.Action.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Violet600)
                }
            } else {
                val locacoes = if (state.tabSelecionada == 0) {
                    state.locacoesAtivas
                } else {
                    state.locacoesFinalizadas
                }

                if (locacoes.isEmpty()) {
                    EmptyState(
                        title = if (state.tabSelecionada == 0) Strings.LOCACOES_EMPTY_ATIVAS_TITLE else Strings.LOCACOES_EMPTY_FINALIZADAS_TITLE,
                        subtitle = if (state.tabSelecionada == 0) {
                            Strings.LOCACOES_EMPTY_ATIVAS_SUBTITLE
                        } else {
                            Strings.LOCACOES_EMPTY_FINALIZADAS_SUBTITLE
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(locacoes) { locacaoComDetalhes ->
                            LocacaoCard(
                                locacaoComDetalhes = locacaoComDetalhes,
                                onClick = {
                                    viewModel.dispatch(
                                        LocacoesContract.Action.SelectLocacao(locacaoComDetalhes.locacao)
                                    )
                                },
                                onDelete = {
                                    locacaoToDelete = locacaoComDetalhes.locacao
                                    showDeleteDialog = true
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDeleteDialog && locacaoToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                locacaoToDelete = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AppColors.RedDark
                )
            },
            title = {
                Text(
                    text = Strings.LOCACOES_EXCLUIR_TITULO,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = Strings.LOCACOES_EXCLUIR_MENSAGEM)
            },
            confirmButton = {
                Button(
                    onClick = {
                        locacaoToDelete?.let {
                            viewModel.dispatch(LocacoesContract.Action.DeleteLocacao(it))
                        }
                        showDeleteDialog = false
                        locacaoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.RedDark)
                ) {
                    Text(Strings.COMMON_EXCLUIR)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        locacaoToDelete = null
                    }
                ) {
                    Text(Strings.COMMON_CANCELAR)
                }
            }
        )
    }

    // Success Toast
    SuccessToast(
        message = successMessage,
        visible = showSuccessToast,
        onDismiss = { showSuccessToast = false }
    )
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) Color.White
                else Color.White.copy(alpha = 0.1f)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) AppColors.Violet600 else Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun LocacaoCard(
    locacaoComDetalhes: LocacaoComDetalhes,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = locacaoComDetalhes.cliente?.nomeRazao ?: Strings.COMMON_CLIENTE_NAO_ENCONTRADO,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = locacaoComDetalhes.equipamento?.nome ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Só mostrar badge se não foi pago E não está normal
                    val isPago = locacaoComDetalhes.locacao.statusPagamento == StatusPagamento.PAGO
                    val statusPrazo = locacaoComDetalhes.statusPrazo
                    if (!isPago && statusPrazo != StatusPrazo.NORMAL) {
                        StatusPrazoBadge(statusPrazo = statusPrazo)
                    }
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = Strings.MENU_TITLE,
                                tint = AppColors.Slate500
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = AppColors.RedDark,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = Strings.COMMON_EXCLUIR,
                                            color = AppColors.RedDark
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoBox(
                    icon = Icons.Default.CalendarMonth,
                    label = Strings.ENTREGAS_VENCIMENTO,
                    value = formatDate(locacaoComDetalhes.locacao.dataFimPrevista),
                    modifier = Modifier.weight(1f)
                )
                InfoBox(
                    icon = Icons.Default.AttachMoney,
                    label = Strings.DETALHES_VALOR,
                    value = formatCurrency(locacaoComDetalhes.locacao.valorLocacao),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status Badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(
                    icon = Icons.Default.AttachMoney,
                    label = if (locacaoComDetalhes.locacao.statusPagamento == StatusPagamento.PAGO) Strings.STATUS_PAGAMENTO_PAGO else Strings.STATUS_PAGAMENTO_PENDENTE,
                    isSuccess = locacaoComDetalhes.locacao.statusPagamento == StatusPagamento.PAGO
                )
                StatusBadge(
                    icon = Icons.Default.LocalShipping,
                    label = when (locacaoComDetalhes.locacao.statusEntrega) {
                        StatusEntrega.ENTREGUE -> Strings.STATUS_ENTREGA_ENTREGUE
                        StatusEntrega.AGENDADA -> Strings.STATUS_ENTREGA_AGENDADA
                        else -> Strings.STATUS_PAGAMENTO_PENDENTE
                    },
                    isSuccess = locacaoComDetalhes.locacao.statusEntrega == StatusEntrega.ENTREGUE
                )
                if (locacaoComDetalhes.locacao.emitirNota) {
                    StatusBadge(
                        icon = Icons.Default.Description,
                        label = if (locacaoComDetalhes.locacao.notaEmitida) "NF Emitida" else "NF Pendente",
                        isSuccess = locacaoComDetalhes.locacao.notaEmitida
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusPrazoBadge(statusPrazo: StatusPrazo) {
    val (backgroundColor, textColor, text) = when (statusPrazo) {
        StatusPrazo.VENCIDO -> Triple(AppColors.RedLight, AppColors.RedDark, Strings.STATUS_PRAZO_VENCIDO)
        StatusPrazo.PROXIMO_VENCIMENTO -> Triple(AppColors.YellowLight, AppColors.Amber500, Strings.STATUS_PRAZO_PROXIMO)
        StatusPrazo.NORMAL -> Triple(AppColors.GreenLight, AppColors.GreenDark, Strings.STATUS_PRAZO_NORMAL)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoBox(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(12.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = AppColors.Slate500
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = AppColors.Slate500
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate900
            )
        }
    }
}

@Composable
private fun StatusBadge(
    icon: ImageVector,
    label: String,
    isSuccess: Boolean
) {
    val backgroundColor = if (isSuccess) AppColors.GreenLight else AppColors.Slate200
    val textColor = if (isSuccess) AppColors.GreenDark else AppColors.Slate700

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = textColor
        )
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Violet100, AppColors.Violet100)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.Violet400
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate800,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = AppColors.Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }/${localDateTime.year}"
}

private fun formatCurrency(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()

    // Formatar parte inteira com separador de milhares
    val intPartFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "R$ $intPartFormatted,${decPart.toString().padStart(2, '0')}"
}
