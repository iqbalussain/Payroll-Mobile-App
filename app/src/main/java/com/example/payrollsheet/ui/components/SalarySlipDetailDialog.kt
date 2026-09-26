package com.example.payrollsheet.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.data.model.PayrollLine
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

@Composable
fun SalarySlipDetailDialog(
    employee: Employee,
    line: PayrollLine,
    month: String,
    site: String,
    foreman: String,
    carriedForwardAdvance: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val gross = PayrollCalculations.lineGross(line.hours, line.rate)
    val totalDeductions = line.foodDeduction + line.prevAdvance + line.otherDeduction
    val balance = PayrollCalculations.lineBalance(line.netSalary, line.paid)

    fun shareSlip() {
        val slipText = buildString {
            appendLine("==========================================")
            appendLine("           SALARY PAY SLIP                ")
            appendLine("==========================================")
            appendLine("Period: ${PayrollCalculations.monthLabel(month)}")
            appendLine("Site: ${site.ifBlank { "General Site" }}")
            appendLine("Foreman: ${line.foreman.ifBlank { foreman.ifBlank { "N/A" } }}")
            appendLine("------------------------------------------")
            appendLine("Employee: ${employee.name}")
            appendLine("ID Number: ${employee.idNumber.ifBlank { "N/A" }}")
            appendLine("Trade: ${employee.trade}")
            appendLine("------------------------------------------")
            appendLine("EARNINGS:")
            appendLine("  Hours Worked: ${PayrollCalculations.fmt(line.hours)} hrs")
            appendLine("  Hourly Rate:  ${PayrollCalculations.fmt(line.rate)}")
            appendLine("  GROSS PAY:    ${PayrollCalculations.fmt(gross)}")
            appendLine("------------------------------------------")
            appendLine("DEDUCTIONS:")
            appendLine("  Food Deduction:     ${PayrollCalculations.fmt(line.foodDeduction)}")
            appendLine("  Recovered Advance:  ${PayrollCalculations.fmt(line.prevAdvance)}")
            appendLine("  Other Deductions:   ${PayrollCalculations.fmt(line.otherDeduction)}")
            appendLine("  TOTAL DEDUCTIONS:   ${PayrollCalculations.fmt(totalDeductions)}")
            appendLine("------------------------------------------")
            appendLine("NET PAYABLE:    ${PayrollCalculations.fmt(line.netSalary)}")
            appendLine("AMOUNT PAID:    ${PayrollCalculations.fmt(line.paid)}")
            appendLine("BALANCE DUE:    ${PayrollCalculations.fmt(balance)}")
            appendLine("------------------------------------------")
            appendLine("New Advance Issued: ${PayrollCalculations.fmt(line.newAdvance)}")
            appendLine("Carried Fwd Advance:${PayrollCalculations.fmt(carriedForwardAdvance)}")
            appendLine("==========================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, slipText)
            putExtra(Intent.EXTRA_SUBJECT, "Salary Slip - ${employee.name} (${PayrollCalculations.monthLabel(month)})")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Salary Slip")
        context.startActivity(shareIntent)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("salary_slip_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Salary Pay Slip",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = PayrollCalculations.monthLabel(month),
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate500
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header badge
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Site", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                            Text(site.ifBlank { "General Site" }, fontWeight = FontWeight.SemiBold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Foreman", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                            Text(
                                line.foreman.ifBlank { foreman.ifBlank { "Unassigned" } },
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Employee identity
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(employee.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(employee.trade, color = Teal600, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "ID: ${employee.idNumber.ifBlank { "EMP-${employee.id}" }}",
                            color = Slate500,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Earnings & Deductions Details
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.border(1.dp, Slate200, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Earnings", fontWeight = FontWeight.Bold, color = Navy900)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hours (${PayrollCalculations.fmt(line.hours)} hrs @ ${PayrollCalculations.fmt(line.rate)})", color = Slate600)
                            Text(PayrollCalculations.fmt(gross), fontWeight = FontWeight.SemiBold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Slate200)

                        Text("Deductions", fontWeight = FontWeight.Bold, color = Navy900)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Food Deduction", color = Slate600)
                            Text("-${PayrollCalculations.fmt(line.foodDeduction)}", color = Red600)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Advance Recovered", color = Slate600)
                            Text("-${PayrollCalculations.fmt(line.prevAdvance)}", color = Red600)
                        }
                        if (line.otherDeduction > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Other Deductions", color = Slate600)
                                Text("-${PayrollCalculations.fmt(line.otherDeduction)}", color = Red600)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Slate200)

                        // Net Payable
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net Payable", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(
                                PayrollCalculations.fmt(line.netSalary),
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Amount Paid", color = Slate600)
                            Text(PayrollCalculations.fmt(line.paid), fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due", color = Slate600)
                            Text(
                                PayrollCalculations.fmt(balance),
                                fontWeight = FontWeight.Bold,
                                color = if (balance > 0) Red600 else Slate600
                            )
                        }
                    }
                }

                // Advance carry forward memo
                if (carriedForwardAdvance > 0 || line.newAdvance > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealLight)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (carriedForwardAdvance > 0) {
                                Text(
                                    "Carried forward advance: ${PayrollCalculations.fmt(carriedForwardAdvance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Teal600,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            if (line.newAdvance > 0) {
                                Text(
                                    "New advance added in this period: ${PayrollCalculations.fmt(line.newAdvance)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Teal600
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { shareSlip() },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share / Export")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
    )
}
