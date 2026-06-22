package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.data.database.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : androidx.fragment.app.FragmentActivity() {

    // Lazy initialization of database and repository inside parent Activity
    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "financa_ai_database_v2"
        ).fallbackToDestructiveMigration().build()
    }

    private val repository by lazy {
         FinanceRepository(database.financeDao)
    }

    // Instantiation utilizing ViewModelProvider Factory
    private val financeViewModel: FinanceViewModel by viewModels {
        FinanceViewModelFactory(application, repository)
    }

    override fun onResume() {
        super.onResume()
        financeViewModel.checkInactivityLock()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        financeViewModel.updateActivityTimestamp()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by financeViewModel.isDarkTheme.collectAsState()
            val isScreenshotProtected by financeViewModel.isScreenshotProtected.collectAsState()
            
            // Dynamic screenshot secure flag protection
            LaunchedEffect(isScreenshotProtected) {
                if (isScreenshotProtected) {
                    window.setFlags(
                        android.view.WindowManager.LayoutParams.FLAG_SECURE,
                        android.view.WindowManager.LayoutParams.FLAG_SECURE
                    )
                } else {
                    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
                }
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val isScreenLocked by financeViewModel.isScreenLocked.collectAsState()
                val isFirstAccess by financeViewModel.isFirstAccess.collectAsState()

                if (isFirstAccess) {
                    SetupWizardScreen(
                        viewModel = financeViewModel,
                        onComplete = {
                            // Onboarding registration confirmed
                        }
                    )
                } else if (isScreenLocked) {
                    SecurityLockScreen(
                        viewModel = financeViewModel,
                        activity = this@MainActivity
                    )
                } else {
                    // Main app content
                    MainLayoutContainer(viewModel = financeViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayoutContainer(viewModel: FinanceViewModel) {
    var activeTab by remember { mutableStateOf(0) }
    var sensitiveVerifiedTab by remember { mutableStateOf<Int?>(null) }
    var showSavingsChallengesScreen by remember { mutableStateOf(false) }
    val familyBudgetMode by viewModel.familyBudgetMode.collectAsState()
    val openFinanceConnected by viewModel.openFinanceConnected.collectAsState()
    val authType by viewModel.authType.collectAsState()
    val localCtx = androidx.compose.ui.platform.LocalContext.current
    val hostActivity = localCtx as? androidx.fragment.app.FragmentActivity

    @Composable
    fun RenderSensitiveScreen(targetTab: Int, screenContent: @Composable () -> Unit) {
        if (authType != "NONE" && sensitiveVerifiedTab != targetTab && hostActivity != null) {
            val sectionName = when (targetTab) {
                3 -> "Investimentos"
                4 -> "Gênio Coach"
                5 -> "Configurações de Segurança"
                else -> "Área Restrita"
            }
            SensitiveLockInterceptionLayer(
                viewModel = viewModel,
                areaName = sectionName,
                activity = hostActivity,
                onSuccess = {
                    sensitiveVerifiedTab = targetTab
                },
                onCancel = {
                    activeTab = 0
                }
            )
        } else {
            screenContent()
        }
    }

    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isWideScreen = configuration.screenWidthDp >= 600

    if (showSavingsChallengesScreen) {
        SavingsChallengesScreen(
            viewModel = viewModel,
            onBack = { showSavingsChallengesScreen = false }
        )
    } else {
        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    NavigationRailItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, "Geral") },
                        label = { Text("Painel") },
                        modifier = Modifier.testTag("nav_dashboard")
                    )
                    NavigationRailItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.ReceiptLong, "Lançamentos") },
                        label = { Text("Extrato") },
                        modifier = Modifier.testTag("nav_transactions")
                    )
                    NavigationRailItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.CreditCard, "Contas") },
                        label = { Text("Contas") },
                        modifier = Modifier.testTag("nav_accounts")
                    )
                    NavigationRailItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.PieChart, "Ativos") },
                        label = { Text("Investir") },
                        modifier = Modifier.testTag("nav_investments")
                    )
                    NavigationRailItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(Icons.Default.AutoAwesome, "Gênio Coach") },
                        label = { Text("Gênio") },
                        modifier = Modifier.testTag("nav_genius")
                    )
                    NavigationRailItem(
                        selected = activeTab == 5,
                        onClick = { activeTab = 5 },
                        icon = { Icon(Icons.Default.Settings, "Configurações") },
                        label = { Text("Config") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                    NavigationRailItem(
                        selected = activeTab == 6,
                        onClick = { activeTab = 6 },
                        icon = { Icon(Icons.Default.StickyNote2, "Notas") },
                        label = { Text("Notas") },
                        modifier = Modifier.testTag("nav_notes")
                    )
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Finança AI",
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (familyBudgetMode) {
                                        Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                            Text("Familiar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                        }
                                    }
                                    if (openFinanceConnected) {
                                        Badge(containerColor = Color(0xFFE8F5E9)) {
                                            Text("Open Finance ✓", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                        }
                                    }
                                }
                            },
                            actions = {
                                Text(
                                    text = viewModel.getCurrencySymbol(),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .padding(end = 16.dp)
                                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (activeTab) {
                            0 -> {
                                sensitiveVerifiedTab = null
                                DashboardScreen(
                                    viewModel = viewModel,
                                    onNavigateToChallenges = { showSavingsChallengesScreen = true }
                                )
                            }
                            1 -> {
                                sensitiveVerifiedTab = null
                                TransactionsScreen(viewModel = viewModel)
                            }
                            2 -> {
                                sensitiveVerifiedTab = null
                                AccountsScreen(viewModel = viewModel)
                            }
                            3 -> RenderSensitiveScreen(3) { InvestmentsScreen(viewModel = viewModel) }
                            4 -> RenderSensitiveScreen(4) { AssistantScreen(viewModel = viewModel) }
                            5 -> RenderSensitiveScreen(5) { SettingsScreen(viewModel = viewModel) }
                            6 -> {
                                sensitiveVerifiedTab = null
                                NotesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        } else {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Finança AI",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (familyBudgetMode) {
                                    Badge(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text("Familiar", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }
                                if (openFinanceConnected) {
                                    Badge(containerColor = Color(0xFFE8F5E9)) {
                                        Text("Open Finance ✓", fontSize = 10.sp, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        },
                        actions = {
                            Text(
                                text = viewModel.getCurrencySymbol(),
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        NavigationBarItem(
                            selected = activeTab == 0,
                            onClick = { activeTab = 0 },
                            icon = { Icon(Icons.Default.Dashboard, "Geral") },
                            label = { Text("Painel") },
                            modifier = Modifier.testTag("nav_dashboard")
                        )
                        NavigationBarItem(
                            selected = activeTab == 1,
                            onClick = { activeTab = 1 },
                            icon = { Icon(Icons.Default.ReceiptLong, "Lançamentos") },
                            label = { Text("Extrato") },
                            modifier = Modifier.testTag("nav_transactions")
                        )
                        NavigationBarItem(
                            selected = activeTab == 2,
                            onClick = { activeTab = 2 },
                            icon = { Icon(Icons.Default.CreditCard, "Contas") },
                            label = { Text("Contas") },
                            modifier = Modifier.testTag("nav_accounts")
                        )
                        NavigationBarItem(
                            selected = activeTab == 3,
                            onClick = { activeTab = 3 },
                            icon = { Icon(Icons.Default.PieChart, "Ativos") },
                            label = { Text("Investir") },
                            modifier = Modifier.testTag("nav_investments")
                        )
                        NavigationBarItem(
                            selected = activeTab == 4,
                            onClick = { activeTab = 4 },
                            icon = { Icon(Icons.Default.AutoAwesome, "Gênio Coach") },
                            label = { Text("Gênio") },
                            modifier = Modifier.testTag("nav_genius")
                        )
                        NavigationBarItem(
                            selected = activeTab == 5,
                            onClick = { activeTab = 5 },
                            icon = { Icon(Icons.Default.Settings, "Configurações") },
                            label = { Text("Config") },
                            modifier = Modifier.testTag("nav_settings")
                        )
                        NavigationBarItem(
                            selected = activeTab == 6,
                            onClick = { activeTab = 6 },
                            icon = { Icon(Icons.Default.StickyNote2, "Notas") },
                            label = { Text("Notas") },
                            modifier = Modifier.testTag("nav_notes")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (activeTab) {
                        0 -> {
                            sensitiveVerifiedTab = null
                            DashboardScreen(
                                viewModel = viewModel,
                                onNavigateToChallenges = { showSavingsChallengesScreen = true }
                            )
                        }
                        1 -> {
                            sensitiveVerifiedTab = null
                            TransactionsScreen(viewModel = viewModel)
                        }
                        2 -> {
                            sensitiveVerifiedTab = null
                            AccountsScreen(viewModel = viewModel)
                        }
                        3 -> RenderSensitiveScreen(3) { InvestmentsScreen(viewModel = viewModel) }
                        4 -> RenderSensitiveScreen(4) { AssistantScreen(viewModel = viewModel) }
                        5 -> RenderSensitiveScreen(5) { SettingsScreen(viewModel = viewModel) }
                        6 -> {
                            sensitiveVerifiedTab = null
                            NotesScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(viewModel: FinanceViewModel) {
    var inputPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Secured",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "Finança AI Protegido",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Insira seu PIN numérico de segurança para acessar seus saldos confidenciais",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Asterisk / bullets representing entered password
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0..3) {
                    val entered = i < inputPin.length
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = if (entered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Numerical soft-keyboard in 3x4 grid
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("Limpar", "0", "Corrigir")
                )

                for (row in buttonRows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (key in row) {
                            Button(
                                onClick = {
                                    when (key) {
                                        "Limpar" -> {
                                            inputPin = ""
                                            errorMessage = ""
                                        }
                                        "Corrigir" -> {
                                            if (inputPin.isNotEmpty()) {
                                                inputPin = inputPin.dropLast(1)
                                            }
                                        }
                                        else -> {
                                            if (inputPin.length < 4) {
                                                inputPin += key
                                                errorMessage = ""
                                                
                                                if (inputPin.length == 4) {
                                                    val success = viewModel.unlockScreen(inputPin)
                                                    if (!success) {
                                                        errorMessage = "PIN Incorreto! Tente de novo."
                                                        inputPin = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                shape = CircleShape,
                                modifier = Modifier.size(72.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (key == "Limpar" || key == "Corrigir") MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = if (key == "Limpar" || key == "Corrigir") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Text(
                                    text = key,
                                    fontSize = if (key.length > 2) 11.sp else 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
