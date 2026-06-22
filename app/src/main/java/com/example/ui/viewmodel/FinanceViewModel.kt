package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.model.*
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class RetirementResult(
    val totalAccumulated: Double,
    val interestEarned: Double,
    val monthlyPayout: Double,
    val yearsToCompounding: Int
)

data class FinancingInstallment(
    val monthNumber: Int,
    val installmentAmount: Double,
    val principalAmortized: Double,
    val interestPaid: Double,
    val remainingBalance: Double
)

data class YearlyGrowth(
    val year: Int,
    val totalInvested: Double,
    val interestEarned: Double,
    val futureValue: Double
)

class FinanceViewModel(private val repository: FinanceRepository) : ViewModel() {

    // --- State Streams ---
    val accounts = repository.accounts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val transactions = repository.transactions.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val creditCards = repository.creditCards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val investments = repository.investments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val goals = repository.goals.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val challenges = repository.challenges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Local Customization State ---
    private val _currency = MutableStateFlow("BRL") // BRL, USD, EUR
    val currency: StateFlow<String> get() = _currency

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    private val _pinCode = MutableStateFlow("") // PIN for lock security
    val pinCode: StateFlow<String> get() = _pinCode

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> get() = _isScreenLocked

    private val _familyBudgetMode = MutableStateFlow(false)
    val familyBudgetMode: StateFlow<Boolean> get() = _familyBudgetMode

    private val _openFinanceConnected = MutableStateFlow(false)
    val openFinanceConnected: StateFlow<Boolean> get() = _openFinanceConnected

    // --- AI Assist State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(text = "Olá! Eu sou o seu Finança AI Coach. Como posso te apoiar no seu planejamento financeiro hoje?", isUser = false)
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> get() = _chatMessages

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> get() = _isAiLoading

    private val _aiReport = MutableStateFlow<String>("")
    val aiReport: StateFlow<String> get() = _aiReport

    // --- Initial Seeding ---
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // --- Currency Formatting Utility ---
    fun getCurrencySymbol(): String {
        return when (_currency.value) {
            "BRL" -> "R$"
            "USD" -> "$"
            "EUR" -> "€"
            else -> "R$"
        }
    }

    fun formatMoney(value: Double): String {
        return when (_currency.value) {
            "BRL" -> String.format(Locale("pt", "BR"), "R$ %,.2f", value)
            "USD" -> String.format(Locale.US, "$ %,.2f", value)
            "EUR" -> String.format(Locale.GERMANY, "€ %,.2f", value)
            else -> String.format("R$ %,.2f", value)
        }
    }

    // --- Preference Setters ---
    fun setCurrency(curr: String) {
        _currency.value = curr
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setPin(pin: String) {
        _pinCode.value = pin
        _isScreenLocked.value = pin.isNotEmpty()
    }

    fun unlockScreen(enteredPin: String): Boolean {
        if (enteredPin == _pinCode.value) {
            _isScreenLocked.value = false
            return true
        }
        return false
    }

    fun toggleFamilyMode() {
        _familyBudgetMode.value = !_familyBudgetMode.value
    }

    fun toggleOpenFinance() {
        _openFinanceConnected.value = !_openFinanceConnected.value
    }

    // --- Database Writers ---
    fun addAccount(name: String, type: String, balance: Double, color: String = "#2196F3") {
        viewModelScope.launch {
            repository.insertAccount(FinanceAccount(name = name, type = type, balance = balance, colorHex = color))
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: String,
        category: String,
        sub: String,
        accId: Int,
        isRec: Boolean = false,
        totalInst: Int = 1,
        cardId: Int? = null,
        receiptImg: String? = null
    ) {
        viewModelScope.launch {
            // Insere a transação
            repository.insertTransaction(
                FinanceTransaction(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    subcategory = sub,
                    dateTimestamp = System.currentTimeMillis(),
                    accountId = accId,
                    isRecurring = isRec,
                    totalInstallments = totalInst,
                    creditCardId = cardId,
                    receiptImgUri = receiptImg
                )
            )
            // Atualiza de forma simples o saldo da conta
            val account = accounts.value.find { it.id == accId }
            if (account != null) {
                val newBal = if (type == "INCOME") account.balance + amount else account.balance - amount
                repository.updateAccount(account.copy(balance = newBal))
            }
        }
    }

    fun updateTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: FinanceTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            // Estorna do saldo da conta
            val account = accounts.value.find { it.id == transaction.accountId }
            if (account != null) {
                val newBal = if (transaction.type == "INCOME") account.balance - transaction.amount else account.balance + transaction.amount
                repository.updateAccount(account.copy(balance = newBal))
            }
        }
    }

    fun addCreditCard(name: String, limit: Double, closingDay: Int, dueDay: Int, brand: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCreditCard(
                CreditCard(name = name, limitAmount = limit, closingDay = closingDay, dueDay = dueDay, cardBrand = brand, colorHex = colorHex)
            )
        }
    }

    fun addInvestment(name: String, category: String, qty: Double, buyPrice: Double, currentPrice: Double) {
        viewModelScope.launch {
            repository.insertInvestment(
                InvestmentAsset(name = name, category = category, quantity = qty, purchasePrice = buyPrice, currentPrice = currentPrice)
            )
        }
    }

    fun deleteInvestment(investment: InvestmentAsset) {
        viewModelScope.launch {
            repository.deleteInvestment(investment)
        }
    }

    fun addGoal(title: String, target: Double, current: Double, months: Int, category: String) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, months)
            repository.insertGoal(
                FinancialGoal(title = title, targetAmount = target, currentAmount = current, targetDateTimestamp = cal.timeInMillis, category = category)
            )
        }
    }

    fun contributeToGoal(goalId: Int, value: Double) {
        viewModelScope.launch {
            val goal = goals.value.find { it.id == goalId }
            if (goal != null) {
                val updated = goal.copy(currentAmount = goal.currentAmount + value)
                repository.updateGoal(updated)
            }
        }
    }

    fun deleteGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // --- Savings Challenges Actions ---
    fun addChallenge(title: String, description: String, target: Double, current: Double, months: Int, category: String, isCustom: Boolean = true) {
        viewModelScope.launch {
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, months)
            repository.insertChallenge(
                SavingsChallenge(
                    title = title,
                    description = description,
                    targetAmount = target,
                    currentAmount = current,
                    endDateTimestamp = cal.timeInMillis,
                    isCustom = isCustom,
                    category = category
                )
            )
        }
    }

    fun contributeToChallenge(challengeId: Int, value: Double) {
        viewModelScope.launch {
            val challenge = challenges.value.find { it.id == challengeId }
            if (challenge != null) {
                val updated = challenge.copy(currentAmount = (challenge.currentAmount + value).coerceAtMost(challenge.targetAmount))
                repository.updateChallenge(updated)
            }
        }
    }

    fun deleteChallenge(challenge: SavingsChallenge) {
        viewModelScope.launch {
            repository.deleteChallenge(challenge)
        }
    }

    fun getMotivationalAdvice(progressPercent: Double): String {
        return when {
            progressPercent <= 0.0 -> "Dê o pontapé inicial! O segredo é simplesmente começar. 💪"
            progressPercent < 25.0 -> "Cada pequena economia pavimenta sua estrada financeira. Siga em frente de cabeça erguida! 🚀"
            progressPercent < 50.0 -> "Mais de 1/4 já foi! Seus hábitos de poupança estão gerando frutos de verdade. 👏"
            progressPercent < 75.0 -> "Metade do caminho já ficou para trás! O sucesso se aproxima a passos largos. Excelente foco! ⭐"
            progressPercent < 100.0 -> "Sensacional! Faltam pouquíssimos passos para carimbar sua grande vitória! Segura esse fôlego! 🔥"
            else -> "PARABÉNS! Você devorou o desafio e atingiu 100%! Sua saúde financeira deu um salto gigante! 🏆✨"
        }
    }

    // --- Financial Calculations & Summary ---
    fun getTotalFinancialHealthScore(): String {
        val expenseTotal = transactions.value.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val incomeTotal = transactions.value.filter { it.type == "INCOME" }.sumOf { it.amount }
        if (incomeTotal == 0.0) return "Suficiente (Sem renda registrada)"
        val ratio = expenseTotal / incomeTotal
        return when {
            ratio < 0.5 -> "Excelente (Utilizando menos de 50% de sua renda)"
            ratio < 0.7 -> "Regular (Equilibrado mas limite de 70% de gastos)"
            ratio < 0.9 -> "Alerta (Recomenda-se reduzir custos flutuantes)"
            else -> "Crítico (Gastos muito próximos ou superiores à renda)"
        }
    }

    // --- Simulators Logic ---
    fun simulateRetirement(currentAge: Int, targetAge: Int, monthlySavings: Double, expectedReturnYr: Double): RetirementResult {
        val years = targetAge - currentAge
        if (years <= 0) return RetirementResult(0.0, 0.0, 0.0, 0)
        val months = years * 12
        val rMonthly = (expectedReturnYr / 100.0) / 12.0
        var accumulated = 0.0
        var totalInvested = 0.0

        for (m in 1..months) {
            accumulated = (accumulated + monthlySavings) * (1.0 + rMonthly)
            totalInvested += monthlySavings
        }

        val interest = accumulated - totalInvested
        // Simulate a 4% annual safe withdrawal rate converted to monthly payout
        val monthlyPayout = (accumulated * 0.04) / 12.0

        return RetirementResult(
            totalAccumulated = accumulated,
            interestEarned = interest,
            monthlyPayout = monthlyPayout,
            yearsToCompounding = years
        )
    }

    fun simulateFinancing(principal: Double, interestYr: Double, months: Int, system: String): List<FinancingInstallment> {
        val monthlyRate = (interestYr / 100.0) / 12.0
        val amortizedList = mutableListOf<FinancingInstallment>()
        var balance = principal

        if (system == "PRICE") {
            // Price Amortization: Installment remains constant. PMT = PV * [i * (1+i)^n] / [(1+i)^n - 1]
            val installment = if (monthlyRate > 0.0) {
                principal * (monthlyRate * Math.pow(1.0 + monthlyRate, months.toDouble())) / (Math.pow(1.0 + monthlyRate, months.toDouble()) - 1.0)
            } else {
                principal / months
            }

            for (m in 1..months) {
                val interestPr = balance * monthlyRate
                val principalAmort = installment - interestPr
                balance -= principalAmort
                amortizedList.add(
                    FinancingInstallment(
                        monthNumber = m,
                        installmentAmount = installment,
                        principalAmortized = principalAmort,
                        interestPaid = interestPr,
                        remainingBalance = if (balance < 0.0) 0.0 else balance
                    )
                )
            }
        } else {
            // SAC (Sistema de Amortização Constante): Amortization remains constant
            val principalAmort = principal / months
            for (m in 1..months) {
                val interestPr = balance * monthlyRate
                val installment = principalAmort + interestPr
                balance -= principalAmort
                amortizedList.add(
                    FinancingInstallment(
                        monthNumber = m,
                        installmentAmount = installment,
                        principalAmortized = principalAmort,
                        interestPaid = interestPr,
                        remainingBalance = if (balance < 0.0) 0.0 else balance
                    )
                )
            }
        }
        return amortizedList
    }

    fun simulateInvestments(initial: Double, monthly: Double, interestYr: Double, years: Int): List<YearlyGrowth> {
        val rMonthly = (interestYr / 100.0) / 12.0
        val results = mutableListOf<YearlyGrowth>()
        var futureValue = initial
        var totalInvested = initial

        for (y in 1..years) {
            for (m in 1..12) {
                futureValue = (futureValue + monthly) * (1.0 + rMonthly)
                totalInvested += monthly
            }
            results.add(
                YearlyGrowth(
                    year = y,
                    totalInvested = totalInvested,
                    interestEarned = futureValue - totalInvested,
                    futureValue = futureValue
                )
            )
        }
        return results
    }

    // --- Simular Importação de Arquivos OFX / CSV ---
    fun importOfxText(ofxData: String): Int {
        var importCount = 0
        try {
            // Find account for import (Default Banco Itau - Account ID 1)
            val accountId = accounts.value.firstOrNull()?.id ?: 1

            // Advanced regex simulation mapping standard OFX blocks <STMTTRN>
            val trnRegex = Regex("<STMTTRN>([\\s\\S]*?)</STMTTRN>", RegexOption.IGNORE_CASE)
            val matches = trnRegex.findAll(ofxData)

            for (match in matches) {
                val block = match.groupValues[1]
                val memo = Regex("<MEMO>(.*?)\r?\n").find(block)?.groupValues?.get(1) ?: "Compra OFX Importada"
                val trnamt = Regex("<TRNAMT>(.*?)\r?\n").find(block)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0

                if (trnamt != 0.0) {
                    val isIncome = trnamt > 0.0
                    val amount = Math.abs(trnamt)
                    val type = if (isIncome) "INCOME" else "EXPENSE"
                    val category = if (isIncome) "Renda Extra" else "Alimentação"

                    addTransaction(
                        title = memo.trim(),
                        amount = amount,
                        type = type,
                        category = category,
                        sub = "OFX Importado",
                        accId = accountId
                    )
                    importCount++
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return importCount
    }

    fun importCsvText(csvData: String): Int {
        var importCount = 0
        try {
            val accountId = accounts.value.firstOrNull()?.id ?: 1
            val lines = csvData.split("\n")
            for (line in lines) {
                val parts = line.split(",")
                if (parts.size >= 4) {
                    val title = parts[0].trim()
                    val amount = parts[1].trim().toDoubleOrNull() ?: continue
                    val type = parts[2].trim().uppercase() // INCOME / EXPENSE
                    val category = parts[3].trim()

                    if (type == "INCOME" || type == "EXPENSE") {
                        addTransaction(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            sub = "CSV Importado",
                            accId = accountId
                        )
                        importCount++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return importCount
    }

    // --- AI Assist Chat and Budget Analyzer ---
    private fun assembleFinancialContext(): String {
        val totalBal = accounts.value.sumOf { it.balance }
        val accountsStr = accounts.value.joinToString("\n") { " - ${it.name} (${it.type}): ${formatMoney(it.balance)}" }
        val ccStr = creditCards.value.joinToString("\n") { " - ${it.name} (Limite: ${formatMoney(it.limitAmount)})" }
        val invStr = investments.value.joinToString("\n") { " - ${it.name} (${it.category}): ${it.quantity} x ${formatMoney(it.currentPrice)}" }
        val goalsStr = goals.value.joinToString("\n") { " - ${it.title}: Progresso ${formatMoney(it.currentAmount)} de ${formatMoney(it.targetAmount)}" }

        val recentTx = transactions.value.take(10).joinToString("\n") {
            " - [${if(it.type == "INCOME") "RECEITA" else "DESPESA"}] ${it.title} de ${formatMoney(it.amount)} (Cat: ${it.category})"
        }

        return """
            Resumo do Patrimônio:
            Saldo Total Consolidado das Contas: ${formatMoney(totalBal)}
            
            Suas Contas Financeiras:
            $accountsStr
            
            Cartões de Crédito:
            $ccStr
            
            Investimentos Cadastrados:
            $invStr
            
            Metas de Planejamento:
            $goalsStr
            
            Últimas 10 Movimentações:
            $recentTx
            
            Saúde Financeira Geral: ${getTotalFinancialHealthScore()}
        """.trimIndent()
    }

    fun sendMessageToAi(msg: String) {
        if (msg.trim().isEmpty()) return
        val context = assembleFinancialContext()
        val userMsg = ChatMessage(text = msg, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val responseText = GeminiApiClient.generateFinancialAdvice(msg, context)
            val aiMsg = ChatMessage(text = responseText, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun fetchAutomaticBudgetAnalysis() {
        _isAiLoading.value = true
        val context = assembleFinancialContext()
        val prompt = """
            Por favor, faça uma análise automática detalhada de meus gastos e hábitos de acordo com meu contexto financeiro atual.
            Forneça:
            1. Sugestões de economia práticas baseadas nas minhas categorias de gastos mais expressivos.
            2. Uma previsão simplificada de potenciais despesas urgentes futuras ou áreas críticas.
            3. Três insights inteligentes para eu otimizar minhas sobras de salário.
        """.trimIndent()

        viewModelScope.launch {
            val responseText = GeminiApiClient.generateFinancialAdvice(prompt, context)
            _aiReport.value = responseText
            _isAiLoading.value = false
        }
    }
}

// --- ViewModel Factory ---
class FinanceViewModelFactory(private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
