package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelPlannerScreen(
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accountsState by viewModel.accounts.collectAsState()
    val creditCardsState by viewModel.creditCards.collectAsState()

    // Form states
    var planTitle by remember { mutableStateOf("") }
    var totalCostStr by remember { mutableStateOf("") }
    var installmentsStr by remember { mutableStateOf("10") }
    var selectedAccountId by remember { mutableStateOf<Int?>(null) }
    var selectedCreditCardId by remember { mutableStateOf<Int?>(null) }
    var useCreditCard by remember { mutableStateOf(true) }

    // Set default account when state is ready
    LaunchedEffect(accountsState) {
        if (selectedAccountId == null && accountsState.isNotEmpty()) {
            selectedAccountId = accountsState.first().id
        }
    }

    // Set default card when state is ready
    LaunchedEffect(creditCardsState) {
        if (selectedCreditCardId == null && creditCardsState.isNotEmpty()) {
            selectedCreditCardId = creditCardsState.first().id
        }
    }

    // Preset dreams definitions
    val presets = remember {
        listOf(
            PresetDream(
                title = "Viagem dos Sonhos (Orlando)",
                estimatedCost = 15000.0,
                defaultInstallments = 12,
                icon = "✈️",
                color = Color(0xFF2196F3)
            ),
            PresetDream(
                title = "Fim de Semana na Praia",
                estimatedCost = 1200.0,
                defaultInstallments = 4,
                icon = "🏖️",
                color = Color(0xFF00BCD4)
            ),
            PresetDream(
                title = "Upgrade de Notebook/Celular",
                estimatedCost = 5000.0,
                defaultInstallments = 10,
                icon = "💻",
                color = Color(0xFF9C27B0)
            ),
            PresetDream(
                title = "Intercâmbio Curto",
                estimatedCost = 8000.0,
                defaultInstallments = 12,
                icon = "📚",
                color = Color(0xFF4CAF50)
            )
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("travel_planner_screen"),
        topBar = {
            MediumTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SIMULADOR & PLANEJADOR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Viagens e Compras Parceladas",
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button_travel_planner")) {
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero section describing the purpose
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlightTakeoff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Planeje com Sabedoria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Evite surpresas no orçamento simulando o valor das parcelas e o impacto mensal nas faturas do seu cartão antes de comprar ou viajar.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Presets row section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ideias & Sonhos Rápidos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        presets.forEach { preset ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        planTitle = preset.title
                                        totalCostStr = preset.estimatedCost.toInt().toString()
                                        installmentsStr = preset.defaultInstallments.toString()
                                        Toast.makeText(context, "${preset.title} selecionado!", Toast.LENGTH_SHORT).show()
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = preset.icon,
                                        fontSize = 28.sp
                                    )
                                    Text(
                                        text = preset.title.split(" ").last(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "R$ ${preset.estimatedCost.toInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Simulator Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Simulador de Financiamento / Parcelamento",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Input fields
                        OutlinedTextField(
                            value = planTitle,
                            onValueChange = { planTitle = it },
                            label = { Text("Nome da Viagem ou Compra") },
                            placeholder = { Text("Ex: Viagem para o Rio, Novo PC, etc.") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = totalCostStr,
                            onValueChange = { totalCostStr = it },
                            label = { Text("Valor Estimado (R$)") },
                            placeholder = { Text("Ex: 5000") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = installmentsStr,
                            onValueChange = { installmentsStr = it },
                            label = { Text("Número de Parcelas") },
                            placeholder = { Text("Ex: 10") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Payment type switch (Card vs Account)
                        Text(
                            text = "Forma de Simulação de Pagamento",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            FilterChip(
                                selected = useCreditCard,
                                onClick = { useCreditCard = true },
                                label = { Text("Cartão de Crédito") },
                                leadingIcon = if (useCreditCard) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = !useCreditCard,
                                onClick = { useCreditCard = false },
                                label = { Text("Conta Corrente") },
                                leadingIcon = if (!useCreditCard) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Target Selector Dropdowns
                        if (useCreditCard) {
                            var showCardDropdown by remember { mutableStateOf(false) }
                            val selectedCard = creditCardsState.find { it.id == selectedCreditCardId }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Selecione o Cartão de Crédito", style = MaterialTheme.typography.labelSmall)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { showCardDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selectedCard?.name ?: "Nenhum cartão cadastrado")
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showCardDropdown,
                                        onDismissRequest = { showCardDropdown = false }
                                    ) {
                                        creditCardsState.forEach { card ->
                                            DropdownMenuItem(
                                                text = { Text("${card.name} (Lim: R$ ${card.limitAmount})") },
                                                onClick = {
                                                    selectedCreditCardId = card.id
                                                    showCardDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            var showAccDropdown by remember { mutableStateOf(false) }
                            val selectedAcc = accountsState.find { it.id == selectedAccountId }

                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Selecione a Conta Origem", style = MaterialTheme.typography.labelSmall)
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    OutlinedButton(
                                        onClick = { showAccDropdown = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(selectedAcc?.name ?: "Nenhuma conta cadastrada")
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = showAccDropdown,
                                        onDismissRequest = { showAccDropdown = false }
                                    ) {
                                        accountsState.forEach { acc ->
                                            DropdownMenuItem(
                                                text = { Text("${acc.name} (Saldo: R$ ${acc.balance})") },
                                                onClick = {
                                                    selectedAccountId = acc.id
                                                    showAccDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Calculations and visual impact
            val totalCost = totalCostStr.replace(",", ".").toDoubleOrNull() ?: 0.0
            val installments = installmentsStr.toIntOrNull() ?: 1

            if (totalCost > 0.0 && installments > 0) {
                val installmentValue = totalCost / installments
                val formattedInstVal = String.format(Locale.US, "%.2f", installmentValue).replace(".", ",")

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Análise do Impacto Mensal 📊",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Valor por Parcela", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "${installments}x R$ $formattedInstVal",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Custo Total Estimado", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "R$ ${String.format(Locale.US, "%.2f", totalCost).replace(".", ",")}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                            // Calculate safety warning
                            val safetyRating: String
                            val safetyColor: Color
                            val safetyDescription: String

                            if (useCreditCard) {
                                val selectedCard = creditCardsState.find { it.id == selectedCreditCardId }
                                if (selectedCard != null) {
                                    val percentOfLimit = (totalCost / selectedCard.limitAmount) * 100
                                    when {
                                        percentOfLimit > 100 -> {
                                            safetyRating = "ALTO RISCO (Excede o Limite)"
                                            safetyColor = MaterialTheme.colorScheme.error
                                            safetyDescription = "O custo total excede o limite disponível de R$ ${selectedCard.limitAmount} deste cartão em ${String.format("%.1f", percentOfLimit - 100)}%!"
                                        }
                                        percentOfLimit > 50 -> {
                                            safetyRating = "ATENÇÃO (Ocupa mais de 50%)"
                                            safetyColor = Color(0xFFF9A825)
                                            safetyDescription = "Essa compra comprometerá ${String.format("%.1f", percentOfLimit)}% do limite do seu cartão. Planeje com cuidado."
                                        }
                                        else -> {
                                            safetyRating = "SEGURO E CONTROLADO"
                                            safetyColor = Color(0xFF2E7D32)
                                            safetyDescription = "Uso de apenas ${String.format("%.1f", percentOfLimit)}% do limite do seu cartão. Sem riscos iminentes de estourar o orçamento."
                                        }
                                    }
                                } else {
                                    safetyRating = "NENHUM CARTÃO COMPATÍVEL"
                                    safetyColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    safetyDescription = "Nenhum cartão selecionado para calcular o comprometimento do limite."
                                }
                            } else {
                                val selectedAcc = accountsState.find { it.id == selectedAccountId }
                                if (selectedAcc != null) {
                                    val percentOfBalance = (installmentValue / selectedAcc.balance) * 100
                                    when {
                                        installmentValue > selectedAcc.balance -> {
                                            safetyRating = "RISCO IMINENTE (Excede Saldo)"
                                            safetyColor = MaterialTheme.colorScheme.error
                                            safetyDescription = "Apenas a primeira parcela de R$ $formattedInstVal já é maior do que o seu saldo atual de R$ ${selectedAcc.balance}."
                                        }
                                        percentOfBalance > 30 -> {
                                            safetyRating = "ATENÇÃO (Compromete >30% do Saldo)"
                                            safetyColor = Color(0xFFF9A825)
                                            safetyDescription = "Cada parcela compromete ${String.format("%.1f", percentOfBalance)}% do seu saldo total disponível. Pode faltar para contas fixas."
                                        }
                                        else -> {
                                            safetyRating = "SEGURO"
                                            safetyColor = Color(0xFF2E7D32)
                                            safetyDescription = "Cada parcela representa apenas ${String.format("%.1f", percentOfBalance)}% do saldo total da conta corrente."
                                        }
                                    }
                                } else {
                                    safetyRating = "NENHUMA CONTA SELECIONADA"
                                    safetyColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    safetyDescription = "Selecione uma conta para avaliar o impacto em relação ao seu saldo atual."
                                }
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = safetyColor.copy(alpha = 0.12f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (safetyColor == Color(0xFF2E7D32)) Icons.Default.CheckCircle else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = safetyColor
                                        )
                                        Text(
                                            text = safetyRating,
                                            fontWeight = FontWeight.Bold,
                                            color = safetyColor,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = safetyDescription,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Interactive Timeline of Installments
                            Text(
                                text = "Projeção do Orçamento Mensal",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val sdf = SimpleDateFormat("MMMM / yyyy", Locale("pt", "BR"))
                                for (i in 1..installments.coerceAtMost(6)) {
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.MONTH, i - 1)
                                    val monthStr = sdf.format(cal.time).replaceFirstChar { it.uppercase() }

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(6.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "$i",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            Text(
                                                text = monthStr,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        Text(
                                            text = "R$ $formattedInstVal",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (installments > 6) {
                                    Text(
                                        text = "+ ${installments - 6} parcelas adicionais projetadas nos meses seguintes...",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }

                            // Actions Area
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Save as Goal Button
                                OutlinedButton(
                                    onClick = {
                                        if (planTitle.isNotBlank()) {
                                            viewModel.addGoal(
                                                title = "Meta: $planTitle",
                                                target = totalCost,
                                                current = 0.0,
                                                months = installments,
                                                category = "SHORT_TERM"
                                            )
                                            Toast.makeText(
                                                context,
                                                "Meta '$planTitle' criada com sucesso! Acompanhe no Painel.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Por favor, insira o título para salvar como meta.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.TrackChanges, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Fazer Poupança", fontSize = 11.sp)
                                }

                                // Apply actual transactions Button
                                Button(
                                    onClick = {
                                        if (planTitle.isNotBlank()) {
                                            val finalAccId = selectedAccountId ?: (accountsState.firstOrNull()?.id ?: 1)
                                            viewModel.addTransaction(
                                                title = planTitle,
                                                amount = totalCost,
                                                type = "EXPENSE",
                                                category = "Viagem & Lazer",
                                                sub = "Planejamento Viagem",
                                                accId = finalAccId,
                                                isRec = false,
                                                totalInst = installments,
                                                cardId = if (useCreditCard) selectedCreditCardId else null
                                            )
                                            Toast.makeText(
                                                context,
                                                "Sucesso! $installments parcelas geradas no extrato de faturamento.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Por favor, insira o título para gerar as parcelas.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lançar Parcelas", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PresetDream(
    val title: String,
    val estimatedCost: Double,
    val defaultInstallments: Int,
    val icon: String,
    val color: Color
)
