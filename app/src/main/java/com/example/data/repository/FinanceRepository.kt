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
    val notes: Flow<List<FinancialNote>> = financeDao.getAllNotes()
    val billsToPay: Flow<List<BillToPay>> = financeDao.getAllBillsToPay()
    val billsToReceive: Flow<List<BillToReceive>> = financeDao.getAllBillsToReceive()
    val customCategories: Flow<List<CustomCategory>> = financeDao.getAllCategories()
    val inventoryItems: Flow<List<InventoryItem>> = financeDao.getAllInventoryItems()

    // --- Write Actions ---
    suspend fun insertAccount(account: FinanceAccount) = financeDao.insertAccount(account)
    suspend fun updateAccount(account: FinanceAccount) = financeDao.updateAccount(account)
    suspend fun deleteAccount(account: FinanceAccount) = financeDao.deleteAccount(account)

    suspend fun insertCategory(category: CustomCategory) = financeDao.insertCategory(category)
    suspend fun updateCategory(category: CustomCategory) = financeDao.updateCategory(category)
    suspend fun deleteCategory(category: CustomCategory) = financeDao.deleteCategory(category)

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

    // --- Note Actions ---
    suspend fun insertNote(note: FinancialNote) = financeDao.insertNote(note)
    suspend fun updateNote(note: FinancialNote) = financeDao.updateNote(note)
    suspend fun deleteNote(note: FinancialNote) = financeDao.deleteNote(note)
    suspend fun deleteNoteById(id: Int) = financeDao.deleteNoteById(id)

    // --- Bills To Pay actions ---
    suspend fun insertBillToPay(bill: BillToPay) = financeDao.insertBillToPay(bill)
    suspend fun updateBillToPay(bill: BillToPay) = financeDao.updateBillToPay(bill)
    suspend fun deleteBillToPay(bill: BillToPay) = financeDao.deleteBillToPay(bill)

    // --- Bills To Receive actions ---
    suspend fun insertBillToReceive(bill: BillToReceive) = financeDao.insertBillToReceive(bill)
    suspend fun updateBillToReceive(bill: BillToReceive) = financeDao.updateBillToReceive(bill)
    suspend fun deleteBillToReceive(bill: BillToReceive) = financeDao.deleteBillToReceive(bill)

    // --- Inventory Items actions ---
    suspend fun insertInventoryItem(item: InventoryItem) = financeDao.insertInventoryItem(item)
    suspend fun updateInventoryItem(item: InventoryItem) = financeDao.updateInventoryItem(item)
    suspend fun deleteInventoryItem(item: InventoryItem) = financeDao.deleteInventoryItem(item)

    // --- Seed Initial Mock Data if DB is Empty ---
    suspend fun seedDatabaseIfEmpty() {
        // Seeding default categories ONLY as baseline system infrastructure Catalog
        val existingCategories = financeDao.getAllCategories().firstOrNull()?.isEmpty() ?: true
        if (existingCategories) {
            val defaultCategories = listOf(
                CustomCategory(name = "Salário", type = "INCOME", iconName = "Work", colorHex = "#4CAF50"),
                CustomCategory(name = "Renda Extra", type = "INCOME", iconName = "AddCard", colorHex = "#009688"),
                CustomCategory(name = "Comissões", type = "INCOME", iconName = "Percent", colorHex = "#3F51B5"),
                CustomCategory(name = "Dividendos", type = "INCOME", iconName = "Analytics", colorHex = "#9C27B0"),
                CustomCategory(name = "Outros", type = "INCOME", iconName = "Wallet", colorHex = "#607D8B"),
                
                CustomCategory(name = "Alimentação", type = "EXPENSE", iconName = "Restaurant", colorHex = "#E57373"),
                CustomCategory(name = "Lazer", type = "EXPENSE", iconName = "SportsEsports", colorHex = "#FFB74D"),
                CustomCategory(name = "Transporte", type = "EXPENSE", iconName = "DirectionsCar", colorHex = "#4FC3F7"),
                CustomCategory(name = "Saúde", type = "EXPENSE", iconName = "LocalHospital", colorHex = "#F06292"),
                CustomCategory(name = "Moradia", type = "EXPENSE", iconName = "Home", colorHex = "#7986CB"),
                CustomCategory(name = "Assinaturas", type = "EXPENSE", iconName = "Subscriptions", colorHex = "#BA68C8"),
                CustomCategory(name = "Impostos", type = "EXPENSE", iconName = "AccountBalanceWallet", colorHex = "#A1887F"),
                CustomCategory(name = "Outros", type = "EXPENSE", iconName = "Wallet", colorHex = "#90A4AE")
            )
            for (cat in defaultCategories) {
                financeDao.insertCategory(cat)
            }
        }
    }
}
