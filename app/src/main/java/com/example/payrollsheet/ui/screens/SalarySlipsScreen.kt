package com.example.payrollsheet.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.data.model.PayrollLine
import com.example.payrollsheet.ui.components.SalarySlipDetailDialog
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

data class SlipRowItem(
    val employee: Employee,
    val line: PayrollLine,
    val month: String,
    val site: String,
    val foreman: String,
    val carriedForwardAdvance: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalarySlipsScreen(
    employees: List<Employee>,
    batches: List<PayrollBatch>,
    advances: List<AdvanceTx>
) {
    val context = LocalContext.current
    var selectedMonth by remember { mutableStateOf(PayrollCalculations.MONTHS[2]) } // 2026-09
    var monthDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedLineIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var activeDetailSlip by remember { mutableStateOf<SlipRowItem?>(null) }

    val empMap = remember(employees) { employees.associateBy { it.id } }

    val slipRows = remember(batches, employees, advances, selectedMonth) {
        val list = mutableListOf<SlipRowItem>()
        batches.filter { it.month == selectedMonth }.forEach { batch ->
            batch.lines.forEach { line ->
                val emp = empMap[line.employeeId]
                if (emp != null) {
                    val carried = PayrollCalculations.advanceCarryForward(
                        employeeId = emp.id,
                        month = batch.month,
                        batches = batches,
                        advances = advances
                    )
                    list.add(
                        SlipRowItem(
                            employee = emp,
                            line = line,
                            month = batch.month,
                            site = batch.site,
                            foreman = line.foreman.ifBlank { batch.foreman },
                            carriedForwardAdvance = carried
                        )
                    )
                }
            }
        }
        list
    }

    val filteredSlips = remember(slipRows, searchQuery) {
        slipRows.filter { item ->
            val q = searchQuery.trim().lowercase()
            q.isEmpty() ||
                    item.employee.name.lowercase().contains(q) ||
                    item.employee.trade.lowercase().contains(q) ||
                    item.employee.idNumber.lowercase().contains(q)
        }
    }

    fun shareSelectedSlips() {
        val targets = if (selectedLineIds.isEmpty()) filteredSlips else filteredSlips.filter { selectedLineIds.contains(it.line.id) }
        if (targets.isEmpty()) return

        val text = buildString {
            appendLine("==========================================")
            appendLine("         SALARY SLIPS SUMMARY             ")
            appendLine("==========================================")
            appendLine("Period: ${PayrollCalculations.monthLabel(selectedMonth)}")
            appendLine("Count: ${targets.size} Slips")
            appendLine("------------------------------------------")
            targets.forEach { slip ->
                val gross = PayrollCalculations.lineGross(slip.line.hours, slip.line.rate)
                val bal = PayrollCalculations.lineBalance(slip.line.netSalary, slip.line.paid)
                appendLine("Worker: ${slip.employee.name} (${slip.employee.trade})")
                appendLine("  Hours: ${PayrollCalculations.fmt(slip.line.hours)} | Gross: ${PayrollCalculations.fmt(gross)}")
                appendLine("  Deductions: Food (${PayrollCalculations.fmt(slip.line.foodDeduction)}), Adv (${PayrollCalculations.fmt(slip.line.prevAdvance)})")
                appendLine("  NET PAY: ${PayrollCalculations.fmt(slip.line.netSalary)} | PAID: ${PayrollCalculations.fmt(slip.line.paid)} | BAL: ${PayrollCalculations.fmt(bal)}")
                appendLine("------------------------------------------")
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Salary Slips - ${PayrollCalculations.monthLabel(selectedMonth)}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Salary Slips"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Selector Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("salary_slips_month_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Salary Slips by Month",
                        fontWeight = FontWeight.Bold,
                        color = Navy900,
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = monthDropdownExpanded,
                        onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = PayrollCalculations.monthLabel(selectedMonth),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Selected Period") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
                                        selectedMonth = m
                                        selectedLineIds = emptySet()
                                        monthDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by worker name, trade, ID...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("slips_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Selection & Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val allSelected = filteredSlips.isNotEmpty() && filteredSlips.all { selectedLineIds.contains(it.line.id) }
                    Checkbox(
                        checked = allSelected,
                        onCheckedChange = { check ->
                            selectedLineIds = if (check) filteredSlips.map { it.line.id }.toSet() else emptySet()
                        }
                    )
                    Text(
                        if (selectedLineIds.isEmpty()) "Select All (${filteredSlips.size})" else "${selectedLineIds.size} Selected",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Navy900
                    )
                }

                Button(
                    onClick = { shareSelectedSlips() },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp),
                    enabled = filteredSlips.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export")
                }
            }
        }

        if (filteredSlips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No salary slips found for ${PayrollCalculations.monthLabel(selectedMonth)}", fontWeight = FontWeight.SemiBold, color = Slate600)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Save a monthly payroll batch for this period to generate salary slips.", color = Slate500, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Slips Cards
        items(filteredSlips, key = { it.line.id.ifBlank { "${it.employee.id}_${it.month}" } }) { slip ->
            val isChecked = selectedLineIds.contains(slip.line.id)
            val gross = PayrollCalculations.lineGross(slip.line.hours, slip.line.rate)
            val balance = PayrollCalculations.lineBalance(slip.line.netSalary, slip.line.paid)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeDetailSlip = slip }
                    .testTag("salary_slip_item_${slip.employee.id}"),
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
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { check ->
                                    selectedLineIds = if (check) selectedLineIds + slip.line.id else selectedLineIds - slip.line.id
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text(
                                    slip.employee.name,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Navy900
                                )
                                Text(
                                    "${slip.employee.trade} • ID: ${slip.employee.idNumber.ifBlank { "EMP-${slip.employee.id}" }}",
                                    color = Slate500,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Text(
                            PayrollCalculations.fmt(slip.line.netSalary),
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Hours: ${PayrollCalculations.fmt(slip.line.hours)} hrs (${PayrollCalculations.fmt(slip.line.rate)}/hr)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                        Text(
                            "Gross: ${PayrollCalculations.fmt(gross)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Paid: ${PayrollCalculations.fmt(slip.line.paid)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                        Text(
                            "Balance: ${PayrollCalculations.fmt(balance)}",
                            fontWeight = FontWeight.SemiBold,
                            color = if (balance > 0) Red600 else Slate600,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Tap to view complete slip details",
                            style = MaterialTheme.typography.labelMedium,
                            color = Teal600,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TealLight)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(slip.site.ifBlank { "Site" }, color = Teal600, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    activeDetailSlip?.let { item ->
        SalarySlipDetailDialog(
            employee = item.employee,
            line = item.line,
            month = item.month,
            site = item.site,
            foreman = item.foreman,
            carriedForwardAdvance = item.carriedForwardAdvance,
            onDismiss = { activeDetailSlip = null }
        )
    }
}
