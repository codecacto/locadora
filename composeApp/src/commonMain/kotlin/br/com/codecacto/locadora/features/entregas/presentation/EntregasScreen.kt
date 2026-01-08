package br.com.codecacto.locadora.features.entregas.presentation

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
fun EntregasScreen(
    onNavigateToDetalhes: (String) -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    unreadNotifications: Int = 0,
    viewModel: EntregasViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EntregasContract.Effect.NavigateToDetalhes -> onNavigateToDetalhes(effect.locacaoId)
                is EntregasContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is EntregasContract.Effect.ShowError -> {
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
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.Orange500, AppColors.Orange400)
                    )
                )
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
                        text = Strings.ENTREGAS_TITLE,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    val totalEntregas = state.entregasAtrasadas.size + state.entregasHoje.size + state.entregasAgendadas.size
                    Text(
                        text = Strings.entregasPendentes(totalEntregas),
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

        // Content
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.dispatch(EntregasContract.Action.Refresh) },
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Orange500)
                }
            } else {
                val isEmpty = state.entregasAtrasadas.isEmpty() &&
                        state.entregasHoje.isEmpty() &&
                        state.entregasAgendadas.isEmpty()

                if (isEmpty) {
                    EmptyEntregasState()
                } else {
                    LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Entregas Atrasadas
                    if (state.entregasAtrasadas.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Atrasadas",
                                count = state.entregasAtrasadas.size,
                                backgroundColor = AppColors.RedLight,
                                textColor = AppColors.RedDark
                            )
                        }
                        items(state.entregasAtrasadas) { entrega ->
                            EntregaCard(
                                entrega = entrega,
                                isAtrasada = true,
                                onClick = { viewModel.dispatch(EntregasContract.Action.SelectLocacao(entrega.locacao)) },
                                onMarcarEntregue = { viewModel.dispatch(EntregasContract.Action.MarcarEntregue(entrega.locacao.id)) }
                            )
                        }
                    }

                    // Entregas Hoje
                    if (state.entregasHoje.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Hoje",
                                count = state.entregasHoje.size,
                                backgroundColor = AppColors.YellowLight,
                                textColor = AppColors.Amber500
                            )
                        }
                        items(state.entregasHoje) { entrega ->
                            EntregaCard(
                                entrega = entrega,
                                isAtrasada = false,
                                onClick = { viewModel.dispatch(EntregasContract.Action.SelectLocacao(entrega.locacao)) },
                                onMarcarEntregue = { viewModel.dispatch(EntregasContract.Action.MarcarEntregue(entrega.locacao.id)) }
                            )
                        }
                    }

                    // Entregas Agendadas
                    if (state.entregasAgendadas.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Agendadas",
                                count = state.entregasAgendadas.size,
                                backgroundColor = AppColors.Slate200,
                                textColor = AppColors.Slate700
                            )
                        }
                        items(state.entregasAgendadas) { entrega ->
                            EntregaCard(
                                entrega = entrega,
                                isAtrasada = false,
                                onClick = { viewModel.dispatch(EntregasContract.Action.SelectLocacao(entrega.locacao)) },
                                onMarcarEntregue = { viewModel.dispatch(EntregasContract.Action.MarcarEntregue(entrega.locacao.id)) }
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
private fun SectionHeader(
    title: String,
    count: Int,
    backgroundColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$title ($count)",
                color = textColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun EntregaCard(
    entrega: EntregaComDetalhes,
    isAtrasada: Boolean,
    onClick: () -> Unit,
    onMarcarEntregue: () -> Unit
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
                        text = entrega.cliente?.nomeRazao ?: Strings.COMMON_CLIENTE_NAO_ENCONTRADO,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = entrega.equipamento?.nome ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                if (isAtrasada) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.RedLight)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = Strings.STATUS_PRAZO_VENCIDO,
                            color = AppColors.RedDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Data prevista
            entrega.locacao.dataEntregaPrevista?.let { data ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.Slate500
                    )
                    Text(
                        text = "Prevista: ${formatDate(data)}",
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onMarcarEntregue,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Orange500
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(Strings.DETALHES_MARCAR_ENTREGUE)
            }
        }
    }
}

@Composable
private fun EmptyEntregasState() {
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
                    .background(AppColors.Orange100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.Orange400
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = Strings.ENTREGAS_EMPTY_TITLE,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate800,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = Strings.ENTREGAS_EMPTY_SUBTITLE,
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
