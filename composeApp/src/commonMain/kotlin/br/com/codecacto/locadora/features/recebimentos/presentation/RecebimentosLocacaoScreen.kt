package br.com.codecacto.locadora.features.recebimentos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.model.Locacao
import br.com.codecacto.locadora.core.model.StatusColeta
import br.com.codecacto.locadora.core.model.StatusPagamento
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import br.com.codecacto.locadora.data.repository.ClienteRepository
import br.com.codecacto.locadora.data.repository.EquipamentoRepository
import br.com.codecacto.locadora.data.repository.LocacaoRepository
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun RecebimentosLocacaoScreen(
    locacaoId: String,
    onBack: () -> Unit,
    locacaoRepository: LocacaoRepository = koinInject(),
    clienteRepository: ClienteRepository = koinInject(),
    equipamentoRepository: EquipamentoRepository = koinInject()
) {
    var locacao by remember { mutableStateOf<Locacao?>(null) }
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var equipamento by remember { mutableStateOf<Equipamento?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isProcessing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(locacaoId) {
        isLoading = true
        val loc = locacaoRepository.getLocacaoById(locacaoId)
        locacao = loc
        loc?.let {
            cliente = clienteRepository.getClienteById(it.clienteId)
            equipamento = equipamentoRepository.getEquipamentoById(it.equipamentoId)
        }
        isLoading = false
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
                    .background(
                        Brush.linearGradient(
                            colors = listOf(AppColors.Emerald600, AppColors.Green)
                        )
                    )
                    .padding(horizontal = 16.dp)
                    .padding(top = 48.dp, bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = Strings.COMMON_VOLTAR,
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = Strings.RECEBIMENTOS_TITLE,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = Strings.RECEBIMENTOS_LOCACAO_SUBTITLE,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Emerald600)
                }
            } else if (locacao == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Strings.ERROR_LOCACAO_NAO_ENCONTRADA,
                        color = AppColors.Slate500
                    )
                }
            } else {
                val loc = locacao!!
                val isPago = loc.statusPagamento == StatusPagamento.PAGO

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card principal
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header do card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cliente?.nomeRazao ?: Strings.COMMON_CLIENTE_NAO_ENCONTRADO,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 18.sp,
                                        color = AppColors.Slate900
                                    )
                                    Text(
                                        text = equipamento?.nome ?: Strings.COMMON_EQUIPAMENTO_NAO_ENCONTRADO,
                                        fontSize = 14.sp,
                                        color = AppColors.Slate600
                                    )
                                }
                                StatusBadge(isPago = isPago)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Valor em destaque
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isPago) AppColors.GreenLight else AppColors.Emerald100)
                                    .padding(16.dp)
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
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(if (isPago) AppColors.Green.copy(alpha = 0.2f) else AppColors.Emerald600.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPago) Icons.Default.CheckCircle else Icons.Default.AttachMoney,
                                                contentDescription = null,
                                                tint = if (isPago) AppColors.Green else AppColors.Emerald600,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isPago) Strings.RECEBIMENTOS_VALOR_PAGO else Strings.RECEBIMENTOS_VALOR,
                                            fontSize = 14.sp,
                                            color = AppColors.Slate600
                                        )
                                    }
                                    Text(
                                        text = formatCurrency(loc.valorLocacao),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPago) AppColors.Green else AppColors.Emerald600
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Info Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                InfoBox(
                                    label = Strings.RECEBIMENTOS_PERIODO,
                                    value = "${formatDate(loc.dataInicio)} - ${formatDate(loc.dataFimPrevista)}",
                                    modifier = Modifier.weight(1f)
                                )
                                InfoBox(
                                    label = Strings.RECEBIMENTOS_STATUS,
                                    value = when (loc.statusColeta) {
                                        StatusColeta.COLETADO -> Strings.STATUS_COLETA_COLETADO
                                        StatusColeta.NAO_COLETADO -> Strings.STATUS_COLETA_NAO_COLETADO
                                    },
                                    modifier = Modifier.weight(1f),
                                    valueColor = if (loc.statusColeta == StatusColeta.COLETADO) AppColors.Emerald600 else AppColors.Slate600
                                )
                            }

                            if (isPago && loc.dataPagamento != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                InfoBox(
                                    label = Strings.RECEBIMENTOS_DATA_PAGAMENTO,
                                    value = formatDateFull(loc.dataPagamento!!),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Botao de confirmar recebimento (apenas se pendente)
                            if (!isPago) {
                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        scope.launch {
                                            isProcessing = true
                                            try {
                                                locacaoRepository.marcarPago(locacaoId)
                                                snackbarHostState.showSnackbar(Strings.RECEBIMENTO_CONFIRMADO)
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(e.message ?: Strings.COMMON_ERRO_DESCONHECIDO)
                                            }
                                            isProcessing = false
                                        }
                                    },
                                    enabled = !isProcessing,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AppColors.Emerald600
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.AttachMoney,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(Strings.RECEBIMENTOS_CONFIRMAR)
                                    }
                                }
                            }
                        }
                    }

                    // Botao voltar
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(Strings.COMMON_VOLTAR)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(isPago: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isPago) AppColors.GreenLight else AppColors.YellowLight)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isPago) Strings.STATUS_PAGAMENTO_PAGO else Strings.STATUS_PAGAMENTO_PENDENTE,
            color = if (isPago) AppColors.Green else AppColors.Yellow,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun InfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = AppColors.Slate900
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Slate100)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = AppColors.Slate500
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }"
}

private fun formatDateFull(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }/${localDateTime.year}"
}

private fun formatCurrency(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "R$ $intPart,${decPart.toString().padStart(2, '0')}"
}
