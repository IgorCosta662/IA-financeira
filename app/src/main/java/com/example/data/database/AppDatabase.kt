package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {
    // --- Accounts ---
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun getAllAccounts(): Flow<List<FinanceAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: FinanceAccount)

    @Update
    suspend fun updateAccount(account: FinanceAccount)

    @Delete
    suspend fun deleteAccount(account: FinanceAccount)

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): FinanceAccount?

    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY dateTimestamp DESC")
    fun getAllTransactions(): Flow<List<FinanceTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: FinanceTransaction)

    @Update
    suspend fun updateTransaction(transaction: FinanceTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: FinanceTransaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // --- Credit Cards ---
    @Query("SELECT * FROM credit_cards ORDER BY id ASC")
    fun getAllCreditCards(): Flow<List<CreditCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCreditCard(card: CreditCard)

    @Update
    suspend fun updateCreditCard(card: CreditCard)

    @Delete
    suspend fun deleteCreditCard(card: CreditCard)

    // --- Investments ---
    @Query("SELECT * FROM investments ORDER BY name ASC")
    fun getAllInvestments(): Flow<List<InvestmentAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: InvestmentAsset)

    @Update
    suspend fun updateInvestment(investment: InvestmentAsset)

    @Delete
    suspend fun deleteInvestment(investment: InvestmentAsset)

    // --- Goals ---
    @Query("SELECT * FROM financial_goals ORDER BY targetDateTimestamp ASC")
    fun getAllGoals(): Flow<List<FinancialGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: FinancialGoal)

    @Update
    suspend fun updateGoal(goal: FinancialGoal)

    @Delete
    suspend fun deleteGoal(goal: FinancialGoal)

    // --- Savings Challenges ---
    @Query("SELECT * FROM savings_challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<SavingsChallenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: SavingsChallenge)

    @Update
    suspend fun updateChallenge(challenge: SavingsChallenge)

    @Delete
    suspend fun deleteChallenge(challenge: SavingsChallenge)

    // --- Financial Notes ---
    @Query("SELECT * FROM financial_notes ORDER BY isPinned DESC, dateTimestamp DESC")
    fun getAllNotes(): Flow<List<FinancialNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: FinancialNote)

    @Update
    suspend fun updateNote(note: FinancialNote)

    @Delete
    suspend fun deleteNote(note: FinancialNote)

    @Query("DELETE FROM financial_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)

    // --- Bills To Pay ---
    @Query("SELECT * FROM bills_to_pay ORDER BY dueDateTimestamp ASC")
    fun getAllBillsToPay(): Flow<List<BillToPay>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillToPay(bill: BillToPay)

    @Update
    suspend fun updateBillToPay(bill: BillToPay)

    @Delete
    suspend fun deleteBillToPay(bill: BillToPay)

    // --- Bills To Receive ---
    @Query("SELECT * FROM bills_to_receive ORDER BY dueDateTimestamp ASC")
    fun getAllBillsToReceive(): Flow<List<BillToReceive>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillToReceive(bill: BillToReceive)

    @Update
    suspend fun updateBillToReceive(bill: BillToReceive)

    @Delete
    suspend fun deleteBillToReceive(bill: BillToReceive)

    // --- Custom Categories ---
    @Query("SELECT * FROM custom_categories ORDER BY id ASC")
    fun getAllCategories(): Flow<List<CustomCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategory)

    @Update
    suspend fun updateCategory(category: CustomCategory)

    @Delete
    suspend fun deleteCategory(category: CustomCategory)

    // --- Inventory Items ---
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventoryItem(item: InventoryItem)

    @Update
    suspend fun updateInventoryItem(item: InventoryItem)

    @Delete
    suspend fun deleteInventoryItem(item: InventoryItem)
}

@Database(
    entities = [
        FinanceAccount::class,
        FinanceTransaction::class,
        CreditCard::class,
        InvestmentAsset::class,
        FinancialGoal::class,
        SavingsChallenge::class,
        FinancialNote::class,
        BillToPay::class,
        BillToReceive::class,
        CustomCategory::class,
        InventoryItem::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val financeDao: FinanceDao
}
