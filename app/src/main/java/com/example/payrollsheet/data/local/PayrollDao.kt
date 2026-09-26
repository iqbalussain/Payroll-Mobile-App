package com.example.payrollsheet.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PayrollDao {

    // --- Employees ---
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployeeById(id: Long)

    // --- Batches ---
    @Query("SELECT * FROM payroll_batches ORDER BY month DESC")
    fun getAllBatches(): Flow<List<PayrollBatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: PayrollBatchEntity)

    @Query("DELETE FROM payroll_batches WHERE id = :batchId")
    suspend fun deleteBatch(batchId: String)

    // --- Lines ---
    @Query("SELECT * FROM payroll_lines")
    fun getAllLines(): Flow<List<PayrollLineEntity>>

    @Query("SELECT * FROM payroll_lines WHERE batchId = :batchId")
    suspend fun getLinesForBatch(batchId: String): List<PayrollLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLines(lines: List<PayrollLineEntity>)

    @Query("DELETE FROM payroll_lines WHERE batchId = :batchId")
    suspend fun deleteLinesForBatch(batchId: String)

    @Transaction
    suspend fun saveBatchWithLines(
        batch: PayrollBatchEntity,
        lines: List<PayrollLineEntity>
    ) {
        insertBatch(batch)
        deleteLinesForBatch(batch.id)
        if (lines.isNotEmpty()) {
            insertLines(lines)
        }
    }

    // --- Advances ---
    @Query("SELECT * FROM advance_transactions ORDER BY date DESC, createdAt DESC")
    fun getAllAdvances(): Flow<List<AdvanceTxEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvanceTxEntity)

    @Query("DELETE FROM advance_transactions WHERE id = :id")
    suspend fun deleteAdvanceById(id: String)
}
