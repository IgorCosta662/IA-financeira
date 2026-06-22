package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import com.example.data.model.FinancialNote
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: FinanceViewModel,
    onNavigateToChallenges: () -> Unit = {}
) {
    val accountsState by viewModel.accounts.collectAsState()
    val transactionsState by viewModel.transactions.collectAsState()
    val investmentsState by viewModel.investments.collectAsState()
    val goalsState by viewModel.goals.collectAsState()
    val notesState by viewModel.notes.collectAsState()

    var showGoalsManager by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<com.example.data.model.FinancialGoal?>(null) }
    var showDeleteConfirmGoal by remember { mutableStateOf<com.example.data.model.FinancialGoal?>(null) }
    var showContributeGoal by remember { mutableStateOf<com.example.data.model.FinancialGoal?>(null) }
    
    var goalTitle by remember { mutableStateOf("") }
    var goalTarget by remember { mutableStateOf("") }
    var goalCurrent by remember { mutableStateOf("") }
    var goalMonths by remember { mutableStateOf("") }
    var goalCategory by remember { mutableStateOf("SHORT_TERM") }
    var goalSearchQuery by remember { mutableStateOf("") }
    var goalSelectedFilter by remember { mutableStateOf("ALL") }
    var goalContributeValue by remember { mutableStateOf("") }

    // Calculations
    val totalAccountBalance = accountsState.sumOf { it.balance }
    val totalInvestmentValue = investmentsState.sumOf { it.quantity * it.currentPrice }
    val netWorth = totalAccountBalance + totalInvestmentValue

    val expensesList = transactionsState.filter { it.type == "EXPENSE" }
    val incomeList = transactionsState.filter { it.type == "INCOME" }
    
    val totalIncome = incomeList.sumOf { it.amount }
    val totalExpenses = expensesList.sumOf { it.amount }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1000.dp)
                .testTag("dashboard_screen")
                .padding(if (isWideScreen) 24.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome and Header
            item {
                DashboardHeaderSection()
            }

            if (isWideScreen) {
                // TABLET / WIDE LANDSCAPE ADAPTIVE SPLIT GRID
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardBalanceCard(
                                viewModel = viewModel,
                                netWorth = netWorth,
                                totalAccountBalance = totalAccountBalance,
                                totalInvestmentValue = totalInvestmentValue
                            )
                            DashboardQuickActions(onNavigateToChallenges = onNavigateToChallenges)
                        }

                        Column(
                            modifier = Modifier.weight(0.9f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardSummaryCard(
                                viewModel = viewModel,
                                totalIncome = totalIncome,
                                totalExpenses = totalExpenses
                            )
                            DashboardHealthCard(viewModel = viewModel)
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1.1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardChartCard(viewModel = viewModel)
                        }

                        Column(
                            modifier = Modifier.weight(0.9f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            DashboardGoalsSection(
                                viewModel = viewModel,
                                goalsState = goalsState,
                                onManageGoals = { showGoalsManager = true }
                            )
                            DashboardNotesSection(
                                notes = notesState
                            )
                            DashboardPromoCard(onNavigateToChallenges = onNavigateToChallenges)
                        }
                    }
                }
            } else {
                // PHONE PORTRAIT STACK
                item {
                    DashboardBalanceCard(
                        viewModel = viewModel,
                        netWorth = netWorth,
                        totalAccountBalance = totalAccountBalance,
                        totalInvestmentValue = totalInvestmentValue
                    )
                }
                item {
                    DashboardQuickActions(onNavigateToChallenges = onNavigateToChallenges)
                }
                item {
                    DashboardSummaryCard(
                        viewModel = viewModel,
                        totalIncome = totalIncome,
                        totalExpenses = totalExpenses
                    )
                }
                item {
                    DashboardHealthCard(viewModel = viewModel)
                }
                item {
                    DashboardChartCard(viewModel = viewModel)
                }
                item {
                    DashboardGoalsSection(
                        viewModel = viewModel,
                        goalsState = goalsState,
                        onManageGoals = { showGoalsManager = true }
                    )
                }
                item {
                    DashboardNotesSection(
                        notes = notesState
                    )
                }
                item {
                    DashboardPromoCard(onNavigateToChallenges = onNavigateToChallenges)
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp)) // padding safe area for navigation components
            }
        }

        // --- Goals CRUD Manager Overlay Dialogs ---
        if (showGoalsManager) {
            AlertDialog(
                onDismissRequest = { showGoalsManager = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Gerenciador de Metas", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        IconButton(onClick = { showGoalsManager = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 480.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search bar and Category Filter Chips
                        OutlinedTextField(
                            value = goalSearchQuery,
                            onValueChange = { goalSearchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar meta...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "Todas",
                                "SHORT_TERM" to "Curto Prazo",
                                "MEDIUM_TERM" to "Médio Prazo",
                                "LONG_TERM" to "Longo Prazo"
                            ).forEach { (filt, label) ->
                                FilterChip(
                                    selected = goalSelectedFilter == filt,
                                    onClick = { goalSelectedFilter = filt },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Divider()

                        // Section to Add / Edit form
                        val isGoalEdit = editingGoal != null
                        Text(
                            text = if (isGoalEdit) "Editar Meta Existente" else "Criar Nova Meta Financeira",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        OutlinedTextField(
                            value = goalTitle,
                            onValueChange = { goalTitle = it },
                            label = { Text("Nome da Meta (Ex: Carro Novo)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = goalTarget,
                                onValueChange = { goalTarget = it },
                                label = { Text("Alvo (R$)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = goalCurrent,
                                onValueChange = { goalCurrent = it },
                                label = { Text("Atual (R$)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = goalMonths,
                                onValueChange = { goalMonths = it },
                                label = { Text("Prazo (Meses)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Categoria", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf(
                                        "SHORT_TERM" to "Curto",
                                        "MEDIUM_TERM" to "Médio",
                                        "LONG_TERM" to "Longo"
                                    ).forEach { (cat, label) ->
                                        FilterChip(
                                            selected = goalCategory == cat,
                                            onClick = { goalCategory = cat },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val target = goalTarget.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val current = goalCurrent.replace(",", ".").toDoubleOrNull() ?: 0.0
                                val mths = goalMonths.toIntOrNull() ?: 12
                                if (goalTitle.isNotEmpty() && target > 0.0) {
                                    if (isGoalEdit && editingGoal != null) {
                                        val updated = editingGoal!!.copy(
                                            title = goalTitle,
                                            targetAmount = target,
                                            currentAmount = current,
                                            category = goalCategory
                                        )
                                        viewModel.updateGoal(updated)
                                    } else {
                                        viewModel.addGoal(goalTitle, target, current, mths, goalCategory)
                                    }
                                    goalTitle = ""
                                    goalTarget = ""
                                    goalCurrent = ""
                                    goalMonths = "12"
                                    editingGoal = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isGoalEdit) "Atualizar Meta" else "Gravar Meta")
                        }

                        if (isGoalEdit) {
                            TextButton(
                                onClick = {
                                    editingGoal = null
                                    goalTitle = ""
                                    goalTarget = ""
                                    goalCurrent = ""
                                    goalMonths = "12"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancelar Edição", color = MaterialTheme.colorScheme.error)
                            }
                        }

                        Divider()

                        Text(
                            text = "Metas Cadastradas",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )

                        val filteredGoals = goalsState.filter {
                            (goalSelectedFilter == "ALL" || it.category == goalSelectedFilter) &&
                            (goalSearchQuery.isEmpty() || it.title.contains(goalSearchQuery, ignoreCase = true))
                        }

                        if (filteredGoals.isEmpty()) {
                            Text("Nenhuma meta cadastrada ou correspondente.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            filteredGoals.forEach { g ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(g.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                IconButton(
                                                    modifier = Modifier.size(32.dp),
                                                    onClick = {
                                                        showContributeGoal = g
                                                        goalContributeValue = ""
                                                    }
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Contribuir", tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    modifier = Modifier.size(32.dp),
                                                    onClick = {
                                                        editingGoal = g
                                                        goalTitle = g.title
                                                        goalTarget = g.targetAmount.toString()
                                                        goalCurrent = g.currentAmount.toString()
                                                        goalMonths = "12"
                                                        goalCategory = g.category
                                                    }
                                                ) {
                                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    modifier = Modifier.size(32.dp),
                                                    onClick = { showDeleteConfirmGoal = g }
                                                ) {
                                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        val progress = (g.currentAmount / g.targetAmount).coerceIn(0.0..1.0).toFloat()
                                        LinearProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier.fillMaxWidth().height(6.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${viewModel.formatMoney(g.currentAmount)} / ${viewModel.formatMoney(g.targetAmount)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("${(progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showDeleteConfirmGoal != null) {
            val targetGoal = showDeleteConfirmGoal!!
            AlertDialog(
                onDismissRequest = { showDeleteConfirmGoal = null },
                title = { Text("Confirmar Exclusão de Meta", fontWeight = FontWeight.Bold) },
                text = { Text("Deseja realmente excluir permanentemente a meta '${targetGoal.title}'? Essa ação não pode ser desfeita.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteGoal(targetGoal)
                            showDeleteConfirmGoal = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmGoal = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showContributeGoal != null) {
            val targetGoal = showContributeGoal!!
            AlertDialog(
                onDismissRequest = { showContributeGoal = null },
                title = { Text("Contribuir para Meta", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Adicione saldo para atingir seu objetivo '${targetGoal.title}':")
                        OutlinedTextField(
                            value = goalContributeValue,
                            onValueChange = { goalContributeValue = it },
                            label = { Text("Valor (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val value = goalContributeValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (value > 0.0) {
                                viewModel.contributeToGoal(targetGoal.id, value)
                                showContributeGoal = null
                            }
                        }
                    ) {
                        Text("Contribuir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showContributeGoal = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardHeaderSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "DASHBOARD GERAL",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Olá, Igor Silva",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(48.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "IS",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
fun DashboardBalanceCard(
    viewModel: FinanceViewModel,
    netWorth: Double,
    totalAccountBalance: Double,
    totalInvestmentValue: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("balance_card"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(28.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SALDO TOTAL CONSOLIDADO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    letterSpacing = 1.5.sp
                )
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Trend up icon",
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = viewModel.getCurrencySymbol() + " ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
                val formattedRaw = viewModel.formatMoney(netWorth)
                val symbol = viewModel.getCurrencySymbol()
                val rawAmount = formattedRaw.replace(symbol, "").trim()
                Text(
                    text = rawAmount,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    letterSpacing = (-1).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Card 1: Bank Accounts
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "CONTAS BANCÁRIAS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.formatMoney(totalAccountBalance),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Card 2: Investments
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "INVESTIMENTOS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.formatMoney(totalInvestmentValue),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardQuickActions(onNavigateToChallenges: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Action 1: Pagar (Pay)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { /* Aesthetic shortcut */ }
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Payments,
                        contentDescription = "Pagar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Pagar",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action 2: Receber (Receive)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { /* Aesthetic shortcut */ }
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AddCard,
                        contentDescription = "Receber",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Receber",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action 3: Investir (Invest)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { /* Aesthetic shortcut */ }
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Investir",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Investir",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        // Action 4: Metas (Goals)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToChallenges() }
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = "Metas",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Metas",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun DashboardSummaryCard(
    viewModel: FinanceViewModel,
    totalIncome: Double,
    totalExpenses: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Incoming summary
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "RECEITAS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.formatMoney(totalIncome),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF0F9D58)
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { 0.85f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFF0F9D58),
                    trackColor = Color(0xFF0F9D58).copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Expenses summary
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "DESPESAS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = viewModel.formatMoney(totalExpenses),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFD93025)
                )
                Spacer(modifier = Modifier.height(10.dp))
                val safeIncome = totalIncome.coerceAtLeast(1.0)
                val expenseRatio = (totalExpenses / safeIncome).coerceIn(0.0, 1.0).toFloat()
                LinearProgressIndicator(
                    progress = { expenseRatio },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = Color(0xFFD93025),
                    trackColor = Color(0xFFD93025).copy(alpha = 0.1f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun DashboardHealthCard(viewModel: FinanceViewModel) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "IA",
                        color = MaterialTheme.colorScheme.background,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = "Saúde Financeira (Sugestão Inteligente)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = viewModel.getTotalFinancialHealthScore(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardChartCard(viewModel: FinanceViewModel) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Histórico de Fluxo de Caixa",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Últimos Dias",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Draw Interactive Chart on Canvas
            val linePoints = listOf(0.1f, 0.45f, 0.3f, 0.75f, 0.5f, 0.9f, 0.82f)
            val barPoints = listOf(0.5f, 0.7f, 0.4f, 0.85f, 0.6f, 0.55f, 0.95f)

            val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            val primaryColor = MaterialTheme.colorScheme.primary
            val successColor = Color(0xFF4CAF50)

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val width = size.width
                val height = size.height
                val pointsCount = linePoints.size
                val stepX = width / (pointsCount - 1)

                // Draw Grid lines
                for (i in 0..4) {
                    val y = height * (i / 4f)
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Draw Bar series representing incomes
                val barWidth = 16.dp.toPx()
                for (i in barPoints.indices) {
                    val x = stepX * i
                    val barHeight = height * barPoints[i]
                    drawRect(
                        color = successColor.copy(alpha = 0.25f),
                        topLeft = Offset(x - barWidth / 2, height - barHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                    )
                }

                // Draw Curved Line path representing expenses
                val path = Path()
                for (i in linePoints.indices) {
                    val x = stepX * i
                    val y = height - (height * linePoints[i])
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        val prevX = stepX * (i - 1)
                        val prevY = height - (height * linePoints[i - 1])
                        // Cubic bezier control points
                        path.cubicTo(
                            (prevX + x) / 2, prevY,
                            (prevX + x) / 2, y,
                            x, y
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 3.dp.toPx())
                )

                // Draw Point circular indicators
                for (i in linePoints.indices) {
                    val x = stepX * i
                    val y = height - (height * linePoints[i])
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val days = listOf("Seg", "Ter", "Qua", "Qui", "Sex", "Sab", "Dom")
                days.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(successColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Entradas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(16.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(primaryColor, RoundedCornerShape(4.dp))
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Saídas",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DashboardGoalsSection(
    viewModel: FinanceViewModel,
    goalsState: List<com.example.data.model.FinancialGoal>,
    onManageGoals: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Metas de Planejamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ver Todas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onManageGoals() }
            )
        }

        if (goalsState.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhuma meta cadastrada ainda.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            goalsState.take(2).forEach { goal ->
                val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0..1.0).toFloat()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.contributeToGoal(goal.id, 250.0)
                        },
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = goal.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = "Prazo: " + sdf.format(Date(goal.targetDateTimestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = if (progress >= 1f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = viewModel.formatMoney(goal.currentAmount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Meta de " + viewModel.formatMoney(goal.targetAmount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardPromoCard(onNavigateToChallenges: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToChallenges() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DESAFIO DE POUPANÇA 🏆",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Gostaria de poupar brincando?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Escolha um desafio clássico de 52 semanas ou crie o seu próprio de forma divertida!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            IconButton(
                onClick = onNavigateToChallenges,
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ir para desafios",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun DashboardNotesSection(
    notes: List<FinancialNote>
) {
    val pinnedNotes = notes.filter { it.isPinned }
    if (pinnedNotes.isEmpty()) return

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Lembretes & Planejamentos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        pinnedNotes.take(2).forEach { note ->
            val catColor = when (note.category) {
                "Planejamento" -> Color(0xFF4CAF50)
                "Lembrete" -> Color(0xFFFF9800)
                "Compras" -> Color(0xFFE91E63)
                "Ideias" -> Color(0xFF9C27B0)
                "Investimentos" -> Color(0xFF3F51B5)
                else -> MaterialTheme.colorScheme.secondary
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(
                            modifier = Modifier
                                .background(catColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = note.category,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = catColor
                            )
                        }
                    }
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
