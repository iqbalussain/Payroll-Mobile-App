package com.example.payrollsheet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.local.PayrollEntry
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.components.MonthlyDisbursementPoint
import com.example.payrollsheet.ui.components.PayrollDisbursementLineChart
import com.example.payrollsheet.util.CsvExporter
import java.util.Locale
import com.example.payrollsheet.ui.theme.Amber500
import com.example.payrollsheet.ui.theme.AmberLight
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.RedLight
import com.example.payrollsheet.ui.theme.Slate100
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Slate900
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

private val YEARS = listOf("2024", "2025", "2026", "2027")
private val MONTH_NAMES = listOf(
    "All Months" to "",
    "Jan" to "01",
    "Feb" to "02",
    "Mar" to "03",
    "Apr" to "04",
    "May" to "05",
    "Jun" to "06",
    "Jul" to "07",
    "Aug" to "08",
    "Sep" to "09",
    "Oct" to "10",
    "Nov" to "11",
    "Dec" to "12"
)

data class UnifiedSummaryEntry(
    val id: String,
    val employeeId: Long,
    val employeeName: String,
    val employeeTrade: String,
    val employeeIdNumber: String,
    val month: String,
    val hoursWorked: Double,
    val hourlyRate: Double,
    val grossSalary: Double,
    val deductions: Double,
    val netSalary: Double,
    val paidAmount: Double,
    val paymentDate: String,
    val paymentStatus: String,
    val paymentMethod: String,
    val notes: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollSummaryScreen(
    payrollEntries: List<PayrollEntry>,
    batches: List<PayrollBatch>,
    employees: List<Employee>,
    modifier: Modifier = Modifier,
    onNavigateToBatch: ((String) -> Unit)? = null
) {
    var selectedYear by remember { mutableStateOf("2026") }
    var selectedMonthCode by remember { mutableStateOf("09") } // "09" = Sep
    var yearDropdownExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("ALL") } // "ALL", "PAID", "PARTIAL", "PENDING"
    var showExportDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val employeeMap = remember(employees) { employees.associateBy { it.id } }

    val defaultCsvFilename = remember(selectedYear, selectedMonthCode) {
        "Payroll_Summary_${if (selectedMonthCode.isNotBlank()) "$selectedYear-$selectedMonthCode" else selectedYear}.csv"
    }

    val selectedPeriodDisplay = remember(selectedYear, selectedMonthCode) {
        val monthPair = MONTH_NAMES.firstOrNull { it.second == selectedMonthCode }
        if (selectedMonthCode.isBlank()) "Full Year $selectedYear" else "${monthPair?.first ?: selectedMonthCode} $selectedYear"
    }

    // Merge and unify records from both dedicated PayrollEntry entities and current batches
    val unifiedEntries by remember(payrollEntries, batches, employees, selectedYear, selectedMonthCode) {
        derivedStateOf {
            val list = mutableListOf<UnifiedSummaryEntry>()
            val processedKeys = mutableSetOf<String>()

            // 1. From PayrollEntry entities
            payrollEntries.forEach { entry ->
                val key = "${entry.employeeId}_${entry.month}"
                processedKeys.add(key)
                val emp = employeeMap[entry.employeeId]
                list.add(
                    UnifiedSummaryEntry(
                        id = "entry_${entry.id}",
                        employeeId = entry.employeeId,
                        employeeName = emp?.name ?: "Worker #${entry.employeeId}",
                        employeeTrade = emp?.trade ?: "WORKER",
                        employeeIdNumber = emp?.idNumber ?: "",
                        month = entry.month,
                        hoursWorked = entry.hoursWorked,
                        hourlyRate = entry.hourlyRate,
                        grossSalary = entry.grossSalary,
                        deductions = entry.deductions,
                        netSalary = entry.netSalary,
                        paidAmount = entry.paidAmount,
                        paymentDate = if (entry.paymentDate.isNotBlank()) entry.paymentDate else "${entry.month}-28",
                        paymentStatus = entry.paymentStatus,
                        paymentMethod = entry.paymentMethod,
                        notes = entry.notes
                    )
                )
            }

            // 2. From batches (lines) that might not be in payrollEntries yet
            batches.forEach { batch ->
                batch.lines.forEach { line ->
                    val key = "${line.employeeId}_${batch.month}"
                    if (!processedKeys.contains(key) && line.employeeId > 0) {
                        processedKeys.add(key)
                        val emp = employeeMap[line.employeeId]
                        val gross = line.hours * line.rate
                        val deds = line.foodDeduction + line.newAdvance + line.otherDeduction
                        val status = if (line.paid >= line.netSalary && line.netSalary > 0) "Paid"
                        else if (line.paid > 0) "Partial"
                        else "Pending"

                        list.add(
                            UnifiedSummaryEntry(
                                id = "line_${line.id}",
                                employeeId = line.employeeId,
                                employeeName = emp?.name ?: "Worker #${line.employeeId}",
                                employeeTrade = emp?.trade ?: "WORKER",
                                employeeIdNumber = emp?.idNumber ?: "",
                                month = batch.month,
                                hoursWorked = line.hours,
                                hourlyRate = line.rate,
                                grossSalary = gross,
                                deductions = deds,
                                netSalary = line.netSalary,
                                paidAmount = line.paid,
                                paymentDate = "${batch.month}-28",
                                paymentStatus = status,
                                paymentMethod = "Bank",
                                notes = "Site: ${batch.site}"
                            )
                        )
                    }
                }
            }

            list
        }
    }

    // Dynamic 6-Month Data Points for Line Chart
    val sixMonthDataPoints by remember(unifiedEntries, selectedYear, selectedMonthCode) {
        derivedStateOf {
            val baseMonthInt = selectedMonthCode.toIntOrNull() ?: 9
            val baseYearInt = selectedYear.toIntOrNull() ?: 2026
            val points = mutableListOf<MonthlyDisbursementPoint>()
            val monthNamesShort = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val monthNamesFull = listOf("", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

            for (i in 5 downTo 0) {
                var m = baseMonthInt - i
                var y = baseYearInt
                while (m <= 0) {
                    m += 12
                    y -= 1
                }
                val monthCode = String.format(Locale.US, "%04d-%02d", y, m)
                val monthLabel = "${monthNamesShort.getOrElse(m) { "$m" }} '${y.toString().takeLast(2)}"
                val fullMonthName = "${monthNamesFull.getOrElse(m) { "$m" }} $y"

                val matching = unifiedEntries.filter { it.month == monthCode }
                val netSum = matching.sumOf { it.netSalary }
                val grossSum = matching.sumOf { it.grossSalary }
                val dedsSum = matching.sumOf { it.deductions }
                val workerCount = matching.map { it.employeeId }.distinct().size

                points.add(
                    MonthlyDisbursementPoint(
                        monthCode = monthCode,
                        monthLabel = monthLabel,
                        fullMonthName = fullMonthName,
                        netDisbursed = netSum,
                        grossSalary = grossSum,
                        deductions = dedsSum,
                        employeeCount = workerCount
                    )
                )
            }
            points
        }
    // Filter by year & month
    val filteredEntries by remember(unifiedEntries, selectedYear, selectedMonthCode, searchQuery, statusFilter) {
        derivedStateOf {
            unifiedEntries.filter { item ->
                // Filter by year
                val matchesYear = item.month.startsWith(selectedYear)

                // Filter by month
                val matchesMonth = if (selectedMonthCode.isBlank()) true else item.month.endsWith("-$selectedMonthCode")

                // Filter by search query
                val matchesSearch = if (searchQuery.isBlank()) true else {
                    item.employeeName.contains(searchQuery, ignoreCase = true) ||
                            item.employeeTrade.contains(searchQuery, ignoreCase = true) ||
                            item.employeeIdNumber.contains(searchQuery, ignoreCase = true)
                }

                // Filter by status
                val matchesStatus = when (statusFilter) {
                    "PAID" -> item.paymentStatus.equals("Paid", ignoreCase = true)
                    "PARTIAL" -> item.paymentStatus.equals("Partial", ignoreCase = true)
                    "PENDING" -> item.paymentStatus.equals("Pending", ignoreCase = true)
                    else -> true
                }

                matchesYear && matchesMonth && matchesSearch && matchesStatus
            }.sortedByDescending { it.paymentDate }
        }
    }

    // Disbursements Aggregations
    val totalDisbursedNet by remember(filteredEntries) { derivedStateOf { filteredEntries.sumOf { it.netSalary } } }
    val totalDisbursedGross by remember(filteredEntries) { derivedStateOf { filteredEntries.sumOf { it.grossSalary } } }
    val totalDeductions by remember(filteredEntries) { derivedStateOf { filteredEntries.sumOf { it.deductions } } }
    val totalActualPaid by remember(filteredEntries) { derivedStateOf { filteredEntries.sumOf { it.paidAmount } } }
    val totalRemainingBalance by remember(filteredEntries) { derivedStateOf { maxOf(0.0, totalDisbursedNet - totalActualPaid) } }
    val totalHoursLogged by remember(filteredEntries) { derivedStateOf { filteredEntries.sumOf { it.hoursWorked } } }

    val saveCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val csvData = CsvExporter.generatePayrollSummaryCsv(filteredEntries, selectedPeriodDisplay)
            CsvExporter.writeCsvToUri(context, uri, csvData)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = "Monthly Payroll Summary",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Filtered salary disbursements & payment records",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Export CSV Action Button
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showExportDialog = true }
                                .testTag("export_csv_header_button"),
                            color = Slate900,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export CSV",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Export CSV",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Period Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(TealLight)
                                .border(1.dp, Teal600.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedPeriodDisplay,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Teal600
                            )
                        }
                    }
                }
            }

            // Month & Year Filter Controls Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Teal600,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "SELECT PERIOD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate500,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            // Year Dropdown Selector
                            ExposedDropdownMenuBox(
                                expanded = yearDropdownExpanded,
                                onExpandedChange = { yearDropdownExpanded = !yearDropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = "Year: $selectedYear",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                        .width(135.dp)
                                        .height(44.dp)
                                        .testTag("summary_year_selector"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Teal600,
                                        unfocusedBorderColor = Slate200
                                    )
                                )
                                ExposedDropdownMenu(
                                    expanded = yearDropdownExpanded,
                                    onDismissRequest = { yearDropdownExpanded = false }
                                ) {
                                    YEARS.forEach { yr ->
                                        DropdownMenuItem(
                                            text = { Text(yr) },
                                            onClick = {
                                                selectedYear = yr
                                                yearDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Horizontal Month Filter Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            MONTH_NAMES.forEach { (name, code) ->
                                val isSelected = selectedMonthCode == code
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMonthCode = code },
                                    label = { Text(name, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Teal600,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) Teal600 else Slate200
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Total Salary Disbursements KPI Dashboard
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TOTAL SALARY DISBURSEMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )

                    // Hero Net Disbursement Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Emerald600.copy(alpha = 0.3f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Total Net Salary Payout",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate600
                                )
                                Text(
                                    text = PayrollCalculations.formatCurrency(totalDisbursedNet),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate900
                                )
                                Text(
                                    text = "${filteredEntries.size} worker entries · $selectedPeriodDisplay",
                                    fontSize = 11.sp,
                                    color = Slate500
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Emerald600),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Payments,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    // Secondary Metric Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Gross Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Gross Earnings", fontSize = 11.sp, color = Slate500)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = PayrollCalculations.formatCurrency(totalDisbursedGross),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text("Before deds", fontSize = 10.sp, color = Slate400)
                            }
                        }

                        // Total Deductions Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Total Deductions", fontSize = 11.sp, color = Red600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = PayrollCalculations.formatCurrency(totalDeductions),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Red600
                                )
                                Text("Food & advances", fontSize = 10.sp, color = Slate400)
                            }
                        }

                        // Paid Amount Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Disbursed Paid", fontSize = 11.sp, color = Teal600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = PayrollCalculations.formatCurrency(totalActualPaid),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Teal600
                                )
                                Text("Cleared funds", fontSize = 10.sp, color = Slate400)
                            }
                        }
                    }
                }
            }

            // 6-Month Salary Disbursements Line Chart (Recharts style)
            item {
                PayrollDisbursementLineChart(
                    dataPoints = sixMonthDataPoints,
                    onMonthSelected = { monthCode ->
                        val parts = monthCode.split("-")
                        if (parts.size == 2) {
                            selectedYear = parts[0]
                            selectedMonthCode = parts[1]
                        }
                    }
                )
            }

            // Search & Status Quick Filter
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("summary_search_input"),
                        placeholder = { Text("Search worker name, ID, or trade...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Slate400
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Teal600,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    // Status Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL" to "All", "PAID" to "Paid", "PENDING" to "Due").forEach { (code, label) ->
                            val isSelected = statusFilter == code
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Slate900 else Slate100)
                                    .clickable { statusFilter = code }
                                    .padding(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else Slate600
                                )
                            }
                        }
                    }
                }
            }

            // Summary List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MONTHLY PAYROLL ENTRIES (${filteredEntries.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "${String.format(Locale.US, "%.1f", totalHoursLogged)} hrs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        )

                        // Quick CSV export chip
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showExportDialog = true },
                            color = Slate100,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FileDownload,
                                    contentDescription = "Export CSV",
                                    tint = Slate600,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "CSV",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            }
                        }
                    }
                }
            }

            // Entries List
            if (filteredEntries.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Slate100.copy(alpha = 0.5f)),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Slate400,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(
                                text = "No Payroll Entries Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                            Text(
                                text = "No salary records found for $selectedPeriodDisplay. Select another period or generate a payroll batch.",
                                fontSize = 12.sp,
                                color = Slate500,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(filteredEntries, key = { it.id }) { item ->
                    PayrollEntryCard(item = item)
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TableChart,
                        contentDescription = null,
                        tint = Teal600,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "Export Payroll Summary (CSV)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Export comprehensive monthly salary records and payment dates for accounting audits and ledger reconciliation.",
                        fontSize = 12.sp,
                        color = Slate600
                    )

                    // Summary Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Accounting Period:", fontSize = 11.sp, color = Slate500)
                                Text(selectedPeriodDisplay, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Included Records:", fontSize = 11.sp, color = Slate500)
                                Text("${filteredEntries.size} Employees", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Net Disbursed:", fontSize = 11.sp, color = Slate500)
                                Text(PayrollCalculations.formatCurrency(totalDisbursedNet), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Emerald600)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Gross Salary:", fontSize = 11.sp, color = Slate500)
                                Text(PayrollCalculations.formatCurrency(totalDisbursedGross), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("File Name:", fontSize = 11.sp, color = Slate500)
                                Text(defaultCsvFilename, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Slate600)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExportDialog = false
                        saveCsvLauncher.launch(defaultCsvFilename)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_save_device_csv")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Device")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showExportDialog = false
                            val csvData = CsvExporter.generatePayrollSummaryCsv(filteredEntries, selectedPeriodDisplay)
                            CsvExporter.shareCsvFile(context, defaultCsvFilename, csvData)
                        },
                        shape = RoundedCornerShape(8.dp),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Teal600)),
                        modifier = Modifier.testTag("confirm_share_csv")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Teal600,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share File", color = Teal600)
                    }

                    OutlinedButton(
                        onClick = { showExportDialog = false },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
fun PayrollEntryCard(
    item: UnifiedSummaryEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("payroll_entry_card_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Worker Name, ID, Trade, Payment Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.employeeName.take(1).uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Teal600
                        )
                    }

                    Column {
                        Text(
                            text = item.employeeName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Slate100)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.employeeTrade,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate600
                                )
                            }
                            if (item.employeeIdNumber.isNotBlank()) {
                                Text(
                                    text = item.employeeIdNumber,
                                    fontSize = 10.sp,
                                    color = Slate400
                                )
                            }
                        }
                    }
                }

                // Payment Status Badge
                val isPaid = item.paymentStatus.equals("Paid", ignoreCase = true)
                val isPartial = item.paymentStatus.equals("Partial", ignoreCase = true)
                val badgeBg = if (isPaid) EmeraldLight else if (isPartial) AmberLight else RedLight
                val badgeColor = if (isPaid) Emerald600 else if (isPartial) Amber500 else Red600

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.paymentStatus.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            HorizontalDivider(color = Slate100, thickness = 1.dp)

            // Hours & Rate Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "${String.format("%.1f", item.hoursWorked)} hrs @ $${String.format("%.2f", item.hourlyRate)}/hr",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }

                Text(
                    text = "Gross: ${PayrollCalculations.formatCurrency(item.grossSalary)}",
                    fontSize = 12.sp,
                    color = Slate600
                )
            }

            // Deductions & Net Payout Breakdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate100.copy(alpha = 0.7f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Deductions: -${PayrollCalculations.formatCurrency(item.deductions)}",
                        fontSize = 11.sp,
                        color = if (item.deductions > 0) Red600 else Slate500
                    )
                    Text(
                        text = "Paid: ${PayrollCalculations.formatCurrency(item.paidAmount)}",
                        fontSize = 11.sp,
                        color = Teal600,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net Disbursement",
                        fontSize = 10.sp,
                        color = Slate500
                    )
                    Text(
                        text = PayrollCalculations.formatCurrency(item.netSalary),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Slate900
                    )
                }
            }

            // Payment Date & Method Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Paid on: ${item.paymentDate}",
                        fontSize = 11.sp,
                        color = Slate500
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = item.paymentMethod,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate600
                    )
                }
            }
        }
    }
}
