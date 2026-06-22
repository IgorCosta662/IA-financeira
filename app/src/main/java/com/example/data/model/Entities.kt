package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class FinanceAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // CHECKING (Corrente), SAVINGS (Poupança), CASH (Carteira), INVESTMENT (Investimentos)
    val balance: Double,
    val colorHex: String = "#3F51B5",
    val bankName: String = "",
    val agency: String = "",
    val accountNumber: String = ""
)

@Entity(tableName = "transactions")
data class FinanceTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME (Receita), EXPENSE (Despesa)
    val category: String, // Salário, Renda Bônus, Alimentação, Lazer, Saúde, Assinaturas, etc.
    val subcategory: String = "",
    val dateTimestamp: Long = System.currentTimeMillis(),
    val accountId: Int, // Refers to FinanceAccount
    val isRecurring: Boolean = false, // Scheduled recurring
    val isPaid: Boolean = true, // Reconciled/cleared
    val receiptImgUri: String? = null, // Camera recipe photo URI
    val totalInstallments: Int = 1, // Useful for installment expenses
    val currentInstallment: Int = 1,
    val creditCardId: Int? = null // Under specific card bill if not null
)

@Entity(tableName = "credit_cards")
data class CreditCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val limitAmount: Double,
    val closingDay: Int, // Day bill closes
    val dueDay: Int,     // Day bill is due
    val cardBrand: String = "Visa",
    val colorHex: String = "#E91E63"
)

@Entity(tableName = "investments")
data class InvestmentAsset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String, // Stock ticker or asset name
    val category: String, // STOCKS (Ações), FIIS (Fundos Imobiliários), CRYPTO (Cripto), FIXED_INCOME (Renda Fixa), TREASURY (Tesouro Selic)
    val quantity: Double,
    val purchasePrice: Double,
    val currentPrice: Double,
    val dateTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "financial_goals")
data class FinancialGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDateTimestamp: Long,
    val category: String // SHORT_TERM, MEDIUM_TERM, LONG_TERM
)

@Entity(tableName = "savings_challenges")
data class SavingsChallenge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val startDateTimestamp: Long = System.currentTimeMillis(),
    val endDateTimestamp: Long,
    val isCustom: Boolean = false,
    val category: String = "Geral" // e.g., 52_WEEKS, COIN_JAR, EMERGENCY, CUSTOM
)

@Entity(tableName = "financial_notes")
data class FinancialNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val category: String = "Geral", // e.g., Planejamento, Lembretes, Idéias, Geral
    val isPinned: Boolean = false
)

@Entity(tableName = "bills_to_pay")
data class BillToPay(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val creditor: String,
    val amount: Double,
    val dueDateTimestamp: Long,
    val status: String = "Pendente", // Pendente, Pago, Atrasado
    val notes: String = ""
)

@Entity(tableName = "bills_to_receive")
data class BillToReceive(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val debtor: String,
    val amount: Double,
    val dueDateTimestamp: Long,
    val status: String = "Pendente", // Pendente, Recebido, Atrasado
    val phone: String = "",
    val notes: String = ""
)

@Entity(tableName = "custom_categories")
data class CustomCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // INCOME or EXPENSE
    val iconName: String = "Category",
    val colorHex: String = "#3F51B5"
)

