package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
    onNavigateToForm: (String?) -> Unit,
    onNavigateToFaturamento: (String) -> Unit,
    viewModel: EquipamentosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var equipamentoToDelete by remember { mutableStateOf<Equipamento?>(null) }

    // Modal de confirmação de exclusão
    equipamentoToDelete?.let { equipamento ->
        AlertDialog(
            onDismissRequest = { equipamentoToDelete = null },
            title = {
                Text(
                    text = "Confirmar exclusão",
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text("Deseja realmente excluir o equipamento \"${equipamento.nome}\"? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatch(EquipamentosContract.Action.DeleteEquipamento(equipamento))
                        equipamentoToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { equipamentoToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                            text = Strings.equipamentosCadastrados(state.equipamentos.size),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { onNavigateToForm(null) },
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
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.dispatch(EquipamentosContract.Action.Refresh) },
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
                if (state.filteredEquipamentos.isEmpty()) {
                    EmptyEquipamentosState(
                        hasSearch = state.searchQuery.isNotBlank(),
                        onAddEquipamento = { onNavigateToForm(null) }
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
                                onEdit = { onNavigateToForm(equipamentoComStatus.equipamento.id) },
                                onDelete = { equipamentoToDelete = equipamentoComStatus.equipamento },
                                onFaturamento = { onNavigateToFaturamento(equipamentoComStatus.equipamento.id) }
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
    }
}

@Composable
private fun EquipamentoCard(
    equipamentoComStatus: EquipamentoComStatus,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onFaturamento: () -> Unit
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
                    val primeiroPreco = equipamento.getPrimeiroPrecoDisponivel()
                    Text(
                        text = primeiroPreco?.let { Strings.formatPrecoPeriodo(it.first.label) }
                            ?: Strings.EQUIPAMENTOS_PRECO_LOCACAO,
                        fontSize = 12.sp,
                        color = AppColors.Slate500
                    )
                    Text(
                        text = primeiroPreco?.let { formatCurrency(it.second) } ?: "-",
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
            HorizontalDivider(color = AppColors.Slate200)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onFaturamento,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Emerald600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
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
