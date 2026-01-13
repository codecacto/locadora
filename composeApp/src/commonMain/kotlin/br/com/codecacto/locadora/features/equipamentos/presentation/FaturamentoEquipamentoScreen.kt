package br.com.codecacto.locadora.features.equipamentos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaturamentoEquipamentoScreen(
    equipamentoId: String,
    onBack: () -> Unit,
    viewModel: FaturamentoEquipamentoViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(equipamentoId) {
        viewModel.dispatch(FaturamentoEquipamentoContract.Action.LoadData(equipamentoId))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Slate50)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(AppColors.Violet600, AppColors.Violet500)
                    )
                )
                .padding(horizontal = 16.dp)
                .padding(top = 48.dp, bottom = 16.dp)
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
                        text = "Faturamento",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    state.equipamento?.let { equip ->
                        Text(
                            text = equip.nome,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (!state.isLoading && state.equipamento != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Cards de resumo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Faturamento
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (state.mesSelecionado == null) "Faturamento Total" else "Faturamento do Mês",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatCurrency(state.faturamentoFiltrado),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Lucro
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (state.lucroTotal >= 0) AppColors.Green.copy(alpha = 0.3f)
                                else AppColors.Red.copy(alpha = 0.3f)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Lucro Total",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatCurrency(state.lucroTotal),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Valor de compra
                if (state.valorCompra > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Valor de compra: ${formatCurrency(state.valorCompra)}",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                // Filtro por mês
                if (state.mesesDisponiveis.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    MesFilterDropdown(
                        mesesDisponiveis = state.mesesDisponiveis,
                        mesSelecionado = state.mesSelecionado,
                        onMesSelecionado = { mesAno ->
                            viewModel.dispatch(FaturamentoEquipamentoContract.Action.SelectMes(mesAno))
                        }
                    )
                }
            }
        }

        // Content
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.Violet600)
            }
        } else if (state.locacoesFiltradas.isEmpty()) {
            EmptyFaturamentoState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = Strings.locacoesPagas(state.locacoesFiltradas.size),
                        fontSize = 14.sp,
                        color = AppColors.Slate600,
                        fontWeight = FontWeight.Medium
                    )
                }

                items(state.locacoesFiltradas) { locacaoFaturamento ->
                    LocacaoFaturamentoCard(locacaoFaturamento)
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MesFilterDropdown(
    mesesDisponiveis: List<MesAnoFaturamento>,
    mesSelecionado: MesAnoFaturamento?,
    onMesSelecionado: (MesAnoFaturamento?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = mesSelecionado?.let { Strings.formatMesAno(it.mes, it.ano) }
                            ?: "Todos os meses",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color.White
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Todos os meses",
                        fontWeight = if (mesSelecionado == null) FontWeight.Bold else FontWeight.Normal,
                        color = if (mesSelecionado == null) AppColors.Violet600 else AppColors.Slate900
                    )
                },
                onClick = {
                    onMesSelecionado(null)
                    expanded = false
                },
                leadingIcon = if (mesSelecionado == null) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = AppColors.Violet600,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
            HorizontalDivider(color = AppColors.Slate200)
            mesesDisponiveis.forEach { mesAno ->
                val isSelected = mesSelecionado == mesAno
                DropdownMenuItem(
                    text = {
                        Text(
                            text = Strings.formatMesAno(mesAno.mes, mesAno.ano),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) AppColors.Violet600 else AppColors.Slate900
                        )
                    },
                    onClick = {
                        onMesSelecionado(mesAno)
                        expanded = false
                    },
                    leadingIcon = if (isSelected) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = AppColors.Violet600,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun LocacaoFaturamentoCard(locacaoFaturamento: LocacaoFaturamento) {
    val locacao = locacaoFaturamento.locacao

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
                        text = locacaoFaturamento.clienteNome,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = AppColors.Slate900
                    )
                    Text(
                        text = locacao.periodo.label,
                        fontSize = 14.sp,
                        color = AppColors.Slate500
                    )
                }
                Text(
                    text = formatCurrency(locacao.valorLocacao),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Emerald600
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Período",
                        fontSize = 12.sp,
                        color = AppColors.Slate500
                    )
                    Text(
                        text = "${formatDate(locacao.dataInicio)} - ${formatDate(locacao.dataFimPrevista)}",
                        fontSize = 13.sp,
                        color = AppColors.Slate700
                    )
                }
                locacao.dataPagamento?.let { data ->
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Pago em",
                            fontSize = 12.sp,
                            color = AppColors.Slate500
                        )
                        Text(
                            text = formatDate(data),
                            fontSize = 13.sp,
                            color = AppColors.Emerald600,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyFaturamentoState() {
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
                    .background(AppColors.Violet100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = AppColors.Violet600
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Nenhum faturamento",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.Slate800,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Este equipamento ainda não possui locações pagas.",
                fontSize = 14.sp,
                color = AppColors.Slate500,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatCurrency(value: Double): String {
    val isNegative = value < 0
    val absValue = kotlin.math.abs(value)
    val intPart = absValue.toLong()
    val decPart = ((absValue - intPart) * 100).toInt()

    // Formatar parte inteira com separador de milhares
    val intPartFormatted = intPart.toString()
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    val formatted = "R$ $intPartFormatted,${decPart.toString().padStart(2, '0')}"
    return if (isNegative) "-$formatted" else formatted
}

private fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${localDateTime.dayOfMonth.toString().padStart(2, '0')}/${
        localDateTime.monthNumber.toString().padStart(2, '0')
    }/${localDateTime.year}"
}
