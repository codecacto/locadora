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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.core.ui.util.CnpjVisualTransformation
import br.com.codecacto.locadora.core.ui.util.PhoneVisualTransformation
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DadosEmpresaScreen(
    onBack: () -> Unit,
    viewModel: DadosEmpresaViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DadosEmpresaContract.Effect.NavigateBack -> onBack()
                is DadosEmpresaContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DadosEmpresaContract.Effect.ShowError -> {
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
                            text = Strings.DADOS_EMPRESA_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.DADOS_EMPRESA_SUBTITLE,
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
                    // Nome da Empresa
                    OutlinedTextField(
                        value = state.nomeEmpresa,
                        onValueChange = { viewModel.dispatch(DadosEmpresaContract.Action.SetNomeEmpresa(it)) },
                        label = { Text(Strings.DADOS_EMPRESA_NOME) },
                        placeholder = { Text(Strings.DADOS_EMPRESA_NOME_PLACEHOLDER) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Business,
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

                    // CNPJ
                    OutlinedTextField(
                        value = state.cnpj,
                        onValueChange = {
                            val digits = it.filter { char -> char.isDigit() }
                            if (digits.length <= 14) {
                                viewModel.dispatch(DadosEmpresaContract.Action.SetCnpj(digits))
                            }
                        },
                        label = { Text(Strings.DADOS_EMPRESA_CNPJ) },
                        placeholder = { Text(Strings.DADOS_EMPRESA_CNPJ_PLACEHOLDER) },
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
                        visualTransformation = CnpjVisualTransformation(),
                        singleLine = true,
                        enabled = !state.isSaving
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
                        label = { Text(Strings.DADOS_EMPRESA_TELEFONE) },
                        placeholder = { Text(Strings.DADOS_EMPRESA_TELEFONE_PLACEHOLDER) },
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
                        label = { Text(Strings.DADOS_EMPRESA_EMAIL) },
                        placeholder = { Text(Strings.DADOS_EMPRESA_EMAIL_PLACEHOLDER) },
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

                    // Endereco
                    OutlinedTextField(
                        value = state.endereco,
                        onValueChange = { viewModel.dispatch(DadosEmpresaContract.Action.SetEndereco(it)) },
                        label = { Text(Strings.DADOS_EMPRESA_ENDERECO) },
                        placeholder = { Text(Strings.DADOS_EMPRESA_ENDERECO_PLACEHOLDER) },
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
                            imeAction = ImeAction.Done
                        ),
                        singleLine = false,
                        maxLines = 3,
                        enabled = !state.isSaving
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                                text = Strings.DADOS_EMPRESA_SALVAR,
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
