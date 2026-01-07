package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.*
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesLocacaoScreen(
    locacaoId: String,
    onBack: () -> Unit,
    viewModel: DetalhesLocacaoViewModel = koinViewModel { parametersOf(locacaoId) }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DetalhesLocacaoContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DetalhesLocacaoContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DetalhesLocacaoContract.Effect.NavigateBack -> {
                    onBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(Strings.DETALHES_TITLE) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Strings.DETALHES_VOLTAR
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Violet600,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Violet600)
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = AppColors.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: Strings.COMMON_ERRO_DESCONHECIDO,
                        color = AppColors.Slate600
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text(Strings.DETALHES_VOLTAR)
                    }
                }
            }
        } else {
            state.locacao?.let { locacao ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .background(AppColors.Slate50)
                ) {
                    // Status Header
                    StatusHeader(
                        locacao = locacao,
                        statusPrazo = state.statusPrazo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cliente Info
                    state.cliente?.let { cliente ->
                        InfoCard(
                            title = Strings.DETALHES_CLIENTE,
                            icon = Icons.Default.Person
                        ) {
                            Text(
                                text = cliente.nomeRazao,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Slate900
                            )
                            if (!cliente.telefoneWhatsapp.isNullOrBlank()) {
                                Text(
                                    text = cliente.telefoneWhatsapp,
                                    color = AppColors.Slate600,
                                    fontSize = 14.sp
                                )
                            }
                            cliente.email?.let { email ->
                                Text(
                                    text = email,
                                    color = AppColors.Slate600,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Equipamento Info
                    state.equipamento?.let { equipamento ->
                        InfoCard(
                            title = Strings.DETALHES_EQUIPAMENTO,
                            icon = Icons.Default.Inventory2
                        ) {
                            Text(
                                text = equipamento.nome,
                                fontWeight = FontWeight.SemiBold,
                                color = AppColors.Slate900
                            )
                            Text(
                                text = equipamento.categoria,
                                color = AppColors.Slate600,
                                fontSize = 14.sp
                            )
                            equipamento.identificacao?.let { id ->
                                Text(
                                    text = "ID: $id",
                                    color = AppColors.Slate500,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Locacao Info
                    InfoCard(
                        title = Strings.DETALHES_INFO_LOCACAO,
                        icon = Icons.Default.Info
                    ) {
                        InfoRow(Strings.DETALHES_VALOR, formatCurrency(locacao.valorLocacao))
                        InfoRow(Strings.DETALHES_DATA_INICIO, formatDate(locacao.dataInicio))
                        InfoRow(Strings.DETALHES_DATA_FIM, formatDate(locacao.dataFimPrevista))
                        if (locacao.qtdRenovacoes > 0) {
                            InfoRow(Strings.DETALHES_RENOVACOES, locacao.qtdRenovacoes.toString())
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Status Cards
                    StatusCard(
                        title = Strings.DETALHES_PAGAMENTO,
                        icon = Icons.Default.AttachMoney,
                        status = if (locacao.statusPagamento == StatusPagamento.PAGO) Strings.STATUS_PAGAMENTO_PAGO else Strings.STATUS_PAGAMENTO_PENDENTE,
                        isCompleted = locacao.statusPagamento == StatusPagamento.PAGO,
                        date = locacao.dataPagamento?.let { formatDate(it) },
                        actionLabel = if (locacao.statusPagamento == StatusPagamento.PENDENTE) Strings.DETALHES_MARCAR_PAGO else null,
                        onAction = if (locacao.statusPagamento == StatusPagamento.PENDENTE) {
                            { viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarPago) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusCard(
                        title = Strings.DETALHES_ENTREGA,
                        icon = Icons.Default.LocalShipping,
                        status = when (locacao.statusEntrega) {
                            StatusEntrega.ENTREGUE -> Strings.STATUS_ENTREGA_ENTREGUE
                            StatusEntrega.AGENDADA -> Strings.STATUS_ENTREGA_AGENDADA
                            StatusEntrega.NAO_AGENDADA -> Strings.STATUS_ENTREGA_NAO_AGENDADA
                        },
                        isCompleted = locacao.statusEntrega == StatusEntrega.ENTREGUE,
                        date = locacao.dataEntregaReal?.let { formatDate(it) }
                            ?: locacao.dataEntregaPrevista?.let { "Prevista: ${formatDate(it)}" },
                        actionLabel = if (locacao.statusEntrega != StatusEntrega.ENTREGUE) Strings.DETALHES_MARCAR_ENTREGUE else null,
                        onAction = if (locacao.statusEntrega != StatusEntrega.ENTREGUE) {
                            { viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarEntregue) }
                        } else null
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StatusCard(
                        title = Strings.DETALHES_COLETA,
                        icon = Icons.Default.MoveUp,
                        status = if (locacao.statusColeta == StatusColeta.COLETADO) Strings.STATUS_COLETA_COLETADO else Strings.STATUS_PAGAMENTO_PENDENTE,
                        isCompleted = locacao.statusColeta == StatusColeta.COLETADO,
                        date = locacao.dataColeta?.let { formatDate(it) },
                        actionLabel = if (locacao.statusColeta == StatusColeta.NAO_COLETADO && locacao.statusEntrega == StatusEntrega.ENTREGUE) Strings.DETALHES_MARCAR_COLETADO else null,
                        onAction = if (locacao.statusColeta == StatusColeta.NAO_COLETADO && locacao.statusEntrega == StatusEntrega.ENTREGUE) {
                            { viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarColetado) }
                        } else null
                    )

                    if (locacao.emitirNota) {
                        Spacer(modifier = Modifier.height(12.dp))

                        StatusCard(
                            title = Strings.DETALHES_NOTA_FISCAL,
                            icon = Icons.Default.Description,
                            status = if (locacao.notaEmitida) Strings.DETALHES_NOTA_EMITIDA else Strings.STATUS_PAGAMENTO_PENDENTE,
                            isCompleted = locacao.notaEmitida,
                            date = null,
                            actionLabel = if (!locacao.notaEmitida) Strings.DETALHES_MARCAR_NOTA_EMITIDA else null,
                            onAction = if (!locacao.notaEmitida) {
                                { viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarNotaEmitida) }
                            } else null
                        )
                    }

                    // Renovar Button (only for active rentals)
                    if (locacao.statusLocacao == StatusLocacao.ATIVA) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.dispatch(DetalhesLocacaoContract.Action.ShowRenovarDialog)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Blue600
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Strings.DETALHES_RENOVAR,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Renovar Dialog
    if (state.showRenovarDialog) {
        RenovarDialog(
            currentDataFim = state.locacao?.dataFimPrevista ?: System.currentTimeMillis(),
            currentValor = state.locacao?.valorLocacao ?: 0.0,
            onDismiss = {
                viewModel.dispatch(DetalhesLocacaoContract.Action.HideRenovarDialog)
            },
            onConfirm = { novaDataFim, novoValor ->
                viewModel.dispatch(DetalhesLocacaoContract.Action.Renovar(novaDataFim, novoValor))
            }
        )
    }
}

@Composable
private fun StatusHeader(
    locacao: Locacao,
    statusPrazo: StatusPrazo
) {
    val (backgroundColor, statusText, statusColor) = when {
        locacao.statusLocacao == StatusLocacao.FINALIZADA -> Triple(
            AppColors.Green,
            Strings.STATUS_LOCACAO_FINALIZADA,
            AppColors.GreenLight
        )
        statusPrazo == StatusPrazo.VENCIDO -> Triple(
            AppColors.Red,
            Strings.STATUS_LOCACAO_VENCIDA,
            AppColors.RedLight
        )
        statusPrazo == StatusPrazo.PROXIMO_VENCIMENTO -> Triple(
            AppColors.Yellow,
            Strings.STATUS_LOCACAO_PROXIMO_VENCIMENTO,
            AppColors.YellowLight
        )
        else -> Triple(
            AppColors.Violet600,
            Strings.STATUS_LOCACAO_ATIVA,
            AppColors.Violet100
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = Strings.DETALHES_STATUS,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = statusText,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formatCurrency(locacao.valorLocacao),
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppColors.Violet600,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Slate900
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = AppColors.Slate500,
            fontSize = 14.sp
        )
        Text(
            text = value,
            color = AppColors.Slate900,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    icon: ImageVector,
    status: String,
    isCompleted: Boolean,
    date: String?,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isCompleted) AppColors.GreenLight else AppColors.Slate100
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isCompleted) AppColors.Green else AppColors.Slate500
                        )
                    }
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Slate900
                        )
                        Text(
                            text = status,
                            fontSize = 14.sp,
                            color = if (isCompleted) AppColors.Green else AppColors.Slate500
                        )
                    }
                }
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AppColors.Green,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            date?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = AppColors.Slate500
                )
            }

            onAction?.let { action ->
                actionLabel?.let { label ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = action,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Violet600
                        )
                    ) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenovarDialog(
    currentDataFim: Long,
    currentValor: Double,
    onDismiss: () -> Unit,
    onConfirm: (Long, Double?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var novaDataFim by remember { mutableStateOf(currentDataFim + (30L * 24 * 60 * 60 * 1000)) }
    var novoValor by remember { mutableStateOf(currentValor.toString()) }
    var manterValor by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.RENOVAR_TITLE) },
        text = {
            Column {
                Text(
                    text = Strings.RENOVAR_NOVA_DATA,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Card(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(formatDate(novaDataFim))
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = manterValor,
                        onCheckedChange = { manterValor = it }
                    )
                    Text(Strings.RENOVAR_MANTER_VALOR)
                }

                if (!manterValor) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = novoValor,
                        onValueChange = { novoValor = it },
                        label = { Text(Strings.RENOVAR_NOVO_VALOR) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valor = if (manterValor) null else novoValor.toDoubleOrNull()
                    onConfirm(novaDataFim, valor)
                }
            ) {
                Text(Strings.COMMON_CONFIRMAR)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.COMMON_CANCELAR)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = novaDataFim
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { novaDataFim = it }
                        showDatePicker = false
                    }
                ) {
                    Text(Strings.COMMON_CONFIRMAR)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(Strings.COMMON_CANCELAR)
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
    return "R$ $intPart,${decPart.toString().padStart(2, '0')}"
}
