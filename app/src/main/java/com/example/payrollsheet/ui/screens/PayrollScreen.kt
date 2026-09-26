package com.example.payrollsheet.ui.screens

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.data.model.PayrollLine
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.RedLight
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight
import com.example.payrollsheet.ui.viewmodel.CurrentBatchState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollScreen(
    currentBatch: CurrentBatchState,
    employees: List<Employee>,
    canDelete: Boolean,
    onMonthChange: (String) -> Unit,
    onSiteChange: (String) -> Unit,
    onForemanChange: (String) -> Unit,
    onAddEmployee: (Long) -> Unit,
    onAddAllActive: () -> Unit,
    onUpdateLine: (Int, PayrollLine) -> Unit,
    onRemoveLine: (Int) -> Unit,
    onSaveBatch: () -> Unit,
    onDeleteBatch: (String) -> Unit
) {
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var showEmployeePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val empMap = remember(employees) { employees.associateBy { it.id } }
    val totals = remember(currentBatch.lines) { PayrollCalculations.calculateBatchTotals(currentBatch.lines) }

    val unselectedEmployees = remember(employees, currentBatch.lines) {
        val existingIds = currentBatch.lines.map { it.employeeId }.toSet()
        employees.filter { !existingIds.contains(it.id) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Batch Header & Month Picker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Payroll Batch Details",
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Month selector
                    ExposedDropdownMenuBox(
                        expanded = monthDropdownExpanded,
                        onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = PayrollCalculations.monthLabel(currentBatch.month),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Payroll Month") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("payroll_month_selector"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = monthDropdownExpanded,
                            onDismissRequest = { monthDropdownExpanded = false }
                        ) {
                            PayrollCalculations.MONTHS.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(PayrollCalculations.monthLabel(m)) },
                                    onClick = {
                                        onMonthChange(m)
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = currentBatch.site,
                            onValueChange = onSiteChange,
                            label = { Text("Site / Project Name") },
                            placeholder = { Text("e.g. Marina Tower") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = currentBatch.foreman,
                            onValueChange = onForemanChange,
                            label = { Text("Default Foreman") },
                            placeholder = { Text("e.g. Ahmed") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Totals Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("batch_totals_card")
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Batch Totals (${currentBatch.lines.size} Staff)",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Teal600)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${PayrollCalculations.fmt(totals.hours)} hrs",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Gross Pay", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text(PayrollCalculations.fmt(totals.gross), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Total Deductions", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text("-${PayrollCalculations.fmt(totals.food + totals.prevAdvance + totals.otherDeduction)}", color = RedLight, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Net Payable", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text(PayrollCalculations.fmt(totals.net), color = EmeraldLight, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Paid: ${PayrollCalculations.fmt(totals.paid)}", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f), style = MaterialTheme.typography.bodyMedium)
                        Text("Balance: ${PayrollCalculations.fmt(totals.balance)}", color = if (totals.balance > 0) Amber500 else MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Action buttons: Add Employee & Add All Active
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showEmployeePicker = true },
                    modifier = Modifier.weight(1f).testTag("add_employee_to_batch_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Employee")
                }

                OutlinedButton(
                    onClick = onAddAllActive,
                    modifier = Modifier.weight(1f).testTag("add_all_active_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add All Active")
                }
            }
        }

        if (currentBatch.lines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No employees added to this batch yet", fontWeight = FontWeight.SemiBold, color = Slate600)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Click 'Add Employee' or 'Add All Active' to begin entering hours and wages.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // List of Worker Wage Cards
        itemsIndexed(currentBatch.lines, key = { index, line -> "${line.employeeId}_$index" }) { index, line ->
            val emp = empMap[line.employeeId]
            PayrollLineCard(
                index = index,
                line = line,
                employee = emp,
                onUpdate = { updated -> onUpdateLine(index, updated) },
                onDelete = { onRemoveLine(index) }
            )
        }

        // Bottom Save / Delete bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onSaveBatch,
                    enabled = !currentBatch.isSaving && currentBatch.lines.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_payroll_batch_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (currentBatch.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving Batch...")
                    } else {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Payroll Batch", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                if (canDelete && currentBatch.id.isNotBlank()) {
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("delete_payroll_batch_button"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Red600),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Payroll Batch")
                    }
                }
            }
        }
    }

    // Employee Picker Dialog
    if (showEmployeePicker) {
        AlertDialog(
            onDismissRequest = { showEmployeePicker = false },
            title = { Text("Select Employee for Batch") },
            text = {
                if (unselectedEmployees.isEmpty()) {
                    Text("All employees are already included in this batch.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                        items(unselectedEmployees.size) { i ->
                            val emp = unselectedEmployees[i]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onAddEmployee(emp.id)
                                        showEmployeePicker = false
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(emp.name, fontWeight = FontWeight.Bold)
                                    Text("${emp.trade} • ${PayrollCalculations.fmt(emp.hourlyRate)}/hr", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(TealLight)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(emp.status, color = Teal600, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showEmployeePicker = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Batch Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entire Batch?") },
            text = {
                Text("Are you sure you want to delete this payroll batch for ${PayrollCalculations.monthLabel(currentBatch.month)}? All lines will be deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteBatch(currentBatch.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red600)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PayrollLineCard(
    index: Int,
    line: PayrollLine,
    employee: Employee?,
    onUpdate: (PayrollLine) -> Unit,
    onDelete: () -> Unit
) {
    val gross = PayrollCalculations.lineGross(line.hours, line.rate)
    val balance = PayrollCalculations.lineBalance(line.netSalary, line.paid)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("payroll_line_$index"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Worker header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        employee?.name ?: "Employee #${line.employeeId}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy900
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(employee?.trade ?: "—", color = Teal600, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        if (employee?.idNumber?.isNotBlank() == true) {
                            Text("• ${employee.idNumber}", color = Slate500, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove Line", tint = Red600, modifier = Modifier.size(18.dp))
                }
            }

            // Rate and Hours inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (line.rate > 0) line.rate.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(rate = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Rate") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = if (line.hours > 0) line.hours.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(hours = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Hours") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text("Gross", style = MaterialTheme.typography.labelMedium, color = Slate500)
                        Text(PayrollCalculations.fmt(gross), fontWeight = FontWeight.Bold, color = Navy900)
                    }
                }
            }

            // Deductions inputs: Food, Prev Advance, Other
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (line.foodDeduction > 0) line.foodDeduction.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(foodDeduction = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Food") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = if (line.prevAdvance > 0) line.prevAdvance.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(prevAdvance = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Prev Adv") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = if (line.newAdvance > 0) line.newAdvance.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(newAdvance = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("New Adv") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Other deduction and Line Foreman override
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (line.otherDeduction > 0) line.otherDeduction.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(otherDeduction = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Other Ded") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = line.foreman,
                    onValueChange = { str ->
                        onUpdate(line.copy(foreman = str))
                    },
                    label = { Text("Line Foreman") },
                    placeholder = { Text("Optional") },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            HorizontalDivider(color = Slate200)

            // Net, Paid, Balance Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Net Payable", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    Text(PayrollCalculations.fmt(line.netSalary), fontWeight = FontWeight.Bold, color = Emerald600)
                }

                OutlinedTextField(
                    value = if (line.paid > 0) line.paid.toString() else "",
                    onValueChange = { str ->
                        onUpdate(line.copy(paid = str.toDoubleOrNull() ?: 0.0))
                    },
                    label = { Text("Paid") },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                )

                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("Balance Due", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    Text(
                        PayrollCalculations.fmt(balance),
                        fontWeight = FontWeight.Bold,
                        color = if (balance > 0) Red600 else Slate600
                    )
                }
            }
        }
    }
}
