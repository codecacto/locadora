package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.StatusEntrega
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovaLocacaoScreen(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: NovaLocacaoViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showClienteSelector by remember { mutableStateOf(false) }
    var showEquipamentoSelector by remember { mutableStateOf(false) }
    var showDataInicioPicker by remember { mutableStateOf(false) }
    var showDataFimPicker by remember { mutableStateOf(false) }
    var showDataEntregaPicker by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is NovaLocacaoContract.Effect.LocacaoCriada -> {
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
                IconButton(onClick = onDismiss) {
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
                onClick = { showClienteSelector = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Equipamento Selector
            SectionTitle(Strings.NOVA_LOCACAO_EQUIPAMENTO)
            SelectorCard(
                label = state.equipamentoSelecionado?.let { "${it.nome} - ${it.categoria}" }
                    ?: Strings.NOVA_LOCACAO_SELECIONAR_EQUIPAMENTO,
                isSelected = state.equipamentoSelecionado != null,
                onClick = { showEquipamentoSelector = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Valor da Locacao
            SectionTitle(Strings.NOVA_LOCACAO_VALOR)
            OutlinedTextField(
                value = state.valorLocacao,
                onValueChange = {
                    viewModel.dispatch(NovaLocacaoContract.Action.SetValorLocacao(it))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Strings.NOVA_LOCACAO_VALOR_PLACEHOLDER) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = AppColors.Slate500
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

            // Status de Entrega
            SectionTitle(Strings.NOVA_LOCACAO_STATUS_ENTREGA)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusEntrega.entries.forEach { status ->
                    FilterChip(
                        selected = state.statusEntrega == status,
                        onClick = {
                            viewModel.dispatch(NovaLocacaoContract.Action.SetStatusEntrega(status))
                        },
                        label = {
                            Text(
                                text = when (status) {
                                    StatusEntrega.NAO_AGENDADA -> Strings.STATUS_ENTREGA_NAO_AGENDADA
                                    StatusEntrega.AGENDADA -> Strings.STATUS_ENTREGA_AGENDADA
                                    StatusEntrega.ENTREGUE -> Strings.STATUS_ENTREGA_ENTREGUE
                                },
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AppColors.Violet100,
                            selectedLabelColor = AppColors.Violet600
                        )
                    )
                }
            }

            // Data de Entrega (se agendada)
            if (state.statusEntrega == StatusEntrega.AGENDADA) {
                Spacer(modifier = Modifier.height(16.dp))
                SectionTitle(Strings.NOVA_LOCACAO_DATA_ENTREGA)
                DateSelectorCard(
                    date = state.dataEntregaPrevista,
                    onClick = { showDataEntregaPicker = true }
                )
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

    // Cliente Selector Dialog
    if (showClienteSelector) {
        ClienteSelectorDialog(
            clientes = state.clientes,
            onSelect = { cliente ->
                viewModel.dispatch(NovaLocacaoContract.Action.SelectCliente(cliente))
                showClienteSelector = false
            },
            onDismiss = { showClienteSelector = false }
        )
    }

    // Equipamento Selector Dialog
    if (showEquipamentoSelector) {
        EquipamentoSelectorDialog(
            equipamentos = state.equipamentosDisponiveis,
            onSelect = { equipamento ->
                viewModel.dispatch(NovaLocacaoContract.Action.SelectEquipamento(equipamento))
                showEquipamentoSelector = false
            },
            onDismiss = { showEquipamentoSelector = false }
        )
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
private fun ClienteSelectorDialog(
    clientes: List<Cliente>,
    onSelect: (Cliente) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.NOVA_LOCACAO_CLIENTE) },
        text = {
            if (clientes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.NOVA_LOCACAO_NENHUM_CLIENTE,
                        color = AppColors.Slate500
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(clientes) { cliente ->
                        ListItem(
                            headlineContent = { Text(cliente.nomeRazao) },
                            supportingContent = {
                                cliente.telefoneWhatsapp.let { Text(it) }
                            },
                            modifier = Modifier.clickable { onSelect(cliente) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.COMMON_CANCELAR)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipamentoSelectorDialog(
    equipamentos: List<Equipamento>,
    onSelect: (Equipamento) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(Strings.NOVA_LOCACAO_EQUIPAMENTO) },
        text = {
            if (equipamentos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Strings.NOVA_LOCACAO_NENHUM_EQUIPAMENTO,
                            color = AppColors.Slate500
                        )
                        Text(
                            text = Strings.NOVA_LOCACAO_EQUIPAMENTOS_ALUGADOS,
                            fontSize = 12.sp,
                            color = AppColors.Slate400
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    items(equipamentos) { equipamento ->
                        ListItem(
                            headlineContent = { Text(equipamento.nome) },
                            supportingContent = {
                                Text("${equipamento.categoria} - R$ ${formatCurrency(equipamento.precoPadraoLocacao)}")
                            },
                            modifier = Modifier.clickable { onSelect(equipamento) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.COMMON_CANCELAR)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    initialSelectedDateMillis: Long?
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { onConfirm(it) }
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

private fun formatCurrency(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')}"
}
