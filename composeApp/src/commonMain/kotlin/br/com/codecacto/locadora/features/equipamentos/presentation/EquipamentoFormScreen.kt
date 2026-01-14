package br.com.codecacto.locadora.features.equipamentos.presentation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.components.SuccessToast
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.CurrencyVisualTransformation
import br.com.codecacto.locadora.core.ui.util.filterCurrencyInput
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipamentoFormScreen(
    equipamentoId: String? = null,
    onBack: () -> Unit,
    viewModel: EquipamentosViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = equipamentoId != null

    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(equipamentoId) {
        if (equipamentoId != null) {
            val equipamento = state.equipamentos.find { it.equipamento.id == equipamentoId }?.equipamento
            if (equipamento != null) {
                viewModel.dispatch(EquipamentosContract.Action.EditEquipamento(equipamento))
            }
        } else {
            viewModel.dispatch(EquipamentosContract.Action.ClearForm)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is EquipamentosContract.Effect.ShowSuccess -> {
                    successMessage = effect.message
                    showSuccessToast = true
                }
                is EquipamentosContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                            text = if (isEditing) Strings.EQUIPAMENTO_FORM_EDITAR else Strings.EQUIPAMENTO_FORM_NOVO,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditing) "Atualize os dados do equipamento" else "Preencha os dados do equipamento",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val equipamentoSelecionado = state.categoria.isNotBlank()

                // Equipamento (Categoria) - Dropdown fixo
                var categoriaExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = categoriaExpanded,
                    onExpandedChange = { categoriaExpanded = it }
                ) {
                    OutlinedTextField(
                        value = state.categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Equipamento *") },
                        placeholder = { Text("Selecione o tipo de equipamento") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoriaExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = categoriaExpanded,
                        onDismissRequest = { categoriaExpanded = false }
                    ) {
                        state.categorias.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.nome) },
                                onClick = {
                                    viewModel.dispatch(EquipamentosContract.Action.SetCategoria(categoria.nome))
                                    // Preenche o nome automaticamente se estiver vazio ou igual à categoria anterior
                                    if (state.nome.isBlank() || state.categorias.any { it.nome == state.nome }) {
                                        viewModel.dispatch(EquipamentosContract.Action.SetNome(categoria.nome))
                                    }
                                    categoriaExpanded = false
                                }
                            )
                        }
                    }
                }

                // Nome - Editável, preenchido automaticamente
                OutlinedTextField(
                    value = state.nome,
                    onValueChange = { viewModel.dispatch(EquipamentosContract.Action.SetNome(it)) },
                    enabled = equipamentoSelecionado,
                    label = { Text("Nome *") },
                    placeholder = { Text("Ex: Betoneira 400L") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300
                        )
                    },
                    supportingText = if (equipamentoSelecionado) {
                        { Text("Adicione detalhes como modelo ou capacidade") }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Seção de Quantidade e Patrimônio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = AppColors.Violet100.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Estoque e Identificação",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = AppColors.Violet600
                        )

                        // Switch para usar patrimônio
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Usar patrimônio/identificação",
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = AppColors.Slate800
                                )
                                Text(
                                    text = "Marque se precisa rastrear cada unidade individualmente",
                                    fontSize = 12.sp,
                                    color = AppColors.Slate500
                                )
                            }
                            Switch(
                                checked = state.usaPatrimonio,
                                onCheckedChange = {
                                    viewModel.dispatch(EquipamentosContract.Action.SetUsaPatrimonio(it))
                                },
                                enabled = equipamentoSelecionado,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AppColors.Violet600,
                                    checkedTrackColor = AppColors.Violet200
                                )
                            )
                        }

                        if (state.usaPatrimonio) {
                            // Lista de patrimônios
                            HorizontalDivider(color = AppColors.Violet200)

                            Text(
                                text = "Patrimônios cadastrados: ${state.patrimonios.size}",
                                fontSize = 12.sp,
                                color = AppColors.Slate600
                            )

                            state.patrimonios.forEachIndexed { index, patrimonio ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = patrimonio.codigo,
                                                onValueChange = {
                                                    viewModel.dispatch(EquipamentosContract.Action.UpdatePatrimonioCodigo(index, it))
                                                },
                                                label = { Text("Código *") },
                                                placeholder = { Text("Ex: PAT-001") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = patrimonio.descricao ?: "",
                                                onValueChange = {
                                                    viewModel.dispatch(EquipamentosContract.Action.UpdatePatrimonioDescricao(index, it))
                                                },
                                                label = { Text("Descrição") },
                                                placeholder = { Text("Ex: Cor amarela") },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp),
                                                singleLine = true
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                viewModel.dispatch(EquipamentosContract.Action.RemovePatrimonio(index))
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remover",
                                                tint = AppColors.Red
                                            )
                                        }
                                    }
                                }
                            }

                            // Botão para adicionar patrimônio
                            OutlinedButton(
                                onClick = {
                                    viewModel.dispatch(EquipamentosContract.Action.AddPatrimonio)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = AppColors.Violet600
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adicionar patrimônio")
                            }
                        } else {
                            // Campo de quantidade (se não usa patrimônio)
                            HorizontalDivider(color = AppColors.Violet200)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Quantidade em estoque",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = AppColors.Slate800
                                    )
                                    Text(
                                        text = "Total de unidades disponíveis",
                                        fontSize = 12.sp,
                                        color = AppColors.Slate500
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.dispatch(EquipamentosContract.Action.SetQuantidade(state.quantidade - 1))
                                        },
                                        enabled = state.quantidade > 1
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Diminuir",
                                            tint = if (state.quantidade > 1) AppColors.Violet600 else AppColors.Slate300
                                        )
                                    }
                                    Text(
                                        text = state.quantidade.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = AppColors.Violet600
                                    )
                                    IconButton(
                                        onClick = {
                                            viewModel.dispatch(EquipamentosContract.Action.SetQuantidade(state.quantidade + 1))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Aumentar",
                                            tint = AppColors.Violet600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Valor de Compra
                OutlinedTextField(
                    value = state.valorCompra,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetValorCompra(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_VALOR_COMPRA) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = Strings.CURRENCY_SYMBOL,
                            fontWeight = FontWeight.Medium,
                            color = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Observações
                OutlinedTextField(
                    value = state.observacoes,
                    onValueChange = { viewModel.dispatch(EquipamentosContract.Action.SetObservacoes(it)) },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_OBSERVACOES) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_OBSERVACOES_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 5
                )

                // Seção de Preços por Período
                Text(
                    text = Strings.EQUIPAMENTO_FORM_PRECOS_TITLE,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = AppColors.Slate700
                )
                Text(
                    text = Strings.EQUIPAMENTO_FORM_PRECOS_SUBTITLE,
                    fontSize = 12.sp,
                    color = AppColors.Slate500
                )

                // Preço Diário
                OutlinedTextField(
                    value = state.precoDiario,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetPrecoDiario(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO_DIARIO) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = Strings.CURRENCY_SYMBOL,
                            fontWeight = FontWeight.Medium,
                            color = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Preço Semanal
                OutlinedTextField(
                    value = state.precoSemanal,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetPrecoSemanal(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO_SEMANAL) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = Strings.CURRENCY_SYMBOL,
                            fontWeight = FontWeight.Medium,
                            color = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Preço Quinzenal
                OutlinedTextField(
                    value = state.precoQuinzenal,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetPrecoQuinzenal(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO_QUINZENAL) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = Strings.CURRENCY_SYMBOL,
                            fontWeight = FontWeight.Medium,
                            color = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Preço Mensal
                OutlinedTextField(
                    value = state.precoMensal,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetPrecoMensal(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO_MENSAL) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = Strings.CURRENCY_SYMBOL,
                            fontWeight = FontWeight.Medium,
                            color = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    },
                    visualTransformation = CurrencyVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                val hasAtLeastOnePrice = state.precoDiario.isNotBlank() ||
                        state.precoSemanal.isNotBlank() ||
                        state.precoQuinzenal.isNotBlank() ||
                        state.precoMensal.isNotBlank()
                Button(
                    onClick = { viewModel.dispatch(EquipamentosContract.Action.SaveEquipamento) },
                    enabled = !state.isSaving && state.categoria.isNotBlank() && state.nome.isNotBlank() && hasAtLeastOnePrice,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet600)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEditing) Strings.EQUIPAMENTO_FORM_ATUALIZAR else Strings.EQUIPAMENTO_FORM_CADASTRAR,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Cancel Button
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Strings.COMMON_CANCELAR,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Success Toast
    SuccessToast(
        message = successMessage,
        visible = showSuccessToast,
        onDismiss = {
            showSuccessToast = false
            onBack()
        },
        modifier = Modifier.align(Alignment.TopCenter)
    )
    }
}
