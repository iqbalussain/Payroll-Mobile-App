package com.example.payrollsheet.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing local persistence of [PayrollEntry] records.
 * Provides complete CRUD operations and queries by employee ID, month, and payment dates.
 */
@Dao
interface PayrollEntryDao {

    // --- Flow Queries (Reactive UI) ---

    @Query("SELECT * FROM payroll_entries ORDER BY paymentDate DESC, month DESC")
    fun getAllEntries(): Flow<List<PayrollEntry>>

    /**
     * Query to fetch all payroll entries for a specific employee ID, sorted newest month first.
     */
    @Query("SELECT * FROM payroll_entries WHERE employeeId = :employeeId ORDER BY month DESC")
    fun getEntriesForEmployee(employeeId: Long): Flow<List<PayrollEntry>>

    /**
     * Query to fetch all payroll entries for a specific month (e.g., '2026-09').
     */
    @Query("SELECT * FROM payroll_entries WHERE month = :month ORDER BY id ASC")
    fun getEntriesForMonth(month: String): Flow<List<PayrollEntry>>

    // --- Direct Suspend Queries (One-shot / Background) ---

    /**
     * Suspend query to retrieve all entries for a specific employee ID.
     */
    @Query("SELECT * FROM payroll_entries WHERE employeeId = :employeeId ORDER BY month DESC")
    suspend fun getEntriesByEmployeeId(employeeId: Long): List<PayrollEntry>

    /**
     * Suspend query to retrieve all entries within a specific month.
     */
    @Query("SELECT * FROM payroll_entries WHERE month = :month ORDER BY id ASC")
    suspend fun getEntriesByMonth(month: String): List<PayrollEntry>

    @Query("SELECT * FROM payroll_entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): PayrollEntry?

    @Query("SELECT * FROM payroll_entries WHERE employeeId = :employeeId AND month = :month LIMIT 1")
    suspend fun getEntryForEmployeeAndMonth(employeeId: Long, month: String): PayrollEntry?

    @Query("SELECT COUNT(*) FROM payroll_entries WHERE month = :month")
    suspend fun getEntryCountForMonth(month: String): Int

    // --- CRUD Operations ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: PayrollEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<PayrollEntry>): List<Long>

    @Update
    suspend fun updateEntry(entry: PayrollEntry)

    @Delete
    suspend fun deleteEntry(entry: PayrollEntry)

    @Query("DELETE FROM payroll_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("DELETE FROM payroll_entries WHERE employeeId = :employeeId")
    suspend fun deleteEntriesForEmployee(employeeId: Long)
}
