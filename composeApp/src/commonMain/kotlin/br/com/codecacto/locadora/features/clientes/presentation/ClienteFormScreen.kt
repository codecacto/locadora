package br.com.codecacto.locadora.features.clientes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.ui.components.SuccessToast
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.TipoPessoa
import br.com.codecacto.locadora.core.ui.util.PhoneVisualTransformation
import br.com.codecacto.locadora.core.ui.util.CpfVisualTransformation
import br.com.codecacto.locadora.core.ui.util.CnpjVisualTransformation
import br.com.codecacto.locadora.core.ui.util.filterPhoneInput
import br.com.codecacto.locadora.core.ui.util.filterCpfInput
import br.com.codecacto.locadora.core.ui.util.filterCnpjInput
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClienteFormScreen(
    clienteId: String? = null,
    onBack: () -> Unit,
    viewModel: ClientesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = clienteId != null

    var showSuccessToast by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }

    LaunchedEffect(clienteId) {
        if (clienteId != null) {
            val cliente = state.clientes.find { it.id == clienteId }
            if (cliente != null) {
                viewModel.dispatch(ClientesContract.Action.EditCliente(cliente))
            }
        } else {
            viewModel.dispatch(ClientesContract.Action.ClearForm)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ClientesContract.Effect.ShowSuccess -> {
                    successMessage = effect.message
                    showSuccessToast = true
                }
                is ClientesContract.Effect.ShowError -> {
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
                    .background(AppColors.Blue600)
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
                            text = if (isEditing) Strings.CLIENTE_FORM_EDITAR else Strings.CLIENTE_FORM_NOVO,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isEditing) "Atualize os dados do cliente" else "Preencha os dados do cliente",
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
                // Nome/Razão Social
                OutlinedTextField(
                    value = state.nomeRazao,
                    onValueChange = { viewModel.dispatch(ClientesContract.Action.SetNomeRazao(it)) },
                    label = { Text(Strings.CLIENTE_FORM_NOME) },
                    placeholder = { Text(Strings.CLIENTE_FORM_NOME_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Tipo de Pessoa Selector
                TipoPessoaSelector(
                    tipoPessoaSelecionado = state.tipoPessoa,
                    onTipoPessoaSelected = { tipo ->
                        viewModel.dispatch(ClientesContract.Action.SetTipoPessoa(tipo))
                    },
                    accentColor = AppColors.Blue600
                )

                // CPF/CNPJ
                OutlinedTextField(
                    value = state.cpfCnpj,
                    onValueChange = { newValue ->
                        val filtered = if (state.tipoPessoa == TipoPessoa.FISICA) {
                            filterCpfInput(newValue)
                        } else {
                            filterCnpjInput(newValue)
                        }
                        viewModel.dispatch(ClientesContract.Action.SetCpfCnpj(filtered))
                    },
                    label = {
                        Text(if (state.tipoPessoa == TipoPessoa.FISICA) "CPF" else "CNPJ")
                    },
                    placeholder = {
                        Text(
                            if (state.tipoPessoa == TipoPessoa.FISICA)
                                "000.000.000-00"
                            else
                                "00.000.000/0000-00"
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    visualTransformation = if (state.tipoPessoa == TipoPessoa.FISICA) {
                        CpfVisualTransformation()
                    } else {
                        CnpjVisualTransformation()
                    },
                    isError = state.cpfCnpjError != null,
                    supportingText = state.cpfCnpjError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (state.tipoPessoa == TipoPessoa.FISICA)
                            KeyboardType.Number
                        else
                            KeyboardType.Text
                    ),
                    singleLine = true
                )

                // Telefone/WhatsApp
                OutlinedTextField(
                    value = state.telefoneWhatsapp,
                    onValueChange = { newValue ->
                        val filtered = filterPhoneInput(newValue)
                        viewModel.dispatch(ClientesContract.Action.SetTelefoneWhatsapp(filtered))
                    },
                    label = { Text(Strings.CLIENTE_FORM_TELEFONE) },
                    placeholder = { Text(Strings.CLIENTE_FORM_TELEFONE_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    visualTransformation = PhoneVisualTransformation(),
                    isError = state.telefoneError != null,
                    supportingText = state.telefoneError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )

                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.dispatch(ClientesContract.Action.SetEmail(it)) },
                    label = { Text(Strings.CLIENTE_FORM_EMAIL) },
                    placeholder = { Text(Strings.CLIENTE_FORM_EMAIL_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                // Endereço
                OutlinedTextField(
                    value = state.endereco,
                    onValueChange = { viewModel.dispatch(ClientesContract.Action.SetEndereco(it)) },
                    label = { Text(Strings.CLIENTE_FORM_ENDERECO) },
                    placeholder = { Text(Strings.CLIENTE_FORM_ENDERECO_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Nota Fiscal Switch
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                tint = AppColors.Blue600
                            )
                            Text(
                                text = Strings.CLIENTE_FORM_NOTA_FISCAL,
                                fontWeight = FontWeight.Medium,
                                color = AppColors.Slate800
                            )
                        }
                        Switch(
                            checked = state.precisaNotaFiscalPadrao,
                            onCheckedChange = { viewModel.dispatch(ClientesContract.Action.SetPrecisaNotaFiscal(it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppColors.Blue600
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                Button(
                    onClick = { viewModel.dispatch(ClientesContract.Action.SaveCliente) },
                    enabled = !state.isSaving && state.nomeRazao.isNotBlank() && state.telefoneWhatsapp.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue600)
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
                            text = if (isEditing) Strings.CLIENTE_FORM_ATUALIZAR else Strings.CLIENTE_FORM_CADASTRAR,
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

@Composable
private fun TipoPessoaSelector(
    tipoPessoaSelecionado: TipoPessoa,
    onTipoPessoaSelected: (TipoPessoa) -> Unit,
    accentColor: Color = AppColors.Blue600,
    enabled: Boolean = true
) {
    val accentLight = accentColor.copy(alpha = 0.1f)

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
                            color = accentLight,
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
                        tint = if (isSelected) accentColor else AppColors.Slate500
                    )
                    Text(
                        text = tipo.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) accentColor else AppColors.Slate600
                    )
                }
            }
        }
    }
}
