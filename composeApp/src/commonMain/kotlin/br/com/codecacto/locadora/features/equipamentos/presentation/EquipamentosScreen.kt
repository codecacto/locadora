package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipamentosScreen(
    onBack: () -> Unit,
    viewModel: EquipamentosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EquipamentosContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is EquipamentosContract.Effect.ShowError -> {
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
                .background(AppColors.Violet600)
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = Strings.COMMON_VOLTAR,
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = Strings.EQUIPAMENTOS_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.equipamentos.size} equipamentos cadastrados",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { viewModel.dispatch(EquipamentosContract.Action.ShowForm) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White,
                        contentColor = AppColors.Violet600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.EQUIPAMENTOS_NOVO)
                }
            }
        }

        // Search
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.dispatch(EquipamentosContract.Action.Search(it)) },
            placeholder = { Text(Strings.EQUIPAMENTOS_BUSCAR) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.Slate400
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = AppColors.Slate200,
                focusedBorderColor = AppColors.Violet600
            ),
            singleLine = true
        )

        // Content
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Violet600)
            }
        } else {
            if (state.filteredEquipamentos.isEmpty()) {
                EmptyEquipamentosState(
                    hasSearch = state.searchQuery.isNotBlank(),
                    onAddEquipamento = { viewModel.dispatch(EquipamentosContract.Action.ShowForm) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredEquipamentos) { equipamentoComStatus ->
                        EquipamentoCard(
                            equipamentoComStatus = equipamentoComStatus,
                            onEdit = { viewModel.dispatch(EquipamentosContract.Action.EditEquipamento(equipamentoComStatus.equipamento)) },
                            onDelete = { viewModel.dispatch(EquipamentosContract.Action.DeleteEquipamento(equipamentoComStatus.equipamento)) }
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

    // Form Dialog
    if (state.showForm) {
        EquipamentoFormDialog(
            state = state,
            onDismiss = { viewModel.dispatch(EquipamentosContract.Action.HideForm) },
            onAction = { viewModel.dispatch(it) }
        )
    }
}

@Composable
private fun EquipamentoCard(
    equipamentoComStatus: EquipamentoComStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val equipamento = equipamentoComStatus.equipamento
    val isAlugado = equipamentoComStatus.isAlugado

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                        text = equipamento.nome,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = equipamento.categoria,
                        fontSize = 14.sp,
                        color = AppColors.Slate500
                    )
                    equipamento.identificacao?.let {
                        Text(
                            text = "ID: $it",
                            fontSize = 12.sp,
                            color = AppColors.Slate400
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isAlugado) AppColors.Orange100 else AppColors.GreenLight
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAlugado) Strings.EQUIPAMENTOS_ALUGADO else Strings.EQUIPAMENTOS_DISPONIVEL,
                        color = if (isAlugado) AppColors.Orange500 else AppColors.GreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prices
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = Strings.EQUIPAMENTOS_PRECO_LOCACAO,
                        fontSize = 12.sp,
                        color = AppColors.Slate500
                    )
                    Text(
                        text = formatCurrency(equipamento.precoPadraoLocacao),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Slate900
                    )
                }
                equipamento.valorCompra?.let { valor ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Strings.EQUIPAMENTOS_VALOR_COMPRA,
                            fontSize = 12.sp,
                            color = AppColors.Slate500
                        )
                        Text(
                            text = formatCurrency(valor),
                            fontSize = 14.sp,
                            color = AppColors.Slate700
                        )
                    }
                }
            }

            equipamento.observacoes?.let { obs ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Slate100)
                        .padding(8.dp)
                ) {
                    Text(
                        text = obs,
                        fontSize = 12.sp,
                        color = AppColors.Slate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AppColors.Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.EQUIPAMENTOS_EDITAR)
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isAlugado,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isAlugado) AppColors.Slate400 else AppColors.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyEquipamentosState(
    hasSearch: Boolean,
    onAddEquipamento: () -> Unit
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
                    .background(AppColors.Violet100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.Violet600
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearch) Strings.EQUIPAMENTOS_EMPTY_SEARCH_TITLE else Strings.EQUIPAMENTOS_EMPTY_TITLE,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearch) Strings.EQUIPAMENTOS_EMPTY_SEARCH_SUBTITLE else Strings.EQUIPAMENTOS_EMPTY_SUBTITLE,
                fontSize = 14.sp,
                color = AppColors.Slate500
            )
            if (!hasSearch) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddEquipamento,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.EQUIPAMENTOS_CADASTRAR)
                }
            }
        }
    }
}

@Composable
private fun EquipamentoFormDialog(
    state: EquipamentosContract.State,
    onDismiss: () -> Unit,
    onAction: (EquipamentosContract.Action) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (state.editingEquipamento != null) Strings.EQUIPAMENTO_FORM_EDITAR else Strings.EQUIPAMENTO_FORM_NOVO,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.nome,
                    onValueChange = { onAction(EquipamentosContract.Action.SetNome(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_NOME) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_NOME_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.categoria,
                    onValueChange = { onAction(EquipamentosContract.Action.SetCategoria(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_CATEGORIA) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_CATEGORIA_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.identificacao,
                    onValueChange = { onAction(EquipamentosContract.Action.SetIdentificacao(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_IDENTIFICACAO) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_IDENTIFICACAO_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.precoPadraoLocacao,
                    onValueChange = { onAction(EquipamentosContract.Action.SetPrecoPadraoLocacao(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_PRECO_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.valorCompra,
                    onValueChange = { onAction(EquipamentosContract.Action.SetValorCompra(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_VALOR_COMPRA) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_VALOR_COMPRA_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.observacoes,
                    onValueChange = { onAction(EquipamentosContract.Action.SetObservacoes(it)) },
                    label = { Text(Strings.EQUIPAMENTO_FORM_OBSERVACOES) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_OBSERVACOES_PLACEHOLDER) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAction(EquipamentosContract.Action.SaveEquipamento) },
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(if (state.editingEquipamento != null) Strings.EQUIPAMENTO_FORM_ATUALIZAR else Strings.EQUIPAMENTO_FORM_CADASTRAR)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Strings.COMMON_CANCELAR)
            }
        }
    )
}

private fun formatCurrency(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "R$ $intPart,${decPart.toString().padStart(2, '0')}"
}
