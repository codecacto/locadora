package br.com.codecacto.locadora.features.feedback.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.FeedbackMotivo
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FeedbackScreen(
    onBack: () -> Unit,
    viewModel: FeedbackViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FeedbackContract.Effect.NavigateBack -> onBack()
                is FeedbackContract.Effect.ShowSuccess -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
                is FeedbackContract.Effect.ShowError -> {
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
                            text = Strings.FEEDBACK_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.FEEDBACK_SUBTITLE,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Motivo Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .selectableGroup(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = Strings.FEEDBACK_MOTIVO,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.Slate900
                        )

                        if (state.motivoError != null) {
                            Text(
                                text = state.motivoError!!,
                                color = AppColors.Red,
                                fontSize = 12.sp
                            )
                        }

                        FeedbackMotivo.entries.forEach { motivo ->
                            val selected = state.selectedMotivo == motivo
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .selectable(
                                        selected = selected,
                                        onClick = { viewModel.dispatch(FeedbackContract.Action.OnMotivoSelected(motivo)) },
                                        role = Role.RadioButton
                                    )
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = null,
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = AppColors.Violet600
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (motivo) {
                                        FeedbackMotivo.SUGESTAO -> Strings.FEEDBACK_MOTIVO_SUGESTAO
                                        FeedbackMotivo.BUG -> Strings.FEEDBACK_MOTIVO_BUG
                                        FeedbackMotivo.RECLAMACAO -> Strings.FEEDBACK_MOTIVO_RECLAMACAO
                                        FeedbackMotivo.DUVIDA -> Strings.FEEDBACK_MOTIVO_DUVIDA
                                        FeedbackMotivo.ELOGIO -> Strings.FEEDBACK_MOTIVO_ELOGIO
                                        FeedbackMotivo.OUTRO -> Strings.FEEDBACK_MOTIVO_OUTRO
                                    },
                                    color = AppColors.Slate700
                                )
                            }
                        }
                    }
                }

                // Mensagem TextField
                OutlinedTextField(
                    value = state.mensagem,
                    onValueChange = { viewModel.dispatch(FeedbackContract.Action.OnMensagemChanged(it)) },
                    label = { Text(Strings.FEEDBACK_MENSAGEM) },
                    placeholder = { Text(Strings.FEEDBACK_MENSAGEM_PLACEHOLDER) },
                    isError = state.mensagemError != null,
                    supportingText = state.mensagemError?.let { { Text(it, color = AppColors.Red) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 6,
                    maxLines = 10,
                    enabled = !state.isLoading && !state.isSent
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Button
                val isFormValid = state.selectedMotivo != null && state.mensagem.trim().isNotEmpty()
                Button(
                    onClick = { viewModel.dispatch(FeedbackContract.Action.OnSendClick) },
                    enabled = !state.isLoading && !state.isSent && isFormValid,
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
                        Icon(
                            imageVector = Icons.Default.Feedback,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Strings.FEEDBACK_ENVIAR,
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
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
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
