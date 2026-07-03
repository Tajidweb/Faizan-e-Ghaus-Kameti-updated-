package com.example.data.repository

import com.example.data.db.CommitteeDao
import com.example.data.db.MemberWithPayment
import com.example.data.model.MemberEntity
import com.example.data.model.MonthEntity
import com.example.data.model.PaymentEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CommitteeRepository(private val committeeDao: CommitteeDao) {

    val allMembersFlow: Flow<List<MemberEntity>> = committeeDao.getAllMembersFlow()
    val allMonthsFlow: Flow<List<MonthEntity>> = committeeDao.getAllMonthsFlow()

    suspend fun getAllMembers(): List<MemberEntity> = withContext(Dispatchers.IO) {
        committeeDao.getAllMembers()
    }

    suspend fun getAllMonths(): List<MonthEntity> = withContext(Dispatchers.IO) {
        committeeDao.getAllMonths()
    }

    // Insert new member and initialize their payments for all existing months
    suspend fun addMemberAndInitializePayments(member: MemberEntity): Long = withContext(Dispatchers.IO) {
        val memberId = committeeDao.insertMember(member)
        val months = committeeDao.getAllMonths()
        for (month in months) {
            // Check if payment already exists (failsafe)
            val existing = committeeDao.getPaymentForMemberAndMonth(memberId.toInt(), month.id)
            if (existing == null) {
                committeeDao.insertPayment(
                    PaymentEntity(
                        memberId = memberId.toInt(),
                        monthId = month.id,
                        status = "PENDING",
                        paymentMode = "None"
                    )
                )
            }
        }
        memberId
    }

    suspend fun updateMember(member: MemberEntity) = withContext(Dispatchers.IO) {
        committeeDao.updateMember(member)
    }

    suspend fun deleteMember(member: MemberEntity) = withContext(Dispatchers.IO) {
        committeeDao.deleteMember(member)
    }

    // Insert a new month and initialize a pending payment for all existing members
    suspend fun createMonthAndInitializePayments(monthName: String): Long = withContext(Dispatchers.IO) {
        val monthId = committeeDao.insertMonth(MonthEntity(monthName = monthName))
        val members = committeeDao.getAllMembers()
        for (member in members) {
            committeeDao.insertPayment(
                PaymentEntity(
                    memberId = member.id,
                    monthId = monthId.toInt(),
                    status = "PENDING",
                    paymentMode = "None"
                )
            )
        }
        monthId
    }

    suspend fun deleteMonth(monthId: Int) = withContext(Dispatchers.IO) {
        committeeDao.deleteMonthById(monthId)
    }

    fun getMembersWithPaymentsFlow(monthId: Int): Flow<List<MemberWithPayment>> {
        return committeeDao.getMembersWithPaymentsFlow(monthId)
    }

    suspend fun getMembersWithPayments(monthId: Int): List<MemberWithPayment> = withContext(Dispatchers.IO) {
        committeeDao.getMembersWithPayments(monthId)
    }

    suspend fun updatePayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        committeeDao.updatePayment(payment)
    }

    suspend fun insertPayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        committeeDao.insertPayment(payment)
    }

    suspend fun getPaymentForMemberAndMonth(memberId: Int, monthId: Int): PaymentEntity? = withContext(Dispatchers.IO) {
        committeeDao.getPaymentForMemberAndMonth(memberId, monthId)
    }
    
    // Backup helper returning list of all database contents
    suspend fun exportAllData(): Triple<List<MemberEntity>, List<MonthEntity>, List<PaymentEntity>> = withContext(Dispatchers.IO) {
        val members = committeeDao.getAllMembers()
        val months = committeeDao.getAllMonths()
        val payments = mutableListOf<PaymentEntity>()
        for (month in months) {
            payments.addAll(committeeDao.getPaymentsForMonth(month.id))
        }
        Triple(members, months, payments)
    }

    // Restore helper restoring database contents from exported data
    suspend fun restoreData(
        members: List<MemberEntity>,
        months: List<MonthEntity>,
        payments: List<PaymentEntity>
    ) = withContext(Dispatchers.IO) {
        // Since we are restoring, we wipe current tables and insert everything freshly
        // We can use a Room transaction or clean and insert
        // Note: For simplicity and completeness, we delete existing records
        val currentMembers = committeeDao.getAllMembers()
        for (m in currentMembers) committeeDao.deleteMember(m)
        
        val currentMonths = committeeDao.getAllMonths()
        for (mo in currentMonths) committeeDao.deleteMonthById(mo.id)

        // Insert new members
        for (member in members) {
            committeeDao.insertMember(member)
        }
        // Insert new months
        for (month in months) {
            committeeDao.insertMonth(month)
        }
        // Insert payments
        for (payment in payments) {
            committeeDao.insertPayment(payment)
        }
    }
}
