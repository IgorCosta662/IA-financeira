package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: FinanceViewModel) {
    val currencyState by viewModel.currency.collectAsState()
    val isDarkThemeState by viewModel.isDarkTheme.collectAsState()
    val pinState by viewModel.pinCode.collectAsState()
    val familyBudgetMode by viewModel.familyBudgetMode.collectAsState()
    val openFinanceConnected by viewModel.openFinanceConnected.collectAsState()

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

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                ListItem(
                    headlineContent = { Text("Moeda Padrão") },
                    supportingContent = { Text("Selecione sua localidade para formatação automática") },
                    leadingContent = { Icon(Icons.Default.MonetizationOn, null) },
                    trailingContent = {
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
                )
            }
        }

        // Security controls
        Text("Segurança & Chaves", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("Bloqueio por PIN de Segurança") },
                    supportingContent = { Text(if (pinState.isNotEmpty()) "PIN Configurado ✓" else "Sem proteção ativa") },
                    leadingContent = { Icon(Icons.Default.Lock, null) },
                    trailingContent = {
                        Button(onClick = { showPinDialog = true }) {
                            Text(if (pinState.isNotEmpty()) "Alterar" else "Ativar")
                        }
                    }
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                ListItem(
                    headlineContent = { Text("Autenticação Biométrica") },
                    supportingContent = { Text("Simulador de Biometria ativado como secundário") },
                    leadingContent = { Icon(Icons.Default.Fingerprint, null) },
                    trailingContent = {
                        Switch(checked = true, onCheckedChange = {})
                    }
                )
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
    }
}
