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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CreditCard
import com.example.data.model.FinanceAccount
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(viewModel: FinanceViewModel) {
    val accountsState by viewModel.accounts.collectAsState()
    val cardsState by viewModel.creditCards.collectAsState()
    val txsState by viewModel.transactions.collectAsState()

    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddCardDialog by remember { mutableStateOf(false) }

    // Forms
    var accName by remember { mutableStateOf("") }
    var accType by remember { mutableStateOf("CHECKING") } // CHECKING, SAVINGS, CASH, INVESTMENT
    var accBalance by remember { mutableStateOf("") }

    var cardName by remember { mutableStateOf("") }
    var cardLimit by remember { mutableStateOf("") }
    var cardClosingDay by remember { mutableStateOf("5") }
    var cardDueDay by remember { mutableStateOf("15") }
    var cardBrand by remember { mutableStateOf("Visa") }

    // Future purchases simulator state
    var simValue by remember { mutableStateOf("") }
    var simInstallments by remember { mutableStateOf("1") }
    var simSelectedCardId by remember { mutableStateOf<Int?>(null) }
    var simResultText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .testTag("accounts_screen")
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Bank Accounts Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CARTEIRA DE SALDOS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Contas Bancárias",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Button(
                    onClick = { showAddAccountDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, "Contas")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
        }

        // Horizontal Carousel of Bank Accounts
        item {
            if (accountsState.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhuma conta cadastrada.")
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accountsState) { acc ->
                        val color = try {
                            Color(android.graphics.Color.parseColor(acc.colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }

                        Card(
                            modifier = Modifier
                                .width(200.dp)
                                .height(125.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = color)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (acc.type) {
                                        "SAVINGS" -> Icons.Default.Savings
                                        "CASH" -> Icons.Default.Wallet
                                        "INVESTMENT" -> Icons.Default.ShowChart
                                        else -> Icons.Default.AccountBalance
                                    }
                                    Icon(icon, acc.type, tint = Color.White)
                                    
                                    val typePortuguese = when(acc.type) {
                                        "CHECKING" -> "Corrente"
                                        "SAVINGS" -> "Poupança"
                                        "CASH" -> "Carteira"
                                        "INVESTMENT" -> "Investimentos"
                                        else -> "Corrente"
                                    }
                                    Text(
                                        text = typePortuguese,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }

                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = viewModel.formatMoney(acc.balance),
                                        style = MaterialTheme.typography.titleLarge,
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

        // Credit Cards Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cartões de Crédito",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Controle de limites e faturas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { showAddCardDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CreditCard, "Cartão")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adicionar")
                }
            }
        }

        // Credit Card List
        if (cardsState.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Nenhum cartão cadastrado.")
                    }
                }
            }
        } else {
            items(cardsState) { card ->
                // Calculate invoice spending based on mock linked transaction or hardcoded percentage for prototyping
                val invoiceTotal = txsState.filter { it.creditCardId == card.id }.sumOf { it.amount }
                val availableLimit = (card.limitAmount - invoiceTotal).coerceAtLeast(0.0)
                val utilizationPercent = (invoiceTotal / card.limitAmount).coerceIn(0.0..1.0).toFloat()

                val cardColor = try {
                    Color(android.graphics.Color.parseColor(card.colorHex))
                } catch (e: Exception) {
                    Color(0xFF212121)
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
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
                            Icon(Icons.Default.Nfc, "NFC Payment Icon", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { utilizationPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp),
                            color = if (utilizationPercent > 0.8f) Color(0xFFEF5350) else Color(0xFF81C784),
                            trackColor = Color.White.copy(alpha = 0.2f),
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
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

        // Future Purchases Impact Simulator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Campaign, "Simulator Icon", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Simulador de Compras Futuras",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Saiba exatamente qual será o impacto de uma nova compra parcelada nas parcelas dos seus próximos meses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = simValue,
                        onValueChange = { simValue = it },
                        label = { Text("Valor Total do Item") },
                        placeholder = { Text("Ex: 1200") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = simInstallments,
                        onValueChange = { simInstallments = it },
                        label = { Text("Número de Parcelas desejadas") },
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
                                simResultText = "Impacto Estimado:\nVocê pagará ${inst}x de ${viewModel.formatMoney(monthly)} em sua fatura. Isso comprometerá mais ${String.format("%.1f", (monthly / 5000.0) * 100)}% de sua média salarial mensal."
                            } else {
                                simResultText = "Por favor, digite um valor maior que R$ 0."
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

        // Reconciliation Checklist Section
        item {
            Text(
                text = "Conciliação de Lançamentos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Assegure que suas movimentações registradas batem com seu saldo e extrato real",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        if (txsState.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Ainda não há lançamentos para conciliar.")
                    }
                }
            }
        } else {
            // Take 3 items for reconciliation control in this dashboard
            items(txsState.take(3)) { tx ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "${viewModel.formatMoney(tx.amount)} • ${tx.category}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (tx.isPaid) "Conciliado" else "Pendente",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (tx.isPaid) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Switch(
                                checked = tx.isPaid,
                                onCheckedChange = { isChecked ->
                                    viewModel.updateTransaction(tx.copy(isPaid = isChecked))
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // --- Create Account Dialog ---
    if (showAddAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Nova Conta Bancária", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accName,
                        onValueChange = { accName = it },
                        label = { Text("Nome da Conta") },
                        placeholder = { Text("Ex: Carteira, Itaú, NuConta") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accBalance,
                        onValueChange = { accBalance = it },
                        label = { Text("Saldo Inicial") },
                        placeholder = { Text("Ex: 1500,00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Tipo da Conta", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "CHECKING" to "Corrente",
                            "SAVINGS" to "Poupança",
                            "CASH" to "Metálico/Bolso",
                            "INVESTMENT" to "Investimentos"
                        ).forEach { (type, label) ->
                            FilterChip(
                                selected = accType == type,
                                onClick = { accType = type },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val bal = accBalance.replace(",", ".").toDoubleOrNull() ?: 0.0
                        if (accName.isNotEmpty()) {
                            // Assign a random nice color hex for visualization
                            val colors = listOf("#009688", "#3F51B5", "#F44336", "#E91E63", "#4CAF50", "#FFC107")
                            val randColor = colors.random()
                            viewModel.addAccount(accName, accType, bal, randColor)
                            accName = ""
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

    // --- Create Card Dialog ---
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
                        placeholder = { Text("Ex: Nubank, XP Infinite") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cardLimit,
                        onValueChange = { cardLimit = it },
                        label = { Text("Limite") },
                        placeholder = { Text("Ex: 5000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

                    Text("Bandeira", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        val lim = cardLimit.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val close = cardClosingDay.toIntOrNull() ?: 5
                        val due = cardDueDay.toIntOrNull() ?: 15
                        if (cardName.isNotEmpty() && lim > 0) {
                            val cardHexColors = listOf("#673AB7", "#FF5722", "#1A1A1A", "#0D47A1", "#006064")
                            viewModel.addCreditCard(cardName, lim, close, due, cardBrand, cardHexColors.random())
                            cardName = ""
                            cardLimit = ""
                            cardClosingDay = "5"
                            cardDueDay = "15"
                            showAddCardDialog = false
                        }
                    }
                ) {
                    Text("Salvar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCardDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    }
}
