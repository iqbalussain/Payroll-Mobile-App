package com.example.payrollsheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Teal600

enum class EmployeeDialogMode {
    NEW,
    EDIT,
    VIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDialog(
    mode: EmployeeDialogMode,
    employee: Employee?,
    onDismiss: () -> Unit,
    onSave: (name: String, trade: String, idNumber: String, rate: Double, status: String) -> Unit,
    onSwitchToEdit: () -> Unit = {}
) {
    var name by remember(employee) { mutableStateOf(employee?.name ?: "") }
    var trade by remember(employee) { mutableStateOf(employee?.trade ?: PayrollCalculations.TRADES[2]) }
    var idNumber by remember(employee) { mutableStateOf(employee?.idNumber ?: "") }
    var hourlyRateStr by remember(employee) {
        mutableStateOf(if (employee != null && employee.hourlyRate > 0) employee.hourlyRate.toString() else "")
    }
    var status by remember(employee) { mutableStateOf(employee?.status ?: "Active") }

    var tradeExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("employee_dialog"),
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertifally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = when (mode) {
                            EmployeeDialogMode.NEW -> "New Employee"
                            EmployeeDialogMode.EDIT -> "Edit Employee"
                            EmployeeDialogMode.VIEW -> employee?.name ?: "Employee Details"
                        },
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
                if (mode == EmployeeDialogMode.VIEW && employee != null) {
                    // Profile view card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Employee ID", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                Text(
                                    employee.idNumber.ifBlank { "Not Assigned" },
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Trade / Role", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                Text(
                                    employee.trade,
                                    fontWeight = FontWeight.Bold,
                                    color = Teal600,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Hourly Rate", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                Text(
                                    "${PayrollCalculations.fmt(employee.hourlyRate)} / hr",
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald600,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Status", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                                val statusColor = when (employee.status) {
                                    "Active" -> Emerald600
                                    "Holiday" -> Teal600
                                    else -> Red600
                                }
                                Text(
                                    employee.status,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else {
                    // Form fields
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        placeholder = { Text("e.g. Rajesh Kumar") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Trade selector
                    ExposedDropdownMenuBox(
                        expanded = tradeExpanded,
                        onExpandedChange = { tradeExpanded = !tradeExpanded }
                    ) {
                        OutlinedTextField(
                            value = trade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Trade / Specialty") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tradeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = tradeExpanded,
                            onDismissRequest = { tradeExpanded = false }
                        ) {
                            PayrollCalculations.TRADES.forEach { t ->
                                DropdownMenuItem(
                                    text = { Text(t) },
                                    onClick = {
                                        trade = t
                                        tradeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = idNumber,
                        onValueChange = { idNumber = it },
                        label = { Text("ID Number / Badge") },
                        placeholder = { Text("e.g. EMP-1045") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_id_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = hourlyRateStr,
                        onValueChange = { hourlyRateStr = it },
                        label = { Text("Hourly Rate (Currency)") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_rate_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Status selector
                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = !statusExpanded }
                    ) {
                        OutlinedTextField(
                            value = status,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Employment Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            PayrollCalculations.STATUSES.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s) },
                                    onClick = {
                                        status = s
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (mode == EmployeeDialogMode.VIEW) {
                Button(
                    onClick = onSwitchToEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit")
                }
            } else {
                Button(
                    onClick = {
                        val rate = hourlyRateStr.toDoubleOrNull() ?: 0.0
                        onSave(name, trade, idNumber, rate, status)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_employee_button")
                ) {
                    Text(if (mode == EmployeeDialogMode.NEW) "Add Employee" else "Save Changes")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (mode == EmployeeDialogMode.VIEW) "Close" else "Cancel")
            }
        }
    )
}
