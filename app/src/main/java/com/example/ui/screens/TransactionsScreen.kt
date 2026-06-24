package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FinanceAccount
import com.example.data.model.FinanceTransaction
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(viewModel: FinanceViewModel) {
    val txsState by viewModel.transactions.collectAsState()
    val accountsState by viewModel.accounts.collectAsState()
    val cardsState by viewModel.creditCards.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilterType by remember { mutableStateOf("ALL") } // ALL, INCOME, EXPENSE
    var selectedCategoryFilter by remember { mutableStateOf("Tudo") }

    // Monthly Filter State
    val monthOptions = remember {
        val options = mutableListOf<Pair<String, Calendar?>>()
        options.add("Geral (Todo Período)" to null)

        val monthNames = arrayOf("Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez")

        for (i in -4..4) {
            val tempCal = Calendar.getInstance().apply {
                add(Calendar.MONTH, i)
            }
            val label = if (i == 0) "Este Mês (${monthNames[tempCal.get(Calendar.MONTH)]}/${tempCal.get(Calendar.YEAR).toString().takeLast(2)})"
                        else "${monthNames[tempCal.get(Calendar.MONTH)]}/${tempCal.get(Calendar.YEAR).toString().takeLast(2)}"
            options.add(label to tempCal)
        }
        options
    }

    val initialMonthIndex = remember {
        monthOptions.indexOfFirst { it.first.contains("Este Mês") }.coerceAtLeast(0)
    }
    var selectedMonthOptionIndex by remember { mutableStateOf(initialMonthIndex) }

    val isSameMonthYear = remember {
        { timestamp: Long, target: Calendar ->
            val txCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            txCal.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            txCal.get(Calendar.MONTH) == target.get(Calendar.MONTH)
        }
    }

    // Dialog state
    var txTitle by remember { mutableStateOf("") }
    var txAmount by remember { mutableStateOf("") }
    var txType by remember { mutableStateOf("EXPENSE") } // INCOME or EXPENSE
    var txCategory by remember { mutableStateOf("Alimentação") }
    var txSubcategory by remember { mutableStateOf("") }
    var txAccountId by remember { mutableStateOf(1) }
    var txIsRecurring by remember { mutableStateOf(false) }
    var txInstallments by remember { mutableStateOf("1") }
    var txCardId by remember { mutableStateOf<Int?>(null) }
    var txMockAttachedFile by remember { mutableStateOf<String?>(null) }
    var txIsPaid by remember { mutableStateOf(true) }

    // Populate categories dynamically based on type
    val incomeCategories = listOf("Salário", "Renda Extra", "Comissões", "Dividendos", "Outros")
    val expenseCategories = listOf("Alimentação", "Lazer", "Transporte", "Saúde", "Moradia", "Assinaturas", "Impostos", "Outros")
    val currentCategories = if (txType == "INCOME") incomeCategories else expenseCategories

    val selectedOption = monthOptions.getOrNull(selectedMonthOptionIndex)
    val targetMonthCal = selectedOption?.second

    // Run filters
    val filteredTransactions = txsState.filter {
        val matchesType = selectedFilterType == "ALL" || it.type == selectedFilterType
        val matchesCat = selectedCategoryFilter == "Tudo" || it.category == selectedCategoryFilter
        val matchesMonth = targetMonthCal == null || isSameMonthYear(it.dateTimestamp, targetMonthCal)
        matchesType && matchesCat && matchesMonth
    }

    val monthTxs = txsState.filter { targetMonthCal == null || isSameMonthYear(it.dateTimestamp, targetMonthCal) }
    val monthIncomes = monthTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
    val monthExpenses = monthTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val monthBalance = monthIncomes - monthExpenses

    Box(
        modifier = Modifier.fillMaxSize().testTag("transactions_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .padding(16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Text(
                    text = "HISTÓRICO DE MOVIMENTAÇÕES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Fluxo Financeiro",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Month Filter Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(monthOptions.size) { index ->
                    val pair = monthOptions[index]
                    val isSelected = selectedMonthOptionIndex == index
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedMonthOptionIndex = index },
                        label = { 
                            Text(
                                text = pair.first,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Monthly Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = if (targetMonthCal == null) "RESUMO CONSOLIDADO (GERAL)" else "Gasto e Resumo do Mês",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Total Income Block
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Entradas",
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Receitas",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = viewModel.formatMoney(monthIncomes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Total Expense Block
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "Saídas",
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Despesas (Gasto)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Text(
                                text = viewModel.formatMoney(monthExpenses),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF5350)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.08f))
                    )

                    // Balance Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Saldo Líquido",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = viewModel.formatMoney(monthBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (monthBalance >= 0.0) Color(0xFF4CAF50) else Color(0xFFEF5350)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Transaction Type filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "Todas", "INCOME" to "Entradas", "EXPENSE" to "Saídas").forEach { (type, label) ->
                    val isSelected = selectedFilterType == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterType = type },
                        label = { Text(label) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = "Checked", modifier = Modifier.size(16.dp)) }
                        } else null,
                        modifier = Modifier.testTag("filter_chip_$type")
                    )
                }
            }

            // Category list filter chips
            val allFilterCategories = listOf("Tudo") + (incomeCategories + expenseCategories).distinct()
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allFilterCategories) { category ->
                    val isSelected = selectedCategoryFilter == category
                    ElevatedAssistChip(
                        onClick = { selectedCategoryFilter = category },
                        label = { Text(category) },
                        colors = if (isSelected) {
                            AssistChipDefaults.elevatedAssistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else AssistChipDefaults.elevatedAssistChipColors()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Ledger List
            if (filteredTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = "Empty list",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhuma movimentação encontrada.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            accountName = accountsState.find { it.id == tx.accountId }?.name ?: "Conta Padrão",
                            onDelete = { viewModel.deleteTransaction(tx) },
                            viewModel = viewModel
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // height margin for fab and navigation
                    }
                }
            }
        }

        // Add Transaction Floating Action Button
        LargeFloatingActionButton(
            onClick = {
                // Pre-populate standard account if available
                if (accountsState.isNotEmpty()) {
                    txAccountId = accountsState.first().id
                }
                txCardId = null
                showAddDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 16.dp)
                .testTag("add_transaction_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Nova Transação", modifier = Modifier.size(30.dp))
        }

        // --- Add Transaction Dialog ---
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Registrar Movimentação", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Segmented control (Income / Expense)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { 
                                    txType = "INCOME" 
                                    txCategory = incomeCategories.first()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (txType == "INCOME") Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (txType == "INCOME") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.TrendingUp, "Receita")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Receita")
                            }

                            Button(
                                onClick = { 
                                    txType = "EXPENSE" 
                                    txCategory = expenseCategories.first()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (txType == "EXPENSE") Color(0xFFEF5350) else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (txType == "EXPENSE") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Icon(Icons.Default.TrendingDown, "Despesa")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Despesa")
                            }
                        }

                        // Main info inputs
                        OutlinedTextField(
                            value = txTitle,
                            onValueChange = { txTitle = it },
                            label = { Text("Título / Descrição") },
                            placeholder = { Text("Ex: Supermercado, Salário") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = "Descricao") },
                            modifier = Modifier.fillMaxWidth().testTag("add_tx_title_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = txAmount,
                            onValueChange = { txAmount = it },
                            label = { Text("Valor") },
                            placeholder = { Text("Ex: 154,20") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = "Valor") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("add_tx_amount_input"),
                            singleLine = true
                        )

                        // Selector for Category
                        Text("Categoria", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        var expandedCatDropdown by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(
                                onClick = { expandedCatDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(txCategory)
                                    Icon(Icons.Default.ArrowDropDown, "Dropdown")
                                }
                            }
                            DropdownMenu(
                                expanded = expandedCatDropdown,
                                onDismissRequest = { expandedCatDropdown = false }
                            ) {
                                currentCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            txCategory = cat
                                            expandedCatDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = txSubcategory,
                            onValueChange = { txSubcategory = it },
                            label = { Text("Subcategoria (Opcional)") },
                            placeholder = { Text("Ex: Sushi, Combustível, Uber") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Linked Account selector
                        Text("Conta de Origem", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        var expandedAccDropdown by remember { mutableStateOf(false) }
                        Box {
                            val activeAccountOpt = accountsState.find { it.id == txAccountId }
                            OutlinedButton(
                                onClick = { expandedAccDropdown = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activeAccountOpt?.name ?: "Selecione a Conta")
                                    Icon(Icons.Default.AccountBalance, "Dropdown")
                                }
                            }
                            DropdownMenu(
                                expanded = expandedAccDropdown,
                                onDismissRequest = { expandedAccDropdown = false }
                            ) {
                                accountsState.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text("${acc.name} - ${viewModel.formatMoney(acc.balance)}") },
                                        onClick = {
                                            txAccountId = acc.id
                                            expandedAccDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        // Credit Card selector (if Expense)
                        if (txType == "EXPENSE") {
                            Text("Pagar com Cartão de Crédito (Opcional)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            var expandedCardDropdown by remember { mutableStateOf(false) }
                            Box {
                                val activeCardOpt = cardsState.find { it.id == txCardId }
                                OutlinedButton(
                                    onClick = { expandedCardDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(activeCardOpt?.name ?: "Nenhum (Débito/Dinheiro)")
                                        Icon(Icons.Default.CreditCard, "Dropdown")
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedCardDropdown,
                                    onDismissRequest = { expandedCardDropdown = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Nenhum (Débito/Dinheiro)") },
                                        onClick = {
                                            txCardId = null
                                            expandedCardDropdown = false
                                        }
                                    )
                                    cardsState.forEach { card ->
                                        DropdownMenuItem(
                                            text = { Text(card.name) },
                                            onClick = {
                                                txCardId = card.id
                                                expandedCardDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Installments if Expense
                            OutlinedTextField(
                                value = txInstallments,
                                onValueChange = { txInstallments = it },
                                label = { Text("Número de Parcelas") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            val parsedAmount = txAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val parsedInst = txInstallments.toIntOrNull() ?: 1
                            if (parsedAmount > 0.0 && parsedInst > 1) {
                                val instVal = parsedAmount / parsedInst
                                val formattedInstVal = String.format(Locale.US, "%.2f", instVal).replace(".", ",")
                                Text(
                                    text = "A despesa será desdobrada em ${parsedInst} parcelas mensais de R$ $formattedInstVal cada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Paid / Received status checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { txIsPaid = !txIsPaid }
                        ) {
                            Checkbox(checked = txIsPaid, onCheckedChange = { txIsPaid = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (txType == "INCOME") "Já recebido" else "Já pago")
                        }

                        // Scheduled Recurrence checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { txIsRecurring = !txIsRecurring }
                        ) {
                            Checkbox(checked = txIsRecurring, onCheckedChange = { txIsRecurring = it })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agendamento Recorrente Mensal")
                        }

                        // Mock camera proof trigger
                        OutlinedButton(
                            onClick = {
                                // Simulate snapping receipt photo code
                                txMockAttachedFile = "comprovante_snap_" + System.currentTimeMillis() + ".jpg"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CameraAlt, "Anexar")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (txMockAttachedFile != null) "Comprovante Anexado ✓" else "Anexar Foto do Comprovante")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = txAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                            val inst = if (txType == "EXPENSE") (txInstallments.toIntOrNull() ?: 1) else 1
                            if (txTitle.isNotEmpty() && amt > 0.0) {
                                viewModel.addTransaction(
                                    title = txTitle,
                                    amount = amt,
                                    type = txType,
                                    category = txCategory,
                                    sub = txSubcategory,
                                    accId = txAccountId,
                                    isRec = txIsRecurring,
                                    totalInst = inst,
                                    cardId = txCardId,
                                    receiptImg = txMockAttachedFile,
                                    isPaid = txIsPaid
                                )
                                // Reset and dismiss
                                txTitle = ""
                                txAmount = ""
                                txSubcategory = ""
                                txIsRecurring = false
                                txInstallments = "1"
                                txCardId = null
                                txMockAttachedFile = null
                                txIsPaid = true
                                showAddDialog = false
                            }
                        },
                        modifier = Modifier.testTag("save_transaction_button")
                    ) {
                        Text("Salvar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun TransactionItemRow(
    transaction: FinanceTransaction,
    accountName: String,
    onDelete: () -> Unit,
    viewModel: FinanceViewModel
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val categoryIcon = when (transaction.category) {
        "Salário" -> Icons.Default.Work
        "Renda Extra" -> Icons.Default.AddCard
        "Comissões" -> Icons.Default.Percent
        "Dividendos" -> Icons.Default.Analytics
        "Alimentação" -> Icons.Default.Restaurant
        "Lazer" -> Icons.Default.SportsEsports
        "Transporte" -> Icons.Default.DirectionsCar
        "Saúde" -> Icons.Default.LocalHospital
        "Moradia" -> Icons.Default.Home
        "Assinaturas" -> Icons.Default.Subscriptions
        "Impostos" -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.AccountBalanceWallet
    }

    val iconBgColor = if (transaction.type == "INCOME") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if (transaction.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFEF5350)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDeleteConfirm = true },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Checkbox to mark as paid / unpaid
                Checkbox(
                    checked = transaction.isPaid,
                    onCheckedChange = { viewModel.toggleTransactionPaid(transaction) },
                    modifier = Modifier.padding(end = 4.dp).testTag("tx_paid_checkbox_${transaction.id}")
                )

                // Category badge icon
                Surface(
                    shape = CircleShape,
                    color = iconBgColor,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = "Category Icon",
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (transaction.subcategory.isNotEmpty()) {
                            Text(
                                text = "• " + transaction.subcategory,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = accountName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )

                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(
                            text = sdf.format(Date(transaction.dateTimestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Amount, recurring indicators, attachments
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val sign = if (transaction.type == "INCOME") "+" else "-"
                val prefixColor = if (transaction.type == "INCOME") Color(0xFF4CAF50) else Color(0xFFEF5350)
                
                Text(
                    text = "$sign${viewModel.formatMoney(transaction.amount)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = prefixColor
                )

                // Sub-labels indicating recurring/installments/receipts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (transaction.totalInstallments > 1) {
                        Badge(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("Parc ${transaction.currentInstallment}/${transaction.totalInstallments}", fontSize = 10.sp)
                        }
                    }
                    if (transaction.isRecurring) {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = "Recurrent",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    if (transaction.receiptImgUri != null) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Receipt attached",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }

    // --- Delete confirmation dialog ---
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Excluir Movimentação") },
            text = { Text("Tem certeza que deseja apagar permanentemente '${transaction.title}'? O saldo da conta vinculada será estornado automaticamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
