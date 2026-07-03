package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "members")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val mobile: String,
    val monthlyAmount: Double = 1000.0,
    val serialNumber: Int = 1
) : Serializable

@Entity(tableName = "months")
data class MonthEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val monthName: String, // e.g. "January 2026"
    val createdAt: Long = System.currentTimeMillis()
) : Serializable

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MonthEntity::class,
            parentColumns = ["id"],
            childColumns = ["monthId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memberId"]),
        Index(value = ["monthId"]),
        Index(value = ["memberId", "monthId"], unique = true)
    ]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val memberId: Int,
    val monthId: Int,
    val status: String = "PENDING", // "PAID" or "PENDING"
    val paymentMode: String = "None", // "Cash", "PhonePe", "Google Pay", "UPI", "Bank Transfer", "None"
    val paymentDate: Long? = null,
    val remarks: String = "",
    val receiptNumber: String = ""
) : Serializable
