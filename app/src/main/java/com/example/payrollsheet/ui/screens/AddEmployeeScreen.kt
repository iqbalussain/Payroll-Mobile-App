package com.example.payrollsheet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.ui.theme.Emerald600
import com.example.payrollsheet.ui.theme.EmeraldLight
import com.example.payrollsheet.ui.theme.Navy800
import com.example.payrollsheet.ui.theme.Navy900
import com.example.payrollsheet.ui.theme.Red600
import com.example.payrollsheet.ui.theme.Slate100
import com.example.payrollsheet.ui.theme.Slate200
import com.example.payrollsheet.ui.theme.Slate400
import com.example.payrollsheet.ui.theme.Slate500
import com.example.payrollsheet.ui.theme.Slate600
import com.example.payrollsheet.ui.theme.Slate900
import com.example.payrollsheet.ui.theme.Teal600
import com.example.payrollsheet.ui.theme.TealLight

private val PRESET_ROLES = listOf(
    "FORMAN",
    "CARPENTER",
    "STEEL FIXER",
    "MASON",
    "ELEC",
    "PLUB",
    "HELPER",
    "PAINTER",
    "WELDER"
)

private val STATUS_OPTIONS = listOf("Active", "Holiday", "Cancelled")

@Composable
fun AddEmployeeScreen(
    onSaveEmployee: (name: String, role: String, baseSalary: Double, idNumber: String, status: String) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSubmittedSuccess by remember { mutableStateOf(false) }
    var lastSavedName by remember { mutableStateOf("") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Slate100)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate900
                        )
                    }

                    Column {
                        Text(
                            text = "New Employee Form",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "Register worker in Room database",
                            fontSize = 12.sp,
                            color = Slate600
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        tint = Teal600,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isSubmittedSuccess) {
                // Success Card
                Card(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Emerald600.copy(alpha = 0.4f)))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Emerald600),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Employee Added Successfully!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        Text(
                            text = "$lastSavedName has been stored in local Room persistence and can now be assigned to payroll batches.",
                            fontSize = 13.sp,
                            color = Slate600,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isSubmittedSuccess = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Add Another")
                            }

                            Button(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Teal600)
                            ) {
                                Text("View List")
                            }
                        }
                    }
                }
            } else {
                // Form Card
                Box(modifier = Modifier.widthIn(max = 600.dp)) {
                    AddEmployeeForm(
                        onSubmit = { name, role, salary, idNumber, status ->
                            onSaveEmployee(name, role, salary, idNumber, status)
                            lastSavedName = name
                            isSubmittedSuccess = true
                        },
                        onCancel = onNavigateBack
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEmployeeForm(
    onSubmit: (name: String, role: String, baseSalary: Double, idNumber: String, status: String) -> Unit,
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("HELPER") }
    var customRole by remember { mutableStateOf("") }
    var baseSalaryText by remember { mutableStateOf("15.00") }
    var idNumber by remember { mutableStateOf("EMP-${(1000..9999).random()}") }
    var selectedStatus by remember { mutableStateOf("Active") }

    // Validation error states
    var nameError by remember { mutableStateOf<String?>(null) }
    var roleError by remember { mutableStateOf<String?>(null) }
    var salaryError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        var isValid = true

        // Validate Name
        if (name.isBlank()) {
            nameError = "Employee name is required"
            isValid = false
        } else if (name.trim().length < 2) {
            nameError = "Name must be at least 2 characters"
            isValid = false
        } else {
            nameError = null
        }

        // Validate Role
        val effectiveRole = if (customRole.isNotBlank()) customRole.trim().uppercase() else selectedRole
        if (effectiveRole.isBlank()) {
            roleError = "Please select or enter a role/trade"
            isValid = false
        } else {
            roleError = null
        }

        // Validate Salary
        val parsedSalary = baseSalaryText.toDoubleOrNull()
        if (baseSalaryText.isBlank()) {
            salaryError = "Base salary is required"
            isValid = false
        } else if (parsedSalary == null || parsedSalary <= 0.0) {
            salaryError = "Salary must be a positive number greater than 0"
            isValid = false
        } else {
            salaryError = null
        }

        if (isValid && parsedSalary != null) {
            onSubmit(
                name.trim(),
                effectiveRole,
                parsedSalary,
                idNumber.trim(),
                selectedStatus
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Slate200)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Employee Name Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EMPLOYEE FULL NAME *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError != null) nameError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_name_input"),
                    placeholder = { Text("e.g. Tariq Mansoor") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = if (nameError != null) Red600 else Slate400
                        )
                    },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it, color = Red600) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal600,
                        unfocusedBorderColor = Slate200
                    )
                )
            }

            // Role / Trade Selection
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ROLE / TRADE *",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Selected: ${if (customRole.isNotBlank()) customRole else selectedRole}",
                        fontSize = 11.sp,
                        color = Teal600,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PRESET_ROLES.forEach { role ->
                        val isSelected = selectedRole == role && customRole.isBlank()
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedRole = role
                                customRole = ""
                                if (roleError != null) roleError = null
                            },
                            label = { Text(role, fontSize = 12.sp) },
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

                // Optional Custom Role Input
                OutlinedTextField(
                    value = customRole,
                    onValueChange = {
                        customRole = it
                        if (roleError != null) roleError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_custom_role_input"),
                    placeholder = { Text("Or enter custom trade / role...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Work,
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
                if (roleError != null) {
                    Text(roleError!!, color = Red600, fontSize = 12.sp)
                }
            }

            // Base Salary / Hourly Rate Field
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "BASE SALARY / HOURLY RATE ($) *",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = baseSalaryText,
                    onValueChange = {
                        baseSalaryText = it
                        if (salaryError != null) salaryError = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_salary_input"),
                    placeholder = { Text("e.g. 18.50") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = if (salaryError != null) Red600 else Slate400
                        )
                    },
                    isError = salaryError != null,
                    supportingText = salaryError?.let { { Text(it, color = Red600) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Teal600,
                        unfocusedBorderColor = Slate200
                    )
                )

                // Live calculation preview
                val salaryNum = baseSalaryText.toDoubleOrNull() ?: 0.0
                if (salaryNum > 0.0) {
                    val monthlyEst = salaryNum * 208.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldLight)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Standard Monthly Estimate (208 hrs):",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                        Text(
                            text = PayrollCalculations.formatCurrency(monthlyEst),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600
                        )
                    }
                }
            }

            // Employee ID Badge Number
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "EMPLOYEE BADGE / ID NUMBER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                OutlinedTextField(
                    value = idNumber,
                    onValueChange = { idNumber = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("employee_id_input"),
                    placeholder = { Text("e.g. EMP-1008") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Badge,
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
            }

            // Status Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "EMPLOYMENT STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate500,
                    letterSpacing = 0.5.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    STATUS_OPTIONS.forEach { status ->
                        val isSelected = selectedStatus == status
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TealLight else Slate100)
                                .border(
                                    1.dp,
                                    if (isSelected) Teal600 else Slate200,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedStatus = status }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = status,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Teal600 else Slate600
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onCancel != null) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }
                }

                Button(
                    onClick = { validateAndSubmit() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_employee_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Teal600)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save to Database",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
