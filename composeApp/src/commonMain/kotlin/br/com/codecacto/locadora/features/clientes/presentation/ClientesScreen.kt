package br.com.codecacto.locadora.features.clientes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientesScreen(
    onBack: () -> Unit,
    viewModel: ClientesViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ClientesContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
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
                            text = Strings.CLIENTES_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${state.clientes.size} clientes cadastrados",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
                FilledTonalButton(
                    onClick = { viewModel.dispatch(ClientesContract.Action.ShowForm) },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White,
                        contentColor = AppColors.Blue600
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.CLIENTES_NOVO)
                }
            }
        }

        // Search
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.dispatch(ClientesContract.Action.Search(it)) },
            placeholder = { Text(Strings.CLIENTES_BUSCAR) },
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
                focusedBorderColor = AppColors.Blue600
            ),
            singleLine = true
        )

        // Content
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Blue600)
            }
        } else {
            if (state.filteredClientes.isEmpty()) {
                EmptyClientesState(
                    hasSearch = state.searchQuery.isNotBlank(),
                    onAddCliente = { viewModel.dispatch(ClientesContract.Action.ShowForm) }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredClientes) { cliente ->
                        ClienteCard(
                            cliente = cliente,
                            onEdit = { viewModel.dispatch(ClientesContract.Action.EditCliente(cliente)) },
                            onDelete = { viewModel.dispatch(ClientesContract.Action.DeleteCliente(cliente)) }
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
        ClienteFormDialog(
            state = state,
            onDismiss = { viewModel.dispatch(ClientesContract.Action.HideForm) },
            onAction = { viewModel.dispatch(it) }
        )
    }
}

@Composable
private fun ClienteCard(
    cliente: Cliente,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                        text = cliente.nomeRazao,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    cliente.cpfCnpj?.let {
                        Text(
                            text = it,
                            fontSize = 14.sp,
                            color = AppColors.Slate500
                        )
                    }
                }
                if (cliente.precisaNotaFiscalPadrao) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.Blue100)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = Strings.DETALHES_NOTA_FISCAL,
                            color = AppColors.Blue600,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Contact info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = AppColors.Slate500
                )
                Text(
                    text = cliente.telefoneWhatsapp,
                    fontSize = 14.sp,
                    color = AppColors.Slate600
                )
            }

            cliente.email?.let { email ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppColors.Slate500
                    )
                    Text(
                        text = email,
                        fontSize = 14.sp,
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
                    Text(Strings.CLIENTES_EDITAR)
                }
                OutlinedButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AppColors.Red
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
private fun EmptyClientesState(
    hasSearch: Boolean,
    onAddCliente: () -> Unit
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
                    .background(AppColors.Blue100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.Blue600
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (hasSearch) Strings.CLIENTES_EMPTY_SEARCH_TITLE else Strings.CLIENTES_EMPTY_TITLE,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (hasSearch) Strings.CLIENTES_EMPTY_SEARCH_SUBTITLE else Strings.CLIENTES_EMPTY_SUBTITLE,
                fontSize = 14.sp,
                color = AppColors.Slate500
            )
            if (!hasSearch) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddCliente,
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Blue600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.CLIENTES_CADASTRAR)
                }
            }
        }
    }
}

@Composable
private fun ClienteFormDialog(
    state: ClientesContract.State,
    onDismiss: () -> Unit,
    onAction: (ClientesContract.Action) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (state.editingCliente != null) Strings.CLIENTE_FORM_EDITAR else Strings.CLIENTE_FORM_NOVO,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.nomeRazao,
                    onValueChange = { onAction(ClientesContract.Action.SetNomeRazao(it)) },
                    label = { Text(Strings.CLIENTE_FORM_NOME) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.cpfCnpj,
                    onValueChange = { onAction(ClientesContract.Action.SetCpfCnpj(it)) },
                    label = { Text(Strings.CLIENTE_FORM_CPF_CNPJ) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.telefoneWhatsapp,
                    onValueChange = { onAction(ClientesContract.Action.SetTelefoneWhatsapp(it)) },
                    label = { Text(Strings.CLIENTE_FORM_TELEFONE) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(ClientesContract.Action.SetEmail(it)) },
                    label = { Text(Strings.CLIENTE_FORM_EMAIL) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.endereco,
                    onValueChange = { onAction(ClientesContract.Action.SetEndereco(it)) },
                    label = { Text(Strings.CLIENTE_FORM_ENDERECO) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(Strings.CLIENTE_FORM_NOTA_FISCAL)
                    Switch(
                        checked = state.precisaNotaFiscalPadrao,
                        onCheckedChange = { onAction(ClientesContract.Action.SetPrecisaNotaFiscal(it)) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAction(ClientesContract.Action.SaveCliente) },
                enabled = !state.isSaving
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(if (state.editingCliente != null) Strings.CLIENTE_FORM_ATUALIZAR else Strings.CLIENTE_FORM_CADASTRAR)
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
