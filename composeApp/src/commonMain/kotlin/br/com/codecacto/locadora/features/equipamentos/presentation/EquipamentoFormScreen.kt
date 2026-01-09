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
import br.com.codecacto.locadora.core.ui.util.CategoriaEquipamento
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
                        CategoriaEquipamento.entries.forEach { categoria ->
                            DropdownMenuItem(
                                text = { Text(categoria.label) },
                                onClick = {
                                    viewModel.dispatch(EquipamentosContract.Action.SetCategoria(categoria.label))
                                    // Preenche o nome automaticamente se estiver vazio ou igual à categoria anterior
                                    if (state.nome.isBlank() || CategoriaEquipamento.entries.any { it.label == state.nome }) {
                                        viewModel.dispatch(EquipamentosContract.Action.SetNome(categoria.label))
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

                // Identificação
                OutlinedTextField(
                    value = state.identificacao,
                    onValueChange = { viewModel.dispatch(EquipamentosContract.Action.SetIdentificacao(it)) },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_IDENTIFICACAO) },
                    placeholder = { Text(Strings.EQUIPAMENTO_FORM_IDENTIFICACAO_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = null,
                            tint = if (equipamentoSelecionado) AppColors.Slate500 else AppColors.Slate300
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Preço Padrão Locação
                OutlinedTextField(
                    value = state.precoPadraoLocacao,
                    onValueChange = { newValue ->
                        val filtered = filterCurrencyInput(newValue)
                        viewModel.dispatch(EquipamentosContract.Action.SetPrecoPadraoLocacao(filtered))
                    },
                    enabled = equipamentoSelecionado,
                    label = { Text(Strings.EQUIPAMENTO_FORM_PRECO) },
                    placeholder = { Text("0,00") },
                    leadingIcon = {
                        Text(
                            text = "R$",
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
                            text = "R$",
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

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = { viewModel.dispatch(EquipamentosContract.Action.SaveEquipamento) },
                    enabled = !state.isSaving && state.categoria.isNotBlank() && state.nome.isNotBlank() && state.precoPadraoLocacao.isNotBlank(),
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
