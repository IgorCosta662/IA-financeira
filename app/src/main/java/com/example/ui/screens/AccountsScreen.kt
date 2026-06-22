package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: FinanceViewModel) {
    val accountsState by viewModel.accounts.collectAsState()
    val cardsState by viewModel.creditCards.collectAsState()
    val txsState by viewModel.transactions.collectAsState()
    val billsToPayState by viewModel.billsToPay.collectAsState()
    val billsToReceiveState by viewModel.billsToReceive.collectAsState()

    // Active sub-tab state (0 = Bancos & Cartões, 1 = Pagar, 2 = Receber)
    var activeSubTab by remember { mutableStateOf(0) }

    // Date formatter
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Helpers to classify status dynamically if overdue
    fun getBillStatus(bill: BillToPay): String {
        if (bill.status == "Pago") return "Pago"
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return if (bill.dueDateTimestamp < todayStart) "Atrasado" else "Pendente"
    }

    fun getReceiveStatus(rec: BillToReceive): String {
        if (rec.status == "Recebido") return "Recebido"
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return if (rec.dueDateTimestamp < todayStart) "Atrasado" else "Pendente"
    }

    // High-level Calculations
    val totalAccountBalance = accountsState.sumOf { it.balance }
    val outstandingBillsToPay = billsToPayState.filter { getBillStatus(it) != "Pago" }.sumOf { it.amount }
    val outstandingBillsToReceive = billsToReceiveState.filter { getReceiveStatus(it) != "Recebido" }.sumOf { it.amount }
    val predictedBalance = totalAccountBalance + outstandingBillsToReceive - outstandingBillsToPay

    // Dialog visibilities
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<FinanceAccount?>(null) }
    var showDeleteConfirmAccount by remember { mutableStateOf<FinanceAccount?>(null) }
    var showAddCardDialog by remember { mutableStateOf(false) }
    var showAddPayDialog by remember { mutableStateOf(false) }
    var showAddReceiveDialog by remember { mutableStateOf(false) }

    // Selected objects for editing
    var editingBillToPay by remember { mutableStateOf<BillToPay?>(null) }
    var editingBillToReceive by remember { mutableStateOf<BillToReceive?>(null) }
    var selectedCardForDetails by remember { mutableStateOf<CreditCard?>(null) }
    var editingCardPurchase by remember { mutableStateOf<FinanceTransaction?>(null) }
    var showAddCardPurchaseDialog by remember { mutableStateOf<CreditCard?>(null) }

    // Form Temporary States
    // Bank accounts
    var accName by remember { mutableStateOf("") }
    var accType by remember { mutableStateOf("CHECKING") }
    var accBalance by remember { mutableStateOf("") }
    var accColorHex by remember { mutableStateOf("#3F51B5") }
    var accBankName by remember { mutableStateOf("") }
    var accAgency by remember { mutableStateOf("") }
    var accNumber by remember { mutableStateOf("") }

    // Credit cards
    var cardName by remember { mutableStateOf("") }
    var cardLimit by remember { mutableStateOf("") }
    var cardClosingDay by remember { mutableStateOf("5") }
    var cardDueDay by remember { mutableStateOf("15") }
    var cardBrand by remember { mutableStateOf("Visa") }

    // Bills to pay form
    var payName by remember { mutableStateOf("") }
    var payCreditor by remember { mutableStateOf("") }
    var payAmount by remember { mutableStateOf("") }
    var payDueDateStr by remember { mutableStateOf("") }
    var payStatusSelection by remember { mutableStateOf("Pendente") }
    var payNotes by remember { mutableStateOf("") }

    // Bills to receive form
    var recDebtor by remember { mutableStateOf("") }
    var recAmount by remember { mutableStateOf("") }
    var recDueDateStr by remember { mutableStateOf("") }
    var recStatusSelection by remember { mutableStateOf("Pendente") }
    var recPhone by remember { mutableStateOf("") }
    var recNotes by remember { mutableStateOf("") }

    // Card edit form inside settings
    var detailsCardLimit by remember { mutableStateOf("") }
    var detailsCardClosingDay by remember { mutableStateOf("") }
    var detailsCardDueDay by remember { mutableStateOf("") }
    var detailsCardBrand by remember { mutableStateOf("") }

    // Card transaction form
    var cardTxName by remember { mutableStateOf("") }
    var cardTxCategory by remember { mutableStateOf("Lazer") }
    var cardTxAmount by remember { mutableStateOf("") }

    // Simulator states
    var simValue by remember { mutableStateOf("") }
    var simInstallments by remember { mutableStateOf("1") }
    var simResultText by remember { mutableStateOf("") }

    // Local filters and queries
    var paySearchQuery by remember { mutableStateOf("") }
    var payStatusFilter by remember { mutableStateOf("Todos") } // Todos, Pendente, Pago, Atrasado
    var recSearchQuery by remember { mutableStateOf("") }
    var recStatusFilter by remember { mutableStateOf("Todos") } // Todos, Pendente, Recebido, Atrasado

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- 1. TOP SUMMARY CARD (Unified) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "RESUMO ESTIMADO DO MÊS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.5.sp
                )

                // 3 Column Grid with stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Total a receber
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "A Receber",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatMoney(outstandingBillsToReceive),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }

                    // Total a pagar
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "A Pagar",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatMoney(outstandingBillsToPay),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }

                    // Predicted final balance
                    Column(modifier = Modifier.weight(1.2f), horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Saldo Previsto",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = viewModel.formatMoney(predictedBalance),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- 2. MATERIAL DESIGN 3 NAVIGATION TABS ---
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Contas & Cartões", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.CreditCard, contentDescription = "Bancos e Cartões") },
                modifier = Modifier.testTag("tab_banks_cards")
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Pagar", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Contas a Pagar") },
                modifier = Modifier.testTag("tab_bills_to_pay")
            )
            Tab(
                selected = activeSubTab == 2,
                onClick = { activeSubTab = 2 },
                text = { Text("Receber", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.Handshake, contentDescription = "Contas a Receber") },
                modifier = Modifier.testTag("tab_bills_to_receive")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 3. SUB-TAB VIEW CONTENT ---
        AnimatedContent(
            targetState = activeSubTab,
            label = "tab_transition",
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { targetTab ->
            when (targetTab) {
                0 -> {
                    // TAB 0: Banks & Cards
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Title: Bank accounts
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Saldos Bancários",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { showAddAccountDialog = true },
                                    modifier = Modifier.testTag("btn_add_account")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Nova Conta")
                                }
                            }
                        }

                        // Horizontal Bank Account List
                        item {
                            if (accountsState.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(24.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Nenhuma conta bancária cadastrada.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            } else {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(accountsState) { acc ->
                                        val color = try {
                                            Color(android.graphics.Color.parseColor(acc.colorHex))
                                        } catch (e: Exception) {
                                            MaterialTheme.colorScheme.primary
                                        }

                                        Card(
                                            modifier = Modifier
                                                .width(180.dp)
                                                .height(110.dp)
                                                .clickable {
                                                     editingAccount = acc
                                                     accName = acc.name
                                                     accType = acc.type
                                                     accBalance = acc.balance.toString()
                                                     accColorHex = acc.colorHex
                                                     accBankName = acc.bankName
                                                     accAgency = acc.agency
                                                     accNumber = acc.accountNumber
                                                },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = color)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(14.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val accIcon = when (acc.type) {
                                                        "SAVINGS" -> Icons.Default.Savings
                                                        "CASH" -> Icons.Default.Wallet
                                                        "INVESTMENT" -> Icons.Default.ShowChart
                                                        else -> Icons.Default.AccountBalance
                                                    }
                                                    Icon(accIcon, contentDescription = acc.type, tint = Color.White, modifier = Modifier.size(18.dp))
                                                    Text(
                                                        text = when (acc.type) {
                                                            "CHECKING" -> "Corrente"
                                                            "SAVINGS" -> "Poupança"
                                                            "CASH" -> "Carteira"
                                                            "INVESTMENT" -> "Investimentos"
                                                            else -> "Corrente"
                                                        },
                                                        fontSize = 10.sp,
                                                        color = Color.White.copy(alpha = 0.8f)
                                                    )
                                                }

                                                Column {
                                                    Text(
                                                        text = acc.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = viewModel.formatMoney(acc.balance),
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Title: Credit cards
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Cartões de Crédito",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(
                                    onClick = { showAddCardDialog = true },
                                    modifier = Modifier.testTag("btn_add_card")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Novo Cartão")
                                }
                            }
                        }

                        // Credit Card List
                        if (cardsState.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(24.dp)
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("Nenhum cartão de crédito cadastrado.", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        } else {
                            items(cardsState) { card ->
                                val cardColor = try {
                                    Color(android.graphics.Color.parseColor(card.colorHex))
                                } catch (e: Exception) {
                                    Color(0xFF2A2A2A)
                                }

                                val invoiceTotal = txsState.filter { it.creditCardId == card.id }.sumOf { it.amount }
                                val availableLimit = (card.limitAmount - invoiceTotal).coerceAtLeast(0.0)
                                val utilizationPercent = (if (card.limitAmount > 0) invoiceTotal / card.limitAmount else 0.0).coerceIn(0.0..1.0).toFloat()

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCardForDetails = card
                                            detailsCardLimit = card.limitAmount.toString()
                                            detailsCardClosingDay = card.closingDay.toString()
                                            detailsCardDueDay = card.dueDay.toString()
                                            detailsCardBrand = card.cardBrand
                                        },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardColor)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = card.name,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = card.cardBrand,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Nfc,
                                                    contentDescription = "Contactless",
                                                    tint = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Icon(
                                                    imageVector = Icons.Default.Settings,
                                                    contentDescription = "Configurar",
                                                    tint = Color.White.copy(alpha = 0.8f),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Fatura Atual",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = viewModel.formatMoney(invoiceTotal),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "Limite Disponível",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.6f)
                                                )
                                                Text(
                                                    text = viewModel.formatMoney(availableLimit),
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF81C784)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        LinearProgressIndicator(
                                            progress = { utilizationPercent },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp),
                                            color = if (utilizationPercent > 0.8f) Color(0xFFEF5350) else Color(0xFF81C784),
                                            trackColor = Color.White.copy(alpha = 0.2f)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Fechamento: Dia ${card.closingDay}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "Vencimento: Dia ${card.dueDay}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Simulator Module
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Campaign, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Simulador de Compras Futuras",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "Simule o impacto de novas compras parceladas em seu orçamento no longo prazo.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = simValue,
                                        onValueChange = { simValue = it },
                                        label = { Text("Valor Total") },
                                        placeholder = { Text("Ex: 1500") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = simInstallments,
                                        onValueChange = { simInstallments = it },
                                        label = { Text("Parcelas desejadas") },
                                        placeholder = { Text("Ex: 10") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Button(
                                        onClick = {
                                            val valSum = simValue.replace(",", ".").toDoubleOrNull() ?: 0.0
                                            val inst = simInstallments.toIntOrNull() ?: 1
                                            if (valSum > 0.0) {
                                                val monthly = valSum / inst
                                                simResultText = "Impacto Estimado:\nVocê pagará ${inst}x de ${viewModel.formatMoney(monthly)}.\nIsso comprometerá mais ${String.format("%.1f", (monthly / 4500.0) * 100)}% de uma receita média mensal padrão de R$ 4.500,00."
                                            } else {
                                                simResultText = "Por favor, digite um valor válido superior a zero."
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Simular Impacto")
                                    }

                                    if (simResultText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = simResultText,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 1: Bills To Pay (As dívidas)
                    val filteredBills = remember(billsToPayState, paySearchQuery, payStatusFilter) {
                        billsToPayState.filter { bill ->
                            val matchesSearch = bill.name.contains(paySearchQuery, ignoreCase = true) ||
                                    bill.creditor.contains(paySearchQuery, ignoreCase = true)

                            val billStatus = getBillStatus(bill)
                            val matchesStatus = payStatusFilter == "Todos" || billStatus == payStatusFilter

                            matchesSearch && matchesStatus
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Title + button to add
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contas a Pagar",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddPayDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_add_bill_to_pay")
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Nova Conta")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search and Filters
                        OutlinedTextField(
                            value = paySearchQuery,
                            onValueChange = { paySearchQuery = it },
                            label = { Text("Pesquisar contas...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status Filter Chips
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Todos", "Pendente", "Pago", "Atrasado").forEach { statusLabel ->
                                val count = when (statusLabel) {
                                    "Todos" -> billsToPayState.size
                                    "Pago" -> billsToPayState.count { getBillStatus(it) == "Pago" }
                                    "Pendente" -> billsToPayState.count { getBillStatus(it) == "Pendente" }
                                    "Atrasado" -> billsToPayState.count { getBillStatus(it) == "Atrasado" }
                                    else -> 0
                                }

                                FilterChip(
                                    selected = payStatusFilter == statusLabel,
                                    onClick = { payStatusFilter = statusLabel },
                                    label = { Text("$statusLabel ($count)") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredBills.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhuma conta encontrada com os filtros selecionados.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredBills) { bill ->
                                    val bStatus = getBillStatus(bill)
                                    val isOverdue = bStatus == "Atrasado"
                                    val isPaid = bStatus == "Pago"

                                    // Color scheme based on status
                                    val statusColor = when {
                                        isPaid -> Color(0xFF4CAF50)
                                        isOverdue -> Color(0xFFF44336)
                                        else -> Color(0xFFFF9800)
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        ),
                                        border = if (isOverdue) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF44336).copy(alpha = 0.6f)) else null
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isPaid) Icons.Default.CheckCircle else Icons.Default.ReceiptLong,
                                                    contentDescription = null,
                                                    tint = statusColor,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = bill.name,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Credor: ${bill.creditor}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = "Vence em: ${dateFormatter.format(Date(bill.dueDateTimestamp))}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isOverdue) Color(0xFFF44336) else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = viewModel.formatMoney(bill.amount),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    // Mark as paid quick action
                                                    if (!isPaid) {
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.updateBillToPay(bill.copy(status = "Pago"))
                                                            },
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape)
                                                        ) {
                                                            Icon(Icons.Default.Check, "Marcar como Pago", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                                        }
                                                    }

                                                    // Edit button
                                                    IconButton(
                                                        onClick = {
                                                            editingBillToPay = bill
                                                            payName = bill.name
                                                            payCreditor = bill.creditor
                                                            payAmount = bill.amount.toString()
                                                            payDueDateStr = dateFormatter.format(Date(bill.dueDateTimestamp))
                                                            payStatusSelection = bill.status
                                                            payNotes = bill.notes
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    }

                                                    // Delete button
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteBillToPay(bill)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: Bills To Receive (A receber)
                    val filteredReceives = remember(billsToReceiveState, recSearchQuery, recStatusFilter) {
                        billsToReceiveState.filter { rec ->
                            val matchesSearch = rec.debtor.contains(recSearchQuery, ignoreCase = true)
                            val recStatus = getReceiveStatus(rec)
                            val matchesStatus = recStatusFilter == "Todos" || recStatus == recStatusFilter
                            matchesSearch && matchesStatus
                        }
                    }

                    val overdueCount = billsToReceiveState.count { getReceiveStatus(it) == "Atrasado" }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        // Header with warn alert
                        if (overdueCount > 0) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = "Alerta", tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Atenção: Você possui $overdueCount recebimento(s) em atraso! Envie um lembrete.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        // Title + button to add
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Contas a Receber",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = { showAddReceiveDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("btn_add_bill_to_receive")
                            ) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Novo Registro")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search and Filters
                        OutlinedTextField(
                            value = recSearchQuery,
                            onValueChange = { recSearchQuery = it },
                            label = { Text("Pesquisar parceiro/devedor...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Status filter chips
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Todos", "Pendente", "Recebido", "Atrasado").forEach { statusLabel ->
                                val count = when (statusLabel) {
                                    "Todos" -> billsToReceiveState.size
                                    "Recebido" -> billsToReceiveState.count { getReceiveStatus(it) == "Recebido" }
                                    "Pendente" -> billsToReceiveState.count { getReceiveStatus(it) == "Pendente" }
                                    "Atrasado" -> billsToReceiveState.count { getReceiveStatus(it) == "Atrasado" }
                                    else -> 0
                                }

                                FilterChip(
                                    selected = recStatusFilter == statusLabel,
                                    onClick = { recStatusFilter = statusLabel },
                                    label = { Text("$statusLabel ($count)") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (filteredReceives.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum recebimento registrado ou encontrado.")
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredReceives) { rec ->
                                    val rStatus = getReceiveStatus(rec)
                                    val isOverdue = rStatus == "Atrasado"
                                    val isReceived = rStatus == "Recebido"

                                    val statusColor = when {
                                        isReceived -> Color(0xFF4CAF50)
                                        isOverdue -> Color(0xFFF44336)
                                        else -> Color(0xFFFF9800)
                                    }

                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(
                                                    imageVector = if (isReceived) Icons.Default.CheckCircle else Icons.Default.Handshake,
                                                    contentDescription = null,
                                                    tint = statusColor,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = rec.debtor,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = "Previsão: ${dateFormatter.format(Date(rec.dueDateTimestamp))}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Text(
                                                        text = if (isOverdue) "Atrasado" else "Pendente",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = statusColor
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = viewModel.formatMoney(rec.amount),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = statusColor
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    // Quick mark as received
                                                    if (!isReceived) {
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.updateBillToReceive(rec.copy(status = "Recebido"))
                                                            },
                                                            modifier = Modifier
                                                                .size(28.dp)
                                                                .background(Color(0xFF4CAF50).copy(alpha = 0.15f), CircleShape)
                                                        ) {
                                                            Icon(Icons.Default.Check, "Recebido", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                                        }
                                                    }

                                                    // Edit button
                                                    IconButton(
                                                        onClick = {
                                                            editingBillToReceive = rec
                                                            recDebtor = rec.debtor
                                                            recAmount = rec.amount.toString()
                                                            recDueDateStr = dateFormatter.format(Date(rec.dueDateTimestamp))
                                                            recStatusSelection = rec.status
                                                            recPhone = rec.phone
                                                            recNotes = rec.notes
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, "Editar", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                    }

                                                    // Delete button
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteBillToReceive(rec)
                                                        },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS SECTION ---

    // 1. ADD BANK ACCOUNT DIALOG
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Nova Conta Bancária", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Bancos Populares (Toque para preencher)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("Nubank", "#8E24AA", "💜"),
                            Triple("Itaú", "#FF5722", "🍊"),
                            Triple("Bradesco", "#E91E63", "❤️"),
                            Triple("Santander", "#F44336", "🌹"),
                            Triple("Inter", "#FF9800", "🧡"),
                            Triple("C6 Bank", "#111111", "🖤"),
                            Triple("Banco do Brasil", "#0D47A1", "💛"),
                            Triple("Caixa", "#1976D2", "💙")
                        ).forEach { (name, color, emoji) ->
                            FilterChip(
                                selected = accName == name,
                                onClick = {
                                    accName = name
                                    accColorHex = color
                                    accType = "CHECKING"
                                },
                                label = { Text("$emoji $name") }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Nome da Conta (Ex: Minha Conta)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accBankName,
                        onValueChange = { accBankName = it },
                        label = { Text("Nome do Banco (Ex: Nubank)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accAgency,
                            onValueChange = { accAgency = it },
                            label = { Text("Agência") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = accNumber,
                            onValueChange = { accNumber = it },
                            label = { Text("Número da Conta") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = accBalance,
                        onValueChange = { accBalance = it },
                        label = { Text("Saldo Inicial ou Atual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo de Conta", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "CHECKING" to "Corrente",
                            "SAVINGS" to "Poupança",
                            "CASH" to "Bolso/Dinheiro",
                            "INVESTMENT" to "Investimentos"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = accType == type,
                                onClick = { accType = type },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Cor de Exibição", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("#8E24AA", "#FF5722", "#E91E63", "#F44336", "#FF9800", "#111111", "#0D47A1", "#1976D2", "#4CAF50").forEach { colorStr ->
                            val color = Color(android.graphics.Color.parseColor(colorStr))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(color, CircleShape)
                                    .clickable { accColorHex = colorStr }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (accColorHex == colorStr) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = accBalance.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (accName.isNotBlank()) {
                            viewModel.addAccount(accName, accType, bal, accColorHex, accBankName, accAgency, accNumber)
                            accName = ""
                            accBankName = ""
                            accAgency = ""
                            accNumber = ""
                            accBalance = ""
                            showAddAccountDialog = false
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 1b. EDIT BANK ACCOUNT DIALOG
    if (editingAccount != null) {
        val targetAcc = editingAccount!!
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            title = { Text("Editar Conta Bancária", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Nome da Conta (Ex: Minha Conta)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accBankName,
                        onValueChange = { accBankName = it },
                        label = { Text("Nome do Banco (Ex: Nubank)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = accAgency,
                            onValueChange = { accAgency = it },
                            label = { Text("Agência") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = accNumber,
                            onValueChange = { accNumber = it },
                            label = { Text("Número da Conta") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = accBalance,
                        onValueChange = { accBalance = it },
                        label = { Text("Saldo Inicial ou Atual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo de Conta", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "CHECKING" to "Corrente",
                            "SAVINGS" to "Poupança",
                            "CASH" to "Bolso/Dinheiro",
                            "INVESTMENT" to "Investimentos"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = accType == type,
                                onClick = { accType = type },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text("Cor de Exibição", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("#8E24AA", "#FF5722", "#E91E63", "#F44336", "#FF9800", "#111111", "#0D47A1", "#1976D2", "#4CAF50").forEach { colorStr ->
                            val color = Color(android.graphics.Color.parseColor(colorStr))
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(color, CircleShape)
                                    .clickable { accColorHex = colorStr }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (accColorHex == colorStr) {
                                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val bal = accBalance.replace(",", ".").toDoubleOrNull() ?: targetAcc.balance
                            if (accName.isNotBlank()) {
                                viewModel.updateAccount(
                                    targetAcc.copy(
                                        name = accName,
                                        type = accType,
                                        balance = bal,
                                        colorHex = accColorHex,
                                        bankName = accBankName,
                                        agency = accAgency,
                                        accountNumber = accNumber
                                    )
                                )
                                editingAccount = null
                            }
                        }
                    ) {
                        Text("Atualizar")
                    }

                    Button(
                        onClick = {
                            showDeleteConfirmAccount = targetAcc
                            editingAccount = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAccount = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteConfirmAccount != null) {
        val targetAcc = showDeleteConfirmAccount!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmAccount = null },
            title = { Text("Confirmar Exclusão de Conta", fontWeight = FontWeight.Bold) },
            text = { Text("Deseja realmente excluir permanentemente a conta '${targetAcc.name}'? Ao excluir, o saldo correspondente sairá de sua carteira.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(targetAcc)
                        showDeleteConfirmAccount = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmAccount = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 2. ADD CREDIT CARD DIALOG
    if (showAddCardDialog) {
        AlertDialog(
            onDismissRequest = { showAddCardDialog = false },
            title = { Text("Novo Cartão de Crédito", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardName,
                        onValueChange = { cardName = it },
                        label = { Text("Nome do Cartão") },
                        placeholder = { Text("Ex: Nubank Ultra") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardLimit,
                        onValueChange = { cardLimit = it },
                        label = { Text("Limite Total") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cardClosingDay,
                            onValueChange = { cardClosingDay = it },
                            label = { Text("Fechamento (Dia)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = cardDueDay,
                            onValueChange = { cardDueDay = it },
                            label = { Text("Vencimento (Dia)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Text("Bandeira", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Visa", "Mastercard", "Elo", "Amex").forEach { brand ->
                            FilterChip(
                                selected = cardBrand == brand,
                                onClick = { cardBrand = brand },
                                label = { Text(brand) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = cardLimit.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val close = cardClosingDay.toIntOrNull() ?: 5
                        val due = cardDueDay.toIntOrNull() ?: 15
                        if (cardName.isNotBlank() && limit > 0) {
                            val hexColors = listOf("#673AB7", "#FF5722", "#111111", "#0D47A1", "#006064", "#009688", "#E91E63")
                            viewModel.addCreditCard(cardName, limit, close, due, cardBrand, hexColors.random())
                            cardName = ""
                            cardLimit = ""
                            showAddCardDialog = false
                        }
                    }
                ) {
                    Text("Criar Cartão")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 3. CREDIT CARD DETAILS & SETTINGS CONTAINER (WITH HISTORY & CARD TRANS ACTIONS)
    if (selectedCardForDetails != null) {
        val card = selectedCardForDetails!!
        val cardColor = try { Color(android.graphics.Color.parseColor(card.colorHex)) } catch (e: Exception) { Color(0xFF2A2A2A) }
        val cardTxs = txsState.filter { it.creditCardId == card.id }
        val totalSpent = cardTxs.sumOf { it.amount }

        AlertDialog(
            onDismissRequest = { selectedCardForDetails = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Detalhes: ${card.name}", fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(
                        modifier = Modifier
                            .background(cardColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(card.cardBrand, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Limits / Balance
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Uso da Fatura", style = MaterialTheme.typography.labelSmall)
                                Text("${viewModel.formatMoney(totalSpent)} de ${viewModel.formatMoney(card.limitAmount)}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (if (card.limitAmount > 0) totalSpent / card.limitAmount else 0.0).coerceIn(0.0..1.0).toFloat() },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Card Settings panel (Edit Limits/Closing days)
                    Text("Editar Configurações do Cartão", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = detailsCardLimit,
                        onValueChange = { detailsCardLimit = it },
                        label = { Text("Alterar Limite do Cartão") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = detailsCardClosingDay,
                            onValueChange = { detailsCardClosingDay = it },
                            label = { Text("Melhor Dia Compra") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = detailsCardDueDay,
                            onValueChange = { detailsCardDueDay = it },
                            label = { Text("Dia Vencimento") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val lim = detailsCardLimit.replace(",", ".").toDoubleOrNull() ?: card.limitAmount
                                val clos = detailsCardClosingDay.toIntOrNull() ?: card.closingDay
                                val dDay = detailsCardDueDay.toIntOrNull() ?: card.dueDay
                                viewModel.updateCreditCard(
                                    card.copy(limitAmount = lim, closingDay = clos, dueDay = dDay)
                                )
                                selectedCardForDetails = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salvar Ajustes", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.deleteCreditCard(card)
                                selectedCardForDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Excluir Cartão", fontSize = 12.sp)
                        }
                    }

                    Divider()

                    // Purchases / Transactions History module
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Histórico de Compras", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextButton(
                            onClick = { showAddCardPurchaseDialog = card }
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar")
                        }
                    }

                    if (cardTxs.isEmpty()) {
                        Text(
                            text = "Nenhuma compra registrada nessa fatura.",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                        )
                    } else {
                        cardTxs.forEach { tx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${tx.category} • ${dateFormatter.format(Date(tx.dateTimestamp))}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(viewModel.formatMoney(tx.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                        
                                        IconButton(
                                            onClick = {
                                                // Trigger edit purchase
                                                editingCardPurchase = tx
                                                cardTxName = tx.title
                                                cardTxCategory = tx.category
                                                cardTxAmount = tx.amount.toString()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, "Editar Compra", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteTransaction(tx)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, "Excluir Compra", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedCardForDetails = null }) {
                    Text("Fechar")
                }
            }
        )
    }

    // 4. ADD DIRECT CARD PURCHASE DIALOG
    if (showAddCardPurchaseDialog != null) {
        val card = showAddCardPurchaseDialog!!
        AlertDialog(
            onDismissRequest = { showAddCardPurchaseDialog = null },
            title = { Text("Registrar Compra - ${card.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardTxName,
                        onValueChange = { cardTxName = it },
                        label = { Text("Nome do Lançamento/Estabelecimento") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardTxAmount,
                        onValueChange = { cardTxAmount = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Categoria", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Alimentação", "Lazer", "Transporte", "Saúde", "Assinaturas", "Compras", "Geral").forEach { cat ->
                            FilterChip(
                                selected = cardTxCategory == cat,
                                onClick = { cardTxCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = cardTxAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (cardTxName.isNotBlank() && amt > 0.0) {
                            // Find primary bank account to bind or use 1
                            val accId = accountsState.firstOrNull()?.id ?: 1
                            viewModel.addTransaction(
                                title = cardTxName,
                                amount = amt,
                                type = "EXPENSE",
                                category = cardTxCategory,
                                sub = "Compra Cartão",
                                accId = accId,
                                cardId = card.id
                            )
                            cardTxName = ""
                            cardTxAmount = ""
                            showAddCardPurchaseDialog = null
                        }
                    }
                ) {
                    Text("Adicionar Compra")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardPurchaseDialog = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 5. EDIT CARD PURCHASE DIALOG
    if (editingCardPurchase != null) {
        val tx = editingCardPurchase!!
        AlertDialog(
            onDismissRequest = { editingCardPurchase = null },
            title = { Text("Editar Compra - ${tx.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cardTxName,
                        onValueChange = { cardTxName = it },
                        label = { Text("Nome da Compra") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardTxAmount,
                        onValueChange = { cardTxAmount = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Categoria", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Alimentação", "Lazer", "Transporte", "Saúde", "Assinaturas", "Compras", "Geral").forEach { cat ->
                            FilterChip(
                                selected = cardTxCategory == cat,
                                onClick = { cardTxCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = cardTxAmount.replace(",", ".").toDoubleOrNull() ?: tx.amount
                        if (cardTxName.isNotBlank()) {
                            viewModel.updateTransaction(
                                tx.copy(title = cardTxName, amount = amt, category = cardTxCategory)
                            )
                            cardTxName = ""
                            cardTxAmount = ""
                            editingCardPurchase = null
                        }
                    }
                ) {
                    Text("Atualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCardPurchase = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 6. ADD BILL TO PAY DIALOG
    if (showAddPayDialog) {
        AlertDialog(
            onDismissRequest = { showAddPayDialog = false },
            title = { Text("Nova Conta a Pagar", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = payName,
                        onValueChange = { payName = it },
                        label = { Text("Nome da Conta") },
                        placeholder = { Text("Ex: Conta de Luz") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payCreditor,
                        onValueChange = { payCreditor = it },
                        label = { Text("Empresa ou Credor") },
                        placeholder = { Text("Ex: Enel Distribuidora") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payDueDateStr,
                        onValueChange = { payDueDateStr = it },
                        label = { Text("Data de Vencimento (dd/MM/aaaa)") },
                        placeholder = { Text("Ex: 30/06/2026") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payNotes,
                        onValueChange = { payNotes = it },
                        label = { Text("Observações") },
                        placeholder = { Text("Ex: Conta conjunta ou parcelamento") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Status Inicial", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Pendente", "Pago").forEach { status ->
                            FilterChip(
                                selected = payStatusSelection == status,
                                onClick = { payStatusSelection == status },
                                label = { Text(status) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val dateParsed = try {
                            dateFormatter.parse(payDueDateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        if (payName.isNotBlank() && amount > 0) {
                            viewModel.addBillToPay(payName, payCreditor, amount, dateParsed, payStatusSelection, payNotes)
                            payName = ""
                            payCreditor = ""
                            payAmount = ""
                            payDueDateStr = ""
                            payNotes = ""
                            payStatusSelection = "Pendente"
                            showAddPayDialog = false
                        }
                    }
                ) {
                    Text("Criar Lançamento")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    payName = ""
                    payCreditor = ""
                    payAmount = ""
                    payDueDateStr = ""
                    payNotes = ""
                    showAddPayDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 7. EDIT BILL TO PAY DIALOG
    if (editingBillToPay != null) {
        val bill = editingBillToPay!!
        AlertDialog(
            onDismissRequest = { editingBillToPay = null },
            title = { Text("Editar Conta a Pagar", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
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
                        label = { Text("Empresa ou Credor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payDueDateStr,
                        onValueChange = { payDueDateStr = it },
                        label = { Text("Data de Vencimento (dd/MM/aaaa)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = payNotes,
                        onValueChange = { payNotes = it },
                        label = { Text("Observações") },
                        placeholder = { Text("Ex: Conta conjunta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Pendente", "Pago").forEach { status ->
                            FilterChip(
                                selected = payStatusSelection == status,
                                onClick = { payStatusSelection = status },
                                label = { Text(status) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = payAmount.replace(",", ".").toDoubleOrNull() ?: bill.amount
                        val dateParsed = try {
                            dateFormatter.parse(payDueDateStr)?.time ?: bill.dueDateTimestamp
                        } catch (e: Exception) {
                            bill.dueDateTimestamp
                        }

                        viewModel.updateBillToPay(
                            bill.copy(
                                name = payName,
                                creditor = payCreditor,
                                amount = amount,
                                dueDateTimestamp = dateParsed,
                                status = payStatusSelection,
                                notes = payNotes
                            )
                        )
                        payName = ""
                        payCreditor = ""
                        payAmount = ""
                        payDueDateStr = ""
                        payNotes = ""
                        editingBillToPay = null
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    payName = ""
                    payCreditor = ""
                    payAmount = ""
                    payDueDateStr = ""
                    payNotes = ""
                    editingBillToPay = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 8. ADD BILL TO RECEIVE DIALOG
    if (showAddReceiveDialog) {
        AlertDialog(
            onDismissRequest = { showAddReceiveDialog = false },
            title = { Text("Novo Recebimento", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = recDebtor,
                        onValueChange = { recDebtor = it },
                        label = { Text("Devedor / Parceiro") },
                        placeholder = { Text("Ex: Carlos Client") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recAmount,
                        onValueChange = { recAmount = it },
                        label = { Text("Valor Esperado") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recDueDateStr,
                        onValueChange = { recDueDateStr = it },
                        label = { Text("Data Prevista (dd/MM/aaaa)") },
                        placeholder = { Text("Ex: 15/07/2026") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recPhone,
                        onValueChange = { recPhone = it },
                        label = { Text("Telefone / Contato") },
                        placeholder = { Text("Ex: (11) 99999-9999") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recNotes,
                        onValueChange = { recNotes = it },
                        label = { Text("Observações") },
                        placeholder = { Text("Ex: Prestação de serviços") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Status Inicial", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Pendente", "Recebido").forEach { status ->
                            FilterChip(
                                selected = recStatusSelection == status,
                                onClick = { recStatusSelection = status },
                                label = { Text(status) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = recAmount.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val dateParsed = try {
                            dateFormatter.parse(recDueDateStr)?.time ?: System.currentTimeMillis()
                        } catch (e: Exception) {
                            System.currentTimeMillis()
                        }

                        if (recDebtor.isNotBlank() && amount > 0) {
                            viewModel.addBillToReceive(recDebtor, amount, dateParsed, recStatusSelection, recPhone, recNotes)
                            recDebtor = ""
                            recAmount = ""
                            recDueDateStr = ""
                            recPhone = ""
                            recNotes = ""
                            recStatusSelection = "Pendente"
                            showAddReceiveDialog = false
                        }
                    }
                ) {
                    Text("Registrar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    recDebtor = ""
                    recAmount = ""
                    recDueDateStr = ""
                    recPhone = ""
                    recNotes = ""
                    showAddReceiveDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 9. EDIT BILL TO RECEIVE DIALOG
    if (editingBillToReceive != null) {
        val rec = editingBillToReceive!!
        AlertDialog(
            onDismissRequest = { editingBillToReceive = null },
            title = { Text("Editar Recebimento", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = recDebtor,
                        onValueChange = { recDebtor = it },
                        label = { Text("Devedor / Credor") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recAmount,
                        onValueChange = { recAmount = it },
                        label = { Text("Valor") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recDueDateStr,
                        onValueChange = { recDueDateStr = it },
                        label = { Text("Data Prevista (dd/MM/aaaa)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recPhone,
                        onValueChange = { recPhone = it },
                        label = { Text("Telefone / Contato") },
                        placeholder = { Text("Ex: (11) 99999-9999") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = recNotes,
                        onValueChange = { recNotes = it },
                        label = { Text("Observações") },
                        placeholder = { Text("Ex: Prestação de serviços") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Pendente", "Recebido").forEach { status ->
                            FilterChip(
                                selected = recStatusSelection == status,
                                onClick = { recStatusSelection = status },
                                label = { Text(status) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = recAmount.replace(",", ".").toDoubleOrNull() ?: rec.amount
                        val dateParsed = try {
                            dateFormatter.parse(recDueDateStr)?.time ?: rec.dueDateTimestamp
                        } catch (e: Exception) {
                            rec.dueDateTimestamp
                        }

                        viewModel.updateBillToReceive(
                            rec.copy(
                                debtor = recDebtor,
                                amount = amount,
                                dueDateTimestamp = dateParsed,
                                status = recStatusSelection,
                                phone = recPhone,
                                notes = recNotes
                            )
                        )
                        recDebtor = ""
                        recAmount = ""
                        recDueDateStr = ""
                        recPhone = ""
                        recNotes = ""
                        editingBillToReceive = null
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    recDebtor = ""
                    recAmount = ""
                    recDueDateStr = ""
                    recPhone = ""
                    recNotes = ""
                    editingBillToReceive = null
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
