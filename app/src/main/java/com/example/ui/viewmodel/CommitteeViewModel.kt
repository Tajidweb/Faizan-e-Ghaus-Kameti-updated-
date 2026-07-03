package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.MemberWithPayment
import com.example.data.model.MemberEntity
import com.example.data.model.MonthEntity
import com.example.data.model.PaymentEntity
import com.example.data.repository.CommitteeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommitteeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CommitteeRepository
    
    val allMembers: StateFlow<List<MemberEntity>>
    val allMonths: StateFlow<List<MonthEntity>>

    private val _selectedMonth = MutableStateFlow<MonthEntity?>(null)
    val selectedMonth: StateFlow<MonthEntity?> = _selectedMonth.asStateFlow()

    // Admin Auth State
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminPassword = MutableStateFlow("admin123") // Default, can be updated

    // Filters and Search Queries
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("ALL") // "ALL", "PAID", "PENDING"
    val paymentModeFilter = MutableStateFlow("ALL") // "ALL", "Cash", "PhonePe", "Google Pay", "UPI", "Bank Transfer"

    init {
        val database = AppDatabase.getDatabase(application)
        repository = CommitteeRepository(database.committeeDao())
        
        allMembers = repository.allMembersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allMonths = repository.allMonthsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Automatically select the latest month when the list of months updates
        viewModelScope.launch {
            allMonths.collect { months ->
                if (months.isNotEmpty() && _selectedMonth.value == null) {
                    _selectedMonth.value = months.first()
                }
            }
        }
    }

    // Reactively fetch members with payments based on selected month
    @OptIn(ExperimentalCoroutinesApi::class)
    val membersWithPayments: StateFlow<List<MemberWithPayment>> = selectedMonth
        .flatMapLatest { month ->
            if (month != null) {
                repository.getMembersWithPaymentsFlow(month.id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Reactively combine search query and filters
    val filteredMembersWithPayments: StateFlow<List<MemberWithPayment>> = combine(
        membersWithPayments,
        searchQuery,
        statusFilter,
        paymentModeFilter
    ) { list, query, status, mode ->
        list.filter { m ->
            val matchesQuery = m.name.contains(query, ignoreCase = true) ||
                    m.mobile.contains(query) ||
                    m.serialNumber.toString() == query
            val matchesStatus = when (status) {
                "PAID" -> m.status == "PAID"
                "PENDING" -> m.status != "PAID"
                else -> true
            }
            val matchesMode = when (mode) {
                "ALL" -> true
                else -> m.paymentMode.equals(mode, ignoreCase = true)
            }
            matchesQuery && matchesStatus && matchesMode
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Actions ---

    fun loginAdmin(password: String): Boolean {
        return if (password == _adminPassword.value) {
            _isAdminLoggedIn.value = true
            true
        } else {
            false
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun changeAdminPassword(old: String, new: String): Boolean {
        return if (old == _adminPassword.value && new.isNotBlank()) {
            _adminPassword.value = new
            true
        } else {
            false
        }
    }

    fun selectMonth(month: MonthEntity) {
        _selectedMonth.value = month
    }

    fun addMember(name: String, mobile: String, monthlyAmount: Double, serialNumber: Int) {
        viewModelScope.launch {
            val member = MemberEntity(
                name = name,
                mobile = mobile,
                monthlyAmount = monthlyAmount,
                serialNumber = serialNumber
            )
            repository.addMemberAndInitializePayments(member)
        }
    }

    fun updateMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }

    fun createMonth(monthName: String) {
        viewModelScope.launch {
            val id = repository.createMonthAndInitializePayments(monthName)
            // Select the newly created month immediately
            val newMonth = MonthEntity(id = id.toInt(), monthName = monthName)
            _selectedMonth.value = newMonth
        }
    }

    fun deleteMonth(monthId: Int) {
        viewModelScope.launch {
            repository.deleteMonth(monthId)
            if (_selectedMonth.value?.id == monthId) {
                _selectedMonth.value = null
            }
        }
    }

    fun updatePaymentStatus(
        memberId: Int,
        monthId: Int,
        status: String,
        paymentMode: String,
        remarks: String,
        paymentDate: Long?
    ) {
        viewModelScope.launch {
            val existing = repository.getPaymentForMemberAndMonth(memberId, monthId)
            val receiptNo = existing?.receiptNumber?.ifBlank { null } 
                ?: "REC-${monthId}-${memberId}-${System.currentTimeMillis() % 100000}"
            
            val updatedPayment = PaymentEntity(
                id = existing?.id ?: 0,
                memberId = memberId,
                monthId = monthId,
                status = status,
                paymentMode = paymentMode,
                remarks = remarks,
                paymentDate = paymentDate ?: if (status == "PAID") System.currentTimeMillis() else null,
                receiptNumber = receiptNo
            )
            if (existing != null) {
                repository.updatePayment(updatedPayment)
            } else {
                repository.insertPayment(updatedPayment)
            }
        }
    }

    // --- Backup & Restore ---
    
    suspend fun exportDatabaseToJson(): String {
        val (members, months, payments) = repository.exportAllData()
        val root = JSONObject()
        
        val membersArray = JSONArray()
        for (m in members) {
            val obj = JSONObject()
            obj.put("id", m.id)
            obj.put("name", m.name)
            obj.put("mobile", m.mobile)
            obj.put("monthlyAmount", m.monthlyAmount)
            obj.put("serialNumber", m.serialNumber)
            membersArray.put(obj)
        }
        root.put("members", membersArray)

        val monthsArray = JSONArray()
        for (mo in months) {
            val obj = JSONObject()
            obj.put("id", mo.id)
            obj.put("monthName", mo.monthName)
            obj.put("createdAt", mo.createdAt)
            monthsArray.put(obj)
        }
        root.put("months", monthsArray)

        val paymentsArray = JSONArray()
        for (p in payments) {
            val obj = JSONObject()
            obj.put("id", p.id)
            obj.put("memberId", p.memberId)
            obj.put("monthId", p.monthId)
            obj.put("status", p.status)
            obj.put("paymentMode", p.paymentMode)
            obj.put("paymentDate", p.paymentDate ?: JSONObject.NULL)
            obj.put("remarks", p.remarks)
            obj.put("receiptNumber", p.receiptNumber)
            paymentsArray.put(obj)
        }
        root.put("payments", paymentsArray)

        return root.toString(2)
    }

    suspend fun restoreDatabaseFromJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            
            val membersList = mutableListOf<MemberEntity>()
            val membersArray = root.getJSONArray("members")
            for (i in 0 until membersArray.length()) {
                val obj = membersArray.getJSONObject(i)
                membersList.add(
                    MemberEntity(
                        id = obj.getInt("id"),
                        name = obj.getString("name"),
                        mobile = obj.getString("mobile"),
                        monthlyAmount = obj.getDouble("monthlyAmount"),
                        serialNumber = obj.optInt("serialNumber", 1)
                    )
                )
            }

            val monthsList = mutableListOf<MonthEntity>()
            val monthsArray = root.getJSONArray("months")
            for (i in 0 until monthsArray.length()) {
                val obj = monthsArray.getJSONObject(i)
                monthsList.add(
                    MonthEntity(
                        id = obj.getInt("id"),
                        monthName = obj.getString("monthName"),
                        createdAt = obj.getLong("createdAt")
                    )
                )
            }

            val paymentsList = mutableListOf<PaymentEntity>()
            val paymentsArray = root.getJSONArray("payments")
            for (i in 0 until paymentsArray.length()) {
                val obj = paymentsArray.getJSONObject(i)
                paymentsList.add(
                    PaymentEntity(
                        id = obj.getInt("id"),
                        memberId = obj.getInt("memberId"),
                        monthId = obj.getInt("monthId"),
                        status = obj.getString("status"),
                        paymentMode = obj.getString("paymentMode"),
                        paymentDate = if (obj.isNull("paymentDate")) null else obj.getLong("paymentDate"),
                        remarks = obj.optString("remarks", ""),
                        receiptNumber = obj.optString("receiptNumber", "")
                    )
                )
            }

            repository.restoreData(membersList, monthsList, paymentsList)
            // Re-trigger select on the latest month
            if (monthsList.isNotEmpty()) {
                _selectedMonth.value = monthsList.first()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Pre-seed standard 30 members if database is empty ---
    fun seedSampleDataIfEmpty() {
        viewModelScope.launch {
            val currentMembers = repository.getAllMembers()
            if (currentMembers.isEmpty()) {
                // Seed 30 members with standard names
                val sampleNames = listOf(
                    "Syed Ghaus Mohiuddin", "Syed Faizan Ali", "Mohammad Tajuddin", 
                    "Abdul Qadir", "Sajid Hussain", "Arshad Khan", "Raza Ahmed", 
                    "Zeeshan Qadri", "Irfan Ansari", "Farhan Malik", "Naeem Sheikh", 
                    "Tanveer Alam", "Sameer Siddiqui", "Riazuddin Ahmed", "Salim Raza", 
                    "Hussain Noori", "Imran Khan", "Waseem Akram", "Javed Miandad", 
                    "Zaheer Abbas", "Mohammad Azhar", "Sardar Patel", "Aslam Qadri", 
                    "Nawaz Sharif", "Sufyan Ahmed", "Mustafa Raza", "Gulam Mustafa", 
                    "Shariq Iqbal", "Yusuf Ansari", "Bilal Qadri"
                )
                
                // Initialize members
                sampleNames.forEachIndexed { index, name ->
                    val num = "98765432" + String.format("%02d", index)
                    val member = MemberEntity(
                        name = name,
                        mobile = num,
                        monthlyAmount = 1000.0,
                        serialNumber = index + 1
                    )
                    // Just inserts member. Payments are auto-seeded once a month is created
                    repository.addMemberAndInitializePayments(member)
                }

                // Auto create initial month
                createMonth("January 2026")
            }
        }
    }
}
