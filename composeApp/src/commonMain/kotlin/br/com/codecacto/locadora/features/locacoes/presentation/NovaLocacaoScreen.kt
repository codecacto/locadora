package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.PeriodoLocacao
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.CurrencyVisualTransformation
import br.com.codecacto.locadora.core.ui.util.filterCurrencyInput
import br.com.codecacto.locadora.core.util.adjustDatePickerTimestamp
import br.com.codecacto.locadora.core.util.toDatePickerMillis
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaLocacaoScreen(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onNavigateToSelecionarCliente: () -> Unit,
    onNavigateToSelecionarEquipamento: () -> Unit,
    viewModel: NovaLocacaoViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showDataInicioPicker by remember { mutableStateOf(false) }
    var showDataFimPicker by remember { mutableStateOf(false) }
    var showDataVencimentoPicker by remember { mutableStateOf(false) }
    var showDataEntregaPicker by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Verifica se há dados preenchidos
    val hasData = state.clienteSelecionado != null ||
                  state.equipamentoSelecionado != null ||
                  state.valorLocacao.isNotBlank()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NovaLocacaoContract.Effect.LocacaoCriada -> {
                    viewModel.dispatch(NovaLocacaoContract.Action.ClearForm)
                    onSuccess()
                }
                is NovaLocacaoContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.NOVA_LOCACAO_TITLE,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Slate900
                )
                IconButton(
                    onClick = {
                        if (hasData) {
                            showDiscardDialog = true
                        } else {
                            onDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = Strings.COMMON_FECHAR,
                        tint = AppColors.Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cliente Selector
            SectionTitle(Strings.NOVA_LOCACAO_CLIENTE)
            SelectorCard(
                label = state.clienteSelecionado?.nomeRazao ?: Strings.NOVA_LOCACAO_SELECIONAR_CLIENTE,
                isSelected = state.clienteSelecionado != null,
                onClick = onNavigateToSelecionarCliente
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Equipamento Selector
            SectionTitle(Strings.NOVA_LOCACAO_EQUIPAMENTO)
            SelectorCard(
                label = state.equipamentoSelecionado?.let { "${it.nome} - ${it.categoria}" }
                    ?: Strings.NOVA_LOCACAO_SELECIONAR_EQUIPAMENTO,
                isSelected = state.equipamentoSelecionado != null,
                onClick = onNavigateToSelecionarEquipamento
            )

            // Período Selector (só aparece após selecionar equipamento)
            if (state.periodosDisponiveis.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(Strings.NOVA_LOCACAO_PERIODO)
                PeriodoSelector(
                    periodos = state.periodosDisponiveis,
                    periodoSelecionado = state.periodoSelecionado,
                    equipamento = state.equipamentoSelecionado,
                    onPeriodoSelected = { periodo ->
                        viewModel.dispatch(NovaLocacaoContract.Action.SetPeriodo(periodo))
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Valor da Locacao
            SectionTitle(Strings.NOVA_LOCACAO_VALOR)
            OutlinedTextField(
                value = state.valorLocacao,
                onValueChange = { newValue ->
                    val filtered = filterCurrencyInput(newValue)
                    viewModel.dispatch(NovaLocacaoContract.Action.SetValorLocacao(filtered))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.NOVA_LOCACAO_VALOR_PLACEHOLDER) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = CurrencyVisualTransformation(),
                leadingIcon = {
                    Text(
                        text = Strings.CURRENCY_SYMBOL,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Slate500,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppColors.Violet600,
                    unfocusedBorderColor = AppColors.Slate300
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Datas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle(Strings.NOVA_LOCACAO_DATA_INICIO)
                    DateSelectorCard(
                        date = state.dataInicio,
                        onClick = { showDataInicioPicker = true }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle(Strings.NOVA_LOCACAO_DATA_FIM)
                    DateSelectorCard(
                        date = state.dataFimPrevista,
                        onClick = { showDataFimPicker = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data de Vencimento do Pagamento
            SectionTitle(Strings.NOVA_LOCACAO_DATA_VENCIMENTO)
            DateSelectorCard(
                date = state.dataVencimentoPagamento,
                onClick = { showDataVencimentoPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Status de Entrega
            SectionTitle(Strings.NOVA_LOCACAO_STATUS_ENTREGA)
            StatusEntregaSelector(
                statusSelecionado = state.statusEntrega,
                onStatusSelected = { status ->
                    viewModel.dispatch(NovaLocacaoContract.Action.SetStatusEntrega(status))
                }
            )

            // Data de Entrega (se agendada)
            if (state.statusEntrega == StatusEntrega.AGENDADA) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(Strings.NOVA_LOCACAO_DATA_ENTREGA)
                DateSelectorCard(
                    date = state.dataEntregaPrevista,
                    onClick = { showDataEntregaPicker = true }
                )
            }

            // Seletores de Sábado/Domingo (apenas para período Diário)
            if (state.periodoSelecionado == PeriodoLocacao.DIARIO) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Dias da semana inclusos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppColors.Slate700
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sábado
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (state.incluiSabado) AppColors.Violet100 else AppColors.Slate100)
                            .clickable {
                                viewModel.dispatch(NovaLocacaoContract.Action.SetIncluiSabado(!state.incluiSabado))
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sábado",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (state.incluiSabado) AppColors.Violet600 else AppColors.Slate700
                        )
                        Switch(
                            checked = state.incluiSabado,
                            onCheckedChange = {
                                viewModel.dispatch(NovaLocacaoContract.Action.SetIncluiSabado(it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Violet600,
                                checkedTrackColor = AppColors.Violet100
                            )
                        )
                    }

                    // Domingo
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (state.incluiDomingo) AppColors.Violet100 else AppColors.Slate100)
                            .clickable {
                                viewModel.dispatch(NovaLocacaoContract.Action.SetIncluiDomingo(!state.incluiDomingo))
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Domingo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (state.incluiDomingo) AppColors.Violet600 else AppColors.Slate700
                        )
                        Switch(
                            checked = state.incluiDomingo,
                            onCheckedChange = {
                                viewModel.dispatch(NovaLocacaoContract.Action.SetIncluiDomingo(it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = AppColors.Violet600,
                                checkedTrackColor = AppColors.Violet100
                            )
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emitir Nota Fiscal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Slate100)
                    .clickable {
                        viewModel.dispatch(NovaLocacaoContract.Action.SetEmitirNota(!state.emitirNota))
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AppColors.Slate600
                    )
                    Column {
                        Text(
                            text = Strings.NOVA_LOCACAO_EMITIR_NOTA,
                            fontWeight = FontWeight.Medium,
                            color = AppColors.Slate900
                        )
                        Text(
                            text = Strings.NOVA_LOCACAO_EMITIR_NOTA_SUBTITLE,
                            fontSize = 12.sp,
                            color = AppColors.Slate500
                        )
                    }
                }
                Switch(
                    checked = state.emitirNota,
                    onCheckedChange = {
                        viewModel.dispatch(NovaLocacaoContract.Action.SetEmitirNota(it))
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AppColors.Violet600,
                        checkedTrackColor = AppColors.Violet100
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Submit Button
            Button(
                onClick = {
                    viewModel.dispatch(NovaLocacaoContract.Action.CriarLocacao)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !state.isSaving,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Violet600,
                    disabledContainerColor = AppColors.Slate300
                )
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = Strings.NOVA_LOCACAO_CRIAR,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Date Pickers
    if (showDataInicioPicker) {
        DatePickerDialog(
            onDismiss = { showDataInicioPicker = false },
            onConfirm = { millis ->
                viewModel.dispatch(NovaLocacaoContract.Action.SetDataInicio(millis))
                showDataInicioPicker = false
            },
            initialSelectedDateMillis = state.dataInicio
        )
    }

    if (showDataFimPicker) {
        DatePickerDialog(
            onDismiss = { showDataFimPicker = false },
            onConfirm = { millis ->
                viewModel.dispatch(NovaLocacaoContract.Action.SetDataFimPrevista(millis))
                showDataFimPicker = false
            },
            initialSelectedDateMillis = state.dataFimPrevista
        )
    }

    if (showDataVencimentoPicker) {
        DatePickerDialog(
            onDismiss = { showDataVencimentoPicker = false },
            onConfirm = { millis ->
                viewModel.dispatch(NovaLocacaoContract.Action.SetDataVencimentoPagamento(millis))
                showDataVencimentoPicker = false
            },
            initialSelectedDateMillis = state.dataVencimentoPagamento
        )
    }

    if (showDataEntregaPicker) {
        DatePickerDialog(
            onDismiss = { showDataEntregaPicker = false },
            onConfirm = { millis ->
                viewModel.dispatch(NovaLocacaoContract.Action.SetDataEntregaPrevista(millis))
                showDataEntregaPicker = false
            },
            initialSelectedDateMillis = state.dataEntregaPrevista
        )
    }

    // Discard Confirmation Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = {
                Text(
                    text = Strings.NOVA_LOCACAO_DESCARTAR_TITULO,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = Strings.NOVA_LOCACAO_DESCARTAR_MENSAGEM,
                    color = AppColors.Slate600
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        viewModel.dispatch(NovaLocacaoContract.Action.ClearForm)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text(Strings.NOVA_LOCACAO_DESCARTAR)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text(Strings.COMMON_CANCELAR)
                }
            }
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = AppColors.Slate700,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun SelectorCard(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.Violet100 else AppColors.Slate100
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = if (isSelected) AppColors.Violet600 else AppColors.Slate500,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (isSelected) AppColors.Violet600 else AppColors.Slate400
            )
        }
    }
}

@Composable
private fun DateSelectorCard(
    date: Long?,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.Slate100)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.Slate500,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = date?.let { formatDate(it) } ?: Strings.NOVA_LOCACAO_SELECIONAR_DATA,
                    color = if (date != null) AppColors.Slate900 else AppColors.Slate500
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    initialSelectedDateMillis: Long?
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis?.let { toDatePickerMillis(it) }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { utcMillis ->
                        onConfirm(adjustDatePickerTimestamp(utcMillis))
                    }
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
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }/${localDateTime.year}"
}

private fun formatCurrencyValue(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')}"
}

@Composable
private fun PeriodoSelector(
    periodos: List<PeriodoLocacao>,
    periodoSelecionado: PeriodoLocacao?,
    equipamento: Equipamento?,
    onPeriodoSelected: (PeriodoLocacao) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        periodos.forEach { periodo ->
            val isSelected = periodoSelecionado == periodo
            val preco = equipamento?.getPreco(periodo)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.dp,
                            color = AppColors.Violet200,
                            shape = RoundedCornerShape(10.dp)
                        ) else Modifier
                    )
                    .clickable { onPeriodoSelected(periodo) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = periodo.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AppColors.Violet600 else AppColors.Slate600
                    )
                    preco?.let {
                        Text(
                            text = "${Strings.CURRENCY_SYMBOL} ${formatCurrencyValue(it)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) AppColors.Violet600 else AppColors.Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusEntregaSelector(
    statusSelecionado: StatusEntrega,
    onStatusSelected: (StatusEntrega) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        StatusEntrega.entries.forEach { status ->
            val isSelected = statusSelecionado == status
            val (label, icon) = when (status) {
                StatusEntrega.NAO_AGENDADA -> Strings.STATUS_ENTREGA_NAO_AGENDADA to Icons.Default.Schedule
                StatusEntrega.AGENDADA -> Strings.STATUS_ENTREGA_AGENDADA to Icons.Default.Event
                StatusEntrega.ENTREGUE -> Strings.STATUS_ENTREGA_ENTREGUE to Icons.Default.CheckCircle
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White else Color.Transparent)
                    .then(
                        if (isSelected) Modifier.border(
                            width = 1.dp,
                            color = AppColors.Violet200,
                            shape = RoundedCornerShape(10.dp)
                        ) else Modifier
                    )
                    .clickable { onStatusSelected(status) }
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) AppColors.Violet600 else AppColors.Slate500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AppColors.Violet600 else AppColors.Slate600,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}
