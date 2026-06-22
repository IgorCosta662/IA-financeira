package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.api.AiProvider
import com.example.data.api.ConnectionStatus
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

data class AnalysisRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String,
    val model: String,
    val analysisType: String,
    val prompt: String,
    val response: String,
    val tokensEstimated: Int
)

object Obfuscator {
    fun encrypt(plainText: String): String {
        return try {
            val key = "FinancaAiSecureKey"
            val clean = plainText.toByteArray(Charsets.UTF_8)
            val encrypted = ByteArray(clean.size)
            for (i in clean.indices) {
                encrypted[i] = (clean[i].toInt() xor key[i % key.length].toInt()).toByte()
            }
            android.util.Base64.encodeToString(encrypted, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            plainText
        }
    }

    fun decrypt(encryptedText: String): String {
        return try {
            val key = "FinancaAiSecureKey"
            val decoded = android.util.Base64.decode(encryptedText, android.util.Base64.DEFAULT)
            val decrypted = ByteArray(decoded.size)
            for (i in decrypted.indices) {
                decrypted[i] = (decoded[i].toInt() xor key[i % key.length].toInt()).toByte()
            }
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            encryptedText
        }
    }
}

class FinanceViewModel(application: Application, private val repository: FinanceRepository) : AndroidViewModel(application) {

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

    val notes = repository.notes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val billsToPay = repository.billsToPay.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val billsToReceive = repository.billsToReceive.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customCategories = repository.customCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val inventoryItems = repository.inventoryItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Access History Log Data Class ---
    data class AccessLog(
        val id: String = UUID.randomUUID().toString(),
        val timestamp: Long = System.currentTimeMillis(),
        val deviceName: String = android.os.Build.MODEL ?: "Dispositivo Desconhecido",
        val authMethod: String,
        val success: Boolean
    )

    // --- Local Customization State ---
    private val _currency = MutableStateFlow("BRL") // BRL, USD, EUR
    val currency: StateFlow<String> get() = _currency

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> get() = _isDarkTheme

    // --- Advanced Biometric and Passcode Security Configuration ---
    // Methods: NONE, PIN, PASSWORD
    private val _authType = MutableStateFlow("NONE")
    val authType: StateFlow<String> get() = _authType

    private val _pinCode = MutableStateFlow("") // 4 or 6 digit PIN (stored encrypted in Prefs)
    val pinCode: StateFlow<String> get() = _pinCode

    private val _passwordValue = MutableStateFlow("") // Alphanumeric password (stored encrypted in Prefs)
    val passwordValue: StateFlow<String> get() = _passwordValue

    private val _isBiometricsEnabled = MutableStateFlow(false)
    val isBiometricsEnabled: StateFlow<Boolean> get() = _isBiometricsEnabled

    private val _isFirstAccess = MutableStateFlow(true)
    val isFirstAccess: StateFlow<Boolean> get() = _isFirstAccess

    private val _trustedDevice = MutableStateFlow(false)
    val trustedDevice: StateFlow<Boolean> get() = _trustedDevice

    private val _autoLockTime = MutableStateFlow(0) // in minutes: 0 = immediate, 1, 5, 15, 30
    val autoLockTime: StateFlow<Int> get() = _autoLockTime

    private val _is2FAEnabled = MutableStateFlow(false)
    val is2FAEnabled: StateFlow<Boolean> get() = _is2FAEnabled

    private val _twoFactorType = MutableStateFlow("EMAIL") // EMAIL, TOTP
    val twoFactorType: StateFlow<String> get() = _twoFactorType

    private val _twoFactorSecret = MutableStateFlow("GA3XMYKTOB2GQ4DP")
    val twoFactorSecret: StateFlow<String> get() = _twoFactorSecret

    private val _hideBalances = MutableStateFlow(false)
    val hideBalances: StateFlow<Boolean> get() = _hideBalances

    private val _isScreenshotProtected = MutableStateFlow(false)
    val isScreenshotProtected: StateFlow<Boolean> get() = _isScreenshotProtected

    private val _isScreenLocked = MutableStateFlow(false)
    val isScreenLocked: StateFlow<Boolean> get() = _isScreenLocked

    private val _failedAttempts = MutableStateFlow(0)
    val failedAttempts: StateFlow<Int> get() = _failedAttempts

    private val _accessLogs = MutableStateFlow<List<AccessLog>>(emptyList())
    val accessLogs: StateFlow<List<AccessLog>> get() = _accessLogs

    private val _familyBudgetMode = MutableStateFlow(false)
    val familyBudgetMode: StateFlow<Boolean> get() = _familyBudgetMode

    private val _openFinanceConnected = MutableStateFlow(false)
    val openFinanceConnected: StateFlow<Boolean> get() = _openFinanceConnected

    private val _recoveryEmail = MutableStateFlow("")
    val recoveryEmail: StateFlow<String> get() = _recoveryEmail

    private val _userName = MutableStateFlow("Usuário")
    val userName: StateFlow<String> get() = _userName

    private val _userAvatarId = MutableStateFlow("avatar_1")
    val userAvatarId: StateFlow<String> get() = _userAvatarId

    // --- SharedPreferences & AI Configuration States ---
    private val sharedPrefs = getApplication<Application>().getSharedPreferences("ai_settings_prefs", Context.MODE_PRIVATE)

    private fun encrypt(value: String): String = Obfuscator.encrypt(value)
    private fun decrypt(value: String): String = Obfuscator.decrypt(value)

    private fun loadHistory(): List<AnalysisRecord> {
        val jsonStr = sharedPrefs.getString("analysis_history", "[]") ?: "[]"
        val list = mutableListOf<AnalysisRecord>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AnalysisRecord(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        provider = obj.optString("provider", ""),
                        model = obj.optString("model", ""),
                        analysisType = obj.optString("analysisType", ""),
                        prompt = obj.optString("prompt", ""),
                        response = obj.optString("response", ""),
                        tokensEstimated = obj.optInt("tokensEstimated", 0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun saveHistory(list: List<AnalysisRecord>) {
        try {
            val arr = org.json.JSONArray()
            list.forEach { item ->
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("timestamp", item.timestamp)
                    put("provider", item.provider)
                    put("model", item.model)
                    put("analysisType", item.analysisType)
                    put("prompt", item.prompt)
                    put("response", item.response)
                    put("tokensEstimated", item.tokensEstimated)
                }
                arr.put(obj)
            }
            sharedPrefs.edit().putString("analysis_history", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val _selectedProvider = MutableStateFlow(
        try {
            AiProvider.valueOf(sharedPrefs.getString("selected_provider", AiProvider.GOOGLE_DEFAULT.name) ?: AiProvider.GOOGLE_DEFAULT.name)
        } catch (e: Exception) {
            AiProvider.GOOGLE_DEFAULT
        }
    )
    val selectedProvider: StateFlow<AiProvider> get() = _selectedProvider

    private val _selectedModel = MutableStateFlow(
        sharedPrefs.getString("selected_model", "") ?: ""
    )
    val selectedModel: StateFlow<String> get() = _selectedModel

    private val _isDataSharingEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("is_data_sharing_enabled", true)
    )
    val isDataSharingEnabled: StateFlow<Boolean> get() = _isDataSharingEnabled

    private val _totalInputTokens = MutableStateFlow(
        sharedPrefs.getLong("total_input_tokens", 0L)
    )
    val totalInputTokens: StateFlow<Long> get() = _totalInputTokens

    private val _totalOutputTokens = MutableStateFlow(
        sharedPrefs.getLong("total_output_tokens", 0L)
    )
    val totalOutputTokens: StateFlow<Long> get() = _totalOutputTokens

    private val _analysisHistory = MutableStateFlow<List<AnalysisRecord>>(loadHistory())
    val analysisHistory: StateFlow<List<AnalysisRecord>> get() = _analysisHistory

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.NOT_TESTED)
    val connectionStatus: StateFlow<ConnectionStatus> get() = _connectionStatus

    fun setAiProvider(provider: AiProvider) {
        _selectedProvider.value = provider
        sharedPrefs.edit().putString("selected_provider", provider.name).apply()
        val model = getProviderModel(provider)
        setAiModel(model)
        _connectionStatus.value = ConnectionStatus.NOT_TESTED
    }

    fun setAiModel(model: String) {
        _selectedModel.value = model
        sharedPrefs.edit().putString("selected_model", model).apply()
    }

    fun getProviderKey(provider: AiProvider): String {
        val keyName = "key_api_" + provider.name
        val encrypted = sharedPrefs.getString(keyName, "") ?: ""
        return if (encrypted.isNotEmpty()) decrypt(encrypted) else ""
    }

    fun setProviderKey(provider: AiProvider, key: String) {
        val keyName = "key_api_" + provider.name
        val encrypted = encrypt(key.trim())
        sharedPrefs.edit().putString(keyName, encrypted).apply()
        _connectionStatus.value = ConnectionStatus.NOT_TESTED
    }

    fun removeProviderKey(provider: AiProvider) {
        val keyName = "key_api_" + provider.name
        sharedPrefs.edit().remove(keyName).apply()
        _connectionStatus.value = ConnectionStatus.NOT_TESTED
    }

    fun getCustomUrl(): String {
        return sharedPrefs.getString("custom_url", "") ?: ""
    }

    fun setCustomUrl(url: String) {
        sharedPrefs.edit().putString("custom_url", url.trim()).apply()
        _connectionStatus.value = ConnectionStatus.NOT_TESTED
    }

    fun setDataSharingEnabled(enabled: Boolean) {
        _isDataSharingEnabled.value = enabled
        sharedPrefs.edit().putBoolean("is_data_sharing_enabled", enabled).apply()
    }

    fun clearTokenUsage() {
        _totalInputTokens.value = 0L
        _totalOutputTokens.value = 0L
        sharedPrefs.edit()
            .putLong("total_input_tokens", 0L)
            .putLong("total_output_tokens", 0L)
            .apply()
    }

    fun getProviderModel(provider: AiProvider): String {
        val modelPref = sharedPrefs.getString("model_" + provider.name, "") ?: ""
        return if (modelPref.isNotEmpty()) modelPref else provider.defaultModel
    }

    fun setProviderModel(provider: AiProvider, model: String) {
        sharedPrefs.edit().putString("model_" + provider.name, model.trim()).apply()
        if (_selectedProvider.value == provider) {
            _selectedModel.value = model.trim()
            sharedPrefs.edit().putString("selected_model", model.trim()).apply()
        }
    }

    fun clearAnalysisHistory() {
        _analysisHistory.value = emptyList()
        sharedPrefs.edit().remove("analysis_history").apply()
    }

    fun removeAnalysisRecord(recordId: String) {
        val newList = _analysisHistory.value.filter { it.id != recordId }
        _analysisHistory.value = newList
        saveHistory(newList)
    }

    fun testAiConnection() {
        _connectionStatus.value = ConnectionStatus.NOT_TESTED
        viewModelScope.launch {
            val provider = _selectedProvider.value
            val apiKey = getProviderKey(provider)
            val model = _selectedModel.value.ifEmpty { provider.defaultModel }
            val customUrl = getCustomUrl()

            val result = com.example.data.api.MultiAiClient.testConnection(
                provider = provider,
                apiKey = apiKey,
                model = model,
                customUrl = customUrl
            )
            _connectionStatus.value = result
        }
    }

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

    // --- Initial Seeding & Security Setup ---
    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
        loadSecurityConfigs()
    }

    private var lastInteractionTime = System.currentTimeMillis()

    fun updateActivityTimestamp() {
        lastInteractionTime = System.currentTimeMillis()
    }

    fun checkInactivityLock() {
        if (_authType.value == "NONE") return
        if (_trustedDevice.value) return // Respect "Lembrar dispositivo confiável" (do not auto-lock on trusted device)
        val lockTimeoutMinutes = _autoLockTime.value
        val allowedInactivityMs = when (lockTimeoutMinutes) {
            0 -> 0L // Imediato
            1 -> 60_000L
            5 -> 5 * 60_000L
            15 -> 15 * 60_000L
            30 -> 30 * 60_000L
            else -> 0L
        }
        val elapsed = System.currentTimeMillis() - lastInteractionTime
        if (elapsed >= allowedInactivityMs) {
            _isScreenLocked.value = true
        }
    }

    private fun loadSecurityConfigs() {
        _currency.value = sharedPrefs.getString("currency_pref", "BRL") ?: "BRL"
        _isDarkTheme.value = sharedPrefs.getBoolean("dark_theme_pref", true)
        _authType.value = sharedPrefs.getString("auth_type", "NONE") ?: "NONE"
        
        val encryptedPin = sharedPrefs.getString("pin_code", "") ?: ""
        _pinCode.value = if (encryptedPin.isNotEmpty()) decrypt(encryptedPin) else ""
        
        val encryptedPass = sharedPrefs.getString("password_value", "") ?: ""
        _passwordValue.value = if (encryptedPass.isNotEmpty()) decrypt(encryptedPass) else ""
        
        _isBiometricsEnabled.value = sharedPrefs.getBoolean("biometrics_enabled", false)
        _isFirstAccess.value = sharedPrefs.getBoolean("first_access", true)
        _trustedDevice.value = sharedPrefs.getBoolean("trusted_device", false)
        _autoLockTime.value = sharedPrefs.getInt("auto_lock_time", 0)
        _is2FAEnabled.value = sharedPrefs.getBoolean("two_factor_enabled", false)
        _twoFactorType.value = sharedPrefs.getString("two_factor_type", "EMAIL") ?: "EMAIL"
        _twoFactorSecret.value = sharedPrefs.getString("two_factor_secret", "GA3XMYKTOB2GQ4DP") ?: "GA3XMYKTOB2GQ4DP"
        _hideBalances.value = sharedPrefs.getBoolean("hide_balances", false)
        _isScreenshotProtected.value = sharedPrefs.getBoolean("screenshot_protected", false)
        _recoveryEmail.value = sharedPrefs.getString("recovery_email", "suporte@financa.ai") ?: "suporte@financa.ai"
        _userName.value = sharedPrefs.getString("user_name", "Usuário") ?: "Usuário"
        _userAvatarId.value = sharedPrefs.getString("user_avatar_id", "avatar_1") ?: "avatar_1"
        
        updateScreenLockState()
        loadAccessLogs()
    }

    private fun updateScreenLockState() {
        _isScreenLocked.value = (_authType.value != "NONE")
    }

    fun logAccess(authMethod: String, success: Boolean) {
        val log = AccessLog(
            authMethod = authMethod,
            success = success
        )
        val currentLogs = _accessLogs.value.toMutableList()
        currentLogs.add(0, log) // Add to top of list
        if (currentLogs.size > 20) {
            currentLogs.removeAt(currentLogs.size - 1) // Keep max 20 logs
        }
        _accessLogs.value = currentLogs
        saveAccessLogs(currentLogs)
    }

    private fun loadAccessLogs() {
        val jsonStr = sharedPrefs.getString("access_logs", "[]") ?: "[]"
        val list = mutableListOf<AccessLog>()
        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    AccessLog(
                        id = obj.optString("id", UUID.randomUUID().toString()),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        deviceName = obj.optString("deviceName", "Dispositivo Desconhecido"),
                        authMethod = obj.optString("authMethod", "PIN"),
                        success = obj.optBoolean("success", true)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        _accessLogs.value = list
    }

    private fun saveAccessLogs(list: List<AccessLog>) {
        try {
            val arr = org.json.JSONArray()
            list.forEach { item ->
                val obj = org.json.JSONObject().apply {
                    put("id", item.id)
                    put("timestamp", item.timestamp)
                    put("deviceName", item.deviceName)
                    put("authMethod", item.authMethod)
                    put("success", item.success)
                }
                arr.put(obj)
            }
            sharedPrefs.edit().putString("access_logs", arr.toString()).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clearLogs() {
        _accessLogs.value = emptyList()
        sharedPrefs.edit().remove("access_logs").apply()
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

    fun formatMoney(value: Double, forceShow: Boolean = false): String {
        if (_hideBalances.value && !forceShow) {
            return getCurrencySymbol() + " " + "••••••"
        }
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
        sharedPrefs.edit().putString("currency_pref", curr).apply()
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
        sharedPrefs.edit().putBoolean("dark_theme_pref", _isDarkTheme.value).apply()
    }

    fun setAuthType(type: String) {
        _authType.value = type
        sharedPrefs.edit().putString("auth_type", type).apply()
        updateScreenLockState()
    }

    fun setPin(pin: String) {
        _pinCode.value = pin
        val encryptedPin = if (pin.isNotEmpty()) encrypt(pin) else ""
        sharedPrefs.edit().putString("pin_code", encryptedPin).apply()
        if (pin.isNotEmpty()) {
            setAuthType("PIN")
        } else if (_passwordValue.value.isEmpty()) {
            setAuthType("NONE")
        }
    }

    fun setPasswordValue(pass: String) {
        _passwordValue.value = pass
        val encryptedPass = if (pass.isNotEmpty()) encrypt(pass) else ""
        sharedPrefs.edit().putString("password_value", encryptedPass).apply()
        if (pass.isNotEmpty()) {
            setAuthType("PASSWORD")
        } else if (_pinCode.value.isEmpty()) {
            setAuthType("NONE")
        }
    }

    fun setBiometricsEnabled(enabled: Boolean) {
        _isBiometricsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("biometrics_enabled", enabled).apply()
    }

    fun setUserName(name: String) {
        _userName.value = name
        sharedPrefs.edit().putString("user_name", name).apply()
    }

    fun setUserAvatarId(avatarId: String) {
        _userAvatarId.value = avatarId
        sharedPrefs.edit().putString("user_avatar_id", avatarId).apply()
    }

    fun setFirstAccess(first: Boolean) {
        _isFirstAccess.value = first
        sharedPrefs.edit().putBoolean("first_access", first).apply()
    }

    fun setTrustedDevice(trusted: Boolean) {
        _trustedDevice.value = trusted
        sharedPrefs.edit().putBoolean("trusted_device", trusted).apply()
    }

    fun setAutoLockTime(minutes: Int) {
        _autoLockTime.value = minutes
        sharedPrefs.edit().putInt("auto_lock_time", minutes).apply()
    }

    fun set2FAEnabled(enabled: Boolean) {
        _is2FAEnabled.value = enabled
        sharedPrefs.edit().putBoolean("two_factor_enabled", enabled).apply()
    }

    fun setTwoFactorType(type: String) {
        _twoFactorType.value = type
        sharedPrefs.edit().putString("two_factor_type", type).apply()
    }

    fun setTwoFactorSecret(secret: String) {
        _twoFactorSecret.value = secret
        sharedPrefs.edit().putString("two_factor_secret", secret).apply()
    }

    fun toggleHideBalances() {
        _hideBalances.value = !_hideBalances.value
        sharedPrefs.edit().putBoolean("hide_balances", _hideBalances.value).apply()
    }

    fun setScreenshotProtected(protected: Boolean) {
        _isScreenshotProtected.value = protected
        sharedPrefs.edit().putBoolean("screenshot_protected", protected).apply()
    }

    fun setRecoveryEmail(email: String) {
        _recoveryEmail.value = email
        sharedPrefs.edit().putString("recovery_email", email).apply()
    }

    fun clearSecuritySettings() {
        setPin("")
        setPasswordValue("")
        setAuthType("NONE")
        setBiometricsEnabled(false)
        set2FAEnabled(false)
    }

    fun unlockScreen(enteredValue: String): Boolean {
        return unlockScreenWithMethod(enteredValue, _authType.value)
    }

    fun unlockScreenWithMethod(enteredValue: String, method: String): Boolean {
        val isCorrect = if (method == "PIN") {
            enteredValue == _pinCode.value
        } else if (method == "PASSWORD") {
            enteredValue == _passwordValue.value
        } else {
            true // NONE
        }

        if (isCorrect) {
            _isScreenLocked.value = false
            _failedAttempts.value = 0
            logAccess(method, success = true)
            updateActivityTimestamp()
            return true
        } else {
            _failedAttempts.value += 1
            logAccess(method, success = false)
            return false
        }
    }

    fun forceSetLocked(locked: Boolean) {
        _isScreenLocked.value = locked
    }

    fun toggleFamilyMode() {
        _familyBudgetMode.value = !_familyBudgetMode.value
    }

    fun toggleOpenFinance() {
        _openFinanceConnected.value = !_openFinanceConnected.value
    }

    // --- Database Writers ---
    fun addAccount(name: String, type: String, balance: Double, color: String = "#2196F3", bankName: String = "", agency: String = "", accountNumber: String = "") {
        viewModelScope.launch {
            repository.insertAccount(FinanceAccount(name = name, type = type, balance = balance, colorHex = color, bankName = bankName, agency = agency, accountNumber = accountNumber))
        }
    }

    fun updateAccount(account: FinanceAccount) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(account: FinanceAccount) {
        viewModelScope.launch {
            repository.deleteAccount(account)
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

    fun updateCreditCard(card: CreditCard) {
        viewModelScope.launch {
            repository.updateCreditCard(card)
        }
    }

    fun deleteCreditCard(card: CreditCard) {
        viewModelScope.launch {
            repository.deleteCreditCard(card)
        }
    }

    // --- Bills To Pay CRUD ---
    fun addBillToPay(name: String, creditor: String, amount: Double, dueDate: Long, status: String = "Pendente", notes: String = "", phone: String = "", debtDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertBillToPay(
                BillToPay(name = name, creditor = creditor, amount = amount, dueDateTimestamp = dueDate, status = status, notes = notes, phone = phone, debtDateTimestamp = debtDate)
            )
        }
    }

    fun updateBillToPay(bill: BillToPay) {
        viewModelScope.launch {
            repository.updateBillToPay(bill)
        }
    }

    fun deleteBillToPay(bill: BillToPay) {
        viewModelScope.launch {
            repository.deleteBillToPay(bill)
        }
    }

    // --- Bills To Receive CRUD ---
    fun addBillToReceive(debtor: String, amount: Double, dueDate: Long, status: String = "Pendente", phone: String = "", notes: String = "", loanDate: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            repository.insertBillToReceive(
                BillToReceive(debtor = debtor, amount = amount, dueDateTimestamp = dueDate, status = status, phone = phone, notes = notes, loanDateTimestamp = loanDate)
            )
        }
    }

    fun updateBillToReceive(bill: BillToReceive) {
        viewModelScope.launch {
            repository.updateBillToReceive(bill)
        }
    }

    fun deleteBillToReceive(bill: BillToReceive) {
        viewModelScope.launch {
            repository.deleteBillToReceive(bill)
        }
    }

    // --- Inventory Items CRUD ---
    fun addInventoryItem(name: String, category: String, estimatedValue: Double, purchaseDate: Long, quantity: Int, notes: String = "", photoUri: String = "") {
        viewModelScope.launch {
            repository.insertInventoryItem(
                InventoryItem(name = name, category = category, estimatedValue = estimatedValue, purchaseDateTimestamp = purchaseDate, quantity = quantity, notes = notes, photoUri = photoUri)
            )
        }
    }

    fun updateInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryItem) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    fun addInvestment(name: String, category: String, qty: Double, buyPrice: Double, currentPrice: Double) {
        viewModelScope.launch {
            repository.insertInvestment(
                InvestmentAsset(name = name, category = category, quantity = qty, purchasePrice = buyPrice, currentPrice = currentPrice)
            )
        }
    }

    fun updateInvestment(investment: InvestmentAsset) {
        viewModelScope.launch {
            repository.updateInvestment(investment)
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

    fun updateGoal(goal: FinancialGoal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    // --- Custom Category Writers ---
    fun addCategory(name: String, type: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCategory(CustomCategory(name = name, type = type, iconName = iconName, colorHex = colorHex))
        }
    }

    fun updateCategory(category: CustomCategory) {
        viewModelScope.launch {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CustomCategory) {
        viewModelScope.launch {
            repository.deleteCategory(category)
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

    // --- Notes Actions ---
    fun addNote(title: String, content: String, category: String = "Geral", isPinned: Boolean = false) {
        viewModelScope.launch {
            repository.insertNote(FinancialNote(title = title, content = content, category = category, isPinned = isPinned))
        }
    }

    fun updateNote(note: FinancialNote) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: FinancialNote) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun toggleNotePin(note: FinancialNote) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
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
    fun assembleFinancialContext(): String {
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
        val isSharing = _isDataSharingEnabled.value
        val context = if (isSharing) assembleFinancialContext() else "Compartilhamento de contexto de finanças desativado pelo usuário. Ofereça conselhos financeiros genéricos sem saber saldos, transações ou contas específicas do usuário."
        
        val userMsg = ChatMessage(text = msg, isUser = true)
        _chatMessages.value = _chatMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val provider = _selectedProvider.value
            val apiKey = getProviderKey(provider)
            val model = _selectedModel.value.ifEmpty { provider.defaultModel }
            val customUrl = getCustomUrl()

            val systemPrompt = """
                Você é o 'Finança AI Coach', um especialista financeiro profissional, amigável, certificado e empático.
                Trabalhe com o usuário para ajudá-lo a economizar, planejar metas, analisar gastos, entender seus investimentos e sugerir melhorias práticas no orçamento doméstico.
                Sempre responda em português brasileiro com formatação organizada, títulos claros e marcadores (bullet points), mantendo uma abordagem construtiva.
                Utilize as informações de contexto de contas/transações reais mandadas para guiar seu aconselhamento, caso enviadas.
            """.trimIndent()

            val fullPrompt = if (isSharing) {
                "Contexto Financeiro:\n$context\n\nPergunta do Usuário:\n$msg"
            } else {
                msg
            }

            val responseText = com.example.data.api.MultiAiClient.generateContent(
                provider = provider,
                apiKey = apiKey,
                model = model,
                prompt = fullPrompt,
                systemInstruction = systemPrompt,
                customUrl = customUrl,
                onTokensEstimated = { input, output ->
                    _totalInputTokens.value += input
                    _totalOutputTokens.value += output
                    sharedPrefs.edit()
                        .putLong("total_input_tokens", _totalInputTokens.value)
                        .putLong("total_output_tokens", _totalOutputTokens.value)
                        .apply()
                }
            )

            // Save inside history
            val record = AnalysisRecord(
                provider = provider.displayName,
                model = model,
                analysisType = "Conversa no Chat",
                prompt = msg,
                response = responseText,
                tokensEstimated = com.example.data.api.MultiAiClient.estimateTokens(fullPrompt) + com.example.data.api.MultiAiClient.estimateTokens(responseText)
            )
            val updatedHistory = listOf(record) + _analysisHistory.value
            _analysisHistory.value = updatedHistory
            saveHistory(updatedHistory)

            val aiMsg = ChatMessage(text = responseText, isUser = false)
            _chatMessages.value = _chatMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }

    fun fetchAutomaticBudgetAnalysis() {
        _isAiLoading.value = true
        val isSharing = _isDataSharingEnabled.value
        val context = if (isSharing) assembleFinancialContext() else "Compartilhamento de contexto de finanças desativado pelo usuário. Faça recomendações gerais de boas práticas de economia."
        
        val prompt = if (isSharing) {
            """
                Por favor, faça uma análise automática detalhada de meus gastos e hábitos de acordo com meu contexto financeiro atual.
                Contexto Financeiro:
                $context

                Forneça:
                1. Sugestões de economia práticas baseadas nas minhas categorias de gastos mais expressivos.
                2. Uma previsão simplificada de potenciais despesas urgentes futuras ou áreas críticas baseada no perfil.
                3. Três insights inteligentes para eu otimizar minhas sobras de salário.
            """.trimIndent()
        } else {
            """
                Por favor, forneça recomendações e insights gerais de inteligência financeira:
                1. Dicas para catalogar e reduzir custos fixos.
                2. Boas práticas para montar uma reserva de emergência resiliente.
                3. Regra dos 50/30/20 explicada de forma prática.
            """.trimIndent()
        }

        viewModelScope.launch {
            val provider = _selectedProvider.value
            val apiKey = getProviderKey(provider)
            val model = _selectedModel.value.ifEmpty { provider.defaultModel }
            val customUrl = getCustomUrl()

            val systemPrompt = "Você é o 'Finança AI Coach', um especialista financeiro profissional de investimentos e orçamento doméstico."

            val responseText = com.example.data.api.MultiAiClient.generateContent(
                provider = provider,
                apiKey = apiKey,
                model = model,
                prompt = prompt,
                systemInstruction = systemPrompt,
                customUrl = customUrl,
                onTokensEstimated = { input, output ->
                    _totalInputTokens.value += input
                    _totalOutputTokens.value += output
                    sharedPrefs.edit()
                        .putLong("total_input_tokens", _totalInputTokens.value)
                        .putLong("total_output_tokens", _totalOutputTokens.value)
                        .apply()
                }
            )

            // Save inside history
            val record = AnalysisRecord(
                provider = provider.displayName,
                model = model,
                analysisType = "Análise de Orçamento",
                prompt = "Crie uma análise automatizada com base nos meus dados de conta.",
                response = responseText,
                tokensEstimated = com.example.data.api.MultiAiClient.estimateTokens(prompt) + com.example.data.api.MultiAiClient.estimateTokens(responseText)
            )
            val updatedHistory = listOf(record) + _analysisHistory.value
            _analysisHistory.value = updatedHistory
            saveHistory(updatedHistory)

            _aiReport.value = responseText
            _isAiLoading.value = false
        }
    }

    fun exportBackupToJson(): String {
        return try {
            val root = org.json.JSONObject()
            
            val accs = org.json.JSONArray()
            for (acc in accounts.value) {
                val obj = org.json.JSONObject()
                obj.put("id", acc.id)
                obj.put("name", acc.name)
                obj.put("type", acc.type)
                obj.put("balance", acc.balance)
                obj.put("colorHex", acc.colorHex)
                obj.put("bankName", acc.bankName)
                obj.put("agency", acc.agency)
                obj.put("accountNumber", acc.accountNumber)
                accs.put(obj)
            }
            root.put("accounts", accs)

            val txs = org.json.JSONArray()
            for (tx in transactions.value) {
                val obj = org.json.JSONObject()
                obj.put("id", tx.id)
                obj.put("title", tx.title)
                obj.put("amount", tx.amount)
                obj.put("type", tx.type)
                obj.put("category", tx.category)
                obj.put("subcategory", tx.subcategory)
                obj.put("dateTimestamp", tx.dateTimestamp)
                obj.put("accountId", tx.accountId)
                obj.put("isRecurring", tx.isRecurring)
                obj.put("isPaid", tx.isPaid)
                obj.put("totalInstallments", tx.totalInstallments)
                obj.put("currentInstallment", tx.currentInstallment)
                obj.put("creditCardId", tx.creditCardId ?: -1)
                txs.put(obj)
            }
            root.put("transactions", txs)

            val cards = org.json.JSONArray()
            for (c in creditCards.value) {
                val obj = org.json.JSONObject()
                obj.put("id", c.id)
                obj.put("name", c.name)
                obj.put("limitAmount", c.limitAmount)
                obj.put("closingDay", c.closingDay)
                obj.put("dueDay", c.dueDay)
                obj.put("cardBrand", c.cardBrand)
                obj.put("colorHex", c.colorHex)
                cards.put(obj)
            }
            root.put("creditCards", cards)

            val invs = org.json.JSONArray()
            for (inv in investments.value) {
                val obj = org.json.JSONObject()
                obj.put("id", inv.id)
                obj.put("name", inv.name)
                obj.put("category", inv.category)
                obj.put("quantity", inv.quantity)
                obj.put("purchasePrice", inv.purchasePrice)
                obj.put("currentPrice", inv.currentPrice)
                obj.put("dateTimestamp", inv.dateTimestamp)
                invs.put(obj)
            }
            root.put("investments", invs)

            val gls = org.json.JSONArray()
            for (g in goals.value) {
                val obj = org.json.JSONObject()
                obj.put("id", g.id)
                obj.put("title", g.title)
                obj.put("targetAmount", g.targetAmount)
                obj.put("currentAmount", g.currentAmount)
                obj.put("targetDateTimestamp", g.targetDateTimestamp)
                obj.put("category", g.category)
                gls.put(obj)
            }
            root.put("goals", gls)

            val billsPay = org.json.JSONArray()
            for (b in billsToPay.value) {
                val obj = org.json.JSONObject()
                obj.put("id", b.id)
                obj.put("name", b.name)
                obj.put("creditor", b.creditor)
                obj.put("amount", b.amount)
                obj.put("dueDateTimestamp", b.dueDateTimestamp)
                obj.put("status", b.status)
                obj.put("notes", b.notes)
                billsPay.put(obj)
            }
            root.put("billsToPay", billsPay)

            val billsRec = org.json.JSONArray()
            for (b in billsToReceive.value) {
                val obj = org.json.JSONObject()
                obj.put("id", b.id)
                obj.put("debtor", b.debtor)
                obj.put("amount", b.amount)
                obj.put("dueDateTimestamp", b.dueDateTimestamp)
                obj.put("status", b.status)
                obj.put("phone", b.phone)
                obj.put("notes", b.notes)
                billsRec.put(obj)
            }
            root.put("billsToReceive", billsRec)

            root.toString(2)
        } catch (e: Exception) {
            "Erro ao exportar: ${e.message}"
        }
    }

    fun restoreBackupFromJson(json: String): Boolean {
        return try {
            val root = org.json.JSONObject(json)
            viewModelScope.launch {
                if (root.has("accounts")) {
                    val arr = root.getJSONArray("accounts")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertAccount(
                            FinanceAccount(
                                name = obj.getString("name"),
                                type = obj.getString("type"),
                                balance = obj.getDouble("balance"),
                                colorHex = obj.optString("colorHex", "#3F51B5"),
                                bankName = obj.optString("bankName", ""),
                                agency = obj.optString("agency", ""),
                                accountNumber = obj.optString("accountNumber", "")
                            )
                        )
                    }
                }
                if (root.has("creditCards")) {
                    val arr = root.getJSONArray("creditCards")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertCreditCard(
                            CreditCard(
                                name = obj.getString("name"),
                                limitAmount = obj.getDouble("limitAmount"),
                                closingDay = obj.getInt("closingDay"),
                                dueDay = obj.getInt("dueDay"),
                                cardBrand = obj.optString("cardBrand", "Visa"),
                                colorHex = obj.optString("colorHex", "#E91E63")
                            )
                        )
                    }
                }
                if (root.has("transactions")) {
                    val arr = root.getJSONArray("transactions")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val ccId = obj.optInt("creditCardId", -1)
                        repository.insertTransaction(
                            FinanceTransaction(
                                title = obj.getString("title"),
                                amount = obj.getDouble("amount"),
                                type = obj.getString("type"),
                                category = obj.getString("category"),
                                subcategory = obj.optString("subcategory", ""),
                                dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis()),
                                accountId = obj.optInt("accountId", 1),
                                isRecurring = obj.optBoolean("isRecurring", false),
                                isPaid = obj.optBoolean("isPaid", true),
                                totalInstallments = obj.optInt("totalInstallments", 1),
                                currentInstallment = obj.optInt("currentInstallment", 1),
                                creditCardId = if (ccId == -1) null else ccId
                            )
                        )
                    }
                }
                if (root.has("investments")) {
                    val arr = root.getJSONArray("investments")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertInvestment(
                            InvestmentAsset(
                                name = obj.getString("name"),
                                category = obj.getString("category"),
                                quantity = obj.getDouble("quantity"),
                                purchasePrice = obj.getDouble("purchasePrice"),
                                currentPrice = obj.getDouble("currentPrice"),
                                dateTimestamp = obj.optLong("dateTimestamp", System.currentTimeMillis())
                            )
                        )
                    }
                }
                if (root.has("goals")) {
                    val arr = root.getJSONArray("goals")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertGoal(
                            FinancialGoal(
                                title = obj.getString("title"),
                                targetAmount = obj.getDouble("targetAmount"),
                                currentAmount = obj.getDouble("currentAmount"),
                                targetDateTimestamp = obj.optLong("targetDateTimestamp", System.currentTimeMillis()),
                                category = obj.optString("category", "SHORT_TERM")
                            )
                        )
                    }
                }
                if (root.has("billsToPay")) {
                    val arr = root.getJSONArray("billsToPay")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertBillToPay(
                            BillToPay(
                                name = obj.getString("name"),
                                creditor = obj.getString("creditor"),
                                amount = obj.getDouble("amount"),
                                dueDateTimestamp = obj.getLong("dueDateTimestamp"),
                                status = obj.optString("status", "Pendente"),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }
                if (root.has("billsToReceive")) {
                    val arr = root.getJSONArray("billsToReceive")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        repository.insertBillToReceive(
                            BillToReceive(
                                debtor = obj.getString("debtor"),
                                amount = obj.getDouble("amount"),
                                dueDateTimestamp = obj.getLong("dueDateTimestamp"),
                                status = obj.optString("status", "Pendente"),
                                phone = obj.optString("phone", ""),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}

// --- ViewModel Factory ---
class FinanceViewModelFactory(private val application: Application, private val repository: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
