package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemberEntity
import com.example.data.model.MonthEntity
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.flow.Flow

// Data class to represent merged member and payment info
data class MemberWithPayment(
    val memberId: Int,
    val name: String,
    val mobile: String,
    val monthlyAmount: Double,
    val serialNumber: Int,
    val paymentId: Int?, // Nullable because a payment might not exist yet
    val status: String?, // Default: "PENDING"
    val paymentMode: String?, // Default: "None"
    val paymentDate: Long?,
    val remarks: String?,
    val receiptNumber: String?
)

@Dao
interface CommitteeDao {

    // --- Member Operations ---
    @Query("SELECT * FROM members ORDER BY serialNumber ASC, name ASC")
    fun getAllMembersFlow(): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members ORDER BY serialNumber ASC, name ASC")
    suspend fun getAllMembers(): List<MemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: MemberEntity): Long

    @Update
    suspend fun updateMember(member: MemberEntity)

    @Delete
    suspend fun deleteMember(member: MemberEntity)

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getMemberById(id: Int): MemberEntity?


    // --- Month Operations ---
    @Query("SELECT * FROM months ORDER BY createdAt DESC")
    fun getAllMonthsFlow(): Flow<List<MonthEntity>>

    @Query("SELECT * FROM months ORDER BY createdAt DESC")
    suspend fun getAllMonths(): List<MonthEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonth(month: MonthEntity): Long

    @Query("DELETE FROM months WHERE id = :monthId")
    suspend fun deleteMonthById(monthId: Int)


    // --- Payment Operations ---
    @Query("SELECT * FROM payments WHERE monthId = :monthId")
    fun getPaymentsForMonthFlow(monthId: Int): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE monthId = :monthId")
    suspend fun getPaymentsForMonth(monthId: Int): List<PaymentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Query("SELECT * FROM payments WHERE memberId = :memberId AND monthId = :monthId LIMIT 1")
    suspend fun getPaymentForMemberAndMonth(memberId: Int, monthId: Int): PaymentEntity?

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePaymentById(id: Int)


    // --- Merged Operations ---
    @Query("""
        SELECT 
            m.id AS memberId,
            m.name AS name,
            m.mobile AS mobile,
            m.monthlyAmount AS monthlyAmount,
            m.serialNumber AS serialNumber,
            p.id AS paymentId,
            p.status AS status,
            p.paymentMode AS paymentMode,
            p.paymentDate AS paymentDate,
            p.remarks AS remarks,
            p.receiptNumber AS receiptNumber
        FROM members m
        LEFT JOIN payments p ON m.id = p.memberId AND p.monthId = :monthId
        ORDER BY m.serialNumber ASC, m.name ASC
    """)
    fun getMembersWithPaymentsFlow(monthId: Int): Flow<List<MemberWithPayment>>

    @Query("""
        SELECT 
            m.id AS memberId,
            m.name AS name,
            m.mobile AS mobile,
            m.monthlyAmount AS monthlyAmount,
            m.serialNumber AS serialNumber,
            p.id AS paymentId,
            p.status AS status,
            p.paymentMode AS paymentMode,
            p.paymentDate AS paymentDate,
            p.remarks AS remarks,
            p.receiptNumber AS receiptNumber
        FROM members m
        LEFT JOIN payments p ON m.id = p.memberId AND p.monthId = :monthId
        ORDER BY m.serialNumber ASC, m.name ASC
    """)
    suspend fun getMembersWithPayments(monthId: Int): List<MemberWithPayment>
}
