package br.com.codecacto.locadora.features.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                RegisterContract.Effect.NavigateToHome -> onNavigateToHome()
                RegisterContract.Effect.NavigateToLogin -> onNavigateToLogin()
                is RegisterContract.Effect.ShowSnackbar -> {
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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Violet600, AppColors.Purple600)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.REGISTER_TITLE,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.dispatch(RegisterContract.Action.SetName(it)) },
                    label = { Text(Strings.REGISTER_NOME_LABEL) },
                    placeholder = { Text(Strings.REGISTER_NOME_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Email
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.dispatch(RegisterContract.Action.SetEmail(it)) },
                    label = { Text(Strings.LOGIN_EMAIL_LABEL) },
                    placeholder = { Text(Strings.LOGIN_EMAIL_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = state.email.isNotBlank() && !state.isEmailValid,
                    supportingText = if (state.email.isNotBlank() && !state.isEmailValid) {
                        { Text(Strings.LOGIN_EMAIL_INVALIDO) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Password
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.dispatch(RegisterContract.Action.SetPassword(it)) },
                    label = { Text(Strings.LOGIN_SENHA_LABEL) },
                    placeholder = { Text(Strings.REGISTER_SENHA_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.dispatch(RegisterContract.Action.TogglePasswordVisibility) }
                        ) {
                            Icon(
                                imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (state.isPasswordVisible) Strings.LOGIN_ESCONDER_SENHA else Strings.LOGIN_MOSTRAR_SENHA,
                                tint = AppColors.Slate500
                            )
                        }
                    },
                    visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    isError = state.password.isNotBlank() && !state.isPasswordValid,
                    supportingText = if (state.password.isNotBlank() && !state.isPasswordValid) {
                        { Text(Strings.REGISTER_SENHA_MINIMO) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Confirm Password
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.dispatch(RegisterContract.Action.SetConfirmPassword(it)) },
                    label = { Text(Strings.REGISTER_CONFIRMAR_SENHA_LABEL) },
                    placeholder = { Text(Strings.REGISTER_CONFIRMAR_SENHA_PLACEHOLDER) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = AppColors.Slate500
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { viewModel.dispatch(RegisterContract.Action.ToggleConfirmPasswordVisibility) }
                        ) {
                            Icon(
                                imageVector = if (state.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (state.isConfirmPasswordVisible) Strings.LOGIN_ESCONDER_SENHA else Strings.LOGIN_MOSTRAR_SENHA,
                                tint = AppColors.Slate500
                            )
                        }
                    },
                    visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (state.isRegisterEnabled) {
                                viewModel.dispatch(RegisterContract.Action.Register)
                            }
                        }
                    ),
                    isError = state.confirmPassword.isNotBlank() && !state.doPasswordsMatch,
                    supportingText = if (state.confirmPassword.isNotBlank() && !state.doPasswordsMatch) {
                        { Text(Strings.REGISTER_SENHAS_NAO_COINCIDEM) }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Error
                state.error?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = AppColors.RedLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = AppColors.RedDark
                            )
                            Text(
                                text = error,
                                color = AppColors.RedDark,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Register Button
                Button(
                    onClick = { viewModel.dispatch(RegisterContract.Action.Register) },
                    enabled = state.isRegisterEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Violet600
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = Strings.REGISTER_BOTAO,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.REGISTER_JA_TEM_CONTA,
                        color = AppColors.Slate600
                    )
                    TextButton(
                        onClick = { viewModel.dispatch(RegisterContract.Action.NavigateToLogin) }
                    ) {
                        Text(
                            text = Strings.LOGIN_TITLE,
                            color = AppColors.Violet600,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
