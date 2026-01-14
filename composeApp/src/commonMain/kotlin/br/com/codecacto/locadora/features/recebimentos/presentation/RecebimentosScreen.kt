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
import androidx.compose.ui.text.style.TextOverflow
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

    var showConfirmDialog by remember { mutableStateOf(false) }
    var recebimentoParaConfirmar by remember { mutableStateOf<RecebimentoComDetalhes?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recebimentoParaExcluir by remember { mutableStateOf<RecebimentoComDetalhes?>(null) }

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

    // Modal de Confirmação
    if (showConfirmDialog && recebimentoParaConfirmar != null) {
        AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                recebimentoParaConfirmar = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = AppColors.Emerald600,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Confirmar Recebimento",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                val equipamentoNomes = recebimentoParaConfirmar?.equipamentos
                    ?.joinToString(", ") { it.nome }
                    ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO
                val equipamentoLabel = if ((recebimentoParaConfirmar?.equipamentos?.size ?: 0) > 1) {
                    "aos equipamentos \"$equipamentoNomes\""
                } else {
                    "ao equipamento \"$equipamentoNomes\""
                }
                Text(
                    text = "Deseja confirmar o recebimento de ${formatCurrency(recebimentoParaConfirmar?.recebimento?.valor ?: 0.0)} referente $equipamentoLabel do cliente \"${recebimentoParaConfirmar?.cliente?.nomeRazao}\"?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        recebimentoParaConfirmar?.let {
                            viewModel.dispatch(RecebimentosContract.Action.MarcarRecebido(it.recebimento.id))
                        }
                        showConfirmDialog = false
                        recebimentoParaConfirmar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Emerald600)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showConfirmDialog = false
                        recebimentoParaConfirmar = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de Exclusão
    if (showDeleteDialog && recebimentoParaExcluir != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                recebimentoParaExcluir = null
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AppColors.Red,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Excluir Recebimento",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Deseja excluir o recebimento de ${formatCurrency(recebimentoParaExcluir?.recebimento?.valor ?: 0.0)} do cliente \"${recebimentoParaExcluir?.cliente?.nomeRazao}\"? Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        recebimentoParaExcluir?.let {
                            viewModel.dispatch(RecebimentosContract.Action.DeleteRecebimento(it.recebimento.id))
                        }
                        showDeleteDialog = false
                        recebimentoParaExcluir = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        recebimentoParaExcluir = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
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
                            Strings.recebimentosPendentes(state.recebimentosPendentesFiltrados.size)
                        } else {
                            Strings.recebimentosPagos(state.recebimentosPagosFiltrados.size)
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    val showTotal = if (state.tabSelecionada == 0) {
                        state.recebimentosPendentesFiltrados.isNotEmpty()
                    } else {
                        state.recebimentosPagosFiltrados.isNotEmpty()
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

            // Filtro por Mês
            if (state.mesesDisponiveis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MesFilterDropdown(
                    mesesDisponiveis = state.mesesDisponiveis,
                    mesSelecionado = state.mesSelecionado,
                    onMesSelecionado = { mesAno ->
                        viewModel.dispatch(RecebimentosContract.Action.SelectMes(mesAno))
                    }
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
                    state.recebimentosPendentesFiltrados
                } else {
                    state.recebimentosPagosFiltrados
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
                                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectRecebimento(recebimento.recebimento)) },
                                    onMarcarRecebido = {
                                        recebimentoParaConfirmar = recebimento
                                        showConfirmDialog = true
                                    },
                                    onDelete = {
                                        recebimentoParaExcluir = recebimento
                                        showDeleteDialog = true
                                    }
                                )
                            } else {
                                RecebimentoPagoCard(
                                    recebimento = recebimento,
                                    onClick = { viewModel.dispatch(RecebimentosContract.Action.SelectRecebimento(recebimento.recebimento)) },
                                    onDelete = {
                                        recebimentoParaExcluir = recebimento
                                        showDeleteDialog = true
                                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MesFilterDropdown(
    mesesDisponiveis: List<MesAno>,
    mesSelecionado: MesAno?,
    onMesSelecionado: (MesAno?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
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
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = mesSelecionado?.let { Strings.formatMesAno(it.mes, it.ano) }
                            ?: Strings.RECEBIMENTOS_TODOS_MESES,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = Strings.RECEBIMENTOS_TODOS_MESES,
                        fontWeight = if (mesSelecionado == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (mesSelecionado == null) AppColors.Emerald600 else AppColors.Slate900
                    )
                },
                onClick = {
                    onMesSelecionado(null)
                    expanded = false
                },
                leadingIcon = if (mesSelecionado == null) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AppColors.Emerald600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
            HorizontalDivider(color = AppColors.Slate200)
            mesesDisponiveis.forEach { mesAno ->
                val isSelected = mesSelecionado == mesAno
                DropdownMenuItem(
                    text = {
                        Text(
                            text = Strings.formatMesAno(mesAno.mes, mesAno.ano),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AppColors.Emerald600 else AppColors.Slate900
                        )
                    },
                    onClick = {
                        onMesSelecionado(mesAno)
                        expanded = false
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AppColors.Emerald600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
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
    onMarcarRecebido: () -> Unit,
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
                    val equipamentoNomes = if (recebimento.equipamentos.isNotEmpty()) {
                        recebimento.equipamentos.joinToString(", ") { it.nome }
                    } else {
                        Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO
                    }
                    Text(
                        text = equipamentoNomes,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = AppColors.Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Excluir", color = AppColors.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = AppColors.Red
                                    )
                                }
                            )
                        }
                    }
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
                        text = formatCurrency(recebimento.recebimento.valor),
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
                    label = Strings.RECEBIMENTOS_VENCIMENTO,
                    value = formatDateFull(recebimento.recebimento.dataVencimento),
                    modifier = Modifier.weight(1f)
                )
                if (recebimento.recebimento.numeroRenovacao > 0) {
                    InfoBox(
                        label = Strings.RECEBIMENTOS_RENOVACAO,
                        value = "${recebimento.recebimento.numeroRenovacao}ª",
                        modifier = Modifier.weight(1f),
                        valueColor = AppColors.Emerald600
                    )
                }
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
                    val equipamentoNomes = if (recebimento.equipamentos.isNotEmpty()) {
                        recebimento.equipamentos.joinToString(", ") { it.nome }
                    } else {
                        Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO
                    }
                    Text(
                        text = equipamentoNomes,
                        fontSize = 14.sp,
                        color = AppColors.Slate600
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = AppColors.Slate500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Excluir", color = AppColors.Red) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = AppColors.Red
                                    )
                                }
                            )
                        }
                    }
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
                        text = formatCurrency(recebimento.recebimento.valor),
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
                    label = Strings.RECEBIMENTOS_DATA_PAGAMENTO,
                    value = recebimento.recebimento.dataPagamento?.let { formatDateFull(it) } ?: "-",
                    modifier = Modifier.weight(1f),
                    valueColor = AppColors.Green
                )
                if (recebimento.recebimento.numeroRenovacao > 0) {
                    InfoBox(
                        label = Strings.RECEBIMENTOS_RENOVACAO,
                        value = "${recebimento.recebimento.numeroRenovacao}ª",
                        modifier = Modifier.weight(1f),
                        valueColor = AppColors.Green
                    )
                }
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

    // Formatar parte inteira com separador de milhares
    val intPartFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "R$ $intPartFormatted,${decPart.toString().padStart(2, '0')}"
}
