package com.example.payrollsheet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight
import com.example.payrollsheet.ui.viewmodel.ScreenTab

@Composable
fun HomeScreen(
    employees: List<Employee>,
    batches: List<PayrollBatch>,
    advances: List<AdvanceTx>,
    onNavigate: (ScreenTab) -> Unit
) {
    val activeCount = remember(employees) { employees.count { it.status == "Active" } }

    val totalOutstandingAdvances = remember(employees, batches, advances) {
        employees.sumOf { PayrollCalculations.advanceOutstanding(it.id, batches, advances) }
    }

    val latestBatch = remember(batches) { batches.maxByOrNull { it.month } }
    val latestBatchTotals = remember(latestBatch) {
        if (latestBatch != null) PayrollCalculations.calculateBatchTotals(latestBatch.lines) else null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero welcome banner
        Card(
            colors = CardDefaults.cardColors(containerColor = Navy900),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth().testTag("home_hero_card")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Site Operations Hub",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Accurate wages, advance tracking & cost control",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Active Staff",
                        value = "$activeCount",
                        sub = "${employees.size} total",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        title = "Advances Due",
                        value = PayrollCalculations.fmt(totalOutstandingAdvances),
                        sub = "Outstanding",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Launch Hub
        Text(
            text = "Payroll Modules",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Navy900
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleTile(
                title = "Salary Slips",
                subtitle = "Generate & Share worker pay slips",
                icon = Icons.Default.Receipt,
                color = Teal600,
                bg = TealLight,
                onClick = { onNavigate(ScreenTab.SLIPS) },
                modifier = Modifier.weight(1f)
            )
            ModuleTile(
                title = "Worker History",
                subtitle = "Lifetime wages & advance records",
                icon = Icons.Default.History,
                color = Amber500,
                bg = AmberLight,
                onClick = { onNavigate(ScreenTab.HISTORY) },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModuleTile(
                title = "Monthly Batch",
                subtitle = "Record hours, deductions & pay",
                icon = Icons.Default.Today,
                color = Emerald600,
                bg = EmeraldLight,
                onClick = { onNavigate(ScreenTab.PAYROLL) },
                modifier = Modifier.weight(1f)
            )
            ModuleTile(
                title = "Cost Centers",
                subtitle = "Project cost allocation tracking",
                icon = Icons.Default.BarChart,
                color = Navy800,
                bg = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { onNavigate(ScreenTab.COSTS) },
                modifier = Modifier.weight(1f)
            )
        }

        // Latest Batch Snapshot
        if (latestBatch != null && latestBatchTotals != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Latest Payroll Batch",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                            Text(
                                "${PayrollCalculations.monthLabel(latestBatch.month)} • ${latestBatch.site.ifBlank { "General Site" }}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Slate500
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(TealLight)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${latestBatch.lines.size} Workers",
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
                        Column {
                            Text("Total Net Salary", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                            Text(
                                PayrollCalculations.fmt(latestBatchTotals.net),
                                fontWeight = FontWeight.Bold,
                                color = Emerald600,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Amount Paid", style = MaterialTheme.typography.bodyMedium, color = Slate500)
                            Text(
                                PayrollCalculations.fmt(latestBatchTotals.paid),
                                fontWeight = FontWeight.Bold,
                                color = Navy900,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onNavigate(ScreenTab.PAYROLL) }
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Open Batch Editor",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Teal600
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Teal600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Column {
            Text(title, color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(sub, color = Color.White.copy(alpha = 0.65f), style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ModuleTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    bg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .clickable(onClick = onClick)
            .testTag("module_tile_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(title, fontWeight = FontWeight.Bold, color = Navy900, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = Slate500, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
        }
    }
}
