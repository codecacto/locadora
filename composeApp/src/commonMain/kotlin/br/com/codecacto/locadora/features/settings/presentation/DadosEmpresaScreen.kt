package br.com.codecacto.locadora.features.settings.presentation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import br.com.codecacto.locadora.core.data.BrazilianStates
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import br.com.codecacto.locadora.core.ui.util.CpfVisualTransformation
import br.com.codecacto.locadora.core.ui.util.CnpjVisualTransformation
import br.com.codecacto.locadora.core.ui.util.PhoneVisualTransformation
import br.com.codecacto.locadora.core.ui.util.filterCpfInput
import br.com.codecacto.locadora.core.ui.util.filterCnpjInput
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DadosEmpresaScreen(
    onBack: () -> Unit,
    viewModel: DadosEmpresaViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DadosEmpresaContract.Effect.NavigateBack -> onBack()
                is DadosEmpresaContract.Effect.ShowSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is DadosEmpresaContract.Effect.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
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
                            text = Strings.DADOS_COMPROVANTE_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.DADOS_COMPROVANTE_SUBTITLE,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Violet600)
                }
            } else {
                // Form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nota explicativa
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.Violet100)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AppColors.Violet600,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = Strings.DADOS_COMPROVANTE_NOTA,
                                fontSize = 13.sp,
                                color = AppColors.Violet600
                            )
                        }
                    }

                    // Nome / Razão Social
                    OutlinedTextField(
                        value = state.nomeEmpresa,
                        onValueChange = { viewModel.dispatch(DadosEmpresaContract.Action.SetNomeEmpresa(it)) },
                        label = { Text(Strings.DADOS_COMPROVANTE_NOME) },
                        placeholder = { Text(Strings.DADOS_COMPROVANTE_NOME_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = if (state.tipoPessoa == TipoPessoa.FISICA) Icons.Default.Person else Icons.Default.Business,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        enabled = !state.isSaving
                    )

                    // Tipo de Pessoa Selector
                    TipoPessoaSelector(
                        tipoPessoaSelecionado = state.tipoPessoa,
                        onTipoPessoaSelected = { tipo ->
                            viewModel.dispatch(DadosEmpresaContract.Action.SetTipoPessoa(tipo))
                        },
                        enabled = !state.isSaving
                    )

                    // CPF ou CNPJ (dependendo do tipo de pessoa)
                    OutlinedTextField(
                        value = state.documento,
                        onValueChange = { newValue ->
                            val filtered = if (state.tipoPessoa == TipoPessoa.FISICA) {
                                filterCpfInput(newValue)
                            } else {
                                filterCnpjInput(newValue)
                            }
                            viewModel.dispatch(DadosEmpresaContract.Action.SetDocumento(filtered))
                        },
                        label = {
                            Text(
                                if (state.tipoPessoa == TipoPessoa.FISICA)
                                    Strings.DADOS_COMPROVANTE_CPF
                                else
                                    Strings.DADOS_COMPROVANTE_CNPJ
                            )
                        },
                        placeholder = {
                            Text(
                                if (state.tipoPessoa == TipoPessoa.FISICA)
                                    Strings.DADOS_COMPROVANTE_CPF_PLACEHOLDER
                                else
                                    Strings.DADOS_COMPROVANTE_CNPJ_PLACEHOLDER
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        visualTransformation = if (state.tipoPessoa == TipoPessoa.FISICA)
                            CpfVisualTransformation()
                        else
                            CnpjVisualTransformation(),
                        singleLine = true,
                        enabled = !state.isSaving,
                        isError = state.documentoError != null,
                        supportingText = state.documentoError?.let {
                            { Text(it, color = AppColors.Red) }
                        }
                    )

                    // Telefone
                    OutlinedTextField(
                        value = state.telefone,
                        onValueChange = {
                            val digits = it.filter { char -> char.isDigit() }
                            if (digits.length <= 11) {
                                viewModel.dispatch(DadosEmpresaContract.Action.SetTelefone(digits))
                            }
                        },
                        label = { Text(Strings.DADOS_COMPROVANTE_TELEFONE) },
                        placeholder = { Text(Strings.DADOS_COMPROVANTE_TELEFONE_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        visualTransformation = PhoneVisualTransformation(),
                        singleLine = true,
                        enabled = !state.isSaving
                    )

                    // Email
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.dispatch(DadosEmpresaContract.Action.SetEmail(it)) },
                        label = { Text(Strings.DADOS_COMPROVANTE_EMAIL) },
                        placeholder = { Text(Strings.DADOS_COMPROVANTE_EMAIL_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        enabled = !state.isSaving
                    )

                    // Endereco (Rua, número e bairro)
                    OutlinedTextField(
                        value = state.endereco,
                        onValueChange = { viewModel.dispatch(DadosEmpresaContract.Action.SetEndereco(it)) },
                        label = { Text(Strings.DADOS_COMPROVANTE_ENDERECO) },
                        placeholder = { Text(Strings.DADOS_COMPROVANTE_ENDERECO_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = AppColors.Slate500
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = false,
                        maxLines = 2,
                        enabled = !state.isSaving
                    )

                    // Estado (UF)
                    var estadoExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = estadoExpanded,
                        onExpandedChange = { if (!state.isSaving) estadoExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.estado.let { uf ->
                                if (uf.isNotBlank()) {
                                    BrazilianStates.findByAbbreviation(uf)?.let { "${it.name} ($uf)" } ?: uf
                                } else ""
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Strings.DADOS_COMPROVANTE_ESTADO) },
                            placeholder = { Text(Strings.DADOS_COMPROVANTE_ESTADO_PLACEHOLDER) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = AppColors.Slate500
                                )
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isSaving
                        )

                        ExposedDropdownMenu(
                            expanded = estadoExpanded,
                            onDismissRequest = { estadoExpanded = false }
                        ) {
                            BrazilianStates.all.forEach { estado ->
                                DropdownMenuItem(
                                    text = { Text("${estado.name} (${estado.abbreviation})") },
                                    onClick = {
                                        viewModel.dispatch(DadosEmpresaContract.Action.SetEstado(estado.abbreviation))
                                        estadoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Cidade
                    var cidadeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = cidadeExpanded,
                        onExpandedChange = { if (!state.isSaving && state.estado.isNotBlank()) cidadeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = state.cidade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(Strings.DADOS_COMPROVANTE_CIDADE) },
                            placeholder = {
                                Text(
                                    if (state.estado.isBlank()) Strings.DADOS_COMPROVANTE_CIDADE_SELECIONE_ESTADO
                                    else Strings.DADOS_COMPROVANTE_CIDADE_PLACEHOLDER
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.LocationCity,
                                    contentDescription = null,
                                    tint = AppColors.Slate500
                                )
                            },
                            trailingIcon = {
                                if (state.estado.isNotBlank()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cidadeExpanded)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !state.isSaving && state.estado.isNotBlank()
                        )

                        if (state.cidades.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = cidadeExpanded,
                                onDismissRequest = { cidadeExpanded = false }
                            ) {
                                state.cidades.forEach { cidade ->
                                    DropdownMenuItem(
                                        text = { Text(cidade) },
                                        onClick = {
                                            viewModel.dispatch(DadosEmpresaContract.Action.SetCidade(cidade))
                                            cidadeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = { viewModel.dispatch(DadosEmpresaContract.Action.Save) },
                        enabled = !state.isSaving,
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
                            Text(
                                text = Strings.DADOS_COMPROVANTE_SALVAR,
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

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun TipoPessoaSelector(
    tipoPessoaSelecionado: TipoPessoa,
    onTipoPessoaSelected: (TipoPessoa) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TipoPessoa.entries.forEach { tipo ->
            val isSelected = tipoPessoaSelecionado == tipo
            val icon = when (tipo) {
                TipoPessoa.FISICA -> Icons.Default.Person
                TipoPessoa.JURIDICA -> Icons.Default.Business
            }

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
                    .clickable(enabled = enabled) { onTipoPessoaSelected(tipo) }
                    .padding(vertical = 14.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (isSelected) AppColors.Violet600 else AppColors.Slate500
                    )
                    Text(
                        text = tipo.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) AppColors.Violet600 else AppColors.Slate600
                    )
                }
            }
        }
    }
}
