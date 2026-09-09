package com.example.ui.worker

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobCategory
import com.example.data.model.JobPosting
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*

@Composable
fun WorkerJobsView(
    jobs: List<JobPosting>,
    workerProfile: WorkerProfile,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: JobCategory,
    onSelectCategory: (JobCategory) -> Unit,
    selectedZone: String,
    onSelectZone: (String) -> Unit,
    filterRadiusKm: Float,
    onRadiusChange: (Float) -> Unit,
    filterEmergencyOnly: Boolean,
    onToggleEmergency: () -> Unit,
    onToggleStandby: () -> Unit,
    onJobClick: (JobPosting) -> Unit,
    onApplyJob: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("worker_jobs_screen")
    ) {
        // Standby Mode & Gamification Bar
        Surface(
            color = BrandSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                // Standby Toggle Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (workerProfile.isAvailableNow) BrandPrimaryGreenDim else BrandCardDark
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (workerProfile.isAvailableNow) BrandPrimaryGreen else BrandCardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(if (workerProfile.isAvailableNow) BrandPrimaryGreen else Color.Gray)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (workerProfile.isAvailableNow) "STANDBY MODE: LIVE" else "STANDBY MODE: OFF",
                                    color = if (workerProfile.isAvailableNow) BrandPrimaryGreen else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (workerProfile.isAvailableNow)
                                        "Visible to nearby venues for instant 30-min hire"
                                    else
                                        "Turn on to receive emergency high-wage dispatch",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
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
                            modifier = Modifier.testTag("standby_mode_toggle")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = {
                        Text(
                            "Search shift, catering, restaurant...",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(20.dp)
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
                        .fillMaxWidth()
                        .testTag("job_search_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hyderabad Pilot Zones (Uppal, Habsiguda, Tarnaka, Nacharam)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilot Zone:",
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    listOf("All", "Uppal", "Habsiguda", "Tarnaka", "Nacharam").forEach { zone ->
                        val isSelected = selectedZone.equals(zone, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) BrandSecondaryCyanDim else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) BrandSecondaryCyan else BrandCardBorderDark),
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSelectZone(zone) }
                        ) {
                            Text(
                                text = zone,
                                color = if (isSelected) BrandSecondaryCyan else Color.LightGray,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Emergency 30-min filter chip
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (filterEmergencyOnly) BrandAmberDim else Color.Transparent,
                        border = BorderStroke(1.dp, if (filterEmergencyOnly) BrandAmber else BrandCardBorderDark),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleEmergency() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "30-Min Urgent",
                                color = BrandAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category Scroll Row
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
                        imageVector = Icons.Default.WorkOff,
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No shifts match your filter",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing zone or emergency filters to view all available shifts.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            onSearchChange("")
                            onSelectCategory(JobCategory.ALL)
                            onSelectZone("All")
                            onRadiusChange(25f)
                        }
                    ) {
                        Text("Reset Filters", color = BrandPrimaryGreen)
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
                        onClick = { onJobClick(job) },
                        onApply = { onApplyJob(job.id) }
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
    onClick: () -> Unit,
    onApply: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
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

            // Business name + wage badge + escrow tag
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
                            text = "₹${job.securityDeposit.toInt()} Escrow Deposit Verified",
                            color = BrandPrimaryGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Time & Location Details
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

            // Dress Code Mini Banner
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = BrandAmber,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dress Code: ${job.dressCode}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Actions Row
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

                Button(
                    onClick = onApply,
                    enabled = !job.isApplied,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (job.isApplied) Color.Gray else if (job.isEmergency30Min) BrandAmber else BrandPrimaryGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1.8f)
                        .height(42.dp)
                        .testTag("accept_shift_button_${job.id}")
                ) {
                    Text(
                        text = if (job.isApplied) "Applied ✓" else if (job.isEmergency30Min) "Accept 30-Min Shift" else "Accept Shift Now",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (job.isApplied) Color.White.copy(alpha = 0.7f) else Color.Black,
                            fontSize = 13.sp
                        )
                    )
                }
            }
        }
    }
}
