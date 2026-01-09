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
import br.com.codecacto.locadora.core.model.Cliente
import br.com.codecacto.locadora.core.ui.strings.Strings
import br.com.codecacto.locadora.core.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarClienteScreen(
    clientes: List<Cliente>,
    onSelect: (Cliente) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredClientes = remember(clientes, searchQuery) {
        if (searchQuery.isBlank()) {
            clientes
        } else {
            clientes.filter { cliente ->
                cliente.nomeRazao.contains(searchQuery, ignoreCase = true) ||
                cliente.cpfCnpj?.contains(searchQuery) == true ||
                cliente.telefoneWhatsapp.contains(searchQuery)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Slate50)
    ) {
        // Header com fundo azul igual ClientesScreen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.Blue600)
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
                            text = Strings.SELECIONAR_CLIENTE_TITLE,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = Strings.clientesCadastrados(clientes.size),
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
                        contentDescription = Strings.SELECIONAR_CLIENTE_ADICIONAR,
                        tint = AppColors.Blue600
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
            placeholder = { Text(Strings.SELECIONAR_CLIENTE_BUSCAR) },
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
                            contentDescription = Strings.SELECIONAR_CLIENTE_LIMPAR,
                            tint = AppColors.Slate500
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.Blue600,
                unfocusedBorderColor = AppColors.Slate300,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            singleLine = true
        )

        // Lista de Clientes
        if (filteredClientes.isEmpty()) {
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
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = AppColors.Slate500
                        )
                    }
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            Strings.SELECIONAR_CLIENTE_NENHUM_ENCONTRADO
                        else
                            Strings.SELECIONAR_CLIENTE_NENHUM_CADASTRADO,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.Slate700
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty())
                            Strings.SELECIONAR_CLIENTE_TENTE_OUTROS_TERMOS
                        else
                            Strings.SELECIONAR_CLIENTE_ADICIONE_PRIMEIRO,
                        fontSize = 14.sp,
                        color = AppColors.Slate500
                    )

                    Button(
                        onClick = onAddNew,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Blue600
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Strings.SELECIONAR_CLIENTE_ADICIONAR)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredClientes) { cliente ->
                    ClienteItem(
                        cliente = cliente,
                        onClick = { onSelect(cliente) }
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
private fun ClienteItem(
    cliente: Cliente,
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
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppColors.Blue100),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cliente.nomeRazao.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = AppColors.Blue600
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nomeRazao,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.Slate900
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (cliente.telefoneWhatsapp.isNotBlank()) {
                        Text(
                            text = cliente.telefoneWhatsapp,
                            fontSize = 13.sp,
                            color = AppColors.Slate500
                        )
                    }
                    cliente.cpfCnpj?.let {
                        if (it.isNotBlank()) {
                            Text(
                                text = it,
                                fontSize = 13.sp,
                                color = AppColors.Slate500
                            )
                        }
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
