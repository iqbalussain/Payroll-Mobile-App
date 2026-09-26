package com.example.payrollsheet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.RedLight
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

@Composable
fun EmployeesScreen(
    employees: List<Employee>,
    canDelete: Boolean,
    onNewEmployee: () -> Unit,
    onViewEmployee: (Employee) -> Unit,
    onEditEmployee: (Employee) -> Unit,
    onDeleteEmployee: (Employee) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTrade by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    val filteredEmployees = remember(employees, searchQuery, selectedTrade, selectedStatus) {
        employees
            .filter { selectedTrade == "ALL" || it.trade == selectedTrade }
            .filter { selectedStatus == "ALL" || it.status == selectedStatus }
            .filter {
                val q = searchQuery.trim().lowercase()
                q.isEmpty() ||
                        it.name.lowercase().contains(q) ||
                        it.idNumber.lowercase().contains(q) ||
                        it.id.toString().contains(q)
            }
            .sortedBy { it.id }
    }

    val activeCount = remember(employees) { employees.count { it.status == "Active" } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Metrics Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Navy800),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Active Staff", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$activeCount", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Registered", color = Slate500, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("${employees.size}", color = Navy900, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, ID number...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Trade filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTrade == "ALL",
                        onClick = { selectedTrade = "ALL" },
                        label = { Text("All Trades") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy800,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    PayrollCalculations.TRADES.forEach { trade ->
                        FilterChip(
                            selected = selectedTrade == trade,
                            onClick = { selectedTrade = trade },
                            label = { Text(trade) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy800,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Status filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == "ALL",
                        onClick = { selectedStatus = "ALL" },
                        label = { Text("All Statuses") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    PayrollCalculations.STATUSES.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Teal600,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            // Employee count bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${filteredEmployees.size} employees found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (filteredEmployees.isEmpty()) {
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
                            Text("No employees match your search", fontWeight = FontWeight.SemiBold, color = Slate600)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = {
                                searchQuery = ""
                                selectedTrade = "ALL"
                                selectedStatus = "ALL"
                            }) {
                                Text("Reset filters")
                            }
                        }
                    }
                }
            }

            // List of Employee Cards
            items(filteredEmployees, key = { it.id }) { employee ->
                EmployeeCard(
                    employee = employee,
                    canDelete = canDelete,
                    onView = { onViewEmployee(employee) },
                    onEdit = { onEditEmployee(employee) },
                    onDelete = { employeeToDelete = employee }
                )
            }

            // Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        // Floating Action Button to add employee
        FloatingActionButton(
            onClick = onNewEmployee,
            containerColor = Navy900,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_employee_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Employee")
        }
    }

    // Delete Confirmation Dialog
    if (employeeToDelete != null) {
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Delete Employee?") },
            text = {
                Text("Are you sure you want to remove ${employeeToDelete?.name}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        employeeToDelete?.let { onDeleteEmployee(it) }
                        employeeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red600)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmployeeCard(
    employee: Employee,
    canDelete: Boolean,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("employee_card_${employee.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Navy800),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = employee.name.take(1).uppercase(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = employee.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TealLight)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    employee.trade,
                                    color = Teal600,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            if (employee.idNumber.isNotBlank()) {
                                Text(
                                    employee.idNumber,
                                    color = Slate500,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }

                // Status badge
                val (statusBg, statusFg) = when (employee.status) {
                    "Active" -> EmeraldLight to Emerald600
                    "Holiday" -> TealLight to Teal600
                    else -> RedLight to Red600
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        employee.status,
                        color = statusFg,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Hourly Rate: ${PayrollCalculations.fmt(employee.hourlyRate)} / hr",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate600,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onView, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Visibility, contentDescription = "View", tint = Slate500, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Navy800, modifier = Modifier.size(18.dp))
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Red600, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
