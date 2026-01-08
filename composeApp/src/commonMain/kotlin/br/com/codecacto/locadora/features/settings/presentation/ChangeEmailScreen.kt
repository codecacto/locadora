package br.com.codecacto.locadora.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChangeEmailScreen(
    onBack: () -> Unit,
    viewModel: ChangeEmailViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is ChangeEmailContract.Effect.NavigateBack -> onBack()
                is ChangeEmailContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ChangeEmailContract.Effect.ShowError -> {
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
                            text = Strings.CHANGE_EMAIL_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.CHANGE_EMAIL_SUBTITLE,
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
                // Current Email (read-only)
                OutlinedTextField(
                    value = state.currentEmail,
                    onValueChange = { },
                    label = { Text(Strings.CHANGE_EMAIL_ATUAL) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = false,
                    readOnly = true,
                    singleLine = true
                )

                // New Email
                OutlinedTextField(
                    value = state.newEmail,
                    onValueChange = { viewModel.dispatch(ChangeEmailContract.Action.SetNewEmail(it)) },
                    label = { Text(Strings.CHANGE_EMAIL_NOVO) },
                    placeholder = { Text(Strings.CHANGE_EMAIL_NOVO_PLACEHOLDER) },
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
                    isError = state.newEmailError != null,
                    supportingText = state.newEmailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    enabled = !state.isLoading
                )

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.dispatch(ChangeEmailContract.Action.SetPassword(it)) },
                    label = { Text(Strings.CHANGE_EMAIL_SENHA_CONFIRMAR) },
                    placeholder = { Text(Strings.CHANGE_EMAIL_SENHA_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.dispatch(ChangeEmailContract.Action.TogglePasswordVisibility) }
                        ) {
                            Icon(
                                imageVector = if (state.showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (state.showPassword) Strings.LOGIN_ESCONDER_SENHA else Strings.LOGIN_MOSTRAR_SENHA,
                                tint = AppColors.Slate500
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                Button(
                    onClick = { viewModel.dispatch(ChangeEmailContract.Action.Submit) },
                    enabled = !state.isLoading &&
                            state.newEmail.isNotEmpty() &&
                            state.password.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Violet600)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = Strings.CHANGE_EMAIL_BOTAO,
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
            }
        }
    }
}
