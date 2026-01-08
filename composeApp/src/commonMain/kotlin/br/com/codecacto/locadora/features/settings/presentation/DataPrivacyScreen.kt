package br.com.codecacto.locadora.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.openUrl
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DataPrivacyScreen(
    onBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    viewModel: DataPrivacyViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is DataPrivacyContract.Effect.NavigateBack -> onBack()
                is DataPrivacyContract.Effect.AccountDeleted -> onAccountDeleted()
                is DataPrivacyContract.Effect.OpenUrl -> openUrl(effect.url)
                is DataPrivacyContract.Effect.ShowError -> { /* Handled by dialog */ }
            }
        }
    }

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
                        text = Strings.DATA_PRIVACY_TITLE,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Strings.DATA_PRIVACY_SUBTITLE,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Legal Section
            Text(
                text = Strings.DATA_PRIVACY_SECTION_LEGAL,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate500,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    DataPrivacyItem(
                        icon = Icons.Default.Description,
                        title = Strings.DATA_PRIVACY_TERMOS_USO,
                        subtitle = Strings.DATA_PRIVACY_TERMOS_USO_SUBTITLE,
                        iconBackgroundColor = AppColors.Blue100,
                        iconColor = AppColors.Blue600,
                        onClick = { viewModel.dispatch(DataPrivacyContract.Action.OpenTermsOfUse) }
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = AppColors.Slate100
                    )

                    DataPrivacyItem(
                        icon = Icons.Default.Security,
                        title = Strings.DATA_PRIVACY_POLITICA,
                        subtitle = Strings.DATA_PRIVACY_POLITICA_SUBTITLE,
                        iconBackgroundColor = AppColors.Violet100,
                        iconColor = AppColors.Violet600,
                        onClick = { viewModel.dispatch(DataPrivacyContract.Action.OpenPrivacyPolicy) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Danger Zone
            Text(
                text = Strings.DATA_PRIVACY_SECTION_DANGER,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Red,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                DataPrivacyItem(
                    icon = Icons.Default.Delete,
                    title = Strings.DATA_PRIVACY_EXCLUIR_CONTA,
                    subtitle = Strings.DATA_PRIVACY_EXCLUIR_CONTA_SUBTITLE,
                    iconBackgroundColor = AppColors.RedLight,
                    iconColor = AppColors.Red,
                    onClick = { viewModel.dispatch(DataPrivacyContract.Action.ShowDeleteDialog) }
                )
            }
        }
    }

    // Delete Account Dialog
    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isDeleting) {
                    viewModel.dispatch(DataPrivacyContract.Action.HideDeleteDialog)
                }
            },
            title = {
                Text(
                    text = Strings.DATA_PRIVACY_EXCLUIR_TITULO,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Red
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = Strings.DATA_PRIVACY_EXCLUIR_MENSAGEM,
                        color = AppColors.Slate700
                    )

                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.dispatch(DataPrivacyContract.Action.SetPassword(it)) },
                        label = { Text(Strings.LOGIN_SENHA_LABEL) },
                        placeholder = { Text(Strings.DATA_PRIVACY_DIGITE_SENHA) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !state.isDeleting,
                        isError = state.errorMessage != null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (state.errorMessage != null) {
                        Text(
                            text = state.errorMessage!!,
                            color = AppColors.Red,
                            fontSize = 12.sp
                        )
                    }

                    if (state.isDeleting) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = AppColors.Red
                            )
                            Text(
                                text = Strings.DATA_PRIVACY_EXCLUINDO,
                                fontSize = 14.sp,
                                color = AppColors.Slate500
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dispatch(DataPrivacyContract.Action.ConfirmDeleteAccount) },
                    enabled = !state.isDeleting && state.password.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Red)
                ) {
                    Text(Strings.DATA_PRIVACY_EXCLUIR_CONTA)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.dispatch(DataPrivacyContract.Action.HideDeleteDialog) },
                    enabled = !state.isDeleting
                ) {
                    Text(Strings.COMMON_CANCELAR)
                }
            }
        )
    }
}

@Composable
private fun DataPrivacyItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconBackgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate900
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = AppColors.Slate500
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = AppColors.Slate400
        )
    }
}
