package com.example.payrollsheet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollCalculations
import com.example.payrollsheet.data.model.PayrollLine
import com.example.payrollsheet.data.repository.PayrollRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import com.example.payrollsheet.data.local.PayrollEntry
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class ScreenTab {
    HOME,
    SUMMARY,
    EMPLOYEES,
    ADD_EMPLOYEE,
    PAYROLL,
    ADVANCES,
    COSTS,
    HISTORY,
    SLIPS
}

data class CurrentBatchState(
    val id: String = "",
    val month: String = PayrollCalculations.MONTHS[2], // "2026-09"
    val site: String = "",
    val foreman: String = "",
    val lines: List<PayrollLine> = emptyList(),
    val isSaving: Boolean = false
)

class PayrollViewModel(private val repository: PayrollRepository) : ViewModel() {

    val employees: StateFlow<List<Employee>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<PayrollBatch>> = repository.batches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val advances: StateFlow<List<AdvanceTx>> = repository.advances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val payrollEntries: StateFlow<List<PayrollEntry>> = repository.payrollEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication State
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentTab = MutableStateFlow(ScreenTab.HOME)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    private val _userRole = MutableStateFlow("admin") // "admin" or "hr"
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _currentBatch = MutableStateFlow(CurrentBatchState())
    val currentBatch: StateFlow<CurrentBatchState> = _currentBatch.asStateFlow()

    private val _toastEvent = MutableSharedFlow<Pair<String, Boolean>>() // message, isError
    val toastEvent: SharedFlow<Pair<String, Boolean>> = _toastEvent.asSharedFlow()

    fun login(email: String, role: String = "admin") {
        _isLoggedIn.value = true
        _currentUserEmail.value = email.trim()
        _userRole.value = role
        viewModelScope.launch {
            _toastEvent.emit("Welcome, $email (${role.uppercase()})" to false)
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _currentUserEmail.value = ""
        _currentTab.value = ScreenTab.HOME
        viewModelScope.launch {
            _toastEvent.emit("Signed out successfully" to false)
        }
    }

    init {
        // Initialize batch when batches load or month defaults
        viewModelScope.launch {
            batches.collect { batchList ->
                val current = _currentBatch.value
                val existing = batchList.firstOrNull { it.month == current.month }
                if (existing != null && current.id.isEmpty() && current.lines.isEmpty()) {
                    _currentBatch.value = CurrentBatchState(
                        id = existing.id,
                        month = existing.month,
                        site = existing.site,
                        foreman = existing.foreman,
                        lines = existing.lines
                    )
                }
            }
        }
    }

    fun setTab(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun toggleRole() {
        _userRole.value = if (_userRole.value == "admin") "hr" else "admin"
        viewModelScope.launch {
            _toastEvent.emit("Switched to ${_userRole.value.uppercase()} mode" to false)
        }
    }

    // --- Employee Operations ---

    fun saveEmployee(
        id: Long = 0,
        name: String,
        trade: String,
        idNumber: String,
        hourlyRate: Double,
        status: String
    ) {
        if (name.isBlank()) {
            viewModelScope.launch {
                _toastEvent.emit("Employee name is required" to true)
            }
            return
        }

        viewModelScope.launch {
            try {
                val emp = Employee(
                    id = id,
                    name = name.trim(),
                    trade = trade,
                    idNumber = idNumber.trim(),
                    hourlyRate = hourlyRate,
                    status = status
                )
                repository.saveEmployee(emp)
                _toastEvent.emit("${emp.name} saved successfully" to false)
            } catch (e: Exception) {
                _toastEvent.emit("Error saving employee: ${e.message}" to true)
            }
        }
    }

    fun deleteEmployee(employee: Employee) {
        if (_userRole.value != "admin") {
            viewModelScope.launch {
                _toastEvent.emit("Admin access required to delete employees" to true)
            }
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteEmployee(employee.id)
                _toastEvent.emit("${employee.name} deleted" to false)
            } catch (e: Exception) {
                _toastEvent.emit("Error deleting employee: ${e.message}" to true)
            }
        }
    }

    // --- Payroll Batch Operations ---

    fun selectBatchMonth(month: String) {
        val existing = batches.value.firstOrNull { it.month == month }
        if (existing != null) {
            _currentBatch.value = CurrentBatchState(
                id = existing.id,
                month = existing.month,
                site = existing.site,
                foreman = existing.foreman,
                lines = existing.lines
            )
        } else {
            _currentBatch.value = CurrentBatchState(
                id = "",
                month = month,
                site = _currentBatch.value.site,
                foreman = _currentBatch.value.foreman,
                lines = emptyList()
            )
        }
    }

    fun updateBatchSite(site: String) {
        _currentBatch.value = _currentBatch.value.copy(site = site)
    }

    fun updateBatchForeman(foreman: String) {
        _currentBatch.value = _currentBatch.value.copy(foreman = foreman)
    }

    fun addEmployeeToBatch(employeeId: Long) {
        val emp = employees.value.firstOrNull { it.id == employeeId } ?: return
        val batch = _currentBatch.value
        if (batch.lines.any { it.employeeId == employeeId }) {
            viewModelScope.launch {
                _toastEvent.emit("${emp.name} is already in this batch" to true)
            }
            return
        }

        // Auto-calculate suggested carry forward advance
        val suggestedCarryForward = PayrollCalculations.advanceCarryForward(
            employeeId = employeeId,
            month = batch.month,
            batches = batches.value,
            advances = advances.value
        )

        val newLine = PayrollLine(
            id = UUID.randomUUID().toString(),
            batchId = batch.id,
            employeeId = employeeId,
            month = batch.month,
            foreman = batch.foreman,
            hours = 0.0,
            rate = emp.hourlyRate,
            foodDeduction = 0.0,
            prevAdvance = suggestedCarryForward,
            newAdvance = 0.0,
            otherDeduction = 0.0,
            netSalary = 0.0,
            paid = 0.0
        )

        _currentBatch.value = batch.copy(lines = batch.lines + newLine)
    }

    fun addAllActiveEmployees() {
        val batch = _currentBatch.value
        val locked = PayrollCalculations.lockedEmployeeIds(batches.value, batch.month, batch.id)
        val existingIds = batch.lines.map { it.employeeId }.toSet()
        val toAdd = employees.value
            .filter { it.status == "Active" }
            .filter { !locked.contains(it.id) && !existingIds.contains(it.id) }

        if (toAdd.isEmpty()) {
            viewModelScope.launch {
                _toastEvent.emit("No new active employees to add" to false)
            }
            return
        }

        val newLines = toAdd.map { emp ->
            val suggestedCarry = PayrollCalculations.advanceCarryForward(
                employeeId = emp.id,
                month = batch.month,
                batches = batches.value,
                advances = advances.value
            )
            PayrollLine(
                id = UUID.randomUUID().toString(),
                batchId = batch.id,
                employeeId = emp.id,
                month = batch.month,
                foreman = batch.foreman,
                hours = 0.0,
                rate = emp.hourlyRate,
                foodDeduction = 0.0,
                prevAdvance = suggestedCarry,
                newAdvance = 0.0,
                otherDeduction = 0.0,
                netSalary = 0.0,
                paid = 0.0
            )
        }

        _currentBatch.value = batch.copy(lines = batch.lines + newLines)
        viewModelScope.launch {
            _toastEvent.emit("Added ${toAdd.size} active employees to batch" to false)
        }
    }

    fun updateLine(index: Int, updated: PayrollLine) {
        val batch = _currentBatch.value
        if (index !in batch.lines.indices) return

        // Recalculate net salary based on formula
        val gross = PayrollCalculations.lineGross(updated.hours, updated.rate)
        val computedNet = PayrollCalculations.computeNet(
            gross = gross,
            foodDeduction = updated.foodDeduction,
            prevAdvance = updated.prevAdvance,
            otherDeduction = updated.otherDeduction
        )
        val adjustedLine = updated.copy(netSalary = computedNet)

        val updatedLines = batch.lines.toMutableList()
        updatedLines[index] = adjustedLine
        _currentBatch.value = batch.copy(lines = updatedLines)
    }

    fun removeLine(index: Int) {
        val batch = _currentBatch.value
        if (index !in batch.lines.indices) return
        val updatedLines = batch.lines.toMutableList()
        updatedLines.removeAt(index)
        _currentBatch.value = batch.copy(lines = updatedLines)
    }

    fun saveBatch() {
        val batch = _currentBatch.value
        if (batch.lines.isEmpty()) {
            viewModelScope.launch {
                _toastEvent.emit("Add at least one employee line before saving" to true)
            }
            return
        }

        viewModelScope.launch {
            _currentBatch.value = batch.copy(isSaving = true)
            try {
                val batchDomain = PayrollBatch(
                    id = batch.id,
                    month = batch.month,
                    site = batch.site,
                    foreman = batch.foreman,
                    lines = batch.lines
                )
                val savedId = repository.saveBatch(batchDomain)
                _currentBatch.value = batch.copy(id = savedId, isSaving = false)
                _toastEvent.emit("Payroll batch saved for ${PayrollCalculations.monthLabel(batch.month)}" to false)
            } catch (e: Exception) {
                _currentBatch.value = batch.copy(isSaving = false)
                _toastEvent.emit("Failed to save batch: ${e.message}" to true)
            }
        }
    }

    fun deleteBatch(batchId: String) {
        if (_userRole.value != "admin") {
            viewModelScope.launch {
                _toastEvent.emit("Admin access required to delete batch" to true)
            }
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteBatch(batchId)
                if (_currentBatch.value.id == batchId) {
                    _currentBatch.value = CurrentBatchState(
                        month = _currentBatch.value.month
                    )
                }
                _toastEvent.emit("Payroll batch deleted" to false)
            } catch (e: Exception) {
                _toastEvent.emit("Failed to delete batch: ${e.message}" to true)
            }
        }
    }

    // --- Advance Operations ---

    fun saveAdvance(
        id: String = "",
        employeeId: Long,
        date: String,
        amount: Double,
        reason: String,
        paymentMethod: String,
        notes: String
    ) {
        if (employeeId <= 0) {
            viewModelScope.launch {
                _toastEvent.emit("Please select an employee" to true)
            }
            return
        }
        if (amount <= 0.0) {
            viewModelScope.launch {
                _toastEvent.emit("Advance amount must be greater than zero" to true)
            }
            return
        }

        viewModelScope.launch {
            try {
                val tx = AdvanceTx(
                    id = id,
                    employeeId = employeeId,
                    date = date,
                    amount = amount,
                    reason = reason,
                    paymentMethod = paymentMethod,
                    notes = notes
                )
                repository.saveAdvance(tx)
                _toastEvent.emit("Advance of ${PayrollCalculations.fmt(amount)} recorded" to false)
            } catch (e: Exception) {
                _toastEvent.emit("Failed to record advance: ${e.message}" to true)
            }
        }
    }

    fun deleteAdvance(id: String) {
        if (_userRole.value != "admin") {
            viewModelScope.launch {
                _toastEvent.emit("Admin access required to delete advance transactions" to true)
            }
            return
        }
        viewModelScope.launch {
            try {
                repository.deleteAdvance(id)
                _toastEvent.emit("Advance transaction deleted" to false)
            } catch (e: Exception) {
                _toastEvent.emit("Failed to delete advance: ${e.message}" to true)
            }
        }
    }
}

class PayrollViewModelFactory(private val repository: PayrollRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PayrollViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PayrollViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
