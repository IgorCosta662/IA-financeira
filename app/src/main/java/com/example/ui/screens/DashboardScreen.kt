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
import com.example.data.model.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

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
    val creditCardsState by viewModel.creditCards.collectAsState()
    val billsToPayState by viewModel.billsToPay.collectAsState()
    val billsToReceiveState by viewModel.billsToReceive.collectAsState()
    val inventoryItemsState by viewModel.inventoryItems.collectAsState()

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

    // Global Search State
    var globalSearchQuery by remember { mutableStateOf("") }

    // Managers Dialogs Visibilities
    var showPagarManager by remember { mutableStateOf(false) }
    var showReceberManager by remember { mutableStateOf(false) }
    var showInventarioManager by remember { mutableStateOf(false) }

    // Sub-dialog flows for Add/Edit
    var editingBillToPay by remember { mutableStateOf<com.example.data.model.BillToPay?>(null) }
    var showAddBillToPayForm by remember { mutableStateOf(false) }
    
    var editingBillToReceive by remember { mutableStateOf<com.example.data.model.BillToReceive?>(null) }
    var showAddBillToReceiveForm by remember { mutableStateOf(false) }

    var editingInventoryItem by remember { mutableStateOf<com.example.data.model.InventoryItem?>(null) }
    var showAddInventoryItemForm by remember { mutableStateOf(false) }

    // Temp Form States for Bills to Pay
    var payName by remember { mutableStateOf("") }
    var payCreditor by remember { mutableStateOf("") }
    var payAmount by remember { mutableStateOf("") }
    var payDueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var payDebtDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var payPhone by remember { mutableStateOf("") }
    var payNotes by remember { mutableStateOf("") }
    var payStatus by remember { mutableStateOf("Pendente") }

    // Temp Form States for Bills to Receive
    var recDebtor by remember { mutableStateOf("") }
    var recAmount by remember { mutableStateOf("") }
    var recDueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var recLoanDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var recPhone by remember { mutableStateOf("") }
    var recNotes by remember { mutableStateOf("") }
    var recStatus by remember { mutableStateOf("Pendente") }

    // Temp Form States for Inventory Item
    var invItemName by remember { mutableStateOf("") }
    var invCategory by remember { mutableStateOf("Notebook") }
    var invValue by remember { mutableStateOf("") }
    var invPurchaseDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var invQuantity by remember { mutableStateOf("1") }
    var invNotes by remember { mutableStateOf("") }
    var invPhotoUri by remember { mutableStateOf("") }

    // Search and Filter variables inside managers
    var billPayQuery by remember { mutableStateOf("") }
    var billReceiveQuery by remember { mutableStateOf("") }
    var inventoryQuery by remember { mutableStateOf("") }
    var inventoryFilterCategory by remember { mutableStateOf("ALL") }

    // Calculations
    val totalAccountBalance = accountsState.sumOf { it.balance }
    val totalInvestmentValue = investmentsState.sumOf { it.quantity * it.currentPrice }
    val totalInventoryValue = inventoryItemsState.sumOf { it.estimatedValue * it.quantity }
    val totalPendingAReceber = billsToReceiveState.filter { it.status == "Pendente" }.sumOf { it.amount }
    val totalPendingAPagar = billsToPayState.filter { it.status == "Pendente" }.sumOf { it.amount }

    // Net value calculation
    val netWorth = totalAccountBalance + totalInvestmentValue + totalInventoryValue

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
                DashboardHeaderSection(viewModel = viewModel)
            }

            // Global Search Input
            item {
                OutlinedTextField(
                    value = globalSearchQuery,
                    onValueChange = { globalSearchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Pesquisa global (pessoas, dívidas, ativos...)") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    trailingIcon = {
                        if (globalSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { globalSearchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
            }

            if (globalSearchQuery.isNotEmpty()) {
                // Search Results list
                item {
                    DashboardSearchResults(
                        query = globalSearchQuery,
                        viewModel = viewModel,
                        onClose = { globalSearchQuery = "" }
                    )
                }
            } else {
                // Top consolidated metrics cards
                item {
                    DashboardTopMetricsWidget(
                        viewModel = viewModel,
                        saldoAtual = totalAccountBalance,
                        totalAReceber = totalPendingAReceber,
                        totalAPagar = totalPendingAPagar,
                        patrimonioTotal = netWorth,
                        investimentos = totalInvestmentValue,
                        limiteCartoes = creditCardsState.sumOf { it.limitAmount }
                    )
                }

                // Alerts widget
                item {
                    DashboardAlertsSection(
                        viewModel = viewModel,
                        billsToPay = billsToPayState,
                        billsToReceive = billsToReceiveState,
                        goals = goalsState
                    )
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
                                DashboardQuickActions(
                                    onNavigateToChallenges = onNavigateToChallenges,
                                    onNavigateToPagar = { showPagarManager = true },
                                    onNavigateToReceber = { showReceberManager = true },
                                    onNavigateToInventario = { showInventarioManager = true }
                                )
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
                        DashboardQuickActions(
                            onNavigateToChallenges = onNavigateToChallenges,
                            onNavigateToPagar = { showPagarManager = true },
                            onNavigateToReceber = { showReceberManager = true },
                            onNavigateToInventario = { showInventarioManager = true }
                        )
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

        // --- 1. RECEBER (Valores a Receber) MANAGER DIALOG ---
        val context = androidx.compose.ui.platform.LocalContext.current
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())

        if (showReceberManager) {
            AlertDialog(
                onDismissRequest = { showReceberManager = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contas e Valores a Receber", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showReceberManager = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val totalDebtorsCount = billsToReceiveState.filter { it.status == "Pendente" }.distinctBy { it.debtor }.size
                        val totalToReceiveSum = billsToReceiveState.filter { it.status == "Pendente" }.sumOf { it.amount }
                        val overdueReceiveCount = billsToReceiveState.filter { it.status == "Pendente" && it.dueDateTimestamp < System.currentTimeMillis() }.size

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Resumo de Cobranças", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Devedores Ativos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$totalDebtorsCount pessoa(s)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Total a Receber", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(viewModel.formatMoney(totalToReceiveSum), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF0F9D58))
                                    }
                                    Column {
                                        Text("Parcelas Atrasadas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$overdueReceiveCount pendentes", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = billReceiveQuery,
                                onValueChange = { billReceiveQuery = it },
                                placeholder = { Text("Buscar devedor...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    editingBillToReceive = null
                                    recDebtor = ""
                                    recAmount = ""
                                    recDueDate = System.currentTimeMillis()
                                    recLoanDate = System.currentTimeMillis()
                                    recPhone = ""
                                    recNotes = ""
                                    recStatus = "Pendente"
                                    showAddBillToReceiveForm = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo", fontSize = 12.sp)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        val tempRecQ = billReceiveQuery.lowercase(java.util.Locale.getDefault())
                        val filteredList = billsToReceiveState.filter {
                            it.debtor.lowercase().contains(tempRecQ) || it.notes.lowercase().contains(tempRecQ)
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Handshake, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nenhum devedor cadastrado.", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Adicione seu primeiro registro.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            filteredList.forEach { rec ->
                                val daysLeft = getDaysRemaining(rec.dueDateTimestamp)
                                val finalStatus = if (rec.status == "Recebido") "Recebido" else if (daysLeft < 0) "Atrasado" else "Pendente"
                                val statusColor = when (finalStatus) {
                                    "Recebido" -> Color(0xFF0F9D58)
                                    "Atrasado" -> Color(0xFFD93025)
                                    else -> Color(0xFF1A73E8)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(rec.debtor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                if (rec.phone.isNotBlank()) {
                                                    Text("📱 Tel: " + rec.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(finalStatus, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Vence: " + sdf.format(java.util.Date(rec.dueDateTimestamp)) + " (" + formatDaysRemainingText(daysLeft) + ")",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = viewModel.formatMoney(rec.amount),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = statusColor
                                            )
                                        }

                                        if (rec.notes.isNotBlank()) {
                                            Text("📝 Obs: " + rec.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (rec.status != "Recebido") {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.updateBillToReceive(rec.copy(status = "Recebido"))
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Done, contentDescription = "Receber", tint = Color(0xFF0F9D58))
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    editingBillToReceive = rec
                                                    recDebtor = rec.debtor
                                                    recAmount = rec.amount.toString()
                                                    recDueDate = rec.dueDateTimestamp
                                                    recLoanDate = rec.loanDateTimestamp
                                                    recPhone = rec.phone
                                                    recNotes = rec.notes
                                                    recStatus = rec.status
                                                    showAddBillToReceiveForm = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteBillToReceive(rec)
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showReceberManager = false }) {
                        Text("Voltar")
                    }
                }
            )
        }

        // --- SUB DIALOG: ADD/EDIT DEBTOR (RECEBER) ---
        if (showAddBillToReceiveForm) {
            val isEdit = editingBillToReceive != null
            AlertDialog(
                onDismissRequest = { showAddBillToReceiveForm = false },
                title = { Text(if (isEdit) "Editar Devedor" else "Novo Devedor", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = recDebtor,
                            onValueChange = { recDebtor = it },
                            label = { Text("Nome do Devedor/Empresa") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = recAmount,
                            onValueChange = { recAmount = it },
                            label = { Text("Valor (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = recPhone,
                            onValueChange = { recPhone = it },
                            label = { Text("Telefone (Opcional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Empréstimo: " + sdf.format(java.util.Date(recLoanDate)), fontSize = 13.sp)
                            TextButton(onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = recLoanDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        recLoanDate = cal.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vencimento: " + sdf.format(java.util.Date(recDueDate)), fontSize = 13.sp)
                            TextButton(onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = recDueDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        recDueDate = cal.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }

                        OutlinedTextField(
                            value = recNotes,
                            onValueChange = { recNotes = it },
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val valDeb = recAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (recDebtor.isNotBlank() && valDeb > 0.0) {
                                if (isEdit && editingBillToReceive != null) {
                                    viewModel.updateBillToReceive(
                                        editingBillToReceive!!.copy(
                                            debtor = recDebtor,
                                            amount = valDeb,
                                            dueDateTimestamp = recDueDate,
                                            loanDateTimestamp = recLoanDate,
                                            phone = recPhone,
                                            notes = recNotes,
                                            status = recStatus
                                        )
                                    )
                                } else {
                                    viewModel.addBillToReceive(
                                        debtor = recDebtor,
                                        amount = valDeb,
                                        dueDate = recDueDate,
                                        loanDate = recLoanDate,
                                        status = "Pendente",
                                        phone = recPhone,
                                        notes = recNotes
                                    )
                                }
                                showAddBillToReceiveForm = false
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBillToReceiveForm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- 2. PAGAR (Valores a Pagar) MANAGER DIALOG ---
        if (showPagarManager) {
            AlertDialog(
                onDismissRequest = { showPagarManager = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Contas e Dívidas a Pagar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showPagarManager = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val totalDebtsCount = billsToPayState.filter { it.status == "Pendente" }.distinctBy { it.creditor }.size
                        val totalToPaySum = billsToPayState.filter { it.status == "Pendente" }.sumOf { it.amount }
                        val overduePayCount = billsToPayState.filter { it.status == "Pendente" && it.dueDateTimestamp < System.currentTimeMillis() }.size

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Resumo de Dívidas", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Credores Registrados", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$totalDebtsCount credor(es)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Total a Pagar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(viewModel.formatMoney(totalToPaySum), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFD93025))
                                    }
                                    Column {
                                        Text("Contas Atrasadas", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$overduePayCount vencidas", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = billPayQuery,
                                onValueChange = { billPayQuery = it },
                                placeholder = { Text("Buscar credor...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    editingBillToPay = null
                                    payName = ""
                                    payCreditor = ""
                                    payAmount = ""
                                    payDueDate = System.currentTimeMillis()
                                    payDebtDate = System.currentTimeMillis()
                                    payPhone = ""
                                    payNotes = ""
                                    payStatus = "Pendente"
                                    showAddBillToPayForm = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo", fontSize = 12.sp)
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        val tempPayQ = billPayQuery.lowercase(java.util.Locale.getDefault())
                        val filteredList = billsToPayState.filter {
                            it.name.lowercase().contains(tempPayQ) || it.creditor.lowercase().contains(tempPayQ) || it.notes.lowercase().contains(tempPayQ)
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nenhuma conta cadastrada.", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Adicione seu primeiro registro.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            filteredList.forEach { bill ->
                                val daysLeft = getDaysRemaining(bill.dueDateTimestamp)
                                val finalStatus = if (bill.status == "Pago") "Pago" else if (daysLeft < 0) "Atrasado" else "Pendente"
                                val statusColor = when (finalStatus) {
                                    "Pago" -> Color(0xFF0F9D58)
                                    "Atrasado" -> Color(0xFFD93025)
                                    else -> Color(0xFFFF9800)
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(bill.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Text("Credor: " + bill.creditor, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                if (bill.phone.isNotBlank()) {
                                                    Text("📱 Tel: " + bill.phone, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(finalStatus, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = statusColor)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Vence: " + sdf.format(java.util.Date(bill.dueDateTimestamp)) + " (" + formatDaysRemainingText(daysLeft) + ")",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = viewModel.formatMoney(bill.amount),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 14.sp,
                                                color = statusColor
                                            )
                                        }

                                        if (bill.notes.isNotBlank()) {
                                            Text("📝 Obs: " + bill.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (bill.status != "Pago") {
                                                IconButton(
                                                    onClick = {
                                                        viewModel.updateBillToPay(bill.copy(status = "Pago"))
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Done, contentDescription = "Pagar", tint = Color(0xFF0F9D58))
                                                }
                                            }
                                            IconButton(
                                                onClick = {
                                                    editingBillToPay = bill
                                                    payName = bill.name
                                                    payCreditor = bill.creditor
                                                    payAmount = bill.amount.toString()
                                                    payDueDate = bill.dueDateTimestamp
                                                    payDebtDate = bill.debtDateTimestamp
                                                    payPhone = bill.phone
                                                    payNotes = bill.notes
                                                    payStatus = bill.status
                                                    showAddBillToPayForm = true
                                                }
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteBillToPay(bill)
                                                }
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPagarManager = false }) {
                        Text("Voltar")
                    }
                }
            )
        }

        // --- SUB DIALOG: ADD/EDIT BILL TO PAY (PAGAR) ---
        if (showAddBillToPayForm) {
            val isEdit = editingBillToPay != null
            AlertDialog(
                onDismissRequest = { showAddBillToPayForm = false },
                title = { Text(if (isEdit) "Editar Conta" else "Nova Conta a Pagar", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = payName,
                            onValueChange = { payName = it },
                            label = { Text("Nome da Conta") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = payCreditor,
                            onValueChange = { payCreditor = it },
                            label = { Text("Credor/Instituição") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = payAmount,
                            onValueChange = { payAmount = it },
                            label = { Text("Valor (R$)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = payPhone,
                            onValueChange = { payPhone = it },
                            label = { Text("Telefone (Opcional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Data Contraída: " + sdf.format(java.util.Date(payDebtDate)), fontSize = 13.sp)
                            TextButton(onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = payDebtDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        payDebtDate = cal.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vencimento: " + sdf.format(java.util.Date(payDueDate)), fontSize = 13.sp)
                            TextButton(onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = payDueDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        payDueDate = cal.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }

                        OutlinedTextField(
                            value = payNotes,
                            onValueChange = { payNotes = it },
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val valPay = payAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                            if (payName.isNotBlank() && payCreditor.isNotBlank() && valPay > 0.0) {
                                if (isEdit && editingBillToPay != null) {
                                    viewModel.updateBillToPay(
                                        editingBillToPay!!.copy(
                                            name = payName,
                                            creditor = payCreditor,
                                            amount = valPay,
                                            dueDateTimestamp = payDueDate,
                                            debtDateTimestamp = payDebtDate,
                                            phone = payPhone,
                                            notes = payNotes,
                                            status = payStatus
                                        )
                                    )
                                } else {
                                    viewModel.addBillToPay(
                                        name = payName,
                                        creditor = payCreditor,
                                        amount = valPay,
                                        dueDate = payDueDate,
                                        debtDate = payDebtDate,
                                        status = "Pendente",
                                        phone = payPhone,
                                        notes = payNotes
                                    )
                                }
                                showAddBillToPayForm = false
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddBillToPayForm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // --- 3. INVENTÁRIO (Controle Patrimonial) MANAGER DIALOG ---
        if (showInventarioManager) {
            AlertDialog(
                onDismissRequest = { showInventarioManager = false },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Controle do Patrimônio Físico", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = { showInventarioManager = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val totalVolItems = inventoryItemsState.sumOf { it.quantity }
                        val totalEstValue = inventoryItemsState.sumOf { it.estimatedValue * it.quantity }

                        val mainCategory = if (inventoryItemsState.isEmpty()) {
                            "Nenhum bem"
                        } else {
                            inventoryItemsState.groupBy { it.category }.maxByOrNull { it.value.sumOf { item -> item.quantity } }?.key ?: "Nenhum bem"
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Resumo do Patrimônio Ativo", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Quantidade Unidades", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("$totalVolItems item(ns)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                    Column {
                                        Text("Valor de Ativos", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(viewModel.formatMoney(totalEstValue), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                    }
                                    Column {
                                        Text("Categoria Líder", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(mainCategory, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = inventoryQuery,
                                onValueChange = { inventoryQuery = it },
                                placeholder = { Text("Buscar bem / item...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    editingInventoryItem = null
                                    invItemName = ""
                                    invCategory = "Notebook"
                                    invValue = ""
                                    invPurchaseDate = System.currentTimeMillis()
                                    invQuantity = "1"
                                    invNotes = ""
                                    invPhotoUri = ""
                                    showAddInventoryItemForm = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo", fontSize = 12.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL" to "Todos", "Notebook" to "Notebook", "Computador" to "Computador", "Celular" to "Celular", "Veículos" to "Veículos", "Móveis" to "Móveis", "Equipamentos" to "Equipamentos", "Coleções" to "Coleções", "Outros bens" to "Outros bens").forEach { (v, label) ->
                                FilterChip(
                                    selected = inventoryFilterCategory == v,
                                    onClick = { inventoryFilterCategory = v },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

                        val tempInvQ = inventoryQuery.lowercase(java.util.Locale.getDefault())
                        val filteredList = inventoryItemsState.filter {
                            (inventoryFilterCategory == "ALL" || it.category == inventoryFilterCategory) &&
                            (it.name.lowercase().contains(tempInvQ) || it.notes.lowercase().contains(tempInvQ))
                        }

                        if (filteredList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Nenhum item cadastrado.", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Adicione seu primeiro registro.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            filteredList.forEach { item ->
                                val defaultIcon = when (item.category) {
                                    "Notebook" -> Icons.Default.Laptop
                                    "Computador" -> Icons.Default.Computer
                                    "Celular" -> Icons.Default.Phone
                                    "Veículos" -> Icons.Default.DirectionsCar
                                    "Móveis" -> Icons.Default.Chair
                                    "Equipamentos" -> Icons.Default.Build
                                    "Coleções" -> Icons.Default.Category
                                    else -> Icons.Default.Inventory2
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(defaultIcon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
                                            }
                                        }

                                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                                Box(
                                                    modifier = Modifier
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                                ) {
                                                    Text(item.category, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Qtd: " + item.quantity + " • Compra: " + sdf.format(java.util.Date(item.purchaseDateTimestamp)), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                Text(viewModel.formatMoney(item.estimatedValue * item.quantity), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                            }

                                            if (item.notes.isNotBlank()) {
                                                Text("Obs: " + item.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        editingInventoryItem = item
                                                        invItemName = item.name
                                                        invCategory = item.category
                                                        invValue = item.estimatedValue.toString()
                                                        invPurchaseDate = item.purchaseDateTimestamp
                                                        invQuantity = item.quantity.toString()
                                                        invNotes = item.notes
                                                        invPhotoUri = item.photoUri
                                                        showAddInventoryItemForm = true
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteInventoryItem(item)
                                                    }
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showInventarioManager = false }) {
                        Text("Voltar")
                    }
                }
            )
        }

        // --- SUB DIALOG: ADD/EDIT INVENTORY ITEM ---
        if (showAddInventoryItemForm) {
            val isEdit = editingInventoryItem != null
            AlertDialog(
                onDismissRequest = { showAddInventoryItemForm = false },
                title = { Text(if (isEdit) "Editar Item Patrimonial" else "Novo Item no Patrimônio", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = invItemName,
                            onValueChange = { invItemName = it },
                            label = { Text("Nome do Item") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Categoria do ativo:", style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Notebook", "Computador", "Celular", "Veículos", "Móveis", "Equipamentos", "Coleções", "Outros bens").forEach { cat ->
                                ElevatedFilterChip(
                                    selected = invCategory == cat,
                                    onClick = { invCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = invValue,
                                onValueChange = { invValue = it },
                                label = { Text("Valor Estimado") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1.1f)
                            )
                            OutlinedTextField(
                                value = invQuantity,
                                onValueChange = { invQuantity = it },
                                label = { Text("Quantidade") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(0.9f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Adquirido em: " + sdf.format(java.util.Date(invPurchaseDate)), fontSize = 13.sp)
                            TextButton(onClick = {
                                val calendar = java.util.Calendar.getInstance().apply { timeInMillis = invPurchaseDate }
                                android.app.DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        val cal = java.util.Calendar.getInstance().apply { set(y, m, d) }
                                        invPurchaseDate = cal.timeInMillis
                                    },
                                    calendar.get(java.util.Calendar.YEAR),
                                    calendar.get(java.util.Calendar.MONTH),
                                    calendar.get(java.util.Calendar.DAY_OF_MONTH)
                                ).show()
                            }) {
                                Text("Alterar")
                            }
                        }

                        OutlinedTextField(
                            value = invNotes,
                            onValueChange = { invNotes = it },
                            label = { Text("Observações") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val valItem = invValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val valQty = invQuantity.toIntOrNull() ?: 1
                            if (invItemName.isNotBlank() && valItem > 0.0) {
                                if (isEdit && editingInventoryItem != null) {
                                    viewModel.updateInventoryItem(
                                        editingInventoryItem!!.copy(
                                            name = invItemName,
                                            category = invCategory,
                                            estimatedValue = valItem,
                                            purchaseDateTimestamp = invPurchaseDate,
                                            quantity = valQty,
                                            notes = invNotes,
                                            photoUri = invPhotoUri
                                        )
                                    )
                                } else {
                                    viewModel.addInventoryItem(
                                        name = invItemName,
                                        category = invCategory,
                                        estimatedValue = valItem,
                                        purchaseDate = invPurchaseDate,
                                        quantity = valQty,
                                        notes = invNotes,
                                        photoUri = invPhotoUri
                                    )
                                }
                                showAddInventoryItemForm = false
                            }
                        }
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddInventoryItemForm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun DashboardHeaderSection(viewModel: FinanceViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()

    val avatarColor = when (userAvatarId) {
        "avatar_1" -> Color(0xFF14B8A6) // Teal
        "avatar_2" -> Color(0xFF8B5CF6) // Purple
        "avatar_3" -> Color(0xFFF59E0B) // Amber
        "avatar_4" -> Color(0xFFEC4899) // Pink
        "avatar_5" -> Color(0xFF3B82F6) // Blue
        "avatar_6" -> Color(0xFF10B981) // Emerald
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when (userAvatarId) {
        "avatar_1" -> Icons.Default.Person
        "avatar_2" -> Icons.Default.Face
        "avatar_3" -> Icons.Default.AccountCircle
        "avatar_4" -> Icons.Default.Favorite
        "avatar_5" -> Icons.Default.Star
        "avatar_6" -> Icons.Default.Pets
        else -> Icons.Default.Person
    }

    val shortName = userName.split(" ").firstOrNull() ?: userName

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
                text = "Olá, $shortName!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        
        Surface(
            shape = CircleShape,
            color = avatarColor,
            modifier = Modifier.size(48.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.background)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = "Profile Avatar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
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
fun DashboardQuickActions(
    onNavigateToChallenges: () -> Unit,
    onNavigateToPagar: () -> Unit,
    onNavigateToReceber: () -> Unit,
    onNavigateToInventario: () -> Unit
) {
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
                .clickable { onNavigateToPagar() }
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
                .clickable { onNavigateToReceber() }
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

        // Action 3: Inventário (Control)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToInventario() }
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Inventory2,
                        contentDescription = "Inventário",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Inventário",
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

// --- SUPPORT HELPER AND SUB-COMPOSABLES ---

fun getDaysRemaining(dueDateTimestamp: Long): Long {
    val dueCal = java.util.Calendar.getInstance().apply {
        timeInMillis = dueDateTimestamp
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val todayCal = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 0)
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val diff = dueCal.timeInMillis - todayCal.timeInMillis
    return diff / (24 * 60 * 60 * 1000)
}

fun formatDaysRemainingText(daysLeft: Long): String {
    return when {
        daysLeft < 0 -> "vencida há ${-daysLeft}d"
        daysLeft == 0L -> "vence hoje!"
        daysLeft == 1L -> "vence amanhã"
        else -> "vence em ${daysLeft}d"
    }
}

@Composable
fun DashboardTopMetricsWidget(
    viewModel: FinanceViewModel,
    saldoAtual: Double,
    totalAReceber: Double,
    totalAPagar: Double,
    patrimonioTotal: Double,
    investimentos: Double,
    limiteCartoes: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Resumo Consolidado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Grid of 4 key metrics
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Patrimonio Líquido
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Patrimônio Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(viewModel.formatMoney(patrimonioTotal), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Metric 2: Saldo Disponível
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Contas e Dinheiro", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(viewModel.formatMoney(saldoAtual), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 3: A Receber
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total a Receber", style = MaterialTheme.typography.labelSmall, color = Color(0xFF137333), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(viewModel.formatMoney(totalAReceber), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF0F9D58))
                        }
                    }

                    // Metric 4: A Pagar
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total a Pagar", style = MaterialTheme.typography.labelSmall, color = Color(0xFFC5221F), fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(viewModel.formatMoney(totalAPagar), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFD93025))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardAlertsSection(
    viewModel: FinanceViewModel,
    billsToPay: List<com.example.data.model.BillToPay>,
    billsToReceive: List<com.example.data.model.BillToReceive>,
    goals: List<com.example.data.model.FinancialGoal>
) {
    val today = System.currentTimeMillis()
    val payTodayOrOverdue = billsToPay.filter { it.status == "Pendente" && (getDaysRemaining(it.dueDateTimestamp) <= 0) }
    val receiveOverdue = billsToReceive.filter { it.status == "Pendente" && (getDaysRemaining(it.dueDateTimestamp) < 0) }
    val approachingGoals = goals.filter {
        val progress = if (it.targetAmount > 0) it.currentAmount / it.targetAmount else 0.0
        progress in 0.8..0.99
    }

    if (payTodayOrOverdue.isEmpty() && receiveOverdue.isEmpty() && approachingGoals.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Alertas e Notificações Importantes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        payTodayOrOverdue.forEach { bill ->
            val daysLeft = getDaysRemaining(bill.dueDateTimestamp)
            val text = if (daysLeft == 0L) {
                "⚠️ Vence HOJE: \"${bill.name}\" no valor de ${viewModel.formatMoney(bill.amount)}"
            } else {
                "🚨 ATRASADA: \"${bill.name}\" com o credor ${bill.creditor} (${viewModel.formatMoney(bill.amount)})"
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE8E6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFC5221F))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFFC5221F))
                }
            }
        }

        receiveOverdue.forEach { rec ->
            val text = "💸 Atrasado: ${rec.debtor} te deve ${viewModel.formatMoney(rec.amount)} desde " +
                    java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(rec.dueDateTimestamp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = Color(0xFF1967D2))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF1967D2))
                }
            }
        }

        approachingGoals.forEach { goal ->
            val text = "🏆 Meta Próxima: \"${goal.title}\" está quase lá! Saldo atual é de ${viewModel.formatMoney(goal.currentAmount)} (Meta: ${viewModel.formatMoney(goal.targetAmount)})"
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.StarBorder, contentDescription = null, tint = Color(0xFF137333))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF202124))
                }
            }
        }
    }
}

@Composable
fun DashboardSearchResults(
    query: String,
    viewModel: FinanceViewModel,
    onClose: () -> Unit
) {
    if (query.isBlank()) return

    val creditCardsState by viewModel.creditCards.collectAsState(initial = emptyList())
    val billsToPayState by viewModel.billsToPay.collectAsState(initial = emptyList())
    val billsToReceiveState by viewModel.billsToReceive.collectAsState(initial = emptyList())
    val inventoryItemsState by viewModel.inventoryItems.collectAsState(initial = emptyList())
    val transactionsState by viewModel.transactions.collectAsState(initial = emptyList())
    val investmentsState by viewModel.investments.collectAsState(initial = emptyList())

    val q = query.lowercase(java.util.Locale.getDefault())

    // Filter results across all categories
    val matchedCards = creditCardsState.filter { it.name.lowercase().contains(q) || it.cardBrand.lowercase().contains(q) }
    val matchedPay = billsToPayState.filter { it.name.lowercase().contains(q) || it.creditor.lowercase().contains(q) || it.notes.lowercase().contains(q) }
    val matchedReceive = billsToReceiveState.filter { it.debtor.lowercase().contains(q) || it.notes.lowercase().contains(q) }
    val matchedInventory = inventoryItemsState.filter { it.name.lowercase().contains(q) || it.notes.lowercase().contains(q) }
    val matchedTransactions = transactionsState.filter { it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
    val matchedInvestments = investmentsState.filter { it.name.lowercase().contains(q) || it.category.lowercase().contains(q) }

    val totalMatches = matchedCards.size + matchedPay.size + matchedReceive.size + matchedInventory.size + matchedTransactions.size + matchedInvestments.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resultados da Busca ($totalMatches)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar Busca", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (totalMatches == 0) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Nenhum registro encontrado para \"$query\".", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Match Cards
                    if (matchedCards.isNotEmpty()) {
                        Text("Cartões de Crédito", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        matchedCards.forEach { card ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${card.name} (${card.cardBrand})", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text("Limite: ${viewModel.formatMoney(card.limitAmount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }

                    // Match Pay
                    if (matchedPay.isNotEmpty()) {
                        Text("Contas e Dívidas a Pagar", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFD93025))
                        matchedPay.forEach { p ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${p.name} (Credor: ${p.creditor})", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text(viewModel.formatMoney(p.amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }

                    // Match Receive
                    if (matchedReceive.isNotEmpty()) {
                        Text("Contas e Cobranças a Receber", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF0F9D58))
                        matchedReceive.forEach { r ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Devedor: ${r.debtor}", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text(viewModel.formatMoney(r.amount), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }

                    // Match Inventory
                    if (matchedInventory.isNotEmpty()) {
                        Text("Patrimônio Físico (Inventário)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        matchedInventory.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${item.name} (${item.category} x${item.quantity})", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text(viewModel.formatMoney(item.estimatedValue * item.quantity), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }

                    // Match Transactions
                    if (matchedTransactions.isNotEmpty()) {
                        Text("Transações", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        matchedTransactions.forEach { t ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${t.title} (${t.category})", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                val color = if (t.type == "INCOME") Color(0xFF0F9D58) else Color(0xFFD93025)
                                Text("${if (t.type == "INCOME") "+" else "-"} ${viewModel.formatMoney(t.amount)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = color)
                            }
                        }
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    }

                    // Match Investments
                    if (matchedInvestments.isNotEmpty()) {
                        Text("Investimentos", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                        matchedInvestments.forEach { inv ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${inv.name} (${inv.category})", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                                Text(viewModel.formatMoney(inv.currentPrice * inv.quantity), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}
