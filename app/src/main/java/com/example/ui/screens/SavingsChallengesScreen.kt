package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SavingsChallenge
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsChallengesScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val challenges by viewModel.challenges.collectAsState()

    var selectedSectionTab by remember { mutableStateOf(0) } // 0: Meus Desafios, 1: Desafios Prontos, 2: Criar Customizado

    // Dialog state for Contribution
    var showContributionDialog by remember { mutableStateOf(false) }
    var selectedChallengeToContribute by remember { mutableStateOf<SavingsChallenge?>(null) }
    var contributionAmountStr by remember { mutableStateOf("") }

    // Dialog state for celebratory reward notification
    var showRewardDialog by remember { mutableStateOf(false) }
    var rewardMotivationalText by remember { mutableStateOf("") }
    var rewardTitleText by remember { mutableStateOf("") }

    // Predefined templates list
    val predefinedTemplates = remember {
        listOf(
            SavingsChallengeTemplate(
                title = "Desafio das 52 Semanas",
                description = "Garante uma poupança gradual semana após semana. Ideal para começar guardando pouco (R$ 1 na primeira semana, R$ 2 na segunda, até R$ 52) acumulando R$ 1.378 no total de forma leve!",
                targetAmount = 1378.00,
                months = 12,
                category = "52_WEEKS"
            ),
            SavingsChallengeTemplate(
                title = "Pote de Economia Express",
                description = "Desafio rápido focado em reduzir cafezinhos e pequenas despesas diárias para levantar capital rápido em poucos meses.",
                targetAmount = 300.00,
                months = 2,
                category = "COIN_JAR"
            ),
            SavingsChallengeTemplate(
                title = "Primeira Reserva Escudo",
                description = "Crie sua primeira muralha financeira contra imprevistos com essa meta rápida para acumular uma proteção inicial essencial.",
                targetAmount = 2500.00,
                months = 6,
                category = "EMERGENCY"
            ),
            SavingsChallengeTemplate(
                title = "Smart Travel Challenge",
                description = "Economia focada em um final de semana especial de lazer. Menos gastos domésticos, mais diversão planejada!",
                targetAmount = 1200.00,
                months = 4,
                category = "TRAVEL"
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("savings_challenges_screen"),
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "DESAFIOS DE POUPANÇA",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Economia Divertida",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_challenges")) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 800.dp)
            ) {
            // Elegant Section Selector Tab
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Meus Desafios", "Explorar Prontos", "Criar Novo")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedSectionTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { selectedSectionTab = index }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = selectedSectionTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                label = "challenge_section_animation"
            ) { targetTab ->
                when (targetTab) {
                    0 -> {
                        // Section 0: My active challenges
                        if (challenges.isEmpty()) {
                            ChallengeEmptyState(onNavigateToExplore = { selectedSectionTab = 1 })
                        } else {
                            MyChallengesList(
                                challenges = challenges,
                                viewModel = viewModel,
                                onContributeClick = { challenge ->
                                    selectedChallengeToContribute = challenge
                                    contributionAmountStr = ""
                                    showContributionDialog = true
                                },
                                onDeleteClick = { challenge ->
                                    viewModel.deleteChallenge(challenge)
                                    Toast.makeText(context, "Desafio excluído!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                    1 -> {
                        // Section 1: Explore predefined templates
                        ExploreTemplatesList(
                            templates = predefinedTemplates,
                            activeChallenges = challenges,
                            onStartTemplate = { template ->
                                // Instantiate this template as an active challenge
                                viewModel.addChallenge(
                                    title = template.title,
                                    description = template.description,
                                    target = template.targetAmount,
                                    current = 0.0,
                                    months = template.months,
                                    category = template.category,
                                    isCustom = false
                                )
                                // Pop celebratory notification
                                rewardTitleText = "DESAFIO INICIADO! 🎯"
                                rewardMotivationalText = "Excelente escolha de desafio! Economizar exige consistência, mas o resultado final é transformador. Vá em frente!"
                                showRewardDialog = true
                                selectedSectionTab = 0 // Redirect to tab 0
                            }
                        )
                    }
                    2 -> {
                        // Section 2: Create Custom Challenge
                        CreateCustomChallengeForm(
                            viewModel = viewModel,
                            onCreateSuccess = {
                                rewardTitleText = "DESAFIO CRIADO COM SUCESSO! 🛡️"
                                rewardMotivationalText = "Você desenhou sua própria meta personalizada! O seu empenho hoje é a segurança e tranquilidade financeira do seu amanhã."
                                showRewardDialog = true
                                selectedSectionTab = 0 // Redirect to my active challenges
                            }
                        )
                    }
                }
            }
        }
    }

    // Contribution Popup Input Dialog
    if (showContributionDialog && selectedChallengeToContribute != null) {
        val challenge = selectedChallengeToContribute!!
        val progress = if (challenge.targetAmount > 0) challenge.currentAmount / challenge.targetAmount else 0.0
        val percent = (progress * 100).roundToInt()

        AlertDialog(
            onDismissRequest = { showContributionDialog = false },
            title = {
                Text(
                    text = "Apoiar Desafio 💰",
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Insira o valor que você está separando para o desafio:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = contributionAmountStr,
                        onValueChange = { contributionAmountStr = it },
                        label = { Text("Valor a Poupar") },
                        prefix = { Text(viewModel.getCurrencySymbol() + " ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("contribution_input_field")
                    )

                    // Quick suggestion values
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(10.0, 50.0, 100.0).forEach { value ->
                            OutlinedButton(
                                onClick = { contributionAmountStr = value.toString() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("+R$ ${value.toInt()}", fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = contributionAmountStr.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            viewModel.contributeToChallenge(challenge.id, parsed)
                            showContributionDialog = false

                            // Check new percentage and select motivational message
                            val simulatedNewAmount = (challenge.currentAmount + parsed).coerceAtMost(challenge.targetAmount)
                            val newPercent = (simulatedNewAmount / challenge.targetAmount) * 100.0
                            
                            rewardTitleText = "DEPÓSITO REALIZADO! 🎉"
                            rewardMotivationalText = viewModel.getMotivationalAdvice(newPercent)
                            showRewardDialog = true
                        } else {
                            Toast.makeText(context, "Insira um valor válido!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.testTag("confirm_deposit_button")
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showContributionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Celebration Reward Motivation Dialog
    if (showRewardDialog) {
        AlertDialog(
            onDismissRequest = { showRewardDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Vencer",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = rewardTitleText,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            text = {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = rewardMotivationalText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRewardDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dismiss_reward_button")
                ) {
                    Text("Continuar focado!")
                }
            }
        )
        }
    }
}

// Sub-components

data class SavingsChallengeTemplate(
    val title: String,
    val description: String,
    val targetAmount: Double,
    val months: Int,
    val category: String
)

@Composable
fun ChallengeEmptyState(onNavigateToExplore: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.SentimentSatisfiedAlt,
                        contentDescription = "Vazio",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = "Sem desafios ativos!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Poupar dinheiro não precisa ser doloroso. Escolha um desafio interativo e divirta-se batendo recordes!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Button(
                onClick = onNavigateToExplore,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Explorar Desafios Disponíveis")
            }
        }
    }
}

@Composable
fun MyChallengesList(
    challenges: List<SavingsChallenge>,
    viewModel: FinanceViewModel,
    onContributeClick: (SavingsChallenge) -> Unit,
    onDeleteClick: (SavingsChallenge) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        // High impact header item showing overall progress of active challenges
        item {
            val totalTarget = challenges.sumOf { it.targetAmount }
            val totalSaved = challenges.sumOf { it.currentAmount }
            val totalProgressPercent = if (totalTarget > 0) (totalSaved / totalTarget) * 100.0 else 0.0

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "RESUMO DA SUA POUPANÇA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        letterSpacing = 1.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Acumulado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = viewModel.formatMoney(totalSaved),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text(
                            text = "${totalProgressPercent.roundToInt()}% Concluído",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { (totalProgressPercent / 100.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.2f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
        }

        // Active challenges title
        item {
            Text(
                text = "Desafios em Andamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(challenges, key = { it.id }) { challenge ->
            val progress = if (challenge.targetAmount > 0) challenge.currentAmount / challenge.targetAmount else 0.0
            val percent = (progress * 100).roundToInt()

            // Calculate days left
            val msLeft = challenge.endDateTimestamp - System.currentTimeMillis()
            val daysLeft = (msLeft / (1000 * 60 * 60 * 24)).coerceAtLeast(0)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_challenge_card_${challenge.id}"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val icon = when (challenge.category) {
                                    "52_WEEKS" -> Icons.Default.CalendarToday
                                    "COIN_JAR" -> Icons.Default.Savings
                                    "EMERGENCY" -> Icons.Default.Security
                                    else -> Icons.Default.Star
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = challenge.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = challenge.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Close/Delete Action
                        IconButton(onClick = { onDeleteClick(challenge) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Desistir do Desafio",
                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Progresso",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = viewModel.formatMoney(challenge.currentAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = " / " + viewModel.formatMoney(challenge.targetAmount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Prazo Restante",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$daysLeft dias",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (daysLeft < 10) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Raw Progress Indicator
                    Box(modifier = Modifier.fillMaxWidth()) {
                        LinearProgressIndicator(
                            progress = { progress.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Percentage badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${percent}% Completo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Aberto em " + SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(challenge.startDateTimestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick AI Advice Bubble (Live Motivation element)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Dica Inteligente",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = viewModel.getMotivationalAdvice(percent.toDouble()),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deposit Button
                    Button(
                        onClick = { onContributeClick(challenge) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contribute_button_${challenge.id}"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registrar Economia para este Desafio")
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreTemplatesList(
    templates: List<SavingsChallengeTemplate>,
    activeChallenges: List<SavingsChallenge>,
    onStartTemplate: (SavingsChallengeTemplate) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Text(
                text = "Escolha um Desafio Pré-definido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(templates) { template ->
            val hasActiveWithSameTitle = activeChallenges.any { it.title == template.title }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val icon = when (template.category) {
                                "52_WEEKS" -> Icons.Default.CalendarToday
                                "COIN_JAR" -> Icons.Default.Savings
                                "EMERGENCY" -> Icons.Default.Security
                                else -> Icons.Default.TravelExplore
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Category Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "${template.months} meses",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Meta Alvo",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "R$ " + String.format("%.2f", template.targetAmount),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (hasActiveWithSameTitle) {
                            Button(
                                onClick = {},
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Já Participando")
                            }
                        } else {
                            Button(
                                onClick = { onStartTemplate(template) },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Iniciar Desafio")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomChallengeForm(
    viewModel: FinanceViewModel,
    onCreateSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var startStr by remember { mutableStateOf("") }
    var months by remember { mutableStateOf(6f) }
    var category by remember { mutableStateOf("CUSTOM") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
    ) {
        item {
            Text(
                text = "Desenhe Seu Próprio Desafio",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Escolha suas próprias metas, defina um prazo e desafie-se a economizar seus recursos de forma estruturada.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nome do Desafio") },
                placeholder = { Text("Ex: Comprar Notebook, Viagem de Fim de Ano") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("custom_challenge_title_input")
            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Motivação / Descrição") },
                placeholder = { Text("Ex: Cortar lanches de fim de semana na rua para realizar esse sonho!") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("custom_challenge_description_input")
            )
        }

        item {
            OutlinedTextField(
                value = targetStr,
                onValueChange = { targetStr = it },
                label = { Text("Meta de Economia Alvo") },
                prefix = { Text("R$ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("custom_challenge_target_input")
            )
        }

        item {
            OutlinedTextField(
                value = startStr,
                onValueChange = { startStr = it },
                label = { Text("Valor Guardado Inicial (Opcional)") },
                prefix = { Text("R$ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Months Slider
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Prazo do Desafio",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${months.roundToInt()} meses",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = months,
                        onValueChange = { months = it },
                        valueRange = 1f..24f,
                        steps = 22,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 mês", style = MaterialTheme.typography.labelSmall)
                        Text("12 meses", style = MaterialTheme.typography.labelSmall)
                        Text("24 meses", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }

        // Category
        item {
            Text(
                text = "Ícone / Categoria",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val cats = listOf(
                    Triple("CUSTOM", Icons.Default.Savings, "Poupança"),
                    Triple("EMERGENCY", Icons.Default.Security, "Reserva"),
                    Triple("TRAVEL", Icons.Default.TravelExplore, "Viagem")
                )
                cats.forEach { (catId, icon, label) ->
                    val isSel = category == catId
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { category = catId }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Submit Button
        item {
            Button(
                onClick = {
                    val parsedTarget = targetStr.toDoubleOrNull()
                    val parsedStart = startStr.toDoubleOrNull() ?: 0.0
                    
                    if (title.isBlank()) {
                        Toast.makeText(context, "Insira um nome para o desafio!", Toast.LENGTH_SHORT).show()
                    } else if (parsedTarget == null || parsedTarget <= 0) {
                        Toast.makeText(context, "A meta alvo precisa de um valor positivo!", Toast.LENGTH_SHORT).show()
                    } else if (parsedStart < 0) {
                        Toast.makeText(context, "O valor inicial não pode ser negativo!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.addChallenge(
                            title = title,
                            description = description.ifBlank { "Desafio customizado criado por mim." },
                            target = parsedTarget,
                            current = parsedStart,
                            months = months.roundToInt(),
                            category = category,
                            isCustom = true
                        )
                        onCreateSuccess()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .testTag("submit_challenge_button"),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Criar e Ativar Desafio")
            }
        }
    }
}
