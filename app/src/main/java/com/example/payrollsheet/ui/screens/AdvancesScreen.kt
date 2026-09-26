package com.example.payrollsheet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Amber500
import com.example.payrollsheet.ui.theme.AmberLight
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
fun AdvancesScreen(
    advances: List<AdvanceTx>,
    employees: List<Employee>,
    batches: List<PayrollBatch>,
    canDelete: Boolean,
    onNewAdvance: () -> Unit,
    onEditAdvance: (AdvanceTx) -> Unit,
    onDeleteAdvance: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var onlyOutstanding by remember { mutableStateOf(false) }
    var txToDelete by remember { mutableStateOf<AdvanceTx?>(null) }

    val empMap = remember(employees) { employees.associateBy { it.id } }

    val totalIssued = remember(advances) { advances.sumOf { it.amount } }

    val totalRecovered = remember(batches) {
        batches.sumOf { b -> b.lines.sumOf { it.prevAdvance } }
    }

    val totalOutstanding = remember(employees, batches, advances) {
        employees.sumOf { PayrollCalculations.advanceOutstanding(it.id, batches, advances) }
    }

    val outstandingEmpIds = remember(employees, batches, advances) {
        employees
            .filter { PayrollCalculations.advanceOutstanding(it.id, batches, advances) > 0 }
            .map { it.id }
            .toSet()
    }

    val filteredAdvances = remember(advances, searchQuery, onlyOutstanding, outstandingEmpIds, empMap) {
        advances
            .filter { tx ->
                if (onlyOutstanding && !outstandingEmpIds.contains(tx.employeeId)) return@filter false
                val q = searchQuery.trim().lowercase()
                if (q.isEmpty()) return@filter true
                val emp = empMap[tx.employeeId]
                (emp?.name?.lowercase()?.contains(q) == true) ||
                        tx.reason.lowercase().contains(q) ||
                        tx.paymentMethod.lowercase().contains(q) ||
                        tx.notes.lowercase().contains(q)
            }
            .sortedByDescending { it.date }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // KPI Strip
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("advances_kpi_card"),
                    colors = CardDefaults.cardColors(containerColor = Navy800),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Advance Tracking Summary",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Issued", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalIssued), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("Recovered", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalRecovered), color = TealLight, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Outstanding", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                                Text(PayrollCalculations.fmt(totalOutstanding), color = AmberLight, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search advances by worker, reason...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = !onlyOutstanding,
                        onClick = { onlyOutstanding = false },
                        label = { Text("All Transactions") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Navy800,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                    FilterChip(
                        selected = onlyOutstanding,
                        onClick = { onlyOutstanding = true },
                        label = { Text("Only Outstanding (${outstandingEmpIds.size})") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Teal600,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            if (filteredAdvances.isEmpty()) {
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
                            Text("No advance transactions found", fontWeight = FontWeight.SemiBold, color = Slate600)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap the + button to record a new advance disbursement.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Advance Items
            items(filteredAdvances, key = { it.id }) { tx ->
                val emp = empMap[tx.employeeId]
                val currentOutstanding = if (emp != null) PayrollCalculations.advanceOutstanding(emp.id, batches, advances) else 0.0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("advance_item_${tx.id}"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                        .background(TealLight),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = Teal600, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        emp?.name ?: "Employee #${tx.employeeId}",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Navy900
                                    )
                                    Text(
                                        "${tx.date} • ${tx.paymentMethod}",
                                        color = Slate500,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Text(
                                PayrollCalculations.fmt(tx.amount),
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    tx.reason,
                                    color = Slate600,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }

                            if (currentOutstanding > 0) {
                                Text(
                                    "Balance: ${PayrollCalculations.fmt(currentOutstanding)}",
                                    color = Amber500,
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        if (tx.notes.isNotBlank()) {
                            Text(
                                tx.notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate600
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = { onEditAdvance(tx) }, modifier = Modifier.size(32.dp)) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Navy800, modifier = Modifier.size(16.dp))
                            }
                            if (canDelete) {
                                IconButton(onClick = { txToDelete = tx }, modifier = Modifier.size(32.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Red600, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        FloatingActionButton(
            onClick = onNewAdvance,
            containerColor = Teal600,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("record_advance_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Record Advance")
        }
    }

    if (txToDelete != null) {
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text("Delete Advance Transaction?") },
            text = { Text("Are you sure you want to delete this advance of ${PayrollCalculations.fmt(txToDelete?.amount ?: 0.0)}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        txToDelete?.let { onDeleteAdvance(it.id) }
                        txToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red600)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
