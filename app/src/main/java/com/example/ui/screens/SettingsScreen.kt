package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import com.example.data.api.AiProvider
import com.example.data.api.ConnectionStatus
import com.example.ui.viewmodel.AnalysisRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userAvatarId by viewModel.userAvatarId.collectAsState()
    val currencyState by viewModel.currency.collectAsState()
    val isDarkThemeState by viewModel.isDarkTheme.collectAsState()
    val pinState by viewModel.pinCode.collectAsState()
    val familyBudgetMode by viewModel.familyBudgetMode.collectAsState()
    val openFinanceConnected by viewModel.openFinanceConnected.collectAsState()

    // Advanced Security State Collectors
    val authType by viewModel.authType.collectAsState()
    val passwordState by viewModel.passwordValue.collectAsState()
    val isBiometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val trustedDevice by viewModel.trustedDevice.collectAsState()
    val autoLockTime by viewModel.autoLockTime.collectAsState()
    val is2FAActive by viewModel.is2FAEnabled.collectAsState()
    val twoFactorType by viewModel.twoFactorType.collectAsState()
    val twoFactorSecret by viewModel.twoFactorSecret.collectAsState()
    val hideBalances by viewModel.hideBalances.collectAsState()
    val isScreenshotProtected by viewModel.isScreenshotProtected.collectAsState()
    val recoveryEmailStr by viewModel.recoveryEmail.collectAsState()
    val accessLogsState by viewModel.accessLogs.collectAsState()

    var showSensitiveVerifyActionDialog by remember { mutableStateOf(false) }
    var pendingSecurityVerifyAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    var showChangePinStateDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showBackupEmailChangeDialog by remember { mutableStateOf(false) }
    var showLogsHistoryDialog by remember { mutableStateOf(false) }

    var inputSettingPin by remember { mutableStateOf("") }
    var inputSettingPinConfirm by remember { mutableStateOf("") }
    
    var inputSettingPassword by remember { mutableStateOf("") }
    var inputSettingPasswordConfirm by remember { mutableStateOf("") }
    var inputSettingEmailInput by remember { mutableStateOf("") }

    // AI configurations state
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val isDataSharingEnabled by viewModel.isDataSharingEnabled.collectAsState()
    val totalInputTokens by viewModel.totalInputTokens.collectAsState()
    val totalOutputTokens by viewModel.totalOutputTokens.collectAsState()
    val analysisHistory by viewModel.analysisHistory.collectAsState()
    val connectionStatus by viewModel.connectionStatus.collectAsState()

    var apiKeyBuffer by remember { mutableStateOf("") }
    var modelCustomBuffer by remember { mutableStateOf("") }
    var customUrlBuffer by remember { mutableStateOf("") }
    var isApiKeyVisible by remember { mutableStateOf(false) }
    var showContextPreview by remember { mutableStateOf(false) }
    var showHistoryRecords by remember { mutableStateOf(false) }
    var selectedRecordForDetail by remember { mutableStateOf<AnalysisRecord?>(null) }
    var actionFeedbackMessage by remember { mutableStateOf("") }

    LaunchedEffect(selectedProvider) {
        apiKeyBuffer = viewModel.getProviderKey(selectedProvider)
        modelCustomBuffer = viewModel.getProviderModel(selectedProvider)
        customUrlBuffer = viewModel.getCustomUrl()
    }

    var showPinDialog by remember { mutableStateOf(false) }
    var enteredPinValue by remember { mutableStateOf("") }

    // Import simulated statement states
    var rawImportText by remember { mutableStateOf("") }
    var importStatusMessage by remember { mutableStateOf("") }

    // Mock statement templates
    val mockOfxTemplate = """
<OFX>
<STMTTRN>
<TRNAMT>-120.50</TRNAMT>
<MEMO>Fogo de Chao Churrascaria</MEMO>
</STMTTRN>
<STMTTRN>
<TRNAMT>2500.00</TRNAMT>
<MEMO>PIX Recebido S.A.</MEMO>
</STMTTRN>
<STMTTRN>
<TRNAMT>-45.90</TRNAMT>
<MEMO>Farmacia Sao Joao</MEMO>
</STMTTRN>
</OFX>
    """.trimIndent()

    val mockCsvTemplate = """
Supermercado Guanabara, 185.20, EXPENSE, Alimentacao
Bônus de Diretoria, 1000.00, INCOME, Comissões
Assinatura Spotify, 34.90, EXPENSE, Assinaturas
    """.trimIndent()

    Box(
        modifier = Modifier.fillMaxSize().testTag("settings_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 800.dp)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Settings Header
        Column(modifier = Modifier.padding(bottom = 4.dp)) {
            Text(
                text = "SISTEMA E PREFERÊNCIAS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Configurações Globais",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Divider()

        // --- SEÇÃO DE PERFIL DO USUÁRIO ---
        Text("Meu Perfil", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth().testTag("user_profile_card")) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                
                // Avatar row & info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val avatarColor = when (userAvatarId) {
                        "avatar_1" -> Color(0xFF14B8A6) // Teal
                        "avatar_2" -> Color(0xFF8B5CF6) // Purple
                        "avatar_3" -> Color(0xFFF59E0B) // Amber
                        "avatar_4" -> Color(0xFFEC4899) // Pink
                        "avatar_5" -> Color(0xFF3B82F6) // Blue
                        "avatar_6" -> Color(0xFF10B981) // Emerald
                        else -> MaterialTheme.colorScheme.primary
                    }

                    val icon = when (userAvatarId) {
                        "avatar_1" -> Icons.Default.Person
                        "avatar_2" -> Icons.Default.Face
                        "avatar_3" -> Icons.Default.AccountCircle
                        "avatar_4" -> Icons.Default.Favorite
                        "avatar_5" -> Icons.Default.Star
                        "avatar_6" -> Icons.Default.Pets
                        else -> Icons.Default.Person
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(avatarColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = "User Avatar Icon",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Configurações de Conta e Moeda",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Editable Name Text Field
                OutlinedTextField(
                    value = userName,
                    onValueChange = { viewModel.setUserName(it) },
                    label = { Text("Nome ou Apelido") },
                    placeholder = { Text("Seu nome") },
                    leadingIcon = { Icon(Icons.Default.Edit, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("settings_name_input_field")
                )

                // Selectable Avatars grid title
                Text(
                    text = "Foto de Perfil (Avatar):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // Row of Avatars selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val avatars = listOf(
                        "avatar_1" to Color(0xFF14B8A6), // Teal
                        "avatar_2" to Color(0xFF8B5CF6), // Purple
                        "avatar_3" to Color(0xFFF59E0B), // Amber
                        "avatar_4" to Color(0xFFEC4899), // Pink
                        "avatar_5" to Color(0xFF3B82F6), // Blue
                        "avatar_6" to Color(0xFF10B981)  // Emerald
                    )
                    avatars.forEach { (avId, color) ->
                        val isSelected = userAvatarId == avId
                        val iconOption = when (avId) {
                            "avatar_1" -> Icons.Default.Person
                            "avatar_2" -> Icons.Default.Face
                            "avatar_3" -> Icons.Default.AccountCircle
                            "avatar_4" -> Icons.Default.Favorite
                            "avatar_5" -> Icons.Default.Star
                            "avatar_6" -> Icons.Default.Pets
                            else -> Icons.Default.Person
                        }
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { viewModel.setUserAvatarId(avId) }
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconOption,
                                contentDescription = avId,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Default currency controls in standard Material form
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Moeda Preferencial", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Configura o símbolo monetário global", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("BRL" to "R$", "USD" to "$", "EUR" to "€").forEach { (curr, sym) ->
                            val selected = currencyState == curr
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setCurrency(curr) },
                                label = { Text(sym) }
                            )
                        }
                    }
                }
            }
        }

        // Theme and Currency controls
        Text("Personalização", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("Tema Visual") },
                    supportingContent = { Text(if (isDarkThemeState) "Tema Escuro Ativado" else "Tema Claro Ativado") },
                    leadingContent = { Icon(Icons.Default.Palette, null) },
                    trailingContent = {
                        Switch(
                            checked = isDarkThemeState,
                            onCheckedChange = { viewModel.toggleTheme() }
                        )
                    }
                )
            }
        }

        // Security controls
        Text("Segurança & Proteção de Dados", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Feature 1: Show/Hide Balance Toggle
                ListItem(
                    headlineContent = { Text("Ocultar Valores Financeiros") },
                    supportingContent = { Text("Esconde saldos e investimentos com '••••••' em todas as telas") },
                    leadingContent = { Icon(if (hideBalances) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Switch(
                            checked = hideBalances,
                            onCheckedChange = { viewModel.toggleHideBalances() }
                        )
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 2: Core Autenticação Credential (PIN / Password Selection)
                ListItem(
                    headlineContent = { Text("Credencial Alternativa Principal") },
                    supportingContent = { 
                        Text(
                            when (authType) {
                                "PIN" -> "PIN de Segurança Ativo (${pinState.length} dígitos) ✓"
                                "PASSWORD" -> "Senha Alfanumérica Ativa ✓"
                                else -> "Sem proteção de tela ativa (Inseguro)"
                            }
                        ) 
                    },
                    leadingContent = { Icon(Icons.Default.LockOpen, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (authType != "NONE") {
                                OutlinedButton(
                                    onClick = {
                                        pendingSecurityVerifyAction = {
                                            viewModel.clearSecuritySettings()
                                        }
                                        showSensitiveVerifyActionDialog = true
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Desativar")
                                }
                            }
                            Button(
                                onClick = {
                                    if (authType == "PASSWORD") {
                                        showChangePasswordDialog = true
                                    } else {
                                        showChangePinStateDialog = true
                                    }
                                }
                            ) {
                                Text(if (authType != "NONE") "Alterar" else "Configurar")
                            }
                        }
                    }
                )

                if (authType == "NONE") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showChangePinStateDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Pin, null, modifier = Modifier.padding(end = 4.dp))
                            Text("Usar PIN Numérico")
                        }
                        TextButton(
                            onClick = { showChangePasswordDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Password, null, modifier = Modifier.padding(end = 4.dp))
                            Text("Usar Senha")
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 3: Biometrics Toggle (Requires verification for change)
                ListItem(
                    headlineContent = { Text("Autenticação Biométrica") },
                    supportingContent = { Text("Use sua digital ou face para desbloqueio rápido") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Switch(
                            checked = isBiometricsEnabled,
                            onCheckedChange = { checked ->
                                if (authType == "NONE") {
                                    showChangePinStateDialog = true // Require a backup cred first
                                } else {
                                    pendingSecurityVerifyAction = {
                                        viewModel.setBiometricsEnabled(checked)
                                    }
                                    showSensitiveVerifyActionDialog = true
                                }
                            }
                        )
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 4: Lembrar Dispositivo Confiável
                ListItem(
                    headlineContent = { Text("Dispositivo Confiável") },
                    supportingContent = { Text("Mantém ativo sem pedir PIN ou Biometria ao reabrir") },
                    leadingContent = { Icon(Icons.Default.Devices, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Switch(
                            checked = trustedDevice,
                            onCheckedChange = { checked ->
                                if (authType != "NONE") {
                                    pendingSecurityVerifyAction = {
                                        viewModel.setTrustedDevice(checked)
                                    }
                                    showSensitiveVerifyActionDialog = true
                                } else {
                                    viewModel.setTrustedDevice(checked)
                                }
                            }
                        )
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 5: Prevent Screenshots
                ListItem(
                    headlineContent = { Text("Prevenir Capturas de Tela") },
                    supportingContent = { Text("Bloqueia prints, gravações de tela e vazamentos visuais") },
                    leadingContent = { Icon(Icons.Default.NoPhotography, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        Switch(
                            checked = isScreenshotProtected,
                            onCheckedChange = { checked ->
                                viewModel.setScreenshotProtected(checked)
                            }
                        )
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 6: Auto Lock Inactivity Settings
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tempo Limite de Inatividade antes de Bloquear",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        listOf(0 to "Imediato", 1 to "1 min", 5 to "5 min", 15 to "15 min", 30 to "30 min").forEach { (min, label) ->
                            val selected = autoLockTime == min
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.setAutoLockTime(min) },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 7: E-mail de Recuperação Config
                ListItem(
                    headlineContent = { Text("E-mail de Recuperação de Acesso") },
                    supportingContent = { Text(recoveryEmailStr) },
                    leadingContent = { Icon(Icons.Default.MailOutline, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = {
                        IconButton(
                            onClick = {
                                inputSettingEmailInput = recoveryEmailStr
                                showBackupEmailChangeDialog = true
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit recovery mail")
                        }
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 8: Two-Factor Authentication (2FA) Setup
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Autenticação em Duas Etapas (2FA)", fontWeight = FontWeight.SemiBold)
                            Text("Aumenta a barreira contra logins invasores", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = is2FAActive,
                            onCheckedChange = { checked ->
                                if (authType == "NONE") {
                                    showChangePinStateDialog = true
                                } else {
                                    pendingSecurityVerifyAction = {
                                        viewModel.set2FAEnabled(checked)
                                    }
                                    showSensitiveVerifyActionDialog = true
                                }
                            }
                        )
                    }

                    if (is2FAActive) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FilterChip(
                                selected = twoFactorType == "EMAIL",
                                onClick = { viewModel.setTwoFactorType("EMAIL") },
                                label = { Text("Código por E-mail") }
                            )
                            FilterChip(
                                selected = twoFactorType == "TOTP",
                                onClick = { viewModel.setTwoFactorType("TOTP") },
                                label = { Text("TOTP (Google Auth)") }
                            )
                        }
                        if (twoFactorType == "TOTP") {
                            Text("Chave Secreta TOTP: $twoFactorSecret", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // Feature 9: Access Logging History Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogsHistoryDialog = true }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HistoryToggleOff, null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Histórico de Acessos", fontWeight = FontWeight.Bold)
                            Text("Visualize datas, horários e tipos de logins realizados", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
        }

        // Open Finance & Family Sync
        Text("Integrações Premium & Open Finance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("Integração Open Finance") },
                    supportingContent = { Text(if (openFinanceConnected) "Conectado de forma segura e criptografada ✓" else "Conectar contas de outros bancos automaticamente") },
                    leadingContent = { Icon(Icons.Default.CloudSync, null) },
                    trailingContent = {
                        Switch(
                            checked = openFinanceConnected,
                            onCheckedChange = { viewModel.toggleOpenFinance() }
                        )
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                ListItem(
                    headlineContent = { Text("Controle Financeiro Familiar") },
                    supportingContent = { Text(if (familyBudgetMode) "Compartilhamento do orçamento ativo em tempo real ✓" else "Gerencie o caixa em conjunto com seus familiares") },
                    leadingContent = { Icon(Icons.Default.Group, null) },
                    trailingContent = {
                        Switch(
                            checked = familyBudgetMode,
                            onCheckedChange = { viewModel.toggleFamilyMode() }
                        )
                    }
                )
            }
        }

        // AI Settings Segment
        Text("Configurações de Inteligência Artificial", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Personalize o motor de inteligência artificial de seu Finança AI. Insira suas chaves de API particulares para usar provedores líderes e reduzir gastos do servidor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selectable Providers Chips Row
                Text(
                    text = "Provedor de IA Selecionado",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    val providers = AiProvider.values()
                    items(providers.size) { index ->
                        val prov = providers[index]
                        val isSelected = selectedProvider == prov
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setAiProvider(prov)
                                actionFeedbackMessage = "Provedor alterado para ${prov.displayName}"
                            },
                            label = { Text(prov.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, null, modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }

                // API Key input field
                if (selectedProvider != AiProvider.GOOGLE_DEFAULT) {
                    Text(
                        text = "Chave de API (Secret Key)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = apiKeyBuffer,
                        onValueChange = { apiKeyBuffer = it },
                        placeholder = { Text("Insira sua chave API de ${selectedProvider.displayName}") },
                        label = { Text("Chave API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.VpnKey, null) },
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Visualizar chave"
                                )
                            }
                        },
                        visualTransformation = if (isApiKeyVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Model override field
                    Text(
                        text = "Modelo de Linguagem (L.L.M.)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = modelCustomBuffer,
                        onValueChange = { modelCustomBuffer = it },
                        placeholder = { Text("Ex: ${selectedProvider.defaultModel}") },
                        label = { Text("Modelo Selecionado") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Code, null) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // If Custom, show URL input field
                    if (selectedProvider == AiProvider.CUSTOM) {
                        Text(
                            text = "URL Base da API Customizada",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = customUrlBuffer,
                            onValueChange = { customUrlBuffer = it },
                            placeholder = { Text("Ex: https://api.together.xyz/v1") },
                            label = { Text("Base URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Link, null) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Key management Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.setProviderKey(selectedProvider, apiKeyBuffer)
                                viewModel.setProviderModel(selectedProvider, modelCustomBuffer)
                                if (selectedProvider == AiProvider.CUSTOM) {
                                    viewModel.setCustomUrl(customUrlBuffer)
                                }
                                actionFeedbackMessage = "Chave e Modelo salvos para ${selectedProvider.displayName}!"
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salvar Chave", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.removeProviderKey(selectedProvider)
                                apiKeyBuffer = ""
                                actionFeedbackMessage = "Chave de ${selectedProvider.displayName} removida."
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remover", fontSize = 12.sp)
                        }
                    }
                } else {
                    // Google default notification
                    Surface(
                         color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                         shape = RoundedCornerShape(12.dp),
                         modifier = Modifier.fillMaxWidth()
                    ) {
                         Row(
                             modifier = Modifier.padding(12.dp),
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                             Spacer(modifier = Modifier.width(12.dp))
                             Column {
                                 Text("Google (Padrão do App) Selecionado", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                 Text("O aplicativo faz chamadas gratuitas utilizando o modelo '${selectedProvider.defaultModel}'. Nenhuma configuração ou faturamento é necessário.", style = MaterialTheme.typography.bodySmall)
                             }
                         }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Test connection row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ElevatedButton(
                        onClick = { viewModel.testAiConnection() }
                    ) {
                        Icon(Icons.Default.NetworkCheck, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Testar Conexão", fontSize = 12.sp)
                    }

                    // Visual connection badges
                    val statusText: String
                    val statusColor: Color
                    val statusTextCol: Color
                    when (connectionStatus) {
                        ConnectionStatus.CONNECTED -> {
                            statusText = "Conectado ✓"
                            statusColor = Color(0xFFE8F5E9)
                            statusTextCol = Color(0xFF2E7D32)
                        }
                        ConnectionStatus.INVALID_KEY -> {
                            statusText = "Chave Inválida ✗"
                            statusColor = Color(0xFFFFEBEE)
                            statusTextCol = Color(0xFFC62828)
                        }
                        ConnectionStatus.NO_CONNECTION -> {
                            statusText = "Sem Conexão ⚠"
                            statusColor = Color(0xFFFFF3E0)
                            statusTextCol = Color(0xFFE65100)
                        }
                        ConnectionStatus.NOT_TESTED -> {
                            statusText = "Não Testado"
                            statusColor = MaterialTheme.colorScheme.surfaceVariant
                            statusTextCol = MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    }

                    Surface(
                        color = statusColor,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = statusText,
                            color = statusTextCol,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Action Feedback Message simulation
                if (actionFeedbackMessage.isNotEmpty()) {
                    LaunchedEffect(actionFeedbackMessage) {
                        kotlinx.coroutines.delay(3500)
                        actionFeedbackMessage = ""
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = actionFeedbackMessage,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Privacy block
                Text(
                    text = "Privacidade & Compartilhamento",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )

                ListItem(
                    headlineContent = { Text("Compartilhar Histórico Financeiro", fontSize = 14.sp) },
                    supportingContent = { Text("Permite analisar saldos, investimentos e transações de extrato nas respostas de IA.", fontSize = 11.sp) },
                    trailingContent = {
                        Switch(
                            checked = isDataSharingEnabled,
                            onCheckedChange = { viewModel.setDataSharingEnabled(it) }
                        )
                    },
                    modifier = Modifier.padding(0.dp)
                )

                OutlinedButton(
                    onClick = { showContextPreview = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Dataset, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Exibir Prévia dos Dados Compartilhados", fontSize = 12.sp)
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // Metrics & Token stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Consumo Estimado de Tokens", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Acumulado de chamadas locais", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { viewModel.clearTokenUsage() }) {
                        Text("Zerar", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Enviado (Input)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            Text("$totalInputTokens tkn", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Recebi (Output)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                            Text("$totalOutputTokens tkn", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                // History block
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Histórico de Análises (${analysisHistory.size})", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row {
                        if (analysisHistory.isNotEmpty()) {
                            TextButton(onClick = { viewModel.clearAnalysisHistory() }) {
                                Text("Limpar Tudo", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }
                        }
                        IconButton(onClick = { showHistoryRecords = !showHistoryRecords }) {
                            Icon(
                                imageVector = if (showHistoryRecords) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expandir"
                            )
                        }
                    }
                }

                if (showHistoryRecords) {
                    if (analysisHistory.isEmpty()) {
                        Text("Nenhuma análise de IA registrada ainda.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            analysisHistory.take(8).forEach { record ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedRecordForDetail = record },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(record.analysisType, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            val descriptionSnippet = record.prompt.replace("Contexto Financeiro:\n", "").take(30)
                                            Text("${descriptionSnippet}...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                            Text("Mod: ${record.model} (${record.provider})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontSize = 9.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { viewModel.removeAnalysisRecord(record.id) }) {
                                                Icon(Icons.Default.Delete, "Excluir", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                            }
                                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Statement Import Parser block
        Text("Importação de Extrato Financeiro", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Importe dados bancários copiados de arquivos OFX, CSV ou extratos gerais em massa diretamente para o banco de dados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Paste buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { rawImportText = mockOfxTemplate },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Modelo OFX", fontSize = 12.sp)
                    }
                    Button(
                        onClick = { rawImportText = mockCsvTemplate },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Modelo CSV", fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = rawImportText,
                    onValueChange = { rawImportText = it },
                    label = { Text("Conteúdo do arquivo Extrato") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (rawImportText.isNotEmpty()) {
                                val parseCount = if (rawImportText.contains("<OFX>", ignoreCase = true)) {
                                    viewModel.importOfxText(rawImportText)
                                } else {
                                    viewModel.importCsvText(rawImportText)
                                }
                                importStatusMessage = "Importação Concluída com Sucesso! $parseCount novas transações registradas."
                                rawImportText = ""
                            } else {
                                importStatusMessage = "Por favor, preencha ou carregue um modelo acima primeiro."
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, "import")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Processar e Salvar")
                    }
                }

                if (importStatusMessage.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = importStatusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(72.dp))
    }

    // --- PIN configuration dialog ---
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Configurar PIN de Bloqueio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Digite um PIN numérico de segurança para trancar e proteger o aplicativo Finança AI contra intrusos.", fontSize = 14.sp)
                    OutlinedTextField(
                        value = enteredPinValue,
                        onValueChange = { enteredPinValue = it },
                        label = { Text("Senha do PIN") },
                        placeholder = { Text("Ex: 1234") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setPin(enteredPinValue)
                        enteredPinValue = ""
                        showPinDialog = false
                    }
                ) {
                    Text("Configurar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setPin("") // clear PIN
                        enteredPinValue = ""
                        showPinDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Desativar Proteção")
                }
            }
        )
    }

    // --- Sensitive Verification Dialog ---
    if (showSensitiveVerifyActionDialog) {
        var enteredCredentialSecret by remember { mutableStateOf("") }
        var inputOtpToken2fa by remember { mutableStateOf("") }
        var otpCodeSent by remember { mutableStateOf(false) }
        var credentialErrorMessage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = {
                showSensitiveVerifyActionDialog = false
                pendingSecurityVerifyAction = null
                credentialErrorMessage = ""
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.error)
                    Text("Operação de Alta Segurança")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Para prosseguir com as alterações críticas de segurança ou chaves, insira sua senha ou seu PIN atual.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = enteredCredentialSecret,
                        onValueChange = { enteredCredentialSecret = it },
                        label = { Text(if (authType == "PIN") "Digite o PIN Atual" else "Digite a Senha Atual") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = if (authType == "PIN") KeyboardOptions(keyboardType = KeyboardType.NumberPassword) else KeyboardOptions.Default,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Check if 2FA is active
                    if (is2FAActive) {
                        Text(
                            "Além de sua credencial principal, a verificação em duas etapas (2FA) está ativa neste dispositivo.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        if (twoFactorType == "EMAIL") {
                            if (!otpCodeSent) {
                                Button(
                                    onClick = { otpCodeSent = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.SendToMobile, null, modifier = Modifier.padding(end = 4.dp))
                                    Text("Enviar Token para $recoveryEmailStr")
                                }
                            } else {
                                OutlinedTextField(
                                    value = inputOtpToken2fa,
                                    onValueChange = { inputOtpToken2fa = it },
                                    label = { Text("Código de 6 dígitos recebido (Digite 123456)") },
                                    placeholder = { Text("Ex: 123456") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            // TOTP layout
                            OutlinedTextField(
                                value = inputOtpToken2fa,
                                onValueChange = { inputOtpToken2fa = it },
                                label = { Text("Insira o Código de 6 dígitos do Google Authenticator") },
                                placeholder = { Text("TOTP Token") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (credentialErrorMessage.isNotEmpty()) {
                        Text(
                            text = credentialErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val passMatches = if (authType == "PIN") {
                            enteredCredentialSecret == pinState
                        } else {
                            enteredCredentialSecret == passwordState
                        }

                        if (!passMatches) {
                            credentialErrorMessage = "Credencial incorreta! Verifique os dados inseridos."
                            return@Button
                        }

                        if (is2FAActive) {
                            val tokenMatches = if (twoFactorType == "EMAIL") {
                                inputOtpToken2fa == "123456"
                            } else {
                                inputOtpToken2fa.length == 6 // Simulation accepts 6 characters
                            }

                            if (!tokenMatches) {
                                credentialErrorMessage = "O Token 2FA digitado é inválido!"
                                return@Button
                            }
                        }

                        // Success! Trigger action
                        pendingSecurityVerifyAction?.invoke()
                        showSensitiveVerifyActionDialog = false
                        pendingSecurityVerifyAction = null
                        credentialErrorMessage = ""
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSensitiveVerifyActionDialog = false
                        pendingSecurityVerifyAction = null
                        credentialErrorMessage = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // PIN configuration dialog
    if (showChangePinStateDialog) {
        var pinErrorTxt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                showChangePinStateDialog = false
                inputSettingPin = ""
                inputSettingPinConfirm = ""
                pinErrorTxt = ""
            },
            title = { Text("Configurar PIN de Bloqueio") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Defina um código numérico de 4 ou 6 dígitos para carregar nas autenticações físicas.", fontSize = 13.sp)

                    OutlinedTextField(
                        value = inputSettingPin,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) inputSettingPin = it },
                        label = { Text("Novo PIN de Segurança") },
                        placeholder = { Text("Digite 4 ou 6 números") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputSettingPinConfirm,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) inputSettingPinConfirm = it },
                        label = { Text("Confirme o PIN") },
                        placeholder = { Text("Repita o código PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinErrorTxt.isNotEmpty()) {
                        Text(pinErrorTxt, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputSettingPin.length != 4 && inputSettingPin.length != 6) {
                            pinErrorTxt = "O PIN deve conter exatamente 4 ou 6 dígitos numéricos."
                            return@Button
                        }
                        if (inputSettingPin != inputSettingPinConfirm) {
                            pinErrorTxt = "Os PINs digitados não combinam."
                            return@Button
                        }

                        viewModel.setPin(inputSettingPin)
                        showChangePinStateDialog = false
                        inputSettingPin = ""
                        inputSettingPinConfirm = ""
                        pinErrorTxt = ""
                    }
                ) {
                    Text("Salvar PIN")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePinStateDialog = false
                        inputSettingPin = ""
                        inputSettingPinConfirm = ""
                        pinErrorTxt = ""
                    }
                ) {
                    Text("Voltar")
                }
            }
        )
    }

    // Password configuration dialog
    if (showChangePasswordDialog) {
        var passwordErrorTxt by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                inputSettingPassword = ""
                inputSettingPasswordConfirm = ""
                passwordErrorTxt = ""
            },
            title = { Text("Configurar Senha Alfanumérica") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Crie uma palavra-passe complexa contendo letras e números.", fontSize = 13.sp)

                    OutlinedTextField(
                        value = inputSettingPassword,
                        onValueChange = { inputSettingPassword = it },
                        label = { Text("Nova Senha") },
                        placeholder = { Text("Insira sua senha") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputSettingPasswordConfirm,
                        onValueChange = { inputSettingPasswordConfirm = it },
                        label = { Text("Confirme a nova Senha") },
                        placeholder = { Text("Confirme") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordErrorTxt.isNotEmpty()) {
                        Text(passwordErrorTxt, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputSettingPassword.length < 6) {
                            passwordErrorTxt = "A senha deve conter no mínimo 6 caracteres contendo letras/números."
                            return@Button
                        }
                        if (inputSettingPassword != inputSettingPasswordConfirm) {
                            passwordErrorTxt = "As senhas não combinam."
                            return@Button
                        }

                        viewModel.setPasswordValue(inputSettingPassword)
                        showChangePasswordDialog = false
                        inputSettingPassword = ""
                        inputSettingPasswordConfirm = ""
                        passwordErrorTxt = ""
                    }
                ) {
                    Text("Salvar Senha")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showChangePasswordDialog = false
                        inputSettingPassword = ""
                        inputSettingPasswordConfirm = ""
                        passwordErrorTxt = ""
                    }
                ) {
                    Text("Voltar")
                }
            }
        )
    }

    // E-mail de Recuperação dialog
    if (showBackupEmailChangeDialog) {
        var mailError by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = {
                showBackupEmailChangeDialog = false
                mailError = ""
            },
            title = { Text("E-mail de Recuperação") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Insira seu endereço de e-mail confiável para receber chaves de recuperação ou autenticações 2FA por e-mail.", fontSize = 13.sp)

                    OutlinedTextField(
                        value = inputSettingEmailInput,
                        onValueChange = { inputSettingEmailInput = it },
                        label = { Text("Endereço de E-mail") },
                        placeholder = { Text("usuario@email.com") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (mailError.isNotEmpty()) {
                        Text(mailError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!inputSettingEmailInput.contains("@") || !inputSettingEmailInput.contains(".")) {
                            mailError = "Insira um endereço de e-mail válido!"
                            return@Button
                        }
                        
                        viewModel.setRecoveryEmail(inputSettingEmailInput)
                        showBackupEmailChangeDialog = false
                        mailError = ""
                    }
                ) {
                    Text("Salvar E-mail")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBackupEmailChangeDialog = false
                        mailError = ""
                    }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Access log history modal
    if (showLogsHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showLogsHistoryDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.HistoryToggleOff, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logs Físicos de Acesso", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Lista dos últimos acessos registrados nas últimas conexões de segurança do dispositivo:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        if (accessLogsState.isEmpty()) {
                            Text(
                                "Nenhum histórico de acesso de segurança registrado localmente ainda.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(top = 80.dp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                accessLogsState.forEach { log ->
                                    val formattedTime = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(log.timestamp))
                                    val successText = if (log.success) "Sucesso" else "Falhou"
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (log.success) {
                                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            } else {
                                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                            }
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${log.authMethod} - $successText",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (log.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = formattedTime,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Aparelho: ${log.deviceName}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showLogsHistoryDialog = false }) {
                    Text("Fechar")
                }
            },
            dismissButton = {
                if (accessLogsState.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            viewModel.clearLogs()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Limpar Histórico")
                    }
                }
            }
        )
    }

    // Shared dataset preview dialog
    if (showContextPreview) {
        val sharedContext = viewModel.assembleFinancialContext()
        AlertDialog(
            onDismissRequest = { showContextPreview = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dataset, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dados Compartilhados com a IA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Representação exata do contexto de contas que acompanha suas mensagens:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Divider()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp)
                    ) {
                        Text(
                            text = if (isDataSharingEnabled) sharedContext else "[COMPARTILHAMENTO DESATIVADO EM PRIVACIDADE]\nNenhum saldo ou transação será enviado.",
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showContextPreview = false }) {
                    Text("Fechar")
                }
            }
        )
    }

    // Detail analysis record dialog
    if (selectedRecordForDetail != null) {
        val record = selectedRecordForDetail!!
        AlertDialog(
            onDismissRequest = { selectedRecordForDetail = null },
            title = {
                Column {
                    Text(record.analysisType, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Motor: ${record.model} (${record.provider})", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()).heightIn(max = 400.dp)
                ) {
                    Text("Comando / Pergunta Enviada:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(record.prompt, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(10.dp))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text("Resposta Gerada pelo Cérebro IA:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(record.response, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(12.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedRecordForDetail = null }) {
                    Text("Fechar")
                }
            }
        )
    }
    }
}
