package com.example.payrollsheet.data.repository

import com.example.payrollsheet.data.local.AdvanceTxEntity
import com.example.payrollsheet.data.local.EmployeeDao
import com.example.payrollsheet.data.local.EmployeeEntity
import com.example.payrollsheet.data.local.PayrollBatchEntity
import com.example.payrollsheet.data.local.PayrollDao
import com.example.payrollsheet.data.local.PayrollEntry
import com.example.payrollsheet.data.local.PayrollEntryDao
import com.example.payrollsheet.data.local.PayrollLineEntity
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.UUID

class PayrollRepository(
    private val dao: PayrollDao,
    private val employeeDao: EmployeeDao? = null,
    private val employeeRepository: EmployeeRepository? = null,
    private val payrollEntryDao: PayrollEntryDao? = null
) {

    val payrollEntries: Flow<List<PayrollEntry>> = payrollEntryDao?.getAllEntries()
        ?: flowOf(emptyList())

    fun getPayrollEntriesForMonth(month: String): Flow<List<PayrollEntry>> =
        payrollEntryDao?.getEntriesForMonth(month) ?: flowOf(emptyList())

    val employees: Flow<List<Employee>> = employeeRepository?.allEmployees
        ?: (employeeDao?.getAllEmployees() ?: dao.getAllEmployees()).map { entities ->
            entities.map { it.toDomain() }
        }

    val advances: Flow<List<AdvanceTx>> = dao.getAllAdvances().map { entities ->
        entities.map { it.toDomain() }
    }

    val batches: Flow<List<PayrollBatch>> = combine(
        dao.getAllBatches(),
        dao.getAllLines()
    ) { batchEntities, lineEntities ->
        val linesByBatch = lineEntities.groupBy { it.batchId }
        batchEntities.map { batchEntity ->
            val lines = (linesByBatch[batchEntity.id] ?: emptyList()).map { it.toDomain() }
            PayrollBatch(
                id = batchEntity.id,
                month = batchEntity.month,
                site = batchEntity.site,
                foreman = batchEntity.foreman,
                lines = lines
            )
        }
    }

    suspend fun saveEmployee(employee: Employee): Long {
        return employeeRepository?.insertEmployee(employee)
            ?: (employeeDao?.insertEmployee(EmployeeEntity.fromDomain(employee))
                ?: dao.insertEmployee(EmployeeEntity.fromDomain(employee)))
    }

    suspend fun deleteEmployee(employeeId: Long) {
        if (employeeRepository != null) {
            employeeRepository.deleteEmployeeById(employeeId)
        } else if (employeeDao != null) {
            employeeDao.deleteEmployeeById(employeeId)
        } else {
            dao.deleteEmployeeById(employeeId)
        }
    }

    suspend fun saveBatch(batch: PayrollBatch): String {
        val batchId = if (batch.id.isNotBlank()) batch.id else UUID.randomUUID().toString()
        val batchEntity = PayrollBatchEntity(
            id = batchId,
            month = batch.month,
            site = batch.site,
            foreman = batch.foreman
        )
        val lineEntities = batch.lines
            .filter { it.employeeId > 0 }
            .map { line ->
                val lineId = if (line.id.isNotBlank()) line.id else UUID.randomUUID().toString()
                PayrollLineEntity(
                    id = lineId,
                    batchId = batchId,
                    employeeId = line.employeeId,
                    month = if (line.month.isNotBlank()) line.month else batch.month,
                    foreman = if (line.foreman.isNotBlank()) line.foreman else batch.foreman,
                    hours = line.hours,
                    rate = line.rate,
                    foodDeduction = line.foodDeduction,
                    prevAdvance = line.prevAdvance,
                    newAdvance = line.newAdvance,
                    otherDeduction = line.otherDeduction,
                    netSalary = line.netSalary,
                    paid = line.paid
                )
            }
        dao.saveBatchWithLines(batchEntity, lineEntities)

        // Also persist corresponding PayrollEntry records with payment dates
        payrollEntryDao?.let { pDao ->
            val entries = batch.lines.filter { it.employeeId > 0 }.map { line ->
                val gross = line.hours * line.rate
                val deds = line.foodDeduction + line.newAdvance + line.otherDeduction
                PayrollEntry(
                    employeeId = line.employeeId,
                    month = if (line.month.isNotBlank()) line.month else batch.month,
                    hoursWorked = line.hours,
                    hourlyRate = line.rate,
                    grossSalary = gross,
                    deductions = deds,
                    netSalary = line.netSalary,
                    paidAmount = line.paid,
                    paymentDate = "${batch.month}-28",
                    paymentStatus = if (line.paid >= line.netSalary && line.netSalary > 0) "Paid" else if (line.paid > 0) "Partial" else "Pending",
                    paymentMethod = "Bank",
                    notes = "Batch ${batch.month} on site ${batch.site}"
                )
            }
            if (entries.isNotEmpty()) {
                pDao.insertEntries(entries)
            }
        }

        return batchId
    }

    suspend fun savePayrollEntry(entry: PayrollEntry): Long {
        return payrollEntryDao?.insertEntry(entry) ?: 0L
    }

    suspend fun deleteBatch(batchId: String) {
        dao.deleteBatch(batchId)
    }

    suspend fun saveAdvance(advance: AdvanceTx): String {
        val txId = if (advance.id.isNotBlank()) advance.id else UUID.randomUUID().toString()
        val entity = AdvanceTxEntity(
            id = txId,
            employeeId = advance.employeeId,
            date = advance.date,
            amount = advance.amount,
            reason = advance.reason,
            paymentMethod = advance.paymentMethod,
            notes = advance.notes
        )
        dao.insertAdvance(entity)
        return txId
    }

    suspend fun deleteAdvance(id: String) {
        dao.deleteAdvanceById(id)
    }
}
