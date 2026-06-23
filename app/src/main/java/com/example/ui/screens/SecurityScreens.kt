package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWizardScreen(
    viewModel: FinanceViewModel,
    onComplete: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedAuthMethod by remember { mutableStateOf("PIN") } // "PIN" or "PASSWORD"
    
    // Step 1 Form Inputs
    var inputName by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf("avatar_1") }
    var selectedCurrency by remember { mutableStateOf("BRL") }

    // Form Inputs (Security)
    var isPin4Digits by remember { mutableStateOf(true) } // true = 4 digits, false = 6 digits
    var inputPin by remember { mutableStateOf("") }
    var inputPinConfirm by remember { mutableStateOf("") }
    
    var inputPassword by remember { mutableStateOf("") }
    var inputPasswordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    var enableBiometrics by remember { mutableStateOf(true) }
    var backupEmail by remember { mutableStateOf("") }
    var enable2FA by remember { mutableStateOf(false) }
    var selected2FAType by remember { mutableStateOf("EMAIL") } // "EMAIL" or "TOTP"
    
    var validationError by remember { mutableStateOf("") }

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .testTag("setup_wizard_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Boas-vindas • Passo $step de 5",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    LinearProgressIndicator(
                        progress = { step / 5f },
                        modifier = Modifier
                            .width(100.dp)
                            .height(8.dp),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (step) {
                    1 -> {
                        // Step 1: Profile Customization
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User avatar logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = "Seja bem-vindo ao Finança AI!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Por favor, insira seus dados básicos para configurar o seu perfil totalmente personalizado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Seu Nome ou Apelido") },
                            placeholder = { Text("Ex: Igor") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("setup_name_input"),
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )

                        Text(
                            text = "Escolha um Avatar:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Avatar Palette Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
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
                                val isSelected = selectedAvatar == avId
                                val icon = when (avId) {
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
                                        .size(45.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .clickable { selectedAvatar = avId }
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = avId,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "Moeda Padrão de Exibição:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val currencies = listOf(
                                "BRL" to "R$ Real",
                                "USD" to "$ Dólar",
                                "EUR" to "€ Euro"
                            )
                            currencies.forEach { (code, name) ->
                                val isSelected = selectedCurrency == code
                                Card(
                                    onClick = { selectedCurrency = code },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    ),
                                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(code, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(name.substringAfter(" "), style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        if (validationError.isNotEmpty()) {
                            Text(
                                text = validationError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(
                            onClick = {
                                if (inputName.isBlank()) {
                                    validationError = "Por favor, insira o seu nome!"
                                } else {
                                    validationError = ""
                                    viewModel.setUserName(inputName)
                                    viewModel.setUserAvatarId(selectedAvatar)
                                    viewModel.setCurrency(selectedCurrency)
                                    step = 2
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("setup_wizard_step1_next")
                        ) {
                            Text("Continuar")
                        }
                    }

                    2 -> {
                        // Step 2: Welcome & Method Selection
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Shield logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = "Proteja Seus Dados Financeiros",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Configure seus métodos de autenticação de segurança para criptografar chaves de API e proteger seu painel financeiro.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Escolha seu método alternativo principal:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        // Selector
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                onClick = { selectedAuthMethod = "PIN" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedAuthMethod == "PIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (selectedAuthMethod == "PIN") BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(Icons.Default.Pin, contentDescription = "PIN")
                                    Column {
                                        Text("Código PIN de Segurança", fontWeight = FontWeight.Bold)
                                        Text("Mais rápido. Código PIN composto apenas por 4 ou 6 números.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }

                            Card(
                                onClick = { selectedAuthMethod = "PASSWORD" },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedAuthMethod == "PASSWORD") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                ),
                                border = if (selectedAuthMethod == "PASSWORD") BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Icon(Icons.Default.Password, contentDescription = "Password")
                                    Column {
                                        Text("Senha Alfanumérica", fontWeight = FontWeight.Bold)
                                        Text("Mais segura. Misture letras, números e símbolos especiais.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Voltar")
                            }

                            Button(
                                onClick = { step = 3 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continuar")
                            }
                        }
                    }

                    3 -> {
                        // Step 3: Set Credentials
                        Icon(
                            imageVector = if (selectedAuthMethod == "PIN") Icons.Default.Pin else Icons.Default.Password,
                            contentDescription = "Lock logo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = "Cadastrar Credencial",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        if (selectedAuthMethod == "PIN") {
                            // PIN Input fields
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilterChip(
                                    selected = isPin4Digits,
                                    onClick = {
                                        isPin4Digits = true
                                        inputPin = ""
                                        inputPinConfirm = ""
                                    },
                                    label = { Text("PIN de 4 dígitos") }
                                )
                                FilterChip(
                                    selected = !isPin4Digits,
                                    onClick = {
                                        isPin4Digits = false
                                        inputPin = ""
                                        inputPinConfirm = ""
                                    },
                                    label = { Text("PIN de 6 dígitos") }
                                )
                            }

                            val neededLen = if (isPin4Digits) 4 else 6
                            OutlinedTextField(
                                value = inputPin,
                                onValueChange = { if (it.length <= neededLen && it.all { c -> c.isDigit() }) inputPin = it },
                                label = { Text("Código PIN") },
                                placeholder = { Text("Ex: ${"1".repeat(neededLen)}") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = inputPinConfirm,
                                onValueChange = { if (it.length <= neededLen && it.all { c -> c.isDigit() }) inputPinConfirm = it },
                                label = { Text("Confirmar Código PIN") },
                                placeholder = { Text("Repita o PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // PASSWORD Input
                            OutlinedTextField(
                                value = inputPassword,
                                onValueChange = { inputPassword = it },
                                label = { Text("Senha Segura") },
                                placeholder = { Text("Mínimo de 6 caracteres") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggles visibility"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = inputPasswordConfirm,
                                onValueChange = { inputPasswordConfirm = it },
                                label = { Text("Confirmar Senha Segura") },
                                placeholder = { Text("Repita a mesma senha") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (validationError.isNotEmpty()) {
                            Text(
                                text = validationError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    validationError = ""
                                    step = 2
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Voltar")
                            }

                            Button(
                                onClick = {
                                    validationError = ""
                                    if (selectedAuthMethod == "PIN") {
                                        val length = if (isPin4Digits) 4 else 6
                                        if (inputPin.length != length) {
                                            validationError = "O PIN precisa ter exatamente $length dígitos numéricos!"
                                        } else if (inputPin != inputPinConfirm) {
                                            validationError = "Os PINs digitados não são iguais!"
                                        } else {
                                            step = 4
                                        }
                                    } else {
                                        if (inputPassword.length < 6) {
                                            validationError = "A senha deve ter pelo menos 6 caracteres!"
                                        } else if (inputPassword != inputPasswordConfirm) {
                                            validationError = "As senhas digitadas não coincidem!"
                                        } else {
                                            step = 4
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Próximo")
                            }
                        }
                    }

                    4 -> {
                        // Step 4: Biometrics
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Fingerprint",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        )

                        Text(
                            text = "Sensores Biométricos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Ative a biometria para abrir o aplicativo rapidamente usando sua impressão digital ou reconhecimento facial compatíveis.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Face, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Ativar Login Biométrico", fontWeight = FontWeight.Bold)
                                }
                                Switch(
                                    checked = enableBiometrics,
                                    onCheckedChange = { enableBiometrics = it }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 3 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Voltar")
                            }

                            Button(
                                onClick = { step = 5 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Avançar")
                            }
                        }
                    }

                    5 -> {
                        // Step 5: Email Recovery & 2FA Setup
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Email setup",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )

                        Text(
                            text = "E-mail de Recuperação & 2FA",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Insira um endereço de e-mail confiável para receber tokens de recuperação em caso de bloqueio.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = backupEmail,
                            onValueChange = { backupEmail = it },
                            label = { Text("E-mail de Segurança") },
                            placeholder = { Text("seguro@email.com") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth()
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Configurar Autenticação em Duas Etapas (2FA)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Switch(
                                        checked = enable2FA,
                                        onCheckedChange = { enable2FA = it }
                                    )
                                }

                                if (enable2FA) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        FilterChip(
                                            selected = selected2FAType == "EMAIL",
                                            onClick = { selected2FAType = "EMAIL" },
                                            label = { Text("Código por E-mail") }
                                        )
                                        FilterChip(
                                            selected = selected2FAType == "TOTP",
                                            onClick = { selected2FAType = "TOTP" },
                                            label = { Text("Autenticador TOTP") }
                                        )
                                    }
                                    if (selected2FAType == "TOTP") {
                                        Text(
                                            text = "Chave TOTP Secreta: GA3XMYKTOB2GQ4DP. Copie e adicione ao seu aplicativo Google Authenticator ou similar.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 11.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        if (validationError.isNotEmpty()) {
                            Text(
                                text = validationError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 4 },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Voltar")
                            }

                            Button(
                                onClick = {
                                    if (backupEmail.isBlank() || !backupEmail.contains("@")) {
                                        validationError = "Por favor, insira um e-mail válido!"
                                        return@Button
                                    }
                                    
                                    // Complete Setup
                                    if (selectedAuthMethod == "PIN") {
                                        viewModel.setPin(inputPin)
                                        viewModel.setAuthType("PIN")
                                    } else {
                                        viewModel.setPasswordValue(inputPassword)
                                        viewModel.setAuthType("PASSWORD")
                                    }
                                    
                                    viewModel.setBiometricsEnabled(enableBiometrics)
                                    viewModel.setRecoveryEmail(backupEmail)
                                    viewModel.set2FAEnabled(enable2FA)
                                    viewModel.setTwoFactorType(selected2FAType)
                                    
                                    // Confirming name/avatar/currency persist safely
                                    viewModel.setUserName(inputName)
                                    viewModel.setUserAvatarId(selectedAvatar)
                                    viewModel.setCurrency(selectedCurrency)

                                    viewModel.setFirstAccess(false)
                                    viewModel.forceSetLocked(false)
                                    onComplete()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Concluir")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityLockScreen(
    viewModel: FinanceViewModel,
    activity: androidx.fragment.app.FragmentActivity
) {
    val authType by viewModel.authType.collectAsState()
    val pinState by viewModel.pinCode.collectAsState()
    val passwordState by viewModel.passwordValue.collectAsState()
    val isBiometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val failedAttempts by viewModel.failedAttempts.collectAsState()
    val recoveryEmailStr by viewModel.recoveryEmail.collectAsState()

    var enteredValue by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    
    // Recovery states
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryEmailInput by remember { mutableStateOf("") }
    var recoverySentToken by remember { mutableStateOf(false) }
    var recoveryTokenValue by remember { mutableStateOf("") }
    var recoveryNewPassphrase by remember { mutableStateOf("") }
    var recoverySuccessMsg by remember { mutableStateOf("") }

    // Biometric Trigger States
    var showSimulatedBiometricDialog by remember { mutableStateOf(false) }
    var simulatedScanningProgress by remember { mutableStateOf(0f) }
    var simulatedSuccessState by remember { mutableStateOf(false) }

    fun runActualOrCreateSimulatedBiometrics() {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            // Devices with actual biometric sensors enrolled
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        errorMessage = "Biometria Erro: $errString"
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.forceSetLocked(false)
                        viewModel.logAccess("BIOMETRIA", success = true)
                        viewModel.updateActivityTimestamp()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        errorMessage = "Reconhecimento biométrico falhou!"
                        viewModel.logAccess("BIOMETRIA", success = false)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Finança AI Segurança")
                .setSubtitle("Desbloqueio com Biometria")
                .setDescription("Toque no sensor biométrico ou use o reconhecimento facial")
                .setNegativeButtonText("Acesso via PIN/Senha")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // FALLBACK SIMULATION (For emulators or devices without physical biometric setup)
            showSimulatedBiometricDialog = true
            simulatedScanningProgress = 0f
            simulatedSuccessState = false
        }
    }

    // Automatically trigger biometrics on start if enabled
    LaunchedEffect(isBiometricsEnabled) {
        if (isBiometricsEnabled) {
            runActualOrCreateSimulatedBiometrics()
        }
    }

    // Simulated scanning animation coroutine
    if (showSimulatedBiometricDialog) {
        LaunchedEffect(showSimulatedBiometricDialog) {
            while (simulatedScanningProgress < 1f) {
                delay(50)
                simulatedScanningProgress += 0.05f
            }
            simulatedSuccessState = true
            delay(800)
            showSimulatedBiometricDialog = false
            viewModel.forceSetLocked(false)
            viewModel.logAccess("BIOMETRIA (SIMULAÇÃO)", success = true)
            viewModel.updateActivityTimestamp()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Icon(
                imageVector = if (failedAttempts >= 5) Icons.Default.LockPerson else Icons.Default.Lock,
                contentDescription = "Secured",
                tint = if (failedAttempts >= 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )

            Text(
                text = "Finança AI Seguro",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (failedAttempts >= 5) {
                Text(
                    text = "Acesso bloqueado por segurança devido a excesso de tentativas incorretas ($failedAttempts). Recupere o acesso usando seu e-mail.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text = if (authType == "PIN") "Digite seu PIN de segurança privado para acessar os recursos"
                    else "Insira sua senha alfanumérica de segurança",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (failedAttempts < 5) {
                if (authType == "PIN") {
                    val codeLength = pinState.length
                    // Indicator circle bullets
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until codeLength) {
                            val entered = i < enteredValue.length
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Keypad
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val keyRows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("Limpar", "0", "Corrigir")
                        )

                        for (row in keyRows) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (key in row) {
                                    IconButton(
                                        onClick = {
                                            when (key) {
                                                "Limpar" -> {
                                                    enteredValue = ""
                                                    errorMessage = ""
                                                }
                                                "Corrigir" -> {
                                                    if (enteredValue.isNotEmpty()) {
                                                        enteredValue = enteredValue.dropLast(1)
                                                    }
                                                }
                                                else -> {
                                                    if (enteredValue.length < codeLength) {
                                                        enteredValue += key
                                                        errorMessage = ""
                                                        if (enteredValue.length == codeLength) {
                                                            val success = viewModel.unlockScreenWithMethod(enteredValue, "PIN")
                                                            if (!success) {
                                                                errorMessage = "PIN Incorreto! Tente de novo."
                                                                enteredValue = ""
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(
                                                color = if (key == "Limpar" || key == "Corrigir") MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primaryContainer,
                                                shape = CircleShape
                                            )
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = if (key.length > 2) 11.sp else 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (key == "Limpar" || key == "Corrigir") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Password Text Form
                    var passwordVisible by remember { mutableStateOf(false) }
                    
                    OutlinedTextField(
                        value = enteredValue,
                        onValueChange = { enteredValue = it },
                        label = { Text("Insira sua Senha") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle text"
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val success = viewModel.unlockScreenWithMethod(enteredValue, "PASSWORD")
                            if (!success) {
                                errorMessage = "Senha incorreta!"
                                enteredValue = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Desbloquear")
                    }
                }
            }

            // Quick Scan Button if Biometrics Enabled
            if (isBiometricsEnabled && failedAttempts < 5) {
                OutlinedButton(
                    onClick = { runActualOrCreateSimulatedBiometrics() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Usar Biometria")
                }
            }

            // Recovery / Esqueci Code link
            TextButton(
                onClick = {
                    showRecoveryDialog = true
                    recoveryEmailInput = ""
                    recoverySentToken = false
                    recoverySuccessMsg = ""
                }
            ) {
                Text(
                    text = "Esqueci minhas credenciais / Recuperar Acesso",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }
    }

    // SIMULATED BIOMETRIC DIALOG SENSOR VIEW
    if (showSimulatedBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showSimulatedBiometricDialog = false },
            title = {
                Text(
                    "Autenticação Biométrica",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(12.dp)
                    ) {
                        if (simulatedSuccessState) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Identified",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(56.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Scanning",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                    }

                    if (simulatedSuccessState) {
                        Text(
                            "Impressão digital identificada! Acesso concedido.",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            "Simulando varredura eletrônica do leitor físico...",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        LinearProgressIndicator(
                            progress = { simulatedScanningProgress },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {}
        )
    }

    // CONFIGURABLE RECOVERY ACCESS DIALOG (MOCK EMAIL VERIFICATION AND PASSWORD RESET)
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("Recuperação por E-mail") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (recoverySuccessMsg.isNotEmpty()) {
                        Text(
                            text = recoverySuccessMsg,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    } else if (!recoverySentToken) {
                        Text(
                            "Insira o mesmo e-mail de segurança cadastrado durante o primeiro acesso para receber um token experimental de liberação.",
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = recoveryEmailInput,
                            onValueChange = { recoveryEmailInput = it },
                            label = { Text("E-mail Cadastrado") },
                            placeholder = { Text("exemplo@email.com") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            "Um token de recuperação foi enviado para $recoveryEmailInput. Insira o código recebido abaixo e crie uma nova proteção.",
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = recoveryTokenValue,
                            onValueChange = { recoveryTokenValue = it },
                            label = { Text("Token de Confirmação (Digite 123456)") },
                            placeholder = { Text("Ex: 123456") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        OutlinedTextField(
                            value = recoveryNewPassphrase,
                            onValueChange = { recoveryNewPassphrase = it },
                            label = { Text("Senha do Novo PIN ou Código") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (recoverySuccessMsg.isNotEmpty()) {
                            showRecoveryDialog = false
                        } else if (!recoverySentToken) {
                            if (recoveryEmailInput.trim().lowercase() == recoveryEmailStr.trim().lowercase() || recoveryEmailInput.contains("@")) {
                                recoverySentToken = true
                            } else {
                                errorMessage = "E-mail incorreto!"
                            }
                        } else {
                            if (recoveryTokenValue == "123456" && recoveryNewPassphrase.isNotEmpty()) {
                                if (authType == "PIN") {
                                    viewModel.setPin(recoveryNewPassphrase)
                                } else {
                                    viewModel.setPasswordValue(recoveryNewPassphrase)
                                }
                                viewModel.forceSetLocked(false)
                                viewModel.logAccess("REC_EMAIL_CODE", success = true)
                                recoverySuccessMsg = "Acesso recuperado com sucesso! Sua credencial de entrada foi redefinida."
                            } else {
                                errorMessage = "Token incorreto!"
                            }
                        }
                    }
                ) {
                    Text(if (recoverySuccessMsg.isNotEmpty()) "Ok" else if (!recoverySentToken) "Enviar Código" else "Confirmar Alteração")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Fechar")
                }
            }
        )
    }
}

// SENSITIVE LOCK INTERCEPTION VIEW SCREEN COMPOSABLE OVERLAY
@Composable
fun SensitiveLockInterceptionLayer(
    viewModel: FinanceViewModel,
    areaName: String,
    activity: androidx.fragment.app.FragmentActivity,
    onSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val isBiometricsEnabled by viewModel.isBiometricsEnabled.collectAsState()
    val authType by viewModel.authType.collectAsState()
    val pinState by viewModel.pinCode.collectAsState()
    
    var showSelfMockScan by remember { mutableStateOf(false) }
    var scanProgress by remember { mutableStateOf(0f) }
    var rawInputText by remember { mutableStateOf("") }
    var internalError by remember { mutableStateOf("") }

    // Auto trigger actual biometric flow if enrolled
    fun triggerSectionBiometrics() {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )

        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        internalError = "Erro: $errString"
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.logAccess("SENSIT_SEC_$areaName", success = true)
                        onSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        internalError = "Falha no reconhecimento biométrico"
                        viewModel.logAccess("SENSIT_SEC_$areaName", success = false)
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Seção Confidencial")
                .setSubtitle("Área: $areaName")
                .setDescription("Verifique seus dados biométricos cadastrados para entrar")
                .setNegativeButtonText("Inserir PIN/Senha")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            showSelfMockScan = true
            scanProgress = 0f
        }
    }

    LaunchedEffect(isBiometricsEnabled) {
        if (isBiometricsEnabled) {
            triggerSectionBiometrics()
        }
    }

    if (showSelfMockScan) {
        LaunchedEffect(showSelfMockScan) {
            while (scanProgress < 1f) {
                delay(50)
                scanProgress += 0.05f
            }
            delay(500)
            showSelfMockScan = false
            onSuccess()
            viewModel.logAccess("SENSIT_SEC_$areaName", success = true)
        }
    }

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
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = "Lock",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Área Sensível Protegida",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Para acessar a seção \"$areaName\", verifique seu método de segurança cadastrado para garantir que você é o proprietário das informações.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            if (authType == "PIN") {
                val neededLen = pinState.length
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = {
                        if (it.length <= neededLen && it.all { c -> c.isDigit() }) {
                            rawInputText = it
                            if (it.length == neededLen) {
                                val ok = viewModel.unlockScreenWithMethod(it, "PIN")
                                if (ok) {
                                    viewModel.logAccess("SENSIT_SEC_$areaName", success = true)
                                    onSuccess()
                                } else {
                                    internalError = "PIN incorreto!"
                                    rawInputText = ""
                                }
                            }
                        }
                    },
                    label = { Text("Digite seu PIN de $neededLen números") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth()
                )
            } else if (authType == "PASSWORD") {
                OutlinedTextField(
                    value = rawInputText,
                    onValueChange = { rawInputText = it },
                    label = { Text("Digite sua Senha") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        val ok = viewModel.unlockScreenWithMethod(rawInputText, "PASSWORD")
                        if (ok) {
                            viewModel.logAccess("SENSIT_SEC_$areaName", success = true)
                            onSuccess()
                        } else {
                            internalError = "Senha incorreta!"
                            rawInputText = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verificar Acesso")
                }
            }

            if (internalError.isNotEmpty()) {
                Text(
                    text = internalError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isBiometricsEnabled) {
                OutlinedButton(
                    onClick = { triggerSectionBiometrics() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                    Text("Usar Biometria")
                }
            }

            TextButton(
                onClick = onCancel,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Voltar para Dashboard")
            }
        }
    }

    // SIMULATED DIALOG LOCK
    if (showSelfMockScan) {
        AlertDialog(
            onDismissRequest = { showSelfMockScan = false },
            title = { Text("Autenticação Biométrica", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(72.dp)
                    )
                    Text("Escaneando sua biometria local...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    LinearProgressIndicator(progress = { scanProgress }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {}
        )
    }
}
