package com.example.payrollsheet.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.HistoryRow
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Amber500
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    employees: List<Employee>,
    batches: List<PayrollBatch>,
    advances: List<AdvanceTx>
) {
    val context = LocalContext.current
    var selectedEmployeeId by remember {
        mutableStateOf(employees.firstOrNull()?.id ?: 0L)
    }
    var employeeDropdownExpanded by remember { mutableStateOf(false) }

    val selectedEmployee = remember(employees, selectedEmployeeId) {
        employees.firstOrNull { it.id == selectedEmployeeId }
    }

    val historyRows = remember(batches, selectedEmployeeId) {
        if (selectedEmployeeId > 0) PayrollCalculations.employeeRows(batches, selectedEmployeeId) else emptyList()
    }

    val employeeAdvances = remember(advances, selectedEmployeeId) {
        advances.filter { it.employeeId == selectedEmployeeId }.sortedByDescending { it.date }
    }

    val outstandingAdvance = remember(selectedEmployeeId, batches, advances) {
        if (selectedEmployeeId > 0) PayrollCalculations.advanceOutstanding(selectedEmployeeId, batches, advances) else 0.0
    }

    val totalHours = remember(historyRows) { historyRows.sumOf { it.line.hours } }
    val totalGross = remember(historyRows) { historyRows.sumOf { PayrollCalculations.lineGross(it.line.hours, it.line.rate) } }
    val totalNet = remember(historyRows) { historyRows.sumOf { it.line.netSalary } }
    val totalPaid = remember(historyRows) { historyRows.sumOf { it.line.paid } }
    val totalBalance = remember(historyRows) { historyRows.sumOf { PayrollCalculations.lineBalance(it.line.netSalary, it.line.paid) } }

    fun shareStatement() {
        if (selectedEmployee == null) return
        val text = buildString {
            appendLine("==========================================")
            appendLine("       EMPLOYEE PAYROLL STATEMENT         ")
            appendLine("==========================================")
            appendLine("Employee: ${selectedEmployee.name}")
            appendLine("ID: ${selectedEmployee.idNumber.ifBlank { "N/A" }}")
            appendLine("Trade: ${selectedEmployee.trade}")
            appendLine("Hourly Rate: ${PayrollCalculations.fmt(selectedEmployee.hourlyRate)}")
            appendLine("------------------------------------------")
            appendLine("LIFETIME SUMMARY:")
            appendLine("  Total Hours:      ${PayrollCalculations.fmt(totalHours)} hrs")
            appendLine("  Gross Wages:      ${PayrollCalculations.fmt(totalGross)}")
            appendLine("  Total Net Pay:    ${PayrollCalculations.fmt(totalNet)}")
            appendLine("  Total Paid:       ${PayrollCalculations.fmt(totalPaid)}")
            appendLine("  Current Balance:  ${PayrollCalculations.fmt(totalBalance)}")
            appendLine("  Outstanding Adv:  ${PayrollCalculations.fmt(outstandingAdvance)}")
            appendLine("------------------------------------------")
            appendLine("MONTHLY PAYROLL BREAKDOWN:")
            historyRows.forEach { r ->
                appendLine("${PayrollCalculations.monthLabel(r.month)} | Site: ${r.site}")
                appendLine("  Hours: ${PayrollCalculations.fmt(r.line.hours)} | Net: ${PayrollCalculations.fmt(r.line.netSalary)} | Paid: ${PayrollCalculations.fmt(r.line.paid)} | Bal: ${PayrollCalculations.fmt(PayrollCalculations.lineBalance(r.line.netSalary, r.line.paid))}")
            }
            if (employeeAdvances.isNotEmpty()) {
                appendLine("------------------------------------------")
                appendLine("ADVANCES RECORDED:")
                employeeAdvances.forEach { a ->
                    appendLine("  ${a.date} | ${PayrollCalculations.fmt(a.amount)} (${a.reason}) [${a.paymentMethod}]")
                }
            }
            appendLine("==========================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Payroll Statement - ${selectedEmployee.name}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Employee Statement"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Employee Selector
        item {
            ExposedDropdownMenuBox(
                expanded = employeeDropdownExpanded,
                onExpandedChange = { employeeDropdownExpanded = !employeeDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = if (selectedEmployee != null) "${selectedEmployee.name} — ${selectedEmployee.trade}" else "Select Employee",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Employee") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("history_employee_selector"),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = employeeDropdownExpanded,
                    onDismissRequest = { employeeDropdownExpanded = false }
                ) {
                    employees.forEach { emp ->
                        DropdownMenuItem(
                            text = { Text("${emp.name} (${emp.trade})") },
                            onClick = {
                                selectedEmployeeId = emp.id
                                employeeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        if (selectedEmployee != null) {
            // Lifetime Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("history_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    selectedEmployee.name,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${selectedEmployee.trade} • ID: ${selectedEmployee.idNumber.ifBlank { "EMP-${selectedEmployee.id}" }}",
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            Button(
                                onClick = { shareStatement() },
                                colors = ButtonDefaults.buttonColors(containerColor = Teal600),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Hours", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text("${PayrollCalculations.fmt(totalHours)} hrs", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Gross Wages", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalGross), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Payable", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalNet), color = EmeraldLight, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Paid", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalPaid), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Unpaid Balance", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalBalance), color = if (totalBalance > 0) Amber500 else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Advance Due", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(outstandingAdvance), color = if (outstandingAdvance > 0) Amber500 else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Month-by-month history title
            item {
                Text(
                    "Monthly Payroll Records (${historyRows.size})",
                    fontWeight = FontWeight.Bold,
                    color = Navy900,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (historyRows.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "No payroll history recorded for this worker yet.",
                            modifier = Modifier.padding(16.dp),
                            color = Slate600
                        )
                    }
                }
            }

            items(historyRows, key = { "${it.month}_${it.batchId}" }) { r ->
                val gross = PayrollCalculations.lineGross(r.line.hours, r.line.rate)
                val balance = PayrollCalculations.lineBalance(r.line.netSalary, r.line.paid)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                PayrollCalculations.monthLabel(r.month),
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Site: ${r.site.ifBlank { "General" }}",
                                color = Slate500,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hours: ${PayrollCalculations.fmt(r.line.hours)} hrs", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                            Text("Gross: ${PayrollCalculations.fmt(gross)}", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                            Text("Net: ${PayrollCalculations.fmt(r.line.netSalary)}", fontWeight = FontWeight.Bold, color = Emerald600)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Paid: ${PayrollCalculations.fmt(r.line.paid)}", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                            Text(
                                "Balance: ${PayrollCalculations.fmt(balance)}",
                                fontWeight = FontWeight.SemiBold,
                                color = if (balance > 0) Red600 else Slate600,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Advance transactions section
            if (employeeAdvances.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Disbursed Advances (${employeeAdvances.size})",
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(employeeAdvances, key = { it.id }) { a ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(a.date, fontWeight = FontWeight.SemiBold, color = Navy900)
                                Text("${a.reason} • ${a.paymentMethod}", style = MaterialTheme.typography.labelMedium, color = Slate500)
                            }
                            Text(PayrollCalculations.fmt(a.amount), fontWeight = FontWeight.Bold, color = Teal600)
                        }
                    }
                }
            }
        }
    }
}
