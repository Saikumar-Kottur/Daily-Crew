package com.example.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.ApplicantStatus
import com.example.data.model.JobApplicant
import com.example.ui.theme.*

enum class VerificationFilter(val label: String, val icon: String) {
    ALL("All Workers", "👥"),
    VERIFIED_ONLY("Verified Only", "🛡️"),
    PENDING("Pending Verification", "⏳")
}

val PRESET_SKILLS = listOf(
    "All",
    "Catering",
    "Housekeeping",
    "Kitchen Prep",
    "Table Service",
    "Event Staff",
    "Food Safety",
    "VIP Protocol"
)

fun getSkillEmoji(skill: String): String = when {
    skill.contains("Cater", ignoreCase = true) -> "🍽️"
    skill.contains("Housekeep", ignoreCase = true) || skill.contains("Clean", ignoreCase = true) -> "🧹"
    skill.contains("Kitchen", ignoreCase = true) || skill.contains("Prep", ignoreCase = true) || skill.contains("Cook", ignoreCase = true) -> "🍳"
    skill.contains("Table", ignoreCase = true) || skill.contains("Banquet", ignoreCase = true) -> "🍷"
    skill.contains("Event", ignoreCase = true) || skill.contains("Host", ignoreCase = true) || skill.contains("Usher", ignoreCase = true) -> "✨"
    skill.contains("Safe", ignoreCase = true) || skill.contains("FSSAI", ignoreCase = true) || skill.contains("Sanit", ignoreCase = true) -> "🧼"
    skill.contains("VIP", ignoreCase = true) || skill.contains("Protocol", ignoreCase = true) -> "👔"
    else -> "⭐"
}

@Composable
fun OwnerApplicantsView(
    applicants: List<JobApplicant>,
    onHireWorker: (String) -> Unit,
    onRejectWorker: (String) -> Unit,
    onChatWithWorker: (JobApplicant) -> Unit,
    onReportNoShow: (JobApplicant) -> Unit = {},
    onRateWorker: (JobApplicant) -> Unit = {},
    initialSkillFilter: String = "All",
    initialVerifiedOnly: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSkill by remember { mutableStateOf(initialSkillFilter) }
    var verificationFilter by remember {
        mutableStateOf(if (initialVerifiedOnly) VerificationFilter.VERIFIED_ONLY else VerificationFilter.ALL)
    }

    val verifiedCount = remember(applicants) { applicants.count { it.isVerified } }
    val pendingCount = remember(applicants) { applicants.count { !it.isVerified } }

    // Filtering logic
    val filteredApplicants = remember(applicants, searchQuery, selectedSkill, verificationFilter) {
        applicants.filter { applicant ->
            // Verification filter
            val matchesVerification = when (verificationFilter) {
                VerificationFilter.ALL -> true
                VerificationFilter.VERIFIED_ONLY -> applicant.isVerified
                VerificationFilter.PENDING -> !applicant.isVerified
            }

            // Skill chip filter
            val matchesSkill = if (selectedSkill == "All") {
                true
            } else {
                applicant.skills.any { skill ->
                    skill.contains(selectedSkill, ignoreCase = true)
                } || applicant.role.contains(selectedSkill, ignoreCase = true)
            }

            // Free text search query (searches skills, worker name, role, and job title)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase()
                applicant.workerName.lowercase().contains(q) ||
                applicant.role.lowercase().contains(q) ||
                applicant.jobTitle.lowercase().contains(q) ||
                applicant.skills.any { it.lowercase().contains(q) }
            }

            matchesVerification && matchesSkill && matchesSearch
        }
    }

    val isAnyFilterActive = searchQuery.isNotBlank() || selectedSkill != "All" || verificationFilter != VerificationFilter.ALL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_applicants_screen")
    ) {
        // Header with Title and Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Worker Marketplace",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Filter by skills & government-verified trust status",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandSecondaryCyanDim
            ) {
                Text(
                    text = "${filteredApplicants.size} of ${applicants.size} Workers",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandSecondaryCyan,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("filtered_worker_count_badge")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar for Skills & Worker Name
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_worker_skills_input"),
            placeholder = {
                Text(
                    text = "Search skill (e.g., 'catering', 'housekeeping') or name...",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark),
                    fontSize = 13.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Workers",
                    tint = BrandSecondaryCyan,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.testTag("clear_search_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Search",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark,
                focusedContainerColor = BrandSurfaceDark,
                unfocusedContainerColor = BrandSurfaceDark,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Verification Status Segmented Filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All
            VerificationPill(
                title = "All (${applicants.size})",
                isSelected = verificationFilter == VerificationFilter.ALL,
                selectedColor = BrandSecondaryCyan,
                modifier = Modifier
                    .weight(1f)
                    .testTag("filter_all_workers"),
                onClick = { verificationFilter = VerificationFilter.ALL }
            )

            // Verified Only
            VerificationPill(
                title = "🛡️ Verified (${verifiedCount})",
                isSelected = verificationFilter == VerificationFilter.VERIFIED_ONLY,
                selectedColor = BrandPrimaryGreen,
                modifier = Modifier
                    .weight(1.2f)
                    .testTag("filter_verified_only"),
                onClick = { verificationFilter = VerificationFilter.VERIFIED_ONLY }
            )

            // Pending Verification
            VerificationPill(
                title = "⏳ Pending (${pendingCount})",
                isSelected = verificationFilter == VerificationFilter.PENDING,
                selectedColor = BrandAmber,
                modifier = Modifier
                    .weight(1.1f)
                    .testTag("filter_pending_verification"),
                onClick = { verificationFilter = VerificationFilter.PENDING }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Skill Category Horizontal Chips (Catering, Housekeeping, etc.)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("skills_filter_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(PRESET_SKILLS) { skillName ->
                val isSelected = selectedSkill == skillName
                val emoji = if (skillName == "All") "✨" else getSkillEmoji(skillName)

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) BrandPrimaryGreen else BrandCardDark,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) BrandPrimaryGreen else BrandCardBorderDark
                    ),
                    modifier = Modifier
                        .clickable { selectedSkill = skillName }
                        .testTag("skill_chip_${skillName.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = skillName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else TextPrimaryDark
                            )
                        )
                    }
                }
            }
        }

        // Active Filter Summary & Clear Action
        if (isAnyFilterActive) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = BrandSecondaryCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildString {
                            append("Filtered by: ")
                            if (selectedSkill != "All") append("Skill '$selectedSkill' • ")
                            if (verificationFilter != VerificationFilter.ALL) append("${verificationFilter.label} • ")
                            if (searchQuery.isNotBlank()) append("'$searchQuery'")
                        }.removeSuffix(" • "),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandSecondaryCyan,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                TextButton(
                    onClick = {
                        searchQuery = ""
                        selectedSkill = "All"
                        verificationFilter = VerificationFilter.ALL
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("btn_clear_all_filters")
                ) {
                    Text(
                        text = "Clear all",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BrandErrorRed,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Worker List / Empty State
        if (filteredApplicants.isEmpty()) {
            EmptyMarketplaceState(
                searchQuery = searchQuery,
                selectedSkill = selectedSkill,
                verificationFilter = verificationFilter,
                onReset = {
                    searchQuery = ""
                    selectedSkill = "All"
                    verificationFilter = VerificationFilter.ALL
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .testTag("marketplace_workers_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredApplicants, key = { it.id }) { applicant ->
                    ApplicantCard(
                        applicant = applicant,
                        activeSkillFilter = selectedSkill,
                        onHire = { onHireWorker(applicant.id) },
                        onReject = { onRejectWorker(applicant.id) },
                        onChat = { onChatWithWorker(applicant) },
                        onReportNoShow = { onReportNoShow(applicant) },
                        onRateWorker = { onRateWorker(applicant) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}

@Composable
private fun VerificationPill(
    title: String,
    isSelected: Boolean,
    selectedColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.2f) else BrandCardDark,
        border = BorderStroke(
            1.dp,
            if (isSelected) selectedColor else BrandCardBorderDark
        ),
        modifier = modifier
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) selectedColor else TextSecondaryDark,
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ApplicantCard(
    applicant: JobApplicant,
    activeSkillFilter: String,
    onHire: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit,
    onReportNoShow: () -> Unit,
    onRateWorker: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(
            1.dp,
            if (applicant.isVerified) BrandPrimaryGreen.copy(alpha = 0.4f) else BrandCardBorderDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("applicant_card_${applicant.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Worker Avatar, Name, Verification Badge, and Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (applicant.isVerified) BrandPrimaryGreenDim else BrandAmberDim
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = applicant.workerName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (applicant.isVerified) BrandPrimaryGreen else BrandAmber
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = applicant.workerName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            if (applicant.isVerified) {
                                Spacer(modifier = Modifier.width(5.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Identity",
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .testTag("verified_icon_${applicant.id}")
                                )
                            }
                        }
                        Text(
                            text = applicant.role,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        )
                    }
                }

                // Star Rating & Distance
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandAmberDim
                    ) {
                        Text(
                            text = "⭐ ${applicant.rating}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandAmber,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "📍 ${applicant.distanceKm} km",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verification & Standby Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Verification Badge
                if (applicant.isVerified) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimaryGreenDim
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "UIDAI & Police Verified",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandPrimaryGreen,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandAmberDim
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pending Identity Verification",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandAmber,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (applicant.isStandbyReady) {
                    Text(
                        text = "⚡ Instant Standby Ready",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BrandSecondaryCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Worker Skills Chips
            if (applicant.skills.isNotEmpty()) {
                Text(
                    text = "VERIFIED SKILLS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 9.sp,
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
                    applicant.skills.forEach { skill ->
                        val isMatched = (activeSkillFilter != "All" && skill.contains(activeSkillFilter, ignoreCase = true))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isMatched) BrandSecondaryCyanDim else BrandSurfaceDark,
                            border = BorderStroke(
                                1.dp,
                                if (isMatched) BrandSecondaryCyan else BrandCardBorderDark
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getSkillEmoji(skill),
                                    fontSize = 10.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isMatched) BrandSecondaryCyan else TextPrimaryDark,
                                        fontWeight = if (isMatched) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Trust metrics row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricChip(label = "Completed", value = "${applicant.jobsCompleted} Shifts")
                MetricChip(label = "On-Time", value = "${applicant.onTimeRate}%")
                MetricChip(label = "Trust Score", value = "${applicant.trustScore}/100")
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            when (applicant.status) {
                ApplicantStatus.CHECKED_IN -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandPrimaryGreenDim,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Checked-in at venue (${applicant.checkInTime ?: "4:52 PM"})",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = BrandPrimaryGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReportNoShow,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandErrorRed.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("report_noshow_btn_${applicant.id}")
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandErrorRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Report No-Show", color = BrandErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = onRateWorker,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandAmber, contentColor = Color.Black),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("rate_worker_btn_${applicant.id}")
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rate Crew", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                ApplicantStatus.HIRED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandSecondaryCyanDim,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Hired & Shift Confirmed",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = BrandSecondaryCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onReportNoShow,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandErrorRed.copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("report_noshow_btn_${applicant.id}")
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = BrandErrorRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Report No-Show", color = BrandErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onChat,
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BrandSecondaryCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("chat_worker_button_${applicant.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chat", color = BrandSecondaryCyan, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
                ApplicantStatus.COMPLETED -> {
                    Button(
                        onClick = onRateWorker,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAmber, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("rate_worker_btn_${applicant.id}")
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rate & Review Worker", fontWeight = FontWeight.Bold)
                    }
                }
                ApplicantStatus.REJECTED -> {
                    Text(
                        text = "Application Declined",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                ApplicantStatus.PENDING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onHire,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimaryGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(42.dp)
                                .testTag("hire_worker_button_${applicant.id}")
                        ) {
                            Text("Hire Worker", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onChat,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandSecondaryCyan),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("chat_worker_button_${applicant.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Chat", color = BrandSecondaryCyan, fontWeight = FontWeight.SemiBold)
                        }

                        IconButton(
                            onClick = onReject,
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("reject_worker_button_${applicant.id}")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Decline", tint = BrandErrorRed)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 10.sp))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
    }
}

@Composable
private fun EmptyMarketplaceState(
    searchQuery: String,
    selectedSkill: String,
    verificationFilter: VerificationFilter,
    onReset: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("empty_marketplace_state")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandCardDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonSearch,
                    contentDescription = null,
                    tint = BrandSecondaryCyan,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Workers Match Your Filters",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildString {
                    append("No workers found")
                    if (selectedSkill != "All") append(" with skill '$selectedSkill'")
                    if (verificationFilter != VerificationFilter.ALL) append(" and status '${verificationFilter.label}'")
                    if (searchQuery.isNotBlank()) append(" matching '$searchQuery'")
                    append(". Try broadening your criteria or resetting filters.")
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onReset,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandSecondaryCyan,
                    contentColor = Color.Black
                ),
                modifier = Modifier.testTag("btn_reset_filters")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset All Filters", fontWeight = FontWeight.Bold)
            }
        }
    }
}
