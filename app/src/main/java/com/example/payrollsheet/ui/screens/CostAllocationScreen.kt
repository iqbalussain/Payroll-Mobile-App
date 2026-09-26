package com.example.payrollsheet.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import com.example.payrollsheet.data.model.AllocationRow
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

@Composable
fun CostAllocationScreen(
    batches: List<PayrollBatch>,
    employees: List<Employee>
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSite by remember { mutableStateOf("ALL") }

    val allRows = remember(batches, employees) {
        PayrollCalculations.buildAllocationRows(batches, employees)
    }

    val sites = remember(allRows) {
        listOf("ALL") + allRows.map { it.site }.distinct().filter { it.isNotBlank() }
    }

    val filteredRows = remember(allRows, searchQuery, selectedSite) {
        allRows
            .filter { selectedSite == "ALL" || it.site == selectedSite }
            .filter {
                val q = searchQuery.trim().lowercase()
                q.isEmpty() ||
                        it.employeeName.lowercase().contains(q) ||
                        it.employeeIdNumber.lowercase().contains(q) ||
                        it.foreman.lowercase().contains(q) ||
                        it.site.lowercase().contains(q)
            }
    }

    val totalHours = remember(filteredRows) { filteredRows.sumOf { it.hours } }
    val totalBasic = remember(filteredRows) { filteredRows.sumOf { it.basic } }
    val totalNet = remember(filteredRows) { filteredRows.sumOf { it.total } }
    val totalAllocated = remember(filteredRows) { filteredRows.sumOf { it.allocated } }
    val totalRemaining = remember(filteredRows) { filteredRows.sumOf { it.remaining } }

    fun shareCostReport() {
        val text = buildString {
            appendLine("==========================================")
            appendLine("       COST ALLOCATION SUMMARY REPORT     ")
            appendLine("==========================================")
            appendLine("Site Filter: $selectedSite")
            appendLine("Records: ${filteredRows.size}")
            appendLine("Total Hours: ${PayrollCalculations.fmt(totalHours)} hrs")
            appendLine("Basic Wages: ${PayrollCalculations.fmt(totalBasic)}")
            appendLine("Total Net Cost: ${PayrollCalculations.fmt(totalNet)}")
            appendLine("Allocated (Paid): ${PayrollCalculations.fmt(totalAllocated)}")
            appendLine("Remaining Balance: ${PayrollCalculations.fmt(totalRemaining)}")
            appendLine("------------------------------------------")
            filteredRows.forEach { r ->
                appendLine("${r.employeeName} (${r.trade}) | ${r.site} | ${r.month}")
                appendLine("  Hours: ${PayrollCalculations.fmt(r.hours)} | Net: ${PayrollCalculations.fmt(r.total)} | Paid: ${PayrollCalculations.fmt(r.allocated)} | Bal: ${PayrollCalculations.fmt(r.remaining)}")
            }
            appendLine("==========================================")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, "Cost Allocation Report")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Cost Report"))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // KPI Strip
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("cost_allocation_kpi"),
                colors = CardDefaults.cardColors(containerColor = Navy800),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cost Center Overview",
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
                                "${PayrollCalculations.fmt(totalHours)} Hours",
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
                            Text("Total Net Cost", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text(PayrollCalculations.fmt(totalNet), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text("Allocated (Paid)", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text(PayrollCalculations.fmt(totalAllocated), color = EmeraldLight, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Remaining", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text(PayrollCalculations.fmt(totalRemaining), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }

                    val pct = if (totalNet > 0) (totalAllocated / totalNet).toFloat() else 0f
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Allocation Progress", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.labelMedium)
                            Text("${(pct * 100).toInt()}%", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }
                        LinearProgressIndicator(
                            progress = { pct.coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Emerald600,
                            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Search & Site filter
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by worker, site, foreman...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate400)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("cost_search_input"),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (sites.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    sites.forEach { site ->
                        FilterChip(
                            selected = selectedSite == site,
                            onClick = { selectedSite = site },
                            label = { Text(if (site == "ALL") "All Sites" else site) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Navy800,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Summary bar & Share button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${filteredRows.size} allocation lines",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate500,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = { shareCostReport() },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export Report")
                }
            }
        }

        // List of Allocation Cards
        items(filteredRows, key = { it.key }) { row ->
            AllocationCard(row = row)
        }
    }
}

@Composable
private fun AllocationCard(row: AllocationRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        row.employeeName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy900
                    )
                    Text(
                        "${row.trade} • ID: ${row.employeeIdNumber}",
                        color = Slate500,
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealLight)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        row.month,
                        color = Teal600,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Site: ${row.site}", style = MaterialTheme.typography.bodyMedium, color = Slate600)
                Text("Foreman: ${row.foreman}", style = MaterialTheme.typography.bodyMedium, color = Slate600)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Hours", color = Slate500, style = MaterialTheme.typography.labelMedium)
                    Text("${PayrollCalculations.fmt(row.hours)} hrs", fontWeight = FontWeight.SemiBold)
                }
                Column {
                    Text("Net Total", color = Slate500, style = MaterialTheme.typography.labelMedium)
                    Text(PayrollCalculations.fmt(row.total), fontWeight = FontWeight.Bold, color = Navy900)
                }
                Column {
                    Text("Allocated", color = Slate500, style = MaterialTheme.typography.labelMedium)
                    Text(PayrollCalculations.fmt(row.allocated), fontWeight = FontWeight.Bold, color = Emerald600)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining", color = Slate500, style = MaterialTheme.typography.labelMedium)
                    Text(PayrollCalculations.fmt(row.remaining), fontWeight = FontWeight.Bold, color = if (row.remaining > 0) Red600 else Slate600)
                }
            }

            val pct = if (row.total > 0) (row.allocated / row.total).toFloat() else 0f
            LinearProgressIndicator(
                progress = { pct.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = if (pct >= 1f) Emerald600 else Teal600,
                trackColor = Slate200
            )
        }
    }
}
