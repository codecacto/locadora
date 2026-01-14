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
import br.com.codecacto.locadora.core.pdf.ReceiptPdfGenerator
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.CurrencyVisualTransformation
import br.com.codecacto.locadora.core.ui.util.filterCurrencyInput
import br.com.codecacto.locadora.core.ui.util.currencyToDouble
import br.com.codecacto.locadora.core.util.adjustDatePickerTimestamp
import br.com.codecacto.locadora.core.util.toDatePickerMillis
import br.com.codecacto.locadora.currentTimeMillis
import kotlinx.datetime.Instant
import org.koin.compose.koinInject
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesLocacaoScreen(
    locacaoId: String,
    onBack: () -> Unit,
    onNavigateToRecebimentos: (String) -> Unit = {},
    viewModel: DetalhesLocacaoViewModel = koinViewModel { parametersOf(locacaoId) }
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val receiptPdfGenerator: ReceiptPdfGenerator = koinInject()
    var showConfirmarPagoDialog by remember { mutableStateOf(false) }
    var showConfirmarEntregueDialog by remember { mutableStateOf(false) }
    var showConfirmarColetadoDialog by remember { mutableStateOf(false) }

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
                is DetalhesLocacaoContract.Effect.CompartilharRecibo -> {
                    receiptPdfGenerator.shareReceipt(effect.filePath)
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

                    // Equipamentos Info
                    if (state.equipamentosComItens.isNotEmpty()) {
                        val equipamentoLabel = if (state.equipamentosComItens.size > 1) {
                            "Equipamentos (${state.equipamentosComItens.size})"
                        } else {
                            Strings.DETALHES_EQUIPAMENTO
                        }
                        InfoCard(
                            title = equipamentoLabel,
                            icon = Icons.Default.Inventory2
                        ) {
                            state.equipamentosComItens.forEachIndexed { index, equipamentoComItem ->
                                val equipamento = equipamentoComItem.equipamento
                                val item = equipamentoComItem.item

                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        color = AppColors.Slate200
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
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
                                    }
                                    // Mostra quantidade se > 1
                                    if (item.quantidade > 1) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(AppColors.Violet100)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Qtd: ${item.quantidade}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = AppColors.Violet600
                                            )
                                        }
                                    }
                                }
                                // Mostra patrimônios selecionados
                                if (item.patrimonioIds.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Label,
                                            contentDescription = null,
                                            tint = AppColors.Slate500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        // Encontra os patrimônios selecionados
                                        val patrimoniosSelecionados = equipamento.patrimonios
                                            .filter { it.id in item.patrimonioIds }
                                        Text(
                                            text = patrimoniosSelecionados.joinToString(", ") { it.codigo },
                                            color = AppColors.Slate500,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                // Campo antigo de identificação (retrocompatibilidade)
                                if (item.patrimonioIds.isEmpty()) {
                                    equipamento.identificacao?.let { id ->
                                        Text(
                                            text = "ID: $id",
                                            color = AppColors.Slate500,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
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
                        InfoRow(Strings.DETALHES_PERIODO, locacao.periodo.label)
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
                            { showConfirmarPagoDialog = true }
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
                            { showConfirmarEntregueDialog = true }
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
                            { showConfirmarColetadoDialog = true }
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

                    // Gerar Recibo Button
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.dispatch(DetalhesLocacaoContract.Action.GerarRecibo)
                        },
                        enabled = !state.isGeneratingReceipt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Emerald600
                        )
                    ) {
                        if (state.isGeneratingReceipt) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = AppColors.Emerald600,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.DETALHES_GERAR_RECIBO,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Ver Recebimentos Button
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = { onNavigateToRecebimentos(locacaoId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppColors.Violet600
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.DETALHES_VER_RECEBIMENTOS,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Renovar Dialog
    if (state.showRenovarDialog) {
        RenovarDialog(
            currentDataFim = state.locacao?.dataFimPrevista ?: currentTimeMillis(),
            currentValor = state.locacao?.valorLocacao ?: 0.0,
            onDismiss = {
                viewModel.dispatch(DetalhesLocacaoContract.Action.HideRenovarDialog)
            },
            onConfirm = { novaDataFim, novoValor ->
                viewModel.dispatch(DetalhesLocacaoContract.Action.Renovar(novaDataFim, novoValor))
            }
        )
    }

    // Modal de confirmação - Marcar como Pago
    if (showConfirmarPagoDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmarPagoDialog = false },
            title = {
                Text(
                    text = "Confirmar Pagamento",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Deseja confirmar o recebimento do pagamento desta locação?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarPago)
                        showConfirmarPagoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Green)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmarPagoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de confirmação - Marcar como Entregue
    if (showConfirmarEntregueDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmarEntregueDialog = false },
            title = {
                Text(
                    text = "Confirmar Entrega",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Deseja confirmar que o equipamento foi entregue ao cliente?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarEntregue)
                        showConfirmarEntregueDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Green)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmarEntregueDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal de confirmação - Marcar como Coletado
    if (showConfirmarColetadoDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmarColetadoDialog = false },
            title = {
                Text(
                    text = "Confirmar Coleta",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Deseja confirmar que o equipamento foi coletado do cliente?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatch(DetalhesLocacaoContract.Action.MarcarColetado)
                        showConfirmarColetadoDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Green)
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmarColetadoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun StatusHeader(
    locacao: Locacao,
    statusPrazo: StatusPrazo
) {
    // Se já foi pago, não mostrar status de vencimento
    val isPago = locacao.statusPagamento == StatusPagamento.PAGO

    val (backgroundColor, statusText, statusColor) = when {
        locacao.statusLocacao == StatusLocacao.FINALIZADA -> Triple(
            AppColors.Green,
            Strings.STATUS_LOCACAO_FINALIZADA,
            AppColors.GreenLight
        )
        !isPago && statusPrazo == StatusPrazo.VENCIDO -> Triple(
            AppColors.Red,
            Strings.STATUS_LOCACAO_VENCIDA,
            AppColors.RedLight
        )
        !isPago && statusPrazo == StatusPrazo.PROXIMO_VENCIMENTO -> Triple(
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
    // Valor em centavos para a máscara
    var novoValor by remember { mutableStateOf((currentValor * 100).toLong().toString()) }
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
                        onValueChange = { novoValor = filterCurrencyInput(it) },
                        label = { Text(Strings.RENOVAR_NOVO_VALOR) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = CurrencyVisualTransformation(),
                        leadingIcon = { Text("R$", color = AppColors.Slate500) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valor = if (manterValor) null else novoValor.currencyToDouble()
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
            initialSelectedDateMillis = toDatePickerMillis(novaDataFim)
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { utcMillis ->
                            novaDataFim = adjustDatePickerTimestamp(utcMillis)
                        }
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

    // Formatar parte inteira com separador de milhares
    val intPartFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "R$ $intPartFormatted,${decPart.toString().padStart(2, '0')}"
}
