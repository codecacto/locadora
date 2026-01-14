package br.com.codecacto.locadora.features.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.theme.AppColors
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var deleteConfirmationText by remember { mutableStateOf("") }
    val confirmationWord = "APAGAR"

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SettingsContract.Effect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SettingsContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
            }
        }
    }

    // Modal de Confirmação para Apagar Todos os Dados
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteAllDialog = false
                deleteConfirmationText = ""
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = AppColors.Red,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Apagar Todos os Dados",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Red
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "ATENÇÃO: Esta ação irá excluir permanentemente TODOS os seus dados, incluindo clientes, equipamentos, locações e recebimentos. Esta ação NÃO pode ser desfeita!",
                        color = AppColors.Slate700
                    )
                    Text(
                        text = "Para confirmar, digite \"$confirmationWord\" abaixo:",
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Slate900
                    )
                    OutlinedTextField(
                        value = deleteConfirmationText,
                        onValueChange = { deleteConfirmationText = it.uppercase() },
                        placeholder = { Text("Digite $confirmationWord") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppColors.Red,
                            cursorColor = AppColors.Red
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dispatch(SettingsContract.Action.DeleteAllData)
                        showDeleteAllDialog = false
                        deleteConfirmationText = ""
                    },
                    enabled = deleteConfirmationText == confirmationWord,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Red,
                        disabledContainerColor = AppColors.Slate300
                    )
                ) {
                    Text("Apagar Tudo")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteAllDialog = false
                        deleteConfirmationText = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
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
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
                Column {
                    Text(
                        text = "Configuracoes",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gerencie sua conta",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
            }
        }

        // Settings Items
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Conta Section
            Text(
                text = "Conta",
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
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Alterar Senha",
                        subtitle = "Atualize sua senha de acesso",
                        iconBackgroundColor = AppColors.Blue100,
                        iconColor = AppColors.Blue600,
                        onClick = onNavigateToChangePassword
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = AppColors.Slate100
                    )

                    SettingsItem(
                        icon = Icons.Default.Email,
                        title = "Alterar Email",
                        subtitle = state.currentEmail.ifEmpty { "Atualize seu email" },
                        iconBackgroundColor = AppColors.Violet100,
                        iconColor = AppColors.Violet600,
                        onClick = onNavigateToChangeEmail
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Zona de Perigo Section
            Text(
                text = "Zona de Perigo",
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
                SettingsItem(
                    icon = Icons.Default.DeleteForever,
                    title = "Apagar Todos os Dados",
                    subtitle = "Excluir permanentemente todos os dados",
                    iconBackgroundColor = AppColors.Red.copy(alpha = 0.1f),
                    iconColor = AppColors.Red,
                    onClick = { showDeleteAllDialog = true }
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
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
