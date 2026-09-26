package com.example.payrollsheet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.ui.components.AdvanceDialog
import com.example.payrollsheet.ui.components.EmployeeDialog
import com.example.payrollsheet.ui.components.EmployeeDialogMode
import com.example.payrollsheet.ui.screens.AdvancesScreen
import com.example.payrollsheet.ui.screens.CostAllocationScreen
import com.example.payrollsheet.ui.screens.EmployeesScreen
import com.example.payrollsheet.ui.screens.HistoryScreen
import com.example.payrollsheet.ui.screens.HomeScreen
import com.example.payrollsheet.ui.screens.PayrollScreen
import com.example.payrollsheet.ui.screens.SalarySlipsScreen
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.PayrollSheetTheme
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight
import com.example.payrollsheet.ui.viewmodel.PayrollViewModel
import com.example.payrollsheet.ui.viewmodel.PayrollViewModelFactory
import com.example.payrollsheet.ui.viewmodel.ScreenTab

class MainActivity : ComponentActivity() {

    private val viewModel: PayrollViewModel by viewModels {
        PayrollViewModelFactory((application as PayrollApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayrollSheetTheme {
                PayrollMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayrollMainApp(viewModel: PayrollViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val batches by viewModel.batches.collectAsStateWithLifecycle()
    val advances by viewModel.advances.collectAsStateWithLifecycle()
    val currentBatch by viewModel.currentBatch.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var employeeDialogMode by remember { mutableStateOf<EmployeeDialogMode?>(null) }
    var selectedEmployeeForDialog by remember { mutableStateOf<Employee?>(null) }

    var showAdvanceDialog by remember { mutableStateOf(false) }
    var editingAdvanceTx by remember { mutableStateOf<AdvanceTx?>(null) }

    // Toast listener
    LaunchedEffect(viewModel) {
        viewModel.toastEvent.collect { (msg, isError) ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = if (isError) SnackbarDuration.Long else SnackbarDuration.Short
            )
        }
    }

    // Android Hardware Back button handling
    BackHandler(enabled = currentTab != ScreenTab.HOME) {
        viewModel.setTab(ScreenTab.HOME)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy900,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Teal600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Construction,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Site Payroll Manager",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                text = when (currentTab) {
                                    ScreenTab.HOME -> "Operations Dashboard"
                                    ScreenTab.EMPLOYEES -> "Worker Directory"
                                    ScreenTab.PAYROLL -> "Monthly Payroll"
                                    ScreenTab.ADVANCES -> "Advance Management"
                                    ScreenTab.COSTS -> "Cost Center Allocation"
                                    ScreenTab.HISTORY -> "Employee History"
                                    ScreenTab.SLIPS -> "Salary Pay Slips"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                            )
                        }
                    }
                },
                actions = {
                    // Role switcher pill badge
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Navy800)
                            .clickable { viewModel.toggleRole() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("role_switcher_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (userRole == "admin") Emerald600 else Teal600)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = userRole.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val navItems = listOf(
                    NavigationItem("Home", Icons.Default.Home, ScreenTab.HOME),
                    NavigationItem("Employees", Icons.Default.People, ScreenTab.EMPLOYEES),
                    NavigationItem("Payroll", Icons.Default.Today, ScreenTab.PAYROLL),
                    NavigationItem("Advances", Icons.Default.Payments, ScreenTab.ADVANCES),
                    NavigationItem("Costs", Icons.Default.BarChart, ScreenTab.COSTS)
                )

                navItems.forEach { item ->
                    val isSelected = currentTab == item.tab ||
                            (item.tab == ScreenTab.HOME && (currentTab == ScreenTab.HISTORY || currentTab == ScreenTab.SLIPS))
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.setTab(item.tab) },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                        },
                        label = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = TealLight,
                            unselectedIconColor = Slate500,
                            unselectedTextColor = Slate500
                        ),
                        modifier = Modifier.testTag("nav_tab_${item.label.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Crossfade(targetState = currentTab, label = "screen_transition") { tab ->
                when (tab) {
                    ScreenTab.HOME -> HomeScreen(
                        employees = employees,
                        batches = batches,
                        advances = advances,
                        onNavigate = { nextTab -> viewModel.setTab(nextTab) }
                    )
                    ScreenTab.EMPLOYEES -> EmployeesScreen(
                        employees = employees,
                        canDelete = userRole == "admin",
                        onNewEmployee = {
                            selectedEmployeeForDialog = null
                            employeeDialogMode = EmployeeDialogMode.NEW
                        },
                        onViewEmployee = { emp ->
                            selectedEmployeeForDialog = emp
                            employeeDialogMode = EmployeeDialogMode.VIEW
                        },
                        onEditEmployee = { emp ->
                            selectedEmployeeForDialog = emp
                            employeeDialogMode = EmployeeDialogMode.EDIT
                        },
                        onDeleteEmployee = { emp -> viewModel.deleteEmployee(emp) }
                    )
                    ScreenTab.PAYROLL -> PayrollScreen(
                        currentBatch = currentBatch,
                        employees = employees,
                        canDelete = userRole == "admin",
                        onMonthChange = { viewModel.selectBatchMonth(it) },
                        onSiteChange = { viewModel.updateBatchSite(it) },
                        onForemanChange = { viewModel.updateBatchForeman(it) },
                        onAddEmployee = { viewModel.addEmployeeToBatch(it) },
                        onAddAllActive = { viewModel.addAllActiveEmployees() },
                        onUpdateLine = { idx, line -> viewModel.updateLine(idx, line) },
                        onRemoveLine = { idx -> viewModel.removeLine(idx) },
                        onSaveBatch = { viewModel.saveBatch() },
                        onDeleteBatch = { viewModel.deleteBatch(it) }
                    )
                    ScreenTab.ADVANCES -> AdvancesScreen(
                        advances = advances,
                        employees = employees,
                        batches = batches,
                        canDelete = userRole == "admin",
                        onNewAdvance = {
                            editingAdvanceTx = null
                            showAdvanceDialog = true
                        },
                        onEditAdvance = { tx ->
                            editingAdvanceTx = tx
                            showAdvanceDialog = true
                        },
                        onDeleteAdvance = { viewModel.deleteAdvance(it) }
                    )
                    ScreenTab.COSTS -> CostAllocationScreen(
                        batches = batches,
                        employees = employees
                    )
                    ScreenTab.HISTORY -> HistoryScreen(
                        employees = employees,
                        batches = batches,
                        advances = advances
                    )
                    ScreenTab.SLIPS -> SalarySlipsScreen(
                        employees = employees,
                        batches = batches,
                        advances = advances
                    )
                }
            }
        }
    }

    // Employee Dialog
    employeeDialogMode?.let { mode ->
        EmployeeDialog(
            mode = mode,
            employee = selectedEmployeeForDialog,
            onDismiss = { employeeDialogMode = null },
            onSave = { name, trade, idNumber, rate, status ->
                viewModel.saveEmployee(
                    id = selectedEmployeeForDialog?.id ?: 0L,
                    name = name,
                    trade = trade,
                    idNumber = idNumber,
                    hourlyRate = rate,
                    status = status
                )
                employeeDialogMode = null
            },
            onSwitchToEdit = {
                employeeDialogMode = EmployeeDialogMode.EDIT
            }
        )
    }

    // Advance Dialog
    if (showAdvanceDialog) {
        AdvanceDialog(
            employees = employees,
            existingTx = editingAdvanceTx,
            onDismiss = {
                showAdvanceDialog = false
                editingAdvanceTx = null
            },
            onSave = { empId, date, amount, reason, method, notes ->
                viewModel.saveAdvance(
                    id = editingAdvanceTx?.id ?: "",
                    employeeId = empId,
                    date = date,
                    amount = amount,
                    reason = reason,
                    paymentMethod = method,
                    notes = notes
                )
                showAdvanceDialog = false
                editingAdvanceTx = null
            }
        )
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val tab: ScreenTab
)
