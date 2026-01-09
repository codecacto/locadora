package br.com.codecacto.locadora.features.locacoes.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.core.model.Equipamento
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarEquipamentoScreen(
    equipamentos: List<Equipamento>,
    onSelect: (Equipamento) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredEquipamentos = remember(equipamentos, searchQuery) {
        if (searchQuery.isBlank()) {
            equipamentos
        } else {
            equipamentos.filter { equipamento ->
                equipamento.nome.contains(searchQuery, ignoreCase = true) ||
                equipamento.categoria.contains(searchQuery, ignoreCase = true) ||
                equipamento.identificacao?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Slate50)
    ) {
        // Header com fundo violeta igual EquipamentosScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Violet600)
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
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
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
                            text = Strings.SELECIONAR_EQUIPAMENTO_TITLE,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = Strings.equipamentosCadastrados(equipamentos.size),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Botao Adicionar
                IconButton(
                    onClick = onAddNew,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = Strings.SELECIONAR_EQUIPAMENTO_ADICIONAR,
                        tint = AppColors.Violet600
                    )
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(Strings.SELECIONAR_EQUIPAMENTO_BUSCAR) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = AppColors.Slate500
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Strings.SELECIONAR_EQUIPAMENTO_LIMPAR,
                            tint = AppColors.Slate500
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Violet600,
                unfocusedBorderColor = AppColors.Slate300,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Info sobre equipamentos disponiveis
        if (equipamentos.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = AppColors.Violet600,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = Strings.SELECIONAR_EQUIPAMENTO_INFO,
                    fontSize = 12.sp,
                    color = AppColors.Slate500
                )
            }
        }

        // Lista de Equipamentos
        if (filteredEquipamentos.isEmpty()) {
            // Estado vazio
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(AppColors.Slate200),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = AppColors.Slate500
                        )
                    }
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            Strings.SELECIONAR_EQUIPAMENTO_NENHUM_ENCONTRADO
                        else
                            Strings.SELECIONAR_EQUIPAMENTO_NENHUM_DISPONIVEL,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Slate700
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            Strings.SELECIONAR_EQUIPAMENTO_TENTE_OUTROS_TERMOS
                        else
                            Strings.SELECIONAR_EQUIPAMENTO_TODOS_ALUGADOS,
                        fontSize = 14.sp,
                        color = AppColors.Slate500
                    )

                    Button(
                        onClick = onAddNew,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Violet600
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.SELECIONAR_EQUIPAMENTO_ADICIONAR)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredEquipamentos) { equipamento ->
                    EquipamentoItem(
                        equipamento = equipamento,
                        onClick = { onSelect(equipamento) }
                    )
                }

                // Espaco extra no final
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EquipamentoItem(
    equipamento: Equipamento,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.Violet100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Construction,
                    contentDescription = null,
                    tint = AppColors.Violet600
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipamento.nome,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Slate900
                )
                Text(
                    text = equipamento.categoria,
                    fontSize = 13.sp,
                    color = AppColors.Slate500
                )
                equipamento.identificacao?.let {
                    if (it.isNotBlank()) {
                        Text(
                            text = Strings.formatId(it),
                            fontSize = 12.sp,
                            color = AppColors.Slate400
                        )
                    }
                }
            }

            // Preco
            Column(horizontalAlignment = Alignment.End) {
                val primeiroPreco = equipamento.getPrimeiroPrecoDisponivel()
                primeiroPreco?.let { (periodo, preco) ->
                    Text(
                        text = "${Strings.CURRENCY_SYMBOL} ${formatCurrencyValue(preco)}",
                        fontWeight = FontWeight.Bold,
                        color = AppColors.Violet600
                    )
                    Text(
                        text = Strings.formatPeriodoSufixo(periodo.label),
                        fontSize = 12.sp,
                        color = AppColors.Slate500
                    )
                    val outrosPeriodos = equipamento.getPeriodosDisponiveis().size - 1
                    if (outrosPeriodos > 0) {
                        Text(
                            text = Strings.formatOutrosPeriodos(outrosPeriodos),
                            fontSize = 10.sp,
                            color = AppColors.Violet400
                        )
                    }
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppColors.Slate400
            )
        }
    }
}

private fun formatCurrencyValue(value: Double): String {
    val intPart = value.toLong()
    val decPart = ((value - intPart) * 100).toInt()
    return "$intPart,${decPart.toString().padStart(2, '0')}"
}
