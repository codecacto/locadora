package br.com.codecacto.locadora.features.subscription.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.codecacto.locadora.domain.model.PremiumPlan
import org.koin.compose.viewmodel.koinViewModel

// Cores específicas da tela de assinatura (baseado nas imagens)
private object SubscriptionColors {
    val Orange = Color(0xFFFF6B00)
    val OrangeLight = Color(0xFFFF8534)
    val OrangeBg = Color(0xFFFFF7ED)
    val OrangeAccent = Color(0xFFFFD9B3)
    val Green = Color(0xFF22C55E)
    val GreenBg = Color(0xFFDCFCE7)
    val White = Color(0xFFFFFFFF)
    val Gray50 = Color(0xFFF9FAFB)
    val Gray100 = Color(0xFFF3F4F6)
    val Gray200 = Color(0xFFE5E7EB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray500 = Color(0xFF6B7280)
    val Gray600 = Color(0xFF4B5563)
    val Gray700 = Color(0xFF374151)
    val Gray900 = Color(0xFF111827)
}

@Composable
fun SubscriptionScreen(
    onSubscriptionSuccess: () -> Unit,
    viewModel: SubscriptionViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is SubscriptionContract.Effect.PurchaseSuccess -> {
                    onSubscriptionSuccess()
                }
                is SubscriptionContract.Effect.PurchaseError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SubscriptionContract.Effect.RestoreSuccess -> {
                    onSubscriptionSuccess()
                }
                is SubscriptionContract.Effect.NoPurchasesToRestore -> {
                    snackbarHostState.showSnackbar("Nenhuma compra encontrada para restaurar")
                }
                is SubscriptionContract.Effect.RestoreError -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SubscriptionContract.Effect.OpenUrl -> {
                    // TODO: Abrir URL
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
                .background(SubscriptionColors.White)
                .padding(paddingValues)
        ) {
            // Header com gradiente laranja
            SubscriptionHeader()

            // Conteúdo scrollável
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Cards de planos
                PlansSection(
                    selectedPlan = state.selectedPlan,
                    onPlanSelected = { plan ->
                        viewModel.dispatch(SubscriptionContract.Action.SelectPlan(plan))
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lista de benefícios
                BenefitsSection()

                Spacer(modifier = Modifier.height(24.dp))

                // Card de 7 dias grátis
                FreeTrialCard()

                Spacer(modifier = Modifier.height(24.dp))

                // Botão de compra
                PurchaseButton(
                    isLoading = state.isPurchasing,
                    onClick = { viewModel.dispatch(SubscriptionContract.Action.Purchase) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Restaurar compras
                RestorePurchasesButton(
                    isLoading = state.isRestoring,
                    onClick = { viewModel.dispatch(SubscriptionContract.Action.RestorePurchases) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer com termos
                LegalFooter(
                    onTermsClick = { viewModel.dispatch(SubscriptionContract.Action.OpenTermsOfUse) },
                    onPrivacyClick = { viewModel.dispatch(SubscriptionContract.Action.OpenPrivacyPolicy) }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SubscriptionHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SubscriptionColors.Orange,
                        SubscriptionColors.OrangeLight
                    )
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        Column {
            // Badge PREMIUM
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "\uD83D\uDC51", // Crown emoji
                    fontSize = 20.sp
                )
                Text(
                    text = "PREMIUM",
                    color = SubscriptionColors.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = "Desbloqueie todo o\npotencial",
                color = SubscriptionColors.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Comece com 7 dias grátis, cancele quando quiser",
                color = SubscriptionColors.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PlansSection(
    selectedPlan: PremiumPlan,
    onPlanSelected: (PremiumPlan) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Plano Mensal
        PlanCard(
            plan = PremiumPlan.MENSAL,
            isSelected = selectedPlan == PremiumPlan.MENSAL,
            onClick = { onPlanSelected(PremiumPlan.MENSAL) }
        )

        // Plano Anual
        PlanCard(
            plan = PremiumPlan.ANUAL,
            isSelected = selectedPlan == PremiumPlan.ANUAL,
            onClick = { onPlanSelected(PremiumPlan.ANUAL) }
        )
    }
}

@Composable
private fun PlanCard(
    plan: PremiumPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) SubscriptionColors.Orange else SubscriptionColors.Gray200
    val backgroundColor = if (isSelected) SubscriptionColors.OrangeBg else SubscriptionColors.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Badge "Mais Popular" para anual
                if (plan.isRecommended) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SubscriptionColors.Gray700)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Mais Popular",
                            color = SubscriptionColors.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = plan.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SubscriptionColors.Gray900
                    )

                    // Badge de economia para anual
                    if (plan.savingsPercent > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(SubscriptionColors.Green)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Economize ${plan.savingsPercent}%",
                                color = SubscriptionColors.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "R$ ${String.format("%.2f", plan.pricePerMonth).replace(".", ",")}/mês",
                    fontSize = 14.sp,
                    color = SubscriptionColors.Gray500
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "R$ ${String.format("%.2f", plan.price).replace(".", ",")}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubscriptionColors.Orange
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = SubscriptionColors.Orange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BenefitsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SubscriptionColors.Gray50),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "O que está incluso:",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SubscriptionColors.Gray900
            )

            Spacer(modifier = Modifier.height(16.dp))

            val benefits = listOf(
                "Locações ilimitadas",
                "Gestão completa de clientes",
                "Controle de equipamentos",
                "Entregas programadas",
                "Notificações de vencimento",
                "Renovação automática",
                "Emissão de notas fiscais",
                "Relatórios e histórico",
                "Suporte prioritário"
            )

            benefits.forEach { benefit ->
                BenefitItem(text = benefit)
                if (benefit != benefits.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SubscriptionColors.Orange,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 14.sp,
            color = SubscriptionColors.Gray700
        )
    }
}

@Composable
private fun FreeTrialCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SubscriptionColors.OrangeBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SubscriptionColors.Orange),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = SubscriptionColors.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "7 dias grátis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = SubscriptionColors.Gray900
                )
                Text(
                    text = "Teste todos os recursos sem compromisso. Cancele a qualquer momento sem custos.",
                    fontSize = 13.sp,
                    color = SubscriptionColors.Gray600,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun PurchaseButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = !isLoading,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SubscriptionColors.Orange,
            disabledContainerColor = SubscriptionColors.Orange.copy(alpha = 0.5f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = SubscriptionColors.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Começar 7 dias grátis",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = SubscriptionColors.White
            )
        }
    }
}

@Composable
private fun RestorePurchasesButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = SubscriptionColors.Gray500,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = SubscriptionColors.Gray500,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = "Restaurar compras",
            fontSize = 14.sp,
            color = SubscriptionColors.Gray500,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun LegalFooter(
    onTermsClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ao continuar, você concorda com nossos",
            fontSize = 12.sp,
            color = SubscriptionColors.Gray500,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Termos de Uso",
                fontSize = 12.sp,
                color = SubscriptionColors.Orange,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onTermsClick)
            )

            Text(
                text = "  •  ",
                fontSize = 12.sp,
                color = SubscriptionColors.Gray400
            )

            Text(
                text = "Política de Privacidade",
                fontSize = 12.sp,
                color = SubscriptionColors.Orange,
                fontWeight = FontWeight.Medium,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable(onClick = onPrivacyClick)
            )
        }
    }
}
