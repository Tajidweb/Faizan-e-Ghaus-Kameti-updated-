package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.MemberWithPayment
import com.example.data.model.MemberEntity
import com.example.data.model.MonthEntity
import com.example.data.model.PaymentEntity
import com.example.ui.viewmodel.CommitteeViewModel
import com.example.utils.DocumentExporter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab {
    Dashboard, Members, Reports, Settings
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: CommitteeViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // UI Tab navigation state
    var currentTab by remember { mutableStateOf(AppTab.Dashboard) }

    // Admin login states
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    // Fetch live streams
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val allMonths by viewModel.allMonths.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val unfilteredMembers by viewModel.membersWithPayments.collectAsState()
    val filteredMembers by viewModel.filteredMembersWithPayments.collectAsState()

    // Shared Merchant Config for QR code
    var merchantUpiId by remember { mutableStateOf("faizaneghaus@ybl") }
    var merchantName by remember { mutableStateOf("Faizan e Ghaus Kameti") }

    // Ensure we seed standard 30 members if database is empty
    remember {
        viewModel.seedSampleDataIfEmpty()
        true
    }

    // Adaptive viewport layout checks
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Left Navigation Rail for Tablets / Desktop Emulator
            if (isWideScreen) {
                NavigationRail(
                    containerColor = Color(0xFFF3EDF7),
                    modifier = Modifier.testTag("nav_rail")
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Logo",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(36.dp))
                    
                    NavigationRailItem(
                        selected = currentTab == AppTab.Dashboard,
                        onClick = { currentTab = AppTab.Dashboard },
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                        label = { Text("Dashboard") },
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("rail_tab_dashboard")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.Members,
                        onClick = { currentTab = AppTab.Members },
                        icon = { Icon(Icons.Default.Payments, "Members Directory") },
                        label = { Text("Members") },
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("rail_tab_members")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.Reports,
                        onClick = { currentTab = AppTab.Reports },
                        icon = { Icon(Icons.Default.Print, "Reports") },
                        label = { Text("Reports") },
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("rail_tab_reports")
                    )
                    NavigationRailItem(
                        selected = currentTab == AppTab.Settings,
                        onClick = { currentTab = AppTab.Settings },
                        icon = { Icon(Icons.Default.AdminPanelSettings, "Settings & Admin") },
                        label = { Text("Admin") },
                        colors = androidx.compose.material3.NavigationRailItemDefaults.colors(
                            selectedIconColor = Color(0xFF1D192B),
                            selectedTextColor = Color(0xFF1D192B),
                            indicatorColor = Color(0xFFE8DEF8),
                            unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("rail_tab_settings")
                    )
                }
                VerticalDivider(thickness = 1.dp, color = Color(0xFFCAC4D0).copy(alpha = 0.5f))
            }

            // Main Application Area
            Scaffold(
                bottomBar = {
                    if (!isWideScreen) {
                        NavigationBar(
                            containerColor = Color(0xFFF3EDF7),
                            modifier = Modifier.testTag("bottom_nav")
                        ) {
                            NavigationBarItem(
                                selected = currentTab == AppTab.Dashboard,
                                onClick = { currentTab = AppTab.Dashboard },
                                icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                                label = { Text("Dashboard", fontSize = 11.sp) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D192B),
                                    selectedTextColor = Color(0xFF1D192B),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_dashboard")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.Members,
                                onClick = { currentTab = AppTab.Members },
                                icon = { Icon(Icons.Default.Payments, "Members Directory") },
                                label = { Text("Kameti", fontSize = 11.sp) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D192B),
                                    selectedTextColor = Color(0xFF1D192B),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_members")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.Reports,
                                onClick = { currentTab = AppTab.Reports },
                                icon = { Icon(Icons.Default.Print, "Reports") },
                                label = { Text("Reports", fontSize = 11.sp) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D192B),
                                    selectedTextColor = Color(0xFF1D192B),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_reports")
                            )
                            NavigationBarItem(
                                selected = currentTab == AppTab.Settings,
                                onClick = { currentTab = AppTab.Settings },
                                icon = { Icon(Icons.Default.AdminPanelSettings, "Admin") },
                                label = { Text("Admin", fontSize = 11.sp) },
                                colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1D192B),
                                    selectedTextColor = Color(0xFF1D192B),
                                    indicatorColor = Color(0xFFE8DEF8),
                                    unselectedIconColor = Color(0xFF1D192B).copy(alpha = 0.6f),
                                    unselectedTextColor = Color(0xFF1D192B).copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("tab_settings")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Styled Spiritual Community Header
                    CommunityHeader(
                        selectedMonth = selectedMonth,
                        allMonths = allMonths,
                        onMonthSelected = { viewModel.selectMonth(it) }
                    )

                    // Tab View Switcher
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentTab) {
                            AppTab.Dashboard -> DashboardTab(
                                viewModel = viewModel,
                                members = unfilteredMembers,
                                totalMembersCount = allMembers.size,
                                onNavigateToMembers = { currentTab = AppTab.Members }
                            )
                            AppTab.Members -> MembersTab(
                                viewModel = viewModel,
                                members = filteredMembers,
                                selectedMonthName = selectedMonth?.monthName ?: "Active Month",
                                isAdmin = isAdminLoggedIn,
                                merchantUpiId = merchantUpiId,
                                merchantName = merchantName
                            )
                            AppTab.Reports -> ReportsTab(
                                viewModel = viewModel,
                                members = filteredMembers,
                                selectedMonthName = selectedMonth?.monthName ?: "Active Month"
                            )
                            AppTab.Settings -> SettingsTab(
                                viewModel = viewModel,
                                isAdmin = isAdminLoggedIn,
                                merchantUpiId = merchantUpiId,
                                merchantName = merchantName,
                                onMerchantUpiChanged = { merchantUpiId = it },
                                onMerchantNameChanged = { merchantName = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- Dynamic Header ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityHeader(
    selectedMonth: MonthEntity?,
    allMonths: List<MonthEntity>,
    onMonthSelected: (MonthEntity) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Faizan e Ghaus",
                    color = Color(0xFF1C1B1F),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                // Month Picker Dropdown integrated into the badge Row
                if (allMonths.isNotEmpty()) {
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable { expanded = true }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge 1: Kameti 2026
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(Color(0xFFEADDFF))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Kameti 2026",
                                    color = Color(0xFF6750A4),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge 2: Cycle Name
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "• ${selectedMonth?.monthName ?: "Select Month"}",
                                    color = Color(0xFF49454F),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Cycle",
                                    tint = Color(0xFF49454F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        androidx.compose.material3.DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            allMonths.forEach { month ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = month.monthName,
                                            fontWeight = if (selectedMonth?.id == month.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selectedMonth?.id == month.id) Color(0xFF6750A4) else MaterialTheme.colorScheme.onSurface
                                        ) 
                                    },
                                    onClick = {
                                        onMonthSelected(month)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Initials Avatar: AD in a circle (bg #D0BCFF, text #21005D)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD0BCFF))
                    .border(BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.3f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AD",
                    color = Color(0xFF21005D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// Helper to get name initials for Live Status list
fun getInitials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex())
    return if (parts.size >= 2) {
        (parts[0].take(1) + parts[1].take(1)).uppercase()
    } else if (parts.isNotEmpty()) {
        parts[0].take(2).uppercase()
    } else {
        "KM"
    }
}

// ==========================================
// TABS 1: DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTab(
    viewModel: CommitteeViewModel,
    members: List<MemberWithPayment>,
    totalMembersCount: Int,
    onNavigateToMembers: () -> Unit
) {
    // Collect all members and months for dynamic payout calculation
    val allMembers by viewModel.allMembers.collectAsState()
    val allMonths by viewModel.allMonths.collectAsState()

    val sortedMonths = allMonths.sortedBy { it.createdAt }
    val selectedMonthObj = viewModel.selectedMonth.collectAsState().value
    val sortedMembers = allMembers.sortedBy { it.serialNumber }

    val currentMonthIndex = if (selectedMonthObj != null) {
        sortedMonths.indexOfFirst { it.id == selectedMonthObj.id }
    } else -1

    // Determine current recipient
    val currentRecipient = if (currentMonthIndex != -1 && sortedMembers.isNotEmpty()) {
        val memberIdx = currentMonthIndex % sortedMembers.size
        sortedMembers[memberIdx]
    } else if (sortedMembers.isNotEmpty()) {
        sortedMembers.first()
    } else null

    // Determine next month and next recipient
    val nextMonthObj = if (currentMonthIndex != -1 && currentMonthIndex + 1 < sortedMonths.size) {
        sortedMonths[currentMonthIndex + 1]
    } else null

    val nextRecipient = if (sortedMembers.isNotEmpty()) {
        val nextMonthIndex = if (currentMonthIndex != -1) currentMonthIndex + 1 else 1
        val memberIdx = nextMonthIndex % sortedMembers.size
        sortedMembers[memberIdx]
    } else null

    // Calculators
    val totalPaidCount = members.count { it.status == "PAID" }
    val totalPendingCount = members.size - totalPaidCount
    
    val totalMonthlyCollection = members.filter { it.status == "PAID" }.sumOf { it.monthlyAmount }
    val totalRemainingAmount = members.filter { it.status != "PAID" }.sumOf { it.monthlyAmount }
    val totalTargetCollection = members.sumOf { it.monthlyAmount }

    val collectionPercentage = if (totalTargetCollection > 0) {
        (totalMonthlyCollection / totalTargetCollection * 100).toFloat()
    } else 0f

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Bento Grid Dashboard Layout (2 columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 1: Total Collection
                    BentoMetricCard(
                        title = "Total Collection",
                        value = "₹${String.format("%,.0f", totalMonthlyCollection)}",
                        subtitle = "Target: ₹${String.format("%,.0f", totalTargetCollection)}",
                        containerColor = Color(0xFFE8DEF8), // Soft lavender
                        textColor = Color(0xFF21005D),      // Dark Violet
                        modifier = Modifier.weight(1f)
                    )
                    // Card 2: Members
                    BentoMetricCard(
                        title = "Members",
                        value = "$totalMembersCount",
                        subtitle = "Total Directory",
                        containerColor = Color(0xFFE7E0EC), // Neutral soft grey-purple
                        textColor = Color(0xFF1C1B1F),      // Near Black
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Card 3: Paid
                    BentoMetricCard(
                        title = "Paid",
                        value = "$totalPaidCount",
                        subtitle = "Active Cycle",
                        containerColor = Color(0xFFC2E7FF), // Soft Blue
                        textColor = Color(0xFF001D35),      // Dark Blue Text
                        showDot = true,
                        dotColor = Color(0xFF006493),
                        modifier = Modifier.weight(1f)
                    )
                    // Card 4: Pending
                    BentoMetricCard(
                        title = "Pending",
                        value = String.format("%02d", totalPendingCount),
                        subtitle = "Requires Followup",
                        containerColor = Color(0xFFF2B8B5), // Soft pastel red
                        textColor = Color(0xFF601410),      // Dark red text
                        showDot = true,
                        dotColor = Color(0xFFB3261E),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Card 5: Remaining Collection Target (Wide Card)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
                    border = BorderStroke(1.dp, Color(0xFFCAC4D0).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "REMAINING COLLECTION TARGET",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57F17).copy(alpha = 0.8f),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "₹${String.format("%,.0f", totalRemainingAmount)} Pending",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFF57F17)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Pending Notification Alert",
                            tint = Color(0xFFF57F17).copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Monthly Progress Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "Monthly Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1B1F)
                        )
                        Text(
                            text = "${String.format("%.1f", collectionPercentage)}%",
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = Color(0xFF6750A4)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Styled custom Bento Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(Color(0xFFE7E0EC))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(fraction = collectionPercentage / 100f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(Color(0xFF6750A4))
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Collected: ₹${String.format("%,.0f", totalMonthlyCollection)} out of ₹${String.format("%,.0f", totalTargetCollection)}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF49454F)
                    )
                }
            }
        }

        // Committee Payout & Draws Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Payout Draw Progress",
                            tint = Color(0xFF6750A4),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Committee Payout & Draws",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1B1F)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    // Two columns split layout for Current and Next Payout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Current Recipient Column
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFEADDFF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CURRENT RECIPIENT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = currentRecipient?.name ?: "No Members",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Serial No: #${currentRecipient?.serialNumber ?: 1}",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Payout: ₹${String.format("%,.0f", totalTargetCollection)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                            Text(
                                text = "Collected: ₹${String.format("%,.0f", totalMonthlyCollection)}",
                                fontSize = 10.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Vertical Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(90.dp)
                                .background(Color(0xFFCAC4D0).copy(alpha = 0.5f))
                        )

                        // Next Recipient Column
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE7E0EC))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "NEXT RECIPIENT",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF49454F)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = nextRecipient?.name ?: "No Members",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1C1B1F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Serial No: #${nextRecipient?.serialNumber ?: 1}",
                                fontSize = 11.sp,
                                color = Color(0xFF49454F)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Next Month: ${nextMonthObj?.monthName ?: "Upcoming Cycle"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF49454F),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Expected: ₹${String.format("%,.0f", totalTargetCollection)}",
                                fontSize = 10.sp,
                                color = Color(0xFF49454F).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Recent Members Live Status Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFCAC4D0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1C1B1F)
                        )
                        TextButton(
                            onClick = onNavigateToMembers,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "View All",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6750A4)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Show a list of top 3 members as live previews
                    val previewMembers = members.take(3)
                    if (previewMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No members available",
                                fontSize = 12.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            previewMembers.forEach { m ->
                                val isPaid = m.status == "PAID"
                                val avatarBg = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                val avatarText = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
                                val badgeBg = if (isPaid) Color(0xFF4CAF50) else Color(0xFFEF5350)
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFF7F2FA))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Initials Square Avatar with smooth corner rounding (Bento style)
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(avatarBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = getInitials(m.name),
                                                color = avatarText,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = m.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1C1B1F),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = if (isPaid) "Paid via ${m.paymentMode ?: "UPI"}" else "Contact: ${m.mobile}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF49454F)
                                            )
                                        }
                                    }
                                    
                                    // Status Badge on far right
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isPaid) "PAID" else "PENDING",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Special custom Bento card for metric cells
@Composable
fun BentoMetricCard(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    textColor: Color,
    showDot: Boolean = false,
    dotColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor.copy(alpha = 0.8f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    if (showDot) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(dotColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// ==========================================
// TABS 2: MEMBERS LEDGER DIRECTORY
// ==========================================
@Composable
fun MembersTab(
    viewModel: CommitteeViewModel,
    members: List<MemberWithPayment>,
    selectedMonthName: String,
    isAdmin: Boolean,
    merchantUpiId: String,
    merchantName: String
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val modeFilter by viewModel.paymentModeFilter.collectAsState()

    // Dialog sheets states
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var paymentEditorTarget by remember { mutableStateOf<MemberWithPayment?>(null) }
    var qrCodeTarget by remember { mutableStateOf<MemberWithPayment?>(null) }
    var editMemberTarget by remember { mutableStateOf<MemberWithPayment?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Live Search Bar (Bento styled rounded-full box)
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                    .testTag("search_members_input"),
                placeholder = { Text("Search by name, phone or serial number...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, "Search Icon", tint = Color(0xFF49454F)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(Icons.Default.Close, "Clear Search", tint = Color(0xFF49454F))
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filtering Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                
                // Status Filter Chip (All, Paid, Pending)
                val statusChips = listOf("ALL", "PAID", "PENDING")
                statusChips.forEach { sc ->
                    val isSelected = statusFilter == sc
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFFEADDFF) else Color(0xFFE7E0EC))
                            .clickable { viewModel.statusFilter.value = sc }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sc,
                            color = if (isSelected) Color(0xFF21005D) else Color(0xFF1C1B1F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Member list directory
            if (members.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = "Empty Data",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "No members match the criteria",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(members) { member ->
                        MemberCardRow(
                            member = member,
                            selectedMonthName = selectedMonthName,
                            isAdmin = isAdmin,
                            onEditPayment = { paymentEditorTarget = member },
                            onEditMember = { editMemberTarget = member },
                            onShowQrCode = { qrCodeTarget = member },
                            onPrintReceipt = { DocumentExporter.printReceipt(context, member, selectedMonthName) }
                        )
                    }
                }
            }
        }

        // Add Member FAB for Admin (Bento style)
        if (isAdmin) {
            FloatingActionButton(
                onClick = { showAddMemberDialog = true },
                containerColor = Color(0xFFD0BCFF),
                contentColor = Color(0xFF21005D),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_member_fab")
            ) {
                Icon(Icons.Default.Add, "Add Member")
            }
        }
    }

    // --- DIALOG SHEETS IMPLEMENTATIONS ---

    // 1. Add Member Dialog
    if (showAddMemberDialog) {
        AddOrEditMemberDialog(
            memberToEdit = null,
            onDismiss = { showAddMemberDialog = false },
            onConfirm = { name, mobile, monthlyAmount, serialNo ->
                viewModel.addMember(name, mobile, monthlyAmount, serialNo)
                showAddMemberDialog = false
            }
        )
    }

    // 2. Edit Member Dialog
    if (editMemberTarget != null) {
        val target = editMemberTarget!!
        AddOrEditMemberDialog(
            memberToEdit = target,
            onDismiss = { editMemberTarget = null },
            onConfirm = { name, mobile, monthlyAmount, serialNo ->
                viewModel.updateMember(
                    MemberEntity(
                        id = target.memberId,
                        name = name,
                        mobile = mobile,
                        monthlyAmount = monthlyAmount,
                        serialNumber = serialNo
                    )
                )
                editMemberTarget = null
            },
            onDelete = {
                viewModel.deleteMember(
                    MemberEntity(
                        id = target.memberId,
                        name = target.name,
                        mobile = target.mobile,
                        monthlyAmount = target.monthlyAmount,
                        serialNumber = target.serialNumber
                    )
                )
                editMemberTarget = null
            }
        )
    }

    // 3. Mark/Edit Payment dialog
    if (paymentEditorTarget != null) {
        val target = paymentEditorTarget!!
        PaymentStatusEditorDialog(
            member = target,
            onDismiss = { paymentEditorTarget = null },
            onConfirm = { status, mode, remarks ->
                val monthId = viewModel.selectedMonth.value?.id
                if (monthId != null) {
                    viewModel.updatePaymentStatus(
                        memberId = target.memberId,
                        monthId = monthId,
                        status = status,
                        paymentMode = mode,
                        remarks = remarks,
                        paymentDate = if (status == "PAID") System.currentTimeMillis() else null
                    )
                }
                paymentEditorTarget = null
            }
        )
    }

    // 4. UPI Payment Dynamic QR Code popup
    if (qrCodeTarget != null) {
        val target = qrCodeTarget!!
        PaymentQrCodeDialog(
            member = target,
            selectedMonthName = selectedMonthName,
            merchantUpiId = merchantUpiId,
            merchantName = merchantName,
            onDismiss = { qrCodeTarget = null }
        )
    }
}

// --- High-Craft Member ledger row ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemberCardRow(
    member: MemberWithPayment,
    selectedMonthName: String,
    isAdmin: Boolean,
    onEditPayment: () -> Unit,
    onEditMember: () -> Unit,
    onShowQrCode: () -> Unit,
    onPrintReceipt: () -> Unit
) {
    val context = LocalContext.current
    val isPaid = member.status == "PAID"
    val statusColor = if (isPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
    val statusBg = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("member_card_${member.memberId}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row (Serial, Name, Status Pill)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Styled Serial index
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${member.serialNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = member.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Phone: ${member.mobile}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Payment Status Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = member.status ?: "PENDING",
                        color = statusColor,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Contribution Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monthly Contribution", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${member.monthlyAmount}", fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                }

                if (isPaid) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Paid via: ${member.paymentMode ?: "Cash"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        if (member.paymentDate != null) {
                            val paidDateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(member.paymentDate))
                            Text("Date: $paidDateStr", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Text("Payment Pending", fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Medium)
                }
            }

            if (!member.remarks.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Remarks: ${member.remarks}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            // Ledger Actions Panel
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // If Paid -> Print Receipt
                if (isPaid) {
                    OutlinedButton(
                        onClick = onPrintReceipt,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Print Receipt", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // If Pending -> Show Payment QR Code & Whatsapp Reminder
                    Button(
                        onClick = onShowQrCode,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.QrCode, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSecondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("UPI QR Code", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
                    }

                    // WhatsApp reminder integration
                    OutlinedButton(
                        onClick = {
                            val rawMsg = "Assalamu Alaikum. This is a reminder regarding your Faizan e Ghaus Kameti contribution of ₹${member.monthlyAmount} for the month of $selectedMonthName. Please pay via Cash or UPI as soon as possible. JazaKallah!"
                            val uri = Uri.parse("https://api.whatsapp.com/send?phone=91${member.mobile}&text=${Uri.encode(rawMsg)}")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Notifications, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reminder", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Shared Payment status text copy/share
                OutlinedIconButton(
                    onClick = {
                        val shareText = "Kameti Ledger Status [Faizan e Ghaus Kameti 2026]\n" +
                                "Member Name: ${member.name}\n" +
                                "Month: $selectedMonthName\n" +
                                "Contribution: ₹${member.monthlyAmount}\n" +
                                "Status: ${member.status ?: "PENDING"}\n" +
                                if (isPaid) "Paid via: ${member.paymentMode} Receipt: ${member.receiptNumber}" else "Please complete payment."
                        
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Status"))
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Share, "Share Status", modifier = Modifier.size(14.dp))
                }

                // Admin-specific modifications
                if (isAdmin) {
                    Button(
                        onClick = onEditPayment,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedIconButton(
                        onClick = onEditMember,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, "Edit Member Profile", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

// Minimal placeholder since standard outlined icon button does not exist out of the box in some layouts
@Composable
fun OutlinedIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

// ==========================================
// TABS 3: REPORTS TAB
// ==========================================
@Composable
fun ReportsTab(
    viewModel: CommitteeViewModel,
    members: List<MemberWithPayment>,
    selectedMonthName: String
) {
    val context = LocalContext.current

    val totalMembers = members.size
    val paidMembers = members.filter { it.status == "PAID" }
    val paidCount = paidMembers.size
    val pendingCount = totalMembers - paidCount
    
    val totalCollected = paidMembers.sumOf { it.monthlyAmount }
    val totalRemaining = members.filter { it.status != "PAID" }.sumOf { it.monthlyAmount }

    // Grouping by payment modes
    val paymentModesCount = paidMembers.groupBy { it.paymentMode ?: "Cash" }
        .mapValues { it.value.size }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Month Audit Reports - $selectedMonthName",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Export Actions Panel Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Executive Actions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { DocumentExporter.printReport(context, members, selectedMonthName) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print PDF Report", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { DocumentExporter.exportToCsvAndShare(context, members, selectedMonthName) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export Excel", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Collection stats progress metrics breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Ledger Summary Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ledger Target", fontSize = 13.sp)
                        Text("₹${String.format("%,.0f", totalCollected + totalRemaining)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Collected Total (Paid)", fontSize = 13.sp, color = Color(0xFF2E7D32))
                        Text("₹${String.format("%,.0f", totalCollected)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Outstanding Deficit (Pending)", fontSize = 13.sp, color = Color(0xFFC62828))
                        Text("₹${String.format("%,.0f", totalRemaining)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                    }
                }
            }
        }

        // Custom Visual breakdown representing Payment Modes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Payment Modes Audit", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    if (paidCount == 0) {
                        Text("No completed payments yet in this month.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        paymentModesCount.forEach { (mode, count) ->
                            val perc = (count.toFloat() / paidCount.toFloat())
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(mode, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("$count members (${String.format("%.0f", perc * 100)}%)", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { perc },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.secondary,
                                    trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TABS 4: SETTINGS & ADMIN MANAGEMENT TAB
// ==========================================
@Composable
fun SettingsTab(
    viewModel: CommitteeViewModel,
    isAdmin: Boolean,
    merchantUpiId: String,
    merchantName: String,
    onMerchantUpiChanged: (String) -> Unit,
    onMerchantNameChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var loginPassword by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }

    var newMonthName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // State for Backup Restore JSON string
    var showBackupRestoreDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ADMIN LOGIN / AUTH HEADER PANEL
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (!isAdmin) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (isAdmin) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Admin Status: AUTHORIZED", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text("You possess full write and deletion clearance.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                            }
                            Button(
                                onClick = { viewModel.logoutAdmin() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "Admin Authorization Login",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Unlock features to add/delete members, register monthly collection targets, and override ledger payment metrics.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                showPasswordError = false
                            },
                            label = { Text("Enter Admin Password") },
                            placeholder = { Text("Default: admin123") },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_password_field"),
                            isError = showPasswordError
                        )

                        if (showPasswordError) {
                            Text("Incorrect password, please try again.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (viewModel.loginAdmin(loginPassword)) {
                                    loginPassword = ""
                                    showPasswordError = false
                                } else {
                                    showPasswordError = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_login_submit"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gain Clearance")
                        }
                    }
                }
            }
        }

        // DYNAMIC MERCHANT UPI QR CONFIGURATION CARD
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("UPI Merchant Recipient Config", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Payments generated will dynamically use these recipient merchant values.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = merchantUpiId,
                        onValueChange = onMerchantUpiChanged,
                        label = { Text("Merchant UPI ID / Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAdmin,
                        placeholder = { Text("e.g. name@upi") }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = merchantName,
                        onValueChange = onMerchantNameChanged,
                        label = { Text("Merchant Registered Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAdmin,
                        placeholder = { Text("e.g. Faizan e Ghaus Committee") }
                    )
                }
            }
        }

        // MONTHLY LEDGER LIFECYCLE MANAGER
        if (isAdmin) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add New Ledger Month Cycle", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("This generates a fully synchronized ledger, mapping pending items to all active members.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = newMonthName,
                            onValueChange = { newMonthName = it },
                            label = { Text("Ledger Month Label") },
                            placeholder = { Text("e.g. February 2026") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("new_month_name_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                if (newMonthName.isNotBlank()) {
                                    viewModel.createMonth(newMonthName)
                                    Toast.makeText(context, "Created cycle $newMonthName", Toast.LENGTH_SHORT).show()
                                    newMonthName = ""
                                } else {
                                    Toast.makeText(context, "Please write a valid month label", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_month_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Ledger Month Cycle")
                        }
                    }
                }
            }

            // BACKUP AND DATABASE RESTORATION UTILITIES
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Database Infrastructure & JSON Backups", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Export or recover the full committee databases as high-fidelity secure JSON structures.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val json = viewModel.exportAllDataAsJsonString()
                                        // Copy to clipboard
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("committee_backup", json)
                                        clipboard.setPrimaryClip(clip)
                                        
                                        // Open share dialog
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_SUBJECT, "Faizan_Ghaus_Backup.json")
                                            putExtra(Intent.EXTRA_TEXT, json)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share JSON Backup Code"))
                                        
                                        Toast.makeText(context, "Full DB copied to Clipboard & shared!", Toast.LENGTH_LONG).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Backup, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup DB", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = { showBackupRestoreDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore DB", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Backup Restore Dialogue ---
    if (showBackupRestoreDialog) {
        var restoreJsonString by remember { mutableStateOf("") }
        var isProcessingRestore by remember { mutableStateOf(false) }

        Dialog(onDismissRequest = { showBackupRestoreDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Paste Restore JSON Database", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Warning: Restoring will overwrite all current database directories.", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = restoreJsonString,
                        onValueChange = { restoreJsonString = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 200.dp),
                        label = { Text("Paste JSON text content") },
                        placeholder = { Text("e.g. {\"members\":[...]}") }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showBackupRestoreDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                isProcessingRestore = true
                                coroutineScope.launch {
                                    val result = viewModel.restoreDatabaseFromJson(restoreJsonString)
                                    isProcessingRestore = false
                                    if (result) {
                                        Toast.makeText(context, "Database Restored Successfully!", Toast.LENGTH_SHORT).show()
                                        showBackupRestoreDialog = false
                                    } else {
                                        Toast.makeText(context, "Invalid JSON data, restore aborted", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            enabled = restoreJsonString.isNotBlank() && !isProcessingRestore,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Confirm Restore")
                        }
                    }
                }
            }
        }
    }
}

// Extension to bridge viewModel helper for export
suspend fun CommitteeViewModel.exportAllDataAsJsonString(): String {
    return this.exportDatabaseToJson()
}

// ==========================================
// MODAL FORMS & DETAILED DIALOG SHEETS
// ==========================================

// 1. ADD / EDIT MEMBER PROFILE
@Composable
fun AddOrEditMemberDialog(
    memberToEdit: MemberWithPayment?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, mobile: String, monthlyAmount: Double, serialNo: Int) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(memberToEdit?.name ?: "") }
    var mobile by remember { mutableStateOf(memberToEdit?.mobile ?: "") }
    var amountStr by remember { mutableStateOf(memberToEdit?.monthlyAmount?.toString() ?: "1000.0") }
    var serialNoStr by remember { mutableStateOf(memberToEdit?.serialNumber?.toString() ?: "1") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScrollEnabled()
            ) {
                Text(
                    text = if (memberToEdit == null) "Add Committee Member Profile" else "Edit Member Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Phone Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_mobile_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monthly Contribution (₹)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_amount_input")
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = serialNoStr,
                    onValueChange = { serialNoStr = it },
                    label = { Text("Serial Number Order") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("member_serial_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (memberToEdit != null && onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, "Delete Profile", tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 1000.0
                                val serial = serialNoStr.toIntOrNull() ?: 1
                                if (name.isNotBlank() && mobile.isNotBlank()) {
                                    onConfirm(name, mobile, amount, serial)
                                }
                            },
                            enabled = name.isNotBlank() && mobile.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("submit_member_button")
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

// 2. MARK PAYMENT DIALOG (ADMIN REGISTER)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentStatusEditorDialog(
    member: MemberWithPayment,
    onDismiss: () -> Unit,
    onConfirm: (status: String, mode: String, remarks: String) -> Unit
) {
    var status by remember { mutableStateOf(member.status ?: "PENDING") }
    var paymentMode by remember { mutableStateOf(member.paymentMode ?: "Cash") }
    var remarks by remember { mutableStateOf(member.remarks ?: "") }

    var expandedMode by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Update Payment: ${member.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Status Selector Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Status: $status",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = status == "PAID",
                        onCheckedChange = { status = if (it) "PAID" else "PENDING" },
                        modifier = Modifier.testTag("payment_status_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Mode Dropdown
                if (status == "PAID") {
                    val modes = listOf("Cash", "PhonePe", "Google Pay", "UPI", "Bank Transfer")
                    
                    Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expandedMode,
                            onExpandedChange = { expandedMode = it }
                        ) {
                            OutlinedTextField(
                                value = paymentMode,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("payment_mode_dropdown"),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMode,
                                onDismissRequest = { expandedMode = false }
                            ) {
                                modes.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m) },
                                        onClick = {
                                            paymentMode = m
                                            expandedMode = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Remarks Field
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (Optional)") },
                    placeholder = { Text("e.g. advance payment, delayed, etc.") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_remarks_input")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(status, if (status == "PAID") paymentMode else "None", remarks) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("payment_save_button")
                    ) {
                        Text("Save Status")
                    }
                }
            }
        }
    }
}

// 3. SECURE INTERACTIVE UPI PAYMENT QR CODE POPUP
@Composable
fun PaymentQrCodeDialog(
    member: MemberWithPayment,
    selectedMonthName: String,
    merchantUpiId: String,
    merchantName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Generate QR code bitmap dynamically
    val qrBitmap = remember {
        val tn = "Kameti $selectedMonthName - ${member.name}"
        DocumentExporter.generateUpQrCode(
            payeeAddress = merchantUpiId,
            payeeName = merchantName,
            amount = member.monthlyAmount,
            remarks = tn
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with custom layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Interactive UPI QR Code",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close Dialog")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Scan with GPay, PhonePe, or BHIM to pay.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // The scannable dynamic QR image bitmap
                if (qrBitmap != null) {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "UPI Payment QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(230.dp)
                            .background(Color.LightGray, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Failed to generate QR", color = Color.Red, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bill details Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payee Name", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(merchantName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("UPI Address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(merchantUpiId, fontWeight = FontWeight.Medium, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Billing Name", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(member.name, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Month Cycle", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedMonthName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Amount Due", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("₹${member.monthlyAmount}", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Share link option
                OutlinedButton(
                    onClick = {
                        val tn = "Kameti $selectedMonthName - ${member.name}"
                        val upiUrl = "upi://pay?pa=$merchantUpiId&pn=${merchantName.replace(" ", "%20")}&am=${member.monthlyAmount}&tn=${tn.replace(" ", "%20")}&cu=INR"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "UPI payment prefilled receipt link: $upiUrl")
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Payment Link"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Prefilled Payment link")
                }
            }
        }
    }
}

// Custom modifier to handle tiny scrolls gracefully inside forms
@Composable
fun Modifier.verticalScrollEnabled(): Modifier {
    return this.verticalScroll(rememberScrollState())
}
