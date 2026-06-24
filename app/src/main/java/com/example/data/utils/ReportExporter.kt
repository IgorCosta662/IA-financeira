package com.example.data.utils

import com.example.data.model.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    fun generateHtmlReport(
        userName: String,
        accounts: List<FinanceAccount>,
        transactions: List<FinanceTransaction>,
        investments: List<InvestmentAsset>,
        goals: List<FinancialGoal>,
        challenges: List<SavingsChallenge>,
        billsToPay: List<BillToPay>,
        billsToReceive: List<BillToReceive>,
        currencySymbol: String = "R$"
    ): String {
        val locale = Locale("pt", "BR")
        val currencyFormatter = NumberFormat.getCurrencyInstance(locale).apply {
            val symbol = if (currencySymbol == "USD") "$" else if (currencySymbol == "EUR") "€" else "R$"
            val decimalFormatSymbols = (this as java.text.DecimalFormat).decimalFormatSymbols
            decimalFormatSymbols.currencySymbol = symbol
            this.decimalFormatSymbols = decimalFormatSymbols
        }
        val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", locale)
        val simpleDateFormatter = SimpleDateFormat("dd/MM/yyyy", locale)
        val generationDate = dateFormatter.format(Date())

        val totalBalance = accounts.sumOf { it.balance }
        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val totalInvested = investments.sumOf { it.quantity * it.currentPrice }

        // Financial Health Score Calculation
        val score: Int
        val scoreLabel: String
        val scoreColor: String
        if (totalIncome == 0.0) {
            score = 50
            scoreLabel = "Sem Renda"
            scoreColor = "#FBC02D" // Yellow
        } else {
            val ratio = totalExpense / totalIncome
            when {
                ratio < 0.5 -> {
                    score = 95
                    scoreLabel = "Excelente"
                    scoreColor = "#2E7D32" // Green
                }
                ratio < 0.7 -> {
                    score = 75
                    scoreLabel = "Regular"
                    scoreColor = "#4CAF50" // Light Green
                }
                ratio < 0.9 -> {
                    score = 42
                    scoreLabel = "Alerta"
                    scoreColor = "#EF6C00" // Orange
                }
                else -> {
                    score = 15
                    scoreLabel = "Crítico"
                    scoreColor = "#C62828" // Red
                }
            }
        }

        return """
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Relatório Financeiro - Finança AI</title>
    <style>
        :root {
            --primary: #2563EB;
            --primary-light: #DBEAFE;
            --success: #16A34A;
            --success-light: #DCFCE7;
            --danger: #DC2626;
            --danger-light: #FEE2E2;
            --warning: #EA580C;
            --warning-light: #FFEDD5;
            --bg: #F8FAFC;
            --text-main: #0F172A;
            --text-muted: #64748B;
            --card-bg: #FFFFFF;
            --border: #E2E8F0;
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
        }

        body {
            background-color: var(--bg);
            color: var(--text-main);
            line-height: 1.6;
            padding: 24px 16px;
        }

        .container {
            max-width: 1000px;
            margin: 0 auto;
        }

        /* Header block */
        header {
            background: linear-gradient(135deg, #1E293B, #0F172A);
            color: #FFFFFF;
            padding: 32px;
            border-radius: 16px;
            margin-bottom: 24px;
            box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
        }

        .logo-area h1 {
            font-size: 28px;
            font-weight: 800;
            letter-spacing: -0.5px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .logo-area h1 span {
            color: #38BDF8;
        }

        .logo-area p {
            font-size: 14px;
            color: #94A3B8;
            margin-top: 4px;
        }

        .meta-info {
            text-align: right;
            font-size: 13px;
            color: #E2E8F0;
        }

        .meta-info strong {
            color: #FFFFFF;
        }

        /* Grid of Scorecards */
        .grid-scorecards {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }

        .card-score {
            background-color: var(--card-bg);
            border: 1px solid var(--border);
            padding: 20px;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            position: relative;
            overflow: hidden;
        }

        .card-score::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
        }

        .card-score.blue::before { background-color: var(--primary); }
        .card-score.green::before { background-color: var(--success); }
        .card-score.red::before { background-color: var(--danger); }
        .card-score.orange::before { background-color: var(--warning); }

        .card-score .title {
            font-size: 12px;
            font-weight: 700;
            text-transform: uppercase;
            color: var(--text-muted);
            letter-spacing: 0.5px;
            margin-bottom: 8px;
        }

        .card-score .value {
            font-size: 22px;
            font-weight: 800;
            color: var(--text-main);
        }

        /* Radial gauge section inside scorecards */
        .score-gauge-container {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .score-badge {
            padding: 4px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 800;
            text-transform: uppercase;
        }

        /* Details cards list */
        .section-title {
            font-size: 18px;
            font-weight: 700;
            margin: 32px 0 16px 0;
            color: var(--text-main);
            border-bottom: 2px solid var(--border);
            padding-bottom: 8px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .section-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
            gap: 16px;
        }

        .data-card {
            background-color: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 12px;
            padding: 16px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
        }

        .data-card-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 12px;
        }

        .data-card-title {
            font-weight: 700;
            font-size: 15px;
        }

        .data-card-subtitle {
            font-size: 12px;
            color: var(--text-muted);
        }

        .data-card-value {
            font-size: 18px;
            font-weight: 800;
        }

        /* Progress bars */
        .progress-container {
            margin-top: 10px;
        }

        .progress-label {
            display: flex;
            justify-content: space-between;
            font-size: 11px;
            color: var(--text-muted);
            margin-bottom: 4px;
        }

        .progress-bar {
            background-color: var(--border);
            height: 8px;
            border-radius: 4px;
            overflow: hidden;
        }

        .progress-fill {
            background-color: var(--primary);
            height: 100%;
            border-radius: 4px;
        }

        /* Tables block */
        .table-responsive {
            width: 100%;
            overflow-x: auto;
            background-color: var(--card-bg);
            border: 1px solid var(--border);
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.05);
            margin-bottom: 24px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            text-align: left;
            font-size: 14px;
        }

        th, td {
            padding: 12px 16px;
            border-bottom: 1px solid var(--border);
        }

        th {
            background-color: #F1F5F9;
            font-weight: 700;
            color: var(--text-main);
            text-transform: uppercase;
            font-size: 11px;
            letter-spacing: 0.5px;
        }

        tr:last-child td {
            border-bottom: none;
        }

        tr:hover td {
            background-color: #F8FAFC;
        }

        /* Badges status */
        .badge {
            display: inline-block;
            padding: 2px 6px;
            border-radius: 4px;
            font-size: 11px;
            font-weight: 700;
        }

        .badge-income {
            background-color: var(--success-light);
            color: var(--success);
        }

        .badge-expense {
            background-color: var(--danger-light);
            color: var(--danger);
        }

        .badge-pending {
            background-color: var(--warning-light);
            color: var(--warning);
        }

        .badge-completed {
            background-color: var(--success-light);
            color: var(--success);
        }

        /* Footer block */
        footer {
            margin-top: 48px;
            text-align: center;
            font-size: 12px;
            color: var(--text-muted);
            border-top: 1px solid var(--border);
            padding-top: 24px;
        }

        @media (max-width: 600px) {
            header {
                padding: 20px;
                flex-direction: column;
                text-align: center;
            }
            .meta-info {
                text-align: center;
            }
            th, td {
                padding: 10px 12px;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- HEADER -->
        <header>
            <div class="logo-area">
                <h1>Finança<span>AI</span></h1>
                <p>Análise e Gestão de Patrimônio Inteligente</p>
            </div>
            <div class="meta-info">
                <p>Relatório gerado para: <strong>$userName</strong></p>
                <p>Data de Emissão: <strong>$generationDate</strong></p>
                <p>Status: <span class="badge" style="background-color: $scoreColor; color: #FFFFFF; font-weight:800;">Score $score - $scoreLabel</span></p>
            </div>
        </header>

        <!-- EXECUTIVE SUMMARY -->
        <div class="grid-scorecards">
            <!-- Saldo total -->
            <div class="card-score blue">
                <div class="title">Saldo Total em Contas</div>
                <div class="value">${currencyFormatter.format(totalBalance)}</div>
            </div>
            <!-- Receitas -->
            <div class="card-score green">
                <div class="title">Total de Receitas (Mês)</div>
                <div class="value">${currencyFormatter.format(totalIncome)}</div>
            </div>
            <!-- Despesas -->
            <div class="card-score red">
                <div class="title">Total de Despesas (Mês)</div>
                <div class="value">${currencyFormatter.format(totalExpense)}</div>
            </div>
            <!-- Investido -->
            <div class="card-score orange">
                <div class="title">Total Investido</div>
                <div class="value">${currencyFormatter.format(totalInvested)}</div>
            </div>
        </div>

        <!-- BANK ACCOUNTS SECTION -->
        <div class="section-title">Contas Bancárias & Saldos</div>
        <div class="section-grid">
            ${if (accounts.isEmpty()) {
                "<div class='data-card' style='grid-column: 1/-1; text-align: center; color: var(--text-muted);'>Nenhuma conta bancária registrada.</div>"
            } else {
                accounts.joinToString("\n") { acc ->
                    val typeLabel = when (acc.type) {
                        "CHECKING" -> "Conta Corrente"
                        "SAVINGS" -> "Poupança"
                        "CASH" -> "Dinheiro em Mão"
                        "INVESTMENT" -> "Conta de Investimentos"
                        else -> acc.type
                    }
                    """
                    <div class="data-card">
                        <div class="data-card-header">
                            <div>
                                <div class="data-card-title">${acc.name}</div>
                                <div class="data-card-subtitle">$typeLabel ${if (acc.bankName.isNotEmpty()) "• " + acc.bankName else ""}</div>
                            </div>
                            <span class="badge" style="background-color: ${acc.colorHex}20; color: ${acc.colorHex};">${acc.accountNumber.takeLast(4).padStart(4, '*')}</span>
                        </div>
                        <div class="data-card-value">${currencyFormatter.format(acc.balance)}</div>
                    </div>
                    """.trimIndent()
                }
            }}
        </div>

        <!-- TRANSACTIONS LIST -->
        <div class="section-title">Extrato Recente de Transações</div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Data</th>
                        <th>Descrição</th>
                        <th>Categoria</th>
                        <th>Tipo</th>
                        <th>Valor</th>
                    </tr>
                </thead>
                <tbody>
                    ${if (transactions.isEmpty()) {
                        "<tr><td colspan='5' style='text-align: center; color: var(--text-muted);'>Nenhuma transação registrada.</td></tr>"
                    } else {
                        transactions.sortedByDescending { it.dateTimestamp }.take(30).joinToString("\n") { tx ->
                            val typeBadge = if (tx.type == "INCOME") "<span class='badge badge-income'>Receita</span>" else "<span class='badge badge-expense'>Despesa</span>"
                            val valClass = if (tx.type == "INCOME") "style='color: var(--success); font-weight:700;'" else "style='color: var(--danger); font-weight:700;'"
                            """
                            <tr>
                                <td>${simpleDateFormatter.format(Date(tx.dateTimestamp))}</td>
                                <td><strong>${tx.title}</strong>${if (tx.totalInstallments > 1) " (${tx.currentInstallment}/${tx.totalInstallments})" else ""}</td>
                                <td>${tx.category} ${if (tx.subcategory.isNotEmpty()) "• " + tx.subcategory else ""}</td>
                                <td>$typeBadge</td>
                                <td $valClass>${if (tx.type == "INCOME") "+" else "-"}${currencyFormatter.format(tx.amount)}</td>
                            </tr>
                            """.trimIndent()
                        }
                    }}
                </tbody>
            </table>
        </div>

        <!-- ACTIVE INVESTMENTS -->
        <div class="section-title">Carteira de Investimentos</div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Ativo / Nome</th>
                        <th>Categoria</th>
                        <th>Quantidade</th>
                        <th>Preço de Compra</th>
                        <th>Preço Atual</th>
                        <th>Total Atual</th>
                    </tr>
                </thead>
                <tbody>
                    ${if (investments.isEmpty()) {
                        "<tr><td colspan='6' style='text-align: center; color: var(--text-muted);'>Nenhum ativo de investimento registrado.</td></tr>"
                    } else {
                        investments.joinToString("\n") { inv ->
                            val catLabel = when (inv.category) {
                                "STOCKS" -> "Ações"
                                "FIIS" -> "Fundos Imobiliários"
                                "CRYPTO" -> "Criptomoedas"
                                "FIXED_INCOME" -> "Renda Fixa"
                                "TREASURY" -> "Tesouro Direto"
                                else -> inv.category
                            }
                            val purchaseTotal = inv.quantity * inv.purchasePrice
                            val currentTotal = inv.quantity * inv.currentPrice
                            val profitPercent = if (purchaseTotal > 0) ((currentTotal - purchaseTotal) / purchaseTotal) * 100 else 0.0
                            val profitStyle = if (profitPercent >= 0) "style='color: var(--success); font-weight:700;'" else "style='color: var(--danger); font-weight:700;'"
                            """
                            <tr>
                                <td><strong>${inv.name}</strong></td>
                                <td>$catLabel</td>
                                <td>${inv.quantity}</td>
                                <td>${currencyFormatter.format(inv.purchasePrice)}</td>
                                <td>${currencyFormatter.format(inv.currentPrice)}</td>
                                <td $profitStyle>
                                    ${currencyFormatter.format(currentTotal)}
                                    <span style='font-size: 11px; margin-left: 4px;'>(${if (profitPercent >= 0) "+" else ""}${String.format("%.2f", profitPercent)}%)</span>
                                </td>
                            </tr>
                            """.trimIndent()
                        }
                    }}
                </tbody>
            </table>
        </div>

        <!-- FINANCIAL GOALS & SAVINGS CHALLENGES -->
        <div class="section-title">Metas Financeiras & Desafios</div>
        <div class="section-grid">
            ${if (goals.isEmpty() && challenges.isEmpty()) {
                "<div class='data-card' style='grid-column: 1/-1; text-align: center; color: var(--text-muted);'>Nenhuma meta ou desafio cadastrado.</div>"
            } else {
                val goalsHtml = goals.map { g ->
                    val progress = if (g.targetAmount > 0) (g.currentAmount / g.targetAmount) * 100 else 0.0
                    val cappedProgress = if (progress > 100) 100.0 else progress
                    """
                    <div class="data-card">
                        <div class="data-card-header">
                            <div>
                                <div class="data-card-title">${g.title}</div>
                                <div class="data-card-subtitle">Meta Financeira • Alvo: ${currencyFormatter.format(g.targetAmount)}</div>
                            </div>
                            <span class="badge" style="background-color: var(--primary-light); color: var(--primary);">${String.format("%.1f", progress)}%</span>
                        </div>
                        <div class="data-card-value">${currencyFormatter.format(g.currentAmount)}</div>
                        <div class="progress-container">
                            <div class="progress-label">
                                <span>Progresso acumulado</span>
                                <span>Alvo em: ${simpleDateFormatter.format(Date(g.targetDateTimestamp))}</span>
                            </div>
                            <div class="progress-bar">
                                <div class="progress-fill" style="width: ${cappedProgress}%;"></div>
                            </div>
                        </div>
                    </div>
                    """.trimIndent()
                }

                val challengesHtml = challenges.map { c ->
                    val progress = if (c.targetAmount > 0) (c.currentAmount / c.targetAmount) * 100 else 0.0
                    val cappedProgress = if (progress > 100) 100.0 else progress
                    """
                    <div class="data-card">
                        <div class="data-card-header">
                            <div>
                                <div class="data-card-title">${c.title}</div>
                                <div class="data-card-subtitle">Desafio de Economia • Alvo: ${currencyFormatter.format(c.targetAmount)}</div>
                            </div>
                            <span class="badge" style="background-color: var(--success-light); color: var(--success);">${String.format("%.1f", progress)}%</span>
                        </div>
                        <div class="data-card-value">${currencyFormatter.format(c.currentAmount)}</div>
                        <div class="progress-container">
                            <div class="progress-label">
                                <span>Economizado</span>
                                <span>Expira em: ${simpleDateFormatter.format(Date(c.endDateTimestamp))}</span>
                            </div>
                            <div class="progress-bar">
                                <div class="progress-fill" style="width: ${cappedProgress}%; background-color: var(--success);"></div>
                            </div>
                        </div>
                    </div>
                    """.trimIndent()
                }

                (goalsHtml + challengesHtml).joinToString("\n")
            }}
        </div>

        <!-- BILLS TO PAY & RECEIVE -->
        <div class="section-title">Contas Agendadas (A Pagar & A Receber)</div>
        <div class="section-grid">
            <!-- Bills to Pay -->
            <div class="data-card" style="display: flex; flex-direction: column; gap: 12px;">
                <div class="data-card-title" style="border-bottom: 1px solid var(--border); padding-bottom: 6px; color: var(--danger);">Contas a Pagar</div>
                ${if (billsToPay.isEmpty()) {
                    "<div style='font-size: 13px; color: var(--text-muted); text-align: center; padding: 12px;'>Nenhuma conta a pagar agendada.</div>"
                } else {
                    billsToPay.take(6).joinToString("\n") { b ->
                        val statusBadge = when (b.status) {
                            "Pago" -> "<span class='badge badge-completed'>Pago</span>"
                            "Atrasado" -> "<span class='badge badge-expense'>Atrasado</span>"
                            else -> "<span class='badge badge-pending'>Pendente</span>"
                        }
                        """
                        <div style="display: flex; justify-content: space-between; align-items: center; font-size: 13px; padding: 4px 0;">
                            <div>
                                <div style="font-weight: 700;">${b.name}</div>
                                <div style="color: var(--text-muted); font-size: 11px;">Vence em: ${simpleDateFormatter.format(Date(b.dueDateTimestamp))}</div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 700; color: var(--danger);">${currencyFormatter.format(b.amount)}</div>
                                <div>$statusBadge</div>
                            </div>
                        </div>
                        """.trimIndent()
                    }
                }}
            </div>

            <!-- Bills to Receive -->
            <div class="data-card" style="display: flex; flex-direction: column; gap: 12px;">
                <div class="data-card-title" style="border-bottom: 1px solid var(--border); padding-bottom: 6px; color: var(--success);">Contas a Receber</div>
                ${if (billsToReceive.isEmpty()) {
                    "<div style='font-size: 13px; color: var(--text-muted); text-align: center; padding: 12px;'>Nenhuma conta a receber agendada.</div>"
                } else {
                    billsToReceive.take(6).joinToString("\n") { b ->
                        val statusBadge = when (b.status) {
                            "Recebido" -> "<span class='badge badge-completed'>Recebido</span>"
                            "Atrasado" -> "<span class='badge badge-expense'>Atrasado</span>"
                            else -> "<span class='badge badge-pending'>Pendente</span>"
                        }
                        """
                        <div style="display: flex; justify-content: space-between; align-items: center; font-size: 13px; padding: 4px 0;">
                            <div>
                                <div style="font-weight: 700;">${b.debtor}</div>
                                <div style="color: var(--text-muted); font-size: 11px;">Recebimento em: ${simpleDateFormatter.format(Date(b.dueDateTimestamp))}</div>
                            </div>
                            <div style="text-align: right;">
                                <div style="font-weight: 700; color: var(--success);">${currencyFormatter.format(b.amount)}</div>
                                <div>$statusBadge</div>
                            </div>
                        </div>
                        """.trimIndent()
                    }
                }}
            </div>
        </div>

        <!-- FOOTER -->
        <footer>
            <p>Finança AI - Sistema de Planejamento Financeiro Automatizado</p>
            <p style="margin-top: 4px; font-size: 10px;">Este relatório foi gerado localmente pelo aplicativo e está protegido de acordo com as regras de sigilo de dados.</p>
        </footer>
    </div>
</body>
</html>
        """.trimIndent()
    }
}
