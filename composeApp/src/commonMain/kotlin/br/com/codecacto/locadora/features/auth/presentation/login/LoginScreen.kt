package br.com.codecacto.locadora.features.auth.presentation.login

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import locadora.composeapp.generated.resources.Res
import locadora.composeapp.generated.resources.login_background
import locadora.composeapp.generated.resources.logo
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                LoginContract.Effect.NavigateToHome -> onNavigateToHome()
                LoginContract.Effect.NavigateToRegister -> onNavigateToRegister()
                is LoginContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
        ) {
            // Background Image
            Image(
                painter = painterResource(Res.drawable.login_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark overlay for better readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.3f),
                                Color.Black.copy(alpha = 0.7f),
                                Color.Black.copy(alpha = 0.85f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(80.dp))

                // Logo
                Image(
                    painter = painterResource(Res.drawable.logo),
                    contentDescription = Strings.APP_NAME,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    contentScale = ContentScale.FillWidth
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = Strings.APP_DESCRIPTION,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Login Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = Strings.LOGIN_TITLE,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppColors.Slate900
                        )

                        // Email
                        OutlinedTextField(
                            value = state.email,
                            onValueChange = { viewModel.dispatch(LoginContract.Action.SetEmail(it)) },
                            label = { Text(Strings.LOGIN_EMAIL_LABEL) },
                            placeholder = { Text(Strings.LOGIN_EMAIL_PLACEHOLDER) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = AppColors.Orange500
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
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedPlaceholderColor = AppColors.Slate500,
                                unfocusedPlaceholderColor = AppColors.Slate500,
                                focusedBorderColor = AppColors.Orange500,
                                unfocusedBorderColor = AppColors.Slate300,
                                focusedLabelColor = AppColors.Orange500,
                                unfocusedLabelColor = AppColors.Slate600,
                                cursorColor = AppColors.Orange500
                            )
                        )

                        // Password
                        OutlinedTextField(
                            value = state.password,
                            onValueChange = { viewModel.dispatch(LoginContract.Action.SetPassword(it)) },
                            label = { Text(Strings.LOGIN_SENHA_LABEL) },
                            placeholder = { Text(Strings.LOGIN_SENHA_PLACEHOLDER) },
                            textStyle = LocalTextStyle.current.copy(color = Color.Black),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = AppColors.Orange500
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { viewModel.dispatch(LoginContract.Action.TogglePasswordVisibility) }
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
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    if (state.isLoginEnabled) {
                                        viewModel.dispatch(LoginContract.Action.Login)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedPlaceholderColor = AppColors.Slate500,
                                unfocusedPlaceholderColor = AppColors.Slate500,
                                focusedBorderColor = AppColors.Orange500,
                                unfocusedBorderColor = AppColors.Slate300,
                                focusedLabelColor = AppColors.Orange500,
                                unfocusedLabelColor = AppColors.Slate600,
                                cursorColor = AppColors.Orange500
                            )
                        )

                        // Forgot Password
                        TextButton(
                            onClick = { viewModel.dispatch(LoginContract.Action.ShowForgotPasswordDialog) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = Strings.LOGIN_ESQUECEU_SENHA,
                                color = AppColors.Orange600
                            )
                        }

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

                        // Login Button
                        Button(
                            onClick = { viewModel.dispatch(LoginContract.Action.Login) },
                            enabled = state.isLoginEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppColors.Orange500
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
                                    text = Strings.LOGIN_BOTAO,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Register Link
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Strings.LOGIN_SEM_CONTA,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    TextButton(
                        onClick = { viewModel.dispatch(LoginContract.Action.NavigateToRegister) }
                    ) {
                        Text(
                            text = Strings.LOGIN_CADASTRESE,
                            color = AppColors.Orange400,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Forgot Password Dialog
    if (state.showForgotPasswordDialog) {
        ForgotPasswordDialog(
            email = state.forgotPasswordEmail,
            isLoading = state.forgotPasswordLoading,
            error = state.forgotPasswordError,
            onEmailChange = { viewModel.dispatch(LoginContract.Action.SetForgotPasswordEmail(it)) },
            onSend = { viewModel.dispatch(LoginContract.Action.SendPasswordReset) },
            onDismiss = { viewModel.dispatch(LoginContract.Action.HideForgotPasswordDialog) }
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = Strings.LOGIN_RECUPERAR_SENHA_TITLE,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = Strings.LOGIN_RECUPERAR_SENHA_DESC,
                    color = AppColors.Slate600
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(Strings.LOGIN_EMAIL_LABEL) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedPlaceholderColor = AppColors.Slate500,
                        unfocusedPlaceholderColor = AppColors.Slate500,
                        focusedBorderColor = AppColors.Orange500,
                        unfocusedBorderColor = AppColors.Slate300,
                        focusedLabelColor = AppColors.Orange500,
                        unfocusedLabelColor = AppColors.Slate600
                    )
                )
                error?.let {
                    Text(
                        text = it,
                        color = AppColors.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSend,
                enabled = !isLoading && email.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Orange500)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(Strings.LOGIN_ENVIAR)
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
