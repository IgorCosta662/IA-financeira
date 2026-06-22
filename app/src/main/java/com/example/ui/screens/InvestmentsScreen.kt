package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.InvestmentAsset
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinancingInstallment
import com.example.ui.viewmodel.YearlyGrowth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentsScreen(viewModel: FinanceViewModel) {
    val investmentsState by viewModel.investments.collectAsState()

    var showAddAssetDialog by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<com.example.data.model.InvestmentAsset?>(null) }
    var showDeleteConfirmAsset by remember { mutableStateOf<com.example.data.model.InvestmentAsset?>(null) }
    var activeSubTab by remember { mutableStateOf("PORTFOLIO") } // PORTFOLIO, RETIREMENT, FINANCING, JUR_COMPOSTOS

    // Asset positioning forms
    var assetName by remember { mutableStateOf("") }
    var assetCategory by remember { mutableStateOf("STOCKS") } // STOCKS, FIIS, CRYPTO, FIXED_INCOME, TREASURY
    var assetQty by remember { mutableStateOf("") }
    var assetPurchasePrice by remember { mutableStateOf("") }
    var assetCurrentPrice by remember { mutableStateOf("") }

    // Simulators forms
    // 1. Retirement Form
    var retCurrentAge by remember { mutableStateOf("25") }
    var retRetireAge by remember { mutableStateOf("60") }
    var retMonthlySavings by remember { mutableStateOf("500") }
    var retInterestYr by remember { mutableStateOf("10") }
    var retResult by remember { mutableStateOf<com.example.ui.viewmodel.RetirementResult?>(null) }

    // 2. Financing Form
    var finPrincipal by remember { mutableStateOf("150000") }
    var finInterestYr by remember { mutableStateOf("12") }
    var finMonths by remember { mutableStateOf("120") }
    var finSystem by remember { mutableStateOf("SAC") } // SAC or PRICE
    var finResultsList by remember { mutableStateOf<List<FinancingInstallment>>(emptyList()) }

    // 3. Compound Interest Form
    var compInitial by remember { mutableStateOf("5000") }
    var compMonthly by remember { mutableStateOf("300") }
    var compInterestYr by remember { mutableStateOf("11") }
    var compYears by remember { mutableStateOf("15") }
    var compResultsList by remember { mutableStateOf<List<YearlyGrowth>>(emptyList()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("investments_screen")
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Screen Header
        item {
            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = "PLANEJAMENTO DE ATIVOS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Investimentos & Planejamento",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub Tab buttons
            ScrollableTabRow(
                selectedTabIndex = when(activeSubTab) {
                    "PORTFOLIO" -> 0
                    "RETIREMENT" -> 1
                    "FINANCING" -> 2
                    else -> 3
                },
                edgePadding = 0.dp,
                divider = {}
            ) {
                Tab(selected = activeSubTab == "PORTFOLIO", onClick = { activeSubTab = "PORTFOLIO" }) {
                    Text("Carteira Ativa", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeSubTab == "RETIREMENT", onClick = { activeSubTab = "RETIREMENT" }) {
                    Text("Retenção/Aposentadoria", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeSubTab == "FINANCING", onClick = { activeSubTab = "FINANCING" }) {
                    Text("Financiamentos", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
                Tab(selected = activeSubTab == "JUR_COMPOSTOS", onClick = { activeSubTab = "JUR_COMPOSTOS" }) {
                    Text("Juros Compostos", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        // Conditionally render based on active sub tab
        if (activeSubTab == "PORTFOLIO") {
            // Portfolio summary metrics card
            val totalInvested = investmentsState.sumOf { it.quantity * it.purchasePrice }
            val totalCurrent = investmentsState.sumOf { it.quantity * it.currentPrice }
            val absoluteProfits = totalCurrent - totalInvested
            val profitRatePercent = if (totalInvested > 0.0) (absoluteProfits / totalInvested) * 100.0 else 0.0

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "VALOR DE MERCADO DA CARTEIRA",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                letterSpacing = 1.sp
                            )
                            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                        }
                        
                        Text(
                            viewModel.formatMoney(totalCurrent),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Custo de Aquisição", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                                Text(viewModel.formatMoney(totalInvested), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rentabilidade Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f))
                                val profColor = if (absoluteProfits >= 0.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                Text(
                                    text = "${if (absoluteProfits >= 0.0) "+" else ""}${viewModel.formatMoney(absoluteProfits)} (${String.format(Locale.getDefault(), "%.1f", profitRatePercent)}%)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = profColor
                                )
                            }
                        }
                    }
                }
            }

            // Asset allocation and listing
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Posições em Ativos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = { showAddAssetDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Default.Add, "Ativo")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar")
                    }
                }
            }

            if (investmentsState.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Você ainda não cadastrou nenhum ativo.")
                        }
                    }
                }
            } else {
                items(investmentsState) { asset ->
                    val assetTotalCost = asset.quantity * asset.purchasePrice
                    val assetTotalCurrent = asset.quantity * asset.currentPrice
                    val assetProfit = assetTotalCurrent - assetTotalCost
                    val assetProfitPercent = if (assetTotalCost > 0L) (assetProfit / assetTotalCost) * 100.0 else 0.0

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text(asset.category)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(asset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    "Quantidade: ${asset.quantity} • Unitário: ${viewModel.formatMoney(asset.currentPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    viewModel.formatMoney(assetTotalCurrent),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                val assetProfitColor = if (assetProfit >= 0.0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                Text(
                                    "${if(assetProfit >= 0.0) "+" else ""}${String.format(Locale.getDefault(), "%.1f", assetProfitPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = assetProfitColor,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                editingAsset = asset
                                                assetName = asset.name
                                                assetCategory = asset.category
                                                assetQty = asset.quantity.toString()
                                                assetPurchasePrice = asset.purchasePrice.toString()
                                                assetCurrentPrice = asset.currentPrice.toString()
                                            }
                                    )
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Remover",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable { showDeleteConfirmAsset = asset }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Retirement simulator layout
        else if (activeSubTab == "RETIREMENT") {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Savings, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulador de Independência Financeira / Aposentadoria", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = retCurrentAge,
                            onValueChange = { retCurrentAge = it },
                            label = { Text("Sua Idade Atual") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = retRetireAge,
                            onValueChange = { retRetireAge = it },
                            label = { Text("Idade Alvo para se Aposentar") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = retMonthlySavings,
                            onValueChange = { retMonthlySavings = it },
                            label = { Text("Poupando todo mês") },
                            placeholder = { Text("Ex: 500") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = retInterestYr,
                            onValueChange = { retInterestYr = it },
                            label = { Text("Rentabilidade Estimada da Carteira (% ano)") },
                            placeholder = { Text("Ex: 10") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val age = retCurrentAge.toIntOrNull() ?: 25
                                val target = retRetireAge.toIntOrNull() ?: 60
                                val saving = retMonthlySavings.toDoubleOrNull() ?: 500.0
                                val returnRate = retInterestYr.toDoubleOrNull() ?: 10.0
                                retResult = viewModel.simulateRetirement(age, target, saving, returnRate)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Calcular Independência")
                        }

                        retResult?.let { res ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text("RESULTADO COMPILADO", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            
                            ListItem(
                                headlineContent = { Text(viewModel.formatMoney(res.totalAccumulated)) },
                                overlineContent = { Text("Montante Total Acumulado") },
                                leadingContent = { Icon(Icons.Default.WorkspacePremium, "Prime", tint = Color(0xFFD4AF37)) }
                            )

                            ListItem(
                                headlineContent = { Text(viewModel.formatMoney(res.interestEarned)) },
                                overlineContent = { Text("Total Ganho em Juros Compostos (Multiplicação)") },
                                leadingContent = { Icon(Icons.Default.TrendingUp, "Up", tint = Color(0xFF4CAF50)) }
                            )

                            ListItem(
                                headlineContent = { Text(viewModel.formatMoney(res.monthlyPayout) + " / mês") },
                                overlineContent = { Text("Dividendo Mensal Estimado (Regra dos 4% perpétuos)") },
                                leadingContent = { Icon(Icons.Default.MonetizationOn, "Gold", tint = Color(0xFF4CAF50)) }
                            )

                            Text(
                                text = "Isso significa que você tem exatamente ${res.yearsToCompounding} anos de reinvestimento capitalizado. Quanto mais cedo começar, menor será o esforço de poupança necessário!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Financing simulator layout
        else if (activeSubTab == "FINANCING") {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulador de Financiamento (Imóvel/Carro)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = finPrincipal,
                            onValueChange = { finPrincipal = it },
                            label = { Text("Valor Financiado Principal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = finInterestYr,
                            onValueChange = { finInterestYr = it },
                            label = { Text("Taxa de Juros Anual (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = finMonths,
                            onValueChange = { finMonths = it },
                            label = { Text("Prazo de Pagamento (Meses)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Tabela de Amortização", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("SAC" to "Sistema SAC (Parcelas Decrescentes)", "PRICE" to "Tabela PRICE (Parcelas Fixas)").forEach { (sys, label) ->
                                FilterChip(
                                    selected = finSystem == sys,
                                    onClick = { finSystem = sys },
                                    label = { Text(label) }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val principal = finPrincipal.replace(",", ".").toDoubleOrNull() ?: 150000.0
                                val interestYr = finInterestYr.replace(",", ".").toDoubleOrNull() ?: 12.0
                                val months = finMonths.toIntOrNull() ?: 120
                                finResultsList = viewModel.simulateFinancing(principal, interestYr, months, finSystem)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simular Tabela e Amortizações")
                        }

                        if (finResultsList.isNotEmpty()) {
                            val totalPayable = finResultsList.sumOf { it.installmentAmount }
                            val totalInterest = finResultsList.sumOf { it.interestPaid }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Custo Total Financiado", style = MaterialTheme.typography.labelSmall)
                                    Text(viewModel.formatMoney(totalPayable), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total em Juros Pagos", style = MaterialTheme.typography.labelSmall)
                                    Text(viewModel.formatMoney(totalInterest), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFFEF5350))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Demonstração das Primeiras Parcelas:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            
                            finResultsList.take(4).forEach { inst ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Mês ${inst.monthNumber}", fontWeight = FontWeight.Bold)
                                        Text("Parcela: " + viewModel.formatMoney(inst.installmentAmount), fontWeight = FontWeight.Bold)
                                        Text("Juros: " + viewModel.formatMoney(inst.interestPaid), color = Color(0xFFEF5350), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                            Text("... e mais ${finResultsList.size - 4} parcelas.", style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally))
                        }
                    }
                }
            }
        }

        // Compound interest simulator layout
        else if (activeSubTab == "JUR_COMPOSTOS") {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingUp, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Simulador de Juros Compostos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }

                        OutlinedTextField(
                            value = compInitial,
                            onValueChange = { compInitial = it },
                            label = { Text("Depósito Inicial") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = compMonthly,
                            onValueChange = { compMonthly = it },
                            label = { Text("Depósito Mensal Adicional") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = compInterestYr,
                            onValueChange = { compInterestYr = it },
                            label = { Text("Taxa de Juros Anual (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = compYears,
                            onValueChange = { compYears = it },
                            label = { Text("Período (Anos)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                val initial = compInitial.replace(",", ".").toDoubleOrNull() ?: 5000.0
                                val monthly = compMonthly.replace(",", ".").toDoubleOrNull() ?: 300.0
                                val interestYr = compInterestYr.replace(",", ".").toDoubleOrNull() ?: 11.0
                                val years = compYears.toIntOrNull() ?: 15
                                compResultsList = viewModel.simulateInvestments(initial, monthly, interestYr, years)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Simular Crescimento")
                        }

                        if (compResultsList.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                            val finalGrowth = compResultsList.last()
                            Text(
                                "Patrimônio Final: ${viewModel.formatMoney(finalGrowth.futureValue)}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Text(
                                "Você investiu ${viewModel.formatMoney(finalGrowth.totalInvested)} e ganhou ${viewModel.formatMoney(finalGrowth.interestEarned)} puro em juros capitalizados!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Crescimento Anualizado:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                            compResultsList.take(5).forEach { yr ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ano ${yr.year}", fontSize = 14.sp)
                                    Text("Investido: " + viewModel.formatMoney(yr.totalInvested), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Acumulado: " + viewModel.formatMoney(yr.futureValue), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (compResultsList.size > 5) {
                                Text("...", modifier = Modifier.align(Alignment.CenterHorizontally))
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // --- Create / Edit Asset Dialog ---
    if (showAddAssetDialog || editingAsset != null) {
        val isEdit = editingAsset != null
        AlertDialog(
            onDismissRequest = { 
                showAddAssetDialog = false 
                editingAsset = null
                assetName = ""
                assetQty = ""
                assetPurchasePrice = ""
                assetCurrentPrice = ""
            },
            title = { Text(if (isEdit) "Editar Posição em Ativo" else "Nova Posição em Ativo", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = assetName,
                        onValueChange = { assetName = it },
                        label = { Text("Código do Ativo / Nome") },
                        placeholder = { Text("Ex: PETR4, MXRF11, Bitcoin, Selic") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Categoria de Investimento", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "STOCKS" to "Ações (Ações)",
                            "FIIS" to "FIIs (Imob)",
                            "CRYPTO" to "Cripto",
                            "FIXED_INCOME" to "Renda Fixa",
                            "TREASURY" to "Tesouro Direto"
                        ).forEach { (cat, label) ->
                            FilterChip(
                                selected = assetCategory == cat,
                                onClick = { assetCategory = cat },
                                label = { Text(label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = assetQty,
                        onValueChange = { assetQty = it },
                        label = { Text("Quantidade Adquirida") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = assetPurchasePrice,
                        onValueChange = { assetPurchasePrice = it },
                        label = { Text("Preço Unitário de Compra") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = assetCurrentPrice,
                        onValueChange = { assetCurrentPrice = it },
                        label = { Text("Preço Unitário Atual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = assetQty.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val purchase = assetPurchasePrice.replace(",", ".").toDoubleOrNull() ?: 0.0
                        val current = assetCurrentPrice.replace(",", ".").toDoubleOrNull() ?: purchase
                        if (assetName.isNotEmpty() && qty > 0.0 && purchase > 0.0) {
                            if (isEdit && editingAsset != null) {
                                viewModel.updateInvestment(
                                    editingAsset!!.copy(
                                        name = assetName,
                                        category = assetCategory,
                                        quantity = qty,
                                        purchasePrice = purchase,
                                        currentPrice = current
                                    )
                                )
                            } else {
                                viewModel.addInvestment(assetName, assetCategory, qty, purchase, current)
                            }
                            assetName = ""
                            assetQty = ""
                            assetPurchasePrice = ""
                            assetCurrentPrice = ""
                            showAddAssetDialog = false
                            editingAsset = null
                        }
                    }
                ) {
                    Text("Salvar Ativo")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showAddAssetDialog = false 
                        editingAsset = null
                        assetName = ""
                        assetQty = ""
                        assetPurchasePrice = ""
                        assetCurrentPrice = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- Delete Confirmation Dialog ---
    if (showDeleteConfirmAsset != null) {
        val asset = showDeleteConfirmAsset!!
        AlertDialog(
            onDismissRequest = { showDeleteConfirmAsset = null },
            title = { Text("Confirmar Exclusão de Ativo", fontWeight = FontWeight.Bold) },
            text = {
                Text("Deseja realmente excluir permanentemente a posição no ativo ${asset.name}? Esta ação não pode ser desfeita.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteInvestment(asset)
                        showDeleteConfirmAsset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmAsset = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
