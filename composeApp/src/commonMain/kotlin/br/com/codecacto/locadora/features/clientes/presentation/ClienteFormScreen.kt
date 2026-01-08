package br.com.codecacto.locadora.features.clientes.presentation

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
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
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
                    snackbarHostState.showSnackbar(effect.message)
                    onBack()
                }
                is ClientesContract.Effect.ShowError -> {
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

                // CPF/CNPJ
                OutlinedTextField(
                    value = state.cpfCnpj,
                    onValueChange = { viewModel.dispatch(ClientesContract.Action.SetCpfCnpj(it)) },
                    label = { Text(Strings.CLIENTE_FORM_CPF_CNPJ) },
                    placeholder = { Text(Strings.CLIENTE_FORM_CPF_CNPJ_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Telefone/WhatsApp
                OutlinedTextField(
                    value = state.telefoneWhatsapp,
                    onValueChange = { viewModel.dispatch(ClientesContract.Action.SetTelefoneWhatsapp(it)) },
                    label = { Text(Strings.CLIENTE_FORM_TELEFONE) },
                    placeholder = { Text(Strings.CLIENTE_FORM_TELEFONE_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
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
}
