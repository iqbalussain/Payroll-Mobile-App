package com.example.payrollsheet.data.repository

import com.example.payrollsheet.data.local.AdvanceTxEntity
import com.example.payrollsheet.data.local.EmployeeEntity
import com.example.payrollsheet.data.local.PayrollBatchEntity
import com.example.payrollsheet.data.local.PayrollDao
import com.example.payrollsheet.data.local.PayrollLineEntity
import com.example.payrollsheet.data.model.AdvanceTx
import com.example.payrollsheet.data.model.Employee
import com.example.payrollsheet.data.model.PayrollBatch
import com.example.payrollsheet.data.model.PayrollLine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class PayrollRepository(private val dao: PayrollDao) {

    val employees: Flow<List<Employee>> = dao.getAllEmployees().map { entities ->
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
        return dao.insertEmployee(EmployeeEntity.fromDomain(employee))
    }

    suspend fun deleteEmployee(employeeId: Long) {
        dao.deleteEmployeeById(employeeId)
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
        return batchId
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
