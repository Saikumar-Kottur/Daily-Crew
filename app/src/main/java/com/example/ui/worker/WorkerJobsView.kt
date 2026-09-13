package com.example.ui.worker

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobCategory
import com.example.data.model.JobPosting
import com.example.data.model.WorkerJobTab
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*

@Composable
fun WorkerJobsView(
    jobs: List<JobPosting>,
    workerProfile: WorkerProfile,
    activeTab: WorkerJobTab = WorkerJobTab.NEARBY,
    onTabSelect: (WorkerJobTab) -> Unit = {},
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: JobCategory,
    onSelectCategory: (JobCategory) -> Unit,
    selectedZone: String,
    onSelectZone: (String) -> Unit,
    filterRadiusKm: Float,
    onRadiusChange: (Float) -> Unit,
    minWageFilter: Float = 0f,
    onMinWageChange: (Float) -> Unit = {},
    filterEmergencyOnly: Boolean,
    onToggleEmergency: () -> Unit,
    onToggleStandby: () -> Unit,
    onOpenSuretyDialog: () -> Unit = {},
    onJobClick: (JobPosting) -> Unit,
    onApplyJob: (String) -> Unit,
    onToggleSaveJob: (String) -> Unit = {},
    onWithdrawJob: (String) -> Unit = {}
) {
    var showFilterSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("worker_jobs_screen")
    ) {
        // Top Bar: Standby Switch + Surety Escrow Quick Status
        Surface(
            color = BrandSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                // Surety & Standby Status Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Surety Escrow Quick Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandAmberDim.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onOpenSuretyDialog() }
                            .testTag("surety_status_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Surety: ₹${workerProfile.suretyEscrowBalance.toInt()}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (workerProfile.lockedSuretyAmount > 0) "₹${workerProfile.lockedSuretyAmount.toInt()} locked" else "100% Refundable",
                                    color = BrandAmber,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Standby Live Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (workerProfile.isAvailableNow) BrandPrimaryGreenDim else BrandCardDark,
                        border = BorderStroke(1.dp, if (workerProfile.isAvailableNow) BrandPrimaryGreen else BrandCardBorderDark),
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleStandby() }
                            .testTag("standby_mode_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (workerProfile.isAvailableNow) BrandPrimaryGreen else Color.Gray)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (workerProfile.isAvailableNow) "Standby: LIVE" else "Standby: OFF",
                                        color = if (workerProfile.isAvailableNow) BrandPrimaryGreen else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "30-min shifts",
                                        color = TextSecondaryDark,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            Switch(
                                checked = workerProfile.isAvailableNow,
                                onCheckedChange = { onToggleStandby() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = BrandPrimaryGreen,
                                    checkedTrackColor = BrandPrimaryGreenDim
                                ),
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 6 Worker Dashboard Tabs (Nearby, Recommended AI, Applied, Accepted, Completed, Saved)
                ScrollableTabRow(
                    selectedTabIndex = activeTab.ordinal,
                    edgePadding = 0.dp,
                    containerColor = BrandSurfaceDark,
                    contentColor = BrandPrimaryGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab.ordinal]),
                            color = BrandPrimaryGreen,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    WorkerJobTab.values().forEach { tab ->
                        val isSelected = tab == activeTab
                        Tab(
                            selected = isSelected,
                            onClick = { onTabSelect(tab) },
                            text = {
                                Text(
                                    text = tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) BrandPrimaryGreen else Color.White.copy(alpha = 0.7f)
                                )
                            },
                            modifier = Modifier.testTag("worker_tab_${tab.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Input + Quick Filter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                "Search shifts, catering, retail...",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = TextSecondaryDark)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = BrandCardDark,
                            unfocusedContainerColor = BrandCardDark,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("job_search_input")
                    )

                    IconButton(
                        onClick = { showFilterSheet = !showFilterSheet },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (showFilterSheet || minWageFilter > 0 || filterEmergencyOnly) BrandPrimaryGreenDim else BrandCardDark)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Filters",
                            tint = if (showFilterSheet || minWageFilter > 0 || filterEmergencyOnly) BrandPrimaryGreen else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Expandable Filter Section (Location, Salary, Distance, Category)
                if (showFilterSheet) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Location / Zone Filter
                            Text(
                                text = "LOCATION / PILOT ZONE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("All", "Uppal", "Habsiguda", "Tarnaka", "Nacharam").forEach { zone ->
                                    val isSelected = selectedZone.equals(zone, ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onSelectZone(zone) },
                                        label = { Text(zone, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Min Salary Filter
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MIN WAGE: ${if (minWageFilter > 0) "₹${minWageFilter.toInt()}+" else "Any"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "DISTANCE: ${filterRadiusKm.toInt()} km",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSecondaryDark,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(0f to "All", 600f to "₹600+", 800f to "₹800+", 1000f to "₹1000+").forEach { (wage, label) ->
                                    val isSelected = minWageFilter == wage
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onMinWageChange(wage) },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Distance Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(2f, 5f, 10f, 25f).forEach { r ->
                                    val isSelected = filterRadiusKm == r
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { onRadiusChange(r) },
                                        label = { Text("${r.toInt()} km", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Categories Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    JobCategory.values().forEach { category ->
                        val isSelected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BrandPrimaryGreen else BrandCardDark,
                            border = if (!isSelected) BorderStroke(1.dp, BrandCardBorderDark) else null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectCategory(category) }
                                .testTag("category_filter_${category.name.lowercase()}")
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Job List or Empty State
        if (jobs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = when (activeTab) {
                            WorkerJobTab.SAVED -> Icons.Default.BookmarkBorder
                            WorkerJobTab.APPLIED -> Icons.Default.AssignmentLate
                            WorkerJobTab.ACCEPTED -> Icons.Default.DoneAll
                            WorkerJobTab.COMPLETED -> Icons.Default.CheckCircle
                            else -> Icons.Default.WorkOff
                        },
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (activeTab) {
                            WorkerJobTab.SAVED -> "No bookmarked shifts yet"
                            WorkerJobTab.APPLIED -> "No active applications"
                            WorkerJobTab.ACCEPTED -> "No accepted shifts currently"
                            WorkerJobTab.COMPLETED -> "No completed shifts yet"
                            WorkerJobTab.RECOMMENDED -> "No AI recommendations match current filters"
                            WorkerJobTab.NEARBY -> "No shifts found nearby"
                        },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Browse the Nearby tab and bookmark or apply for shifts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            onTabSelect(WorkerJobTab.NEARBY)
                            onSearchChange("")
                            onSelectCategory(JobCategory.ALL)
                            onSelectZone("All")
                            onRadiusChange(25f)
                            onMinWageChange(0f)
                        }
                    ) {
                        Text("Explore All Nearby Shifts", color = BrandPrimaryGreen)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(jobs, key = { it.id }) { job ->
                    JobCardItem(
                        job = job,
                        isSaved = workerProfile.savedJobIds.contains(job.id) || job.isSaved,
                        onClick = { onJobClick(job) },
                        onApply = { onApplyJob(job.id) },
                        onToggleSave = { onToggleSaveJob(job.id) },
                        onWithdraw = { onWithdrawJob(job.id) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun JobCardItem(
    job: JobPosting,
    isSaved: Boolean = false,
    onClick: () -> Unit,
    onApply: () -> Unit,
    onToggleSave: () -> Unit = {},
    onWithdraw: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(
            1.dp,
            if (job.isEmergency30Min) BrandAmber.copy(alpha = 0.6f) else BrandCardBorderDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("job_card_${job.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Emergency 30-min banner if applicable
            if (job.isEmergency30Min) {
                Surface(
                    color = BrandAmberDim,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ 30-MIN EMERGENCY ARRIVAL REQUIRED • 1.2x SURGE WAGE",
                            color = BrandAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Business name + wage badge + save icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = job.businessName,
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = BrandSecondaryCyan,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Business",
                        tint = BrandPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Wage Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryGreenDim
                    ) {
                        Text(
                            text = "${job.wage} ${job.unit}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = BrandPrimaryGreen,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Bookmark / Save Action
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isSaved) BrandAmber else TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Job Title
            Text(
                text = job.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Badges row: Zone + Security Deposit Escrowed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BrandSurfaceDark
                ) {
                    Text(
                        text = "Zone: ${job.zone}",
                        color = Color.LightGray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BrandPrimaryGreenDim
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "₹${job.securityDeposit.toInt()} Owner Escrow",
                            color = BrandPrimaryGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = BrandAmberDim
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(11.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "₹${job.suretyRequired.toInt()} Worker Surety",
                            color = BrandAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time & Location Details + Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${job.date}, ${job.time}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${job.distanceKm} km",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Navigate Workplace Direct Action Button (Google Maps)
            OutlinedButton(
                onClick = {
                    val uri = Uri.parse("geo:0,0?q=${Uri.encode(job.location)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (_: Exception) {
                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(job.location)}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = "Navigate", tint = BrandSecondaryCyan, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Navigate: ${job.location}",
                    color = BrandSecondaryCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row: Details + Apply/Withdraw
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                ) {
                    Text("Details", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }

                if (job.isApplied) {
                    OutlinedButton(
                        onClick = onWithdraw,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Withdraw", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (job.isEmergency30Min) BrandAmber else BrandPrimaryGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(42.dp)
                            .testTag("accept_shift_button_${job.id}")
                    ) {
                        Text(
                            text = if (job.isEmergency30Min) "Accept 30-Min Shift" else "Apply Now",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
