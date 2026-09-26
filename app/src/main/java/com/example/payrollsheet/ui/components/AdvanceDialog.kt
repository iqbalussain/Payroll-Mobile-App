package com.example.payrollsheet.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Teal600
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvanceDialog(
    employees: List<Employee>,
    existingTx: AdvanceTx? = null,
    preselectedEmployeeId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (employeeId: Long, date: String, amount: Double, reason: String, paymentMethod: String, notes: String) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    var selectedEmployeeId by remember(existingTx, preselectedEmployeeId) {
        mutableStateOf(existingTx?.employeeId ?: preselectedEmployeeId ?: (employees.firstOrNull()?.id ?: 0L))
    }
    var date by remember(existingTx) { mutableStateOf(existingTx?.date ?: today) }
    var amountStr by remember(existingTx) {
        mutableStateOf(if (existingTx != null && existingTx.amount > 0) existingTx.amount.toString() else "")
    }
    var reason by remember(existingTx) {
        mutableStateOf(existingTx?.reason ?: PayrollCalculations.ADVANCE_REASONS[0])
    }
    var paymentMethod by remember(existingTx) {
        mutableStateOf(existingTx?.paymentMethod ?: "Cash")
    }
    var notes by remember(existingTx) { mutableStateOf(existingTx?.notes ?: "") }

    var employeeExpanded by remember { mutableStateOf(false) }
    var reasonExpanded by remember { mutableStateOf(false) }

    val selectedEmployee = employees.firstOrNull { it.id == selectedEmployeeId }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("advance_dialog"),
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
                            .clip(CircleShape)
                            .background(Teal600),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (existingTx != null) "Edit Advance" else "Record Advance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Employee Picker
                ExposedDropdownMenuBox(
                    expanded = employeeExpanded,
                    onExpandedChange = { employeeExpanded = !employeeExpanded }
                ) {
                    OutlinedTextField(
                        value = if (selectedEmployee != null) "${selectedEmployee.name} (${selectedEmployee.trade})" else "Select Employee",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Worker / Employee *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = employeeExpanded,
                        onDismissRequest = { employeeExpanded = false }
                    ) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = { Text("${emp.name} — ${emp.trade}") },
                                onClick = {
                                    selectedEmployeeId = emp.id
                                    employeeExpanded = false
                                }
                            )
                        }
                    }
                }

                // Date
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                // Amount
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Advance Amount (Currency) *") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Reason dropdown
                ExposedDropdownMenuBox(
                    expanded = reasonExpanded,
                    onExpandedChange = { reasonExpanded = !reasonExpanded }
                ) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reason for Advance") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = reasonExpanded,
                        onDismissRequest = { reasonExpanded = false }
                    ) {
                        PayrollCalculations.ADVANCE_REASONS.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    reason = r
                                    reasonExpanded = false
                                }
                            )
                        }
                    }
                }

                // Payment Method
                Column {
                    Text("Payment Method", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PayrollCalculations.PAYMENT_METHODS.forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Navy800,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Purpose (Optional)") },
                    placeholder = { Text("e.g. Travel tickets, family emergency...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    onSave(selectedEmployeeId, date, amount, reason, paymentMethod, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_advance_button")
            ) {
                Text(if (existingTx != null) "Update Advance" else "Record Advance")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
