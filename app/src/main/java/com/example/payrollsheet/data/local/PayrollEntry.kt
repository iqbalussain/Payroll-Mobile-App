package com.example.payrollsheet.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing an individual employee's monthly payroll record,
 * linking to [EmployeeEntity] via a foreign key with cascade deletion.
 *
 * Stores monthly salary computations, paid amounts, and payment dates.
 */
@Entity(
    tableName = "payroll_entries",
    foreignKeys = [
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["employeeId"]),
        Index(value = ["month"]),
        Index(value = ["employeeId", "month"], unique = true),
        Index(value = ["paymentDate"])
    ]
)
data class PayrollEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val employeeId: Long,
    val month: String,                     // Format: "YYYY-MM", e.g. "2026-09"
    val hoursWorked: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val grossSalary: Double = 0.0,
    val deductions: Double = 0.0,
    val netSalary: Double = 0.0,
    val paidAmount: Double = 0.0,
    val paymentDate: String = "",          // Format: "YYYY-MM-DD", e.g. "2026-09-30"
    val paymentStatus: String = "Paid",    // "Paid", "Pending", "Partial"
    val paymentMethod: String = "Bank",    // "Bank", "Cash", "Cheque"
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
