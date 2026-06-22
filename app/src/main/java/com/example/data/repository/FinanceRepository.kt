package com.example.data.repository

import com.example.data.database.FinanceDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class FinanceRepository(private val financeDao: FinanceDao) {

    val accounts: Flow<List<FinanceAccount>> = financeDao.getAllAccounts()
    val transactions: Flow<List<FinanceTransaction>> = financeDao.getAllTransactions()
    val creditCards: Flow<List<CreditCard>> = financeDao.getAllCreditCards()
    val investments: Flow<List<InvestmentAsset>> = financeDao.getAllInvestments()
    val goals: Flow<List<FinancialGoal>> = financeDao.getAllGoals()
    val challenges: Flow<List<SavingsChallenge>> = financeDao.getAllChallenges()

    // --- Write Actions ---
    suspend fun insertAccount(account: FinanceAccount) = financeDao.insertAccount(account)
    suspend fun updateAccount(account: FinanceAccount) = financeDao.updateAccount(account)
    suspend fun deleteAccount(account: FinanceAccount) = financeDao.deleteAccount(account)

    suspend fun insertTransaction(transaction: FinanceTransaction) = financeDao.insertTransaction(transaction)
    suspend fun updateTransaction(transaction: FinanceTransaction) = financeDao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: FinanceTransaction) = financeDao.deleteTransaction(transaction)
    suspend fun deleteTransactionById(id: Int) = financeDao.deleteTransactionById(id)

    suspend fun insertCreditCard(card: CreditCard) = financeDao.insertCreditCard(card)
    suspend fun updateCreditCard(card: CreditCard) = financeDao.updateCreditCard(card)
    suspend fun deleteCreditCard(card: CreditCard) = financeDao.deleteCreditCard(card)

    suspend fun insertInvestment(investment: InvestmentAsset) = financeDao.insertInvestment(investment)
    suspend fun updateInvestment(investment: InvestmentAsset) = financeDao.updateInvestment(investment)
    suspend fun deleteInvestment(investment: InvestmentAsset) = financeDao.deleteInvestment(investment)

    suspend fun insertGoal(goal: FinancialGoal) = financeDao.insertGoal(goal)
    suspend fun updateGoal(goal: FinancialGoal) = financeDao.updateGoal(goal)
    suspend fun deleteGoal(goal: FinancialGoal) = financeDao.deleteGoal(goal)

    suspend fun insertChallenge(challenge: SavingsChallenge) = financeDao.insertChallenge(challenge)
    suspend fun updateChallenge(challenge: SavingsChallenge) = financeDao.updateChallenge(challenge)
    suspend fun deleteChallenge(challenge: SavingsChallenge) = financeDao.deleteChallenge(challenge)

    // --- Seed Initial Mock Data if DB is Empty ---
    suspend fun seedDatabaseIfEmpty() {
        // Check if accounts are empty
        val existingAccounts = financeDao.getAllAccounts().firstOrNull()?.isEmpty() ?: true
        if (existingAccounts) {
            // 1. Seed Accounts
            val acc1 = FinanceAccount(name = "Banco Itau (Main)", type = "CHECKING", balance = 5420.50, colorHex = "#FF5722")
            val acc2 = FinanceAccount(name = "NuConta (Reserva)", type = "SAVINGS", balance = 12500.00, colorHex = "#8E24AA")
            val acc3 = FinanceAccount(name = "Carteira Física", type = "CASH", balance = 350.00, colorHex = "#4CAF50")
            financeDao.insertAccount(acc1)
            financeDao.insertAccount(acc2)
            financeDao.insertAccount(acc3)

            // Get standard seeded account IDs (usually 1, 2, 3 as it starts auto-increment)
            val accountId1 = 1
            val accountId2 = 2
            val accountId3 = 3

            // 2. Seed Credit Cards
            val card1 = CreditCard(name = "Nubank Visa Platinum", limitAmount = 4500.00, closingDay = 5, dueDay = 15, cardBrand = "Visa", colorHex = "#673AB7")
            val card2 = CreditCard(name = "XP Visa Infinite", limitAmount = 8000.00, closingDay = 12, dueDay = 22, cardBrand = "Visa", colorHex = "#212121")
            financeDao.insertCreditCard(card1)
            financeDao.insertCreditCard(card2)

            // 3. Seed Investments
            val inv1 = InvestmentAsset(name = "PETR4 (Petrobras)", category = "STOCKS", quantity = 100.0, purchasePrice = 34.50, currentPrice = 38.60)
            val inv2 = InvestmentAsset(name = "VALE3 (Vale SA)", category = "STOCKS", quantity = 50.0, purchasePrice = 68.20, currentPrice = 61.40)
            val inv3 = InvestmentAsset(name = "MXRF11", category = "FIIS", quantity = 250.0, purchasePrice = 9.80, currentPrice = 10.45)
            val inv4 = InvestmentAsset(name = "Bitcoin", category = "CRYPTO", quantity = 0.015, purchasePrice = 280000.00, currentPrice = 345000.00)
            val inv5 = InvestmentAsset(name = "Tesouro Selic 2029", category = "TREASURY", quantity = 1.0, purchasePrice = 14200.00, currentPrice = 14580.00)
            financeDao.insertInvestment(inv1)
            financeDao.insertInvestment(inv2)
            financeDao.insertInvestment(inv3)
            financeDao.insertInvestment(inv4)
            financeDao.insertInvestment(inv5)

            // 4. Seed Goals
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, 6)
            val target6Months = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 18)
            val target2Years = calendar.timeInMillis

            val goal1 = FinancialGoal(title = "Reserva de Emergência", targetAmount = 15000.00, currentAmount = 12500.50, targetDateTimestamp = target6Months, category = "SHORT_TERM")
            val goal2 = FinancialGoal(title = "Viagem para Buenos Aires", targetAmount = 6000.00, currentAmount = 1800.00, targetDateTimestamp = target6Months, category = "SHORT_TERM")
            val goal3 = FinancialGoal(title = "Entrada Carro Novo", targetAmount = 45000.00, currentAmount = 5000.00, targetDateTimestamp = target2Years, category = "MEDIUM_TERM")
            financeDao.insertGoal(goal1)
            financeDao.insertGoal(goal2)
            financeDao.insertGoal(goal3)

            // 5. Seed Transactions (Income and Expenses)
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            financeDao.insertTransaction(FinanceTransaction(title = "Salário Mensal", amount = 6500.00, type = "INCOME", category = "Salário", accountId = accountId1, dateTimestamp = now - 5 * dayMs))
            financeDao.insertTransaction(FinanceTransaction(title = "Dividendos MXRF11", amount = 26.25, type = "INCOME", category = "Dividendos", accountId = accountId1, dateTimestamp = now - 2 * dayMs))
            financeDao.insertTransaction(FinanceTransaction(title = "Freelance UI Design", amount = 1200.00, type = "INCOME", category = "Renda Extra", accountId = accountId2, dateTimestamp = now - 1 * dayMs))

            financeDao.insertTransaction(FinanceTransaction(title = "Pão de Açúcar Supermercado", amount = 245.50, type = "EXPENSE", category = "Alimentação", accountId = accountId1, dateTimestamp = now - 4 * dayMs, subcategory = "Supermercado"))
            financeDao.insertTransaction(FinanceTransaction(title = "Netflix Assinatura", amount = 55.90, type = "EXPENSE", category = "Assinaturas", accountId = accountId1, dateTimestamp = now - 3 * dayMs, isRecurring = true, subcategory = "Streaming"))
            financeDao.insertTransaction(FinanceTransaction(title = "Uber Viagem", amount = 32.40, type = "EXPENSE", category = "Transporte", accountId = accountId3, dateTimestamp = now - 2 * dayMs, subcategory = "Viagem rápida"))
            financeDao.insertTransaction(FinanceTransaction(title = "Aluguel Mensal", amount = 1600.00, type = "EXPENSE", category = "Habitação", accountId = accountId1, dateTimestamp = now - 10 * dayMs, isRecurring = true, subcategory = "Imobiliária"))
            financeDao.insertTransaction(FinanceTransaction(title = "Posto Ipiranga Combustível", amount = 150.00, type = "EXPENSE", category = "Transporte", accountId = accountId1, dateTimestamp = now, subcategory = "Gasolina"))

            // Credit card installment examples
            financeDao.insertTransaction(FinanceTransaction(title = "iPhone 15 Parcelado Pro", amount = 349.99, type = "EXPENSE", category = "Eletrônicos", accountId = accountId1, dateTimestamp = now - 1 * dayMs, totalInstallments = 10, currentInstallment = 3, creditCardId = 1))

            // 6. Seed Savings Challenges (Desafios de Poupança)
            val challenge1 = SavingsChallenge(
                title = "Desafio de 52 Semanas Clássico",
                description = "Garante uma poupança gradual semana após semana, guardando progressivamente para acumular mais de R$ 1.300 em um ano de forma leve!",
                targetAmount = 1378.00,
                currentAmount = 150.00,
                startDateTimestamp = now,
                endDateTimestamp = now + 52 * 7 * dayMs,
                isCustom = false,
                category = "52_WEEKS"
            )
            val challenge2 = SavingsChallenge(
                title = "Pote de Economia Express",
                description = "Desafio rápido para cortar pequenos cafezinhos ou despesas supérfluas diárias e acumular um excelente dinheiro em 2 meses.",
                targetAmount = 300.00,
                currentAmount = 45.00,
                startDateTimestamp = now,
                endDateTimestamp = now + 60 * dayMs,
                isCustom = false,
                category = "COIN_JAR"
            )
            val challenge3 = SavingsChallenge(
                title = "Reserva de Bronze",
                description = "Pontapé inicial perfeito para criar sua primeira mini reserva líquida protegida contra imprevistos cotidianos comuns.",
                targetAmount = 2000.00,
                currentAmount = 500.00,
                startDateTimestamp = now,
                endDateTimestamp = now + 180 * dayMs,
                isCustom = false,
                category = "EMERGENCY"
            )
            financeDao.insertChallenge(challenge1)
            financeDao.insertChallenge(challenge2)
            financeDao.insertChallenge(challenge3)
        }
    }
}
