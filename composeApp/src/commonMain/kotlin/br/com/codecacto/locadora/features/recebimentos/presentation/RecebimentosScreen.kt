package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.components.NotificationBadge
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecebimentosScreen(
    onNavigateToDetalhes: (String) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    unreadNotifications: Int = 0,
    viewModel: RecebimentosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is RecebimentosContract.Effect.NavigateToDetalhes -> onNavigateToDetalhes(effect.locacaoId)
                is RecebimentosContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is RecebimentosContract.Effect.ShowError -> {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.Emerald600, AppColors.Green)
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
                        text = Strings.RECEBIMENTOS_TITLE,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (state.tabSelecionada == 0) {
                            Strings.recebimentosPendentes(state.recebimentosPendentes.size)
                        } else {
                            Strings.recebimentosPagos(state.recebimentosPagos.size)
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    val showTotal = if (state.tabSelecionada == 0) {
                        state.recebimentosPendentes.isNotEmpty()
                    } else {
                        state.recebimentosPagos.isNotEmpty()
                    }
                    if (showTotal) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.tabSelecionada == 0) Strings.RECEBIMENTOS_TOTAL else Strings.RECEBIMENTOS_TOTAL_PAGO,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatCurrency(if (state.tabSelecionada == 0) state.totalPendente else state.totalPago),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                    text = Strings.RECEBIMENTOS_TAB_PENDENTES,
                    isSelected = state.tabSelecionada == 0,
                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectTab(0)) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = Strings.RECEBIMENTOS_TAB_PAGOS,
                    isSelected = state.tabSelecionada == 1,
                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectTab(1)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Content
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.dispatch(RecebimentosContract.Action.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Emerald600)
                }
            } else {
                val recebimentos = if (state.tabSelecionada == 0) {
                    state.recebimentosPendentes
                } else {
                    state.recebimentosPagos
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (recebimentos.isEmpty()) {
                        item {
                            EmptyRecebimentosState(isPagos = state.tabSelecionada == 1)
                        }
                    } else {
                        items(recebimentos) { recebimento ->
                            if (state.tabSelecionada == 0) {
                                RecebimentoCard(
                                    recebimento = recebimento,
                                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectLocacao(recebimento.locacao)) },
                                    onMarcarRecebido = { viewModel.dispatch(RecebimentosContract.Action.MarcarRecebido(recebimento.locacao.id)) }
                                )
                            } else {
                                RecebimentoPagoCard(
                                    recebimento = recebimento,
                                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectLocacao(recebimento.locacao)) }
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
    }
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
            color = if (isSelected) AppColors.Emerald600 else Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RecebimentoCard(
    recebimento: RecebimentoComDetalhes,
    onClick: () -> Unit,
    onMarcarRecebido: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recebimento.cliente?.nomeRazao ?: Strings.COMMON_CLIENTE_NAO_ENCONTRADO,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = recebimento.equipamento?.nome ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.YellowLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.STATUS_PAGAMENTO_PENDENTE,
                        color = AppColors.Amber500,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Valor em destaque
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Emerald100, AppColors.GreenLight)
                        )
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.Emerald100),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = AppColors.Emerald600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = Strings.RECEBIMENTOS_VALOR,
                            fontSize = 14.sp,
                            color = AppColors.Slate600
                        )
                    }
                    Text(
                        text = formatCurrency(recebimento.locacao.valorLocacao),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Emerald600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoBox(
                    label = Strings.RECEBIMENTOS_PERIODO,
                    value = "${formatDate(recebimento.locacao.dataInicio)} - ${formatDate(recebimento.locacao.dataFimPrevista)}",
                    modifier = Modifier.weight(1f)
                )
                InfoBox(
                    label = Strings.RECEBIMENTOS_STATUS,
                    value = Strings.STATUS_COLETA_COLETADO,
                    modifier = Modifier.weight(1f),
                    valueColor = AppColors.Emerald600
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onMarcarRecebido,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Emerald600
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.RECEBIMENTOS_CONFIRMAR)
            }
        }
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AppColors.Slate900
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = AppColors.Slate500
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun RecebimentoPagoCard(
    recebimento: RecebimentoComDetalhes,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recebimento.cliente?.nomeRazao ?: Strings.COMMON_CLIENTE_NAO_ENCONTRADO,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = recebimento.equipamento?.nome ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.GreenLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = Strings.STATUS_PAGAMENTO_PAGO,
                        color = AppColors.Green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Valor em destaque
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Slate100)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AppColors.GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AppColors.Green,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Text(
                            text = Strings.RECEBIMENTOS_VALOR_PAGO,
                            fontSize = 14.sp,
                            color = AppColors.Slate600
                        )
                    }
                    Text(
                        text = formatCurrency(recebimento.locacao.valorLocacao),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Green
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Info Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoBox(
                    label = Strings.RECEBIMENTOS_PERIODO,
                    value = "${formatDate(recebimento.locacao.dataInicio)} - ${formatDate(recebimento.locacao.dataFimPrevista)}",
                    modifier = Modifier.weight(1f)
                )
                InfoBox(
                    label = Strings.RECEBIMENTOS_DATA_PAGAMENTO,
                    value = recebimento.locacao.dataPagamento?.let { formatDateFull(it) } ?: "-",
                    modifier = Modifier.weight(1f),
                    valueColor = AppColors.Green
                )
            }
        }
    }
}

@Composable
private fun EmptyRecebimentosState(isPagos: Boolean = false) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 64.dp, horizontal = 32.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(AppColors.Emerald100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPagos) Icons.Default.CheckCircle else Icons.Default.AttachMoney,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = AppColors.Green
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isPagos) Strings.RECEBIMENTOS_EMPTY_PAGOS_TITLE else Strings.RECEBIMENTOS_EMPTY_TITLE,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Slate800,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isPagos) Strings.RECEBIMENTOS_EMPTY_PAGOS_SUBTITLE else Strings.RECEBIMENTOS_EMPTY_SUBTITLE,
            fontSize = 14.sp,
            color = AppColors.Slate500,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }"
}

private fun formatDateFull(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }/${localDateTime.year}"
}

private fun formatCurrency(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "R$ $intPart,${decPart.toString().padStart(2, '0')}"
}
