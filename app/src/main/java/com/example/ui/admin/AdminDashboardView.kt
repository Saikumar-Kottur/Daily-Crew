package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.DailyCrewViewModel

@Composable
fun AdminDashboardView(viewModel: DailyCrewViewModel) {
    val disputes by viewModel.disputes.collectAsState()
    val workerProfile by viewModel.workerProfile.collectAsState()
    val businessProfile by viewModel.businessProfile.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val bannedUsers by viewModel.bannedUsers.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Disputes, 1: Verification, 2: Jobs, 3: Bans

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_view")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BrandSecondaryCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Admin Moderation Center",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = "Platform oversight, trust arbitration, and dispute mediation",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            Surface(
                color = BrandSecondaryCyanDim,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "ACTIVE ADMIN",
                    color = BrandSecondaryCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Platform KPI Summary Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("OPEN DISPUTES", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${disputes.count { it.status.startsWith("Under") }}",
                        color = if (disputes.any { it.status.startsWith("Under") }) BrandAmber else BrandPrimaryGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("ACTIVE SHIFTS", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${jobs.size}",
                        color = BrandPrimaryGreen,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("DEPOSIT ESCROW", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "₹${(jobs.size * 500)}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BrandSurfaceDark,
            contentColor = BrandPrimaryGreen,
            divider = {}
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Disputes (${disputes.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Verification", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Anti-Fake Jobs", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { Text("Suspensions", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedTab) {
            0 -> {
                // Disputes Section
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(disputes, key = { it.id }) { disp ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                            border = BorderStroke(1.dp, if (disp.status.startsWith("Under")) BrandAmber.copy(alpha = 0.5f) else BrandCardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = disp.reason,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                    Surface(
                                        color = if (disp.status.startsWith("Under")) BrandAmberDim else BrandPrimaryGreenDim,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = disp.status,
                                            color = if (disp.status.startsWith("Under")) BrandAmber else BrandPrimaryGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Reported by ${disp.reportedByRole}: ${disp.reporterName} vs ${disp.targetName}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = BrandSecondaryCyan, fontSize = 11.sp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = disp.details,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark, fontSize = 12.sp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Evidence Log Box
                                Surface(
                                    color = BrandSurfaceDark,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, BrandCardBorderDark),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = "VERIFIED EVIDENCE LOGS:",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = TextSecondaryDark,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        )
                                        disp.evidenceLogs.forEach { log ->
                                            Text(
                                                text = "• $log",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }
                                }

                                if (disp.adminRuling != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Official Ruling: ${disp.adminRuling}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = BrandPrimaryGreen,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }

                                if (disp.status.startsWith("Under")) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.resolveDispute(
                                                    disputeId = disp.id,
                                                    ruling = "GPS Overtime verified. Extra shift pay released to worker wallet.",
                                                    releasePayment = true
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Release to Worker", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.resolveDispute(
                                                    disputeId = disp.id,
                                                    ruling = "Worker no-show verified. Penalty applied and deposit refunded to owner.",
                                                    releasePayment = false
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BrandSurfaceDark),
                                            border = BorderStroke(1.dp, BrandAmber),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Refund Owner", color = BrandAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Verification Review
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = workerProfile.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text("Worker Profile (${workerProfile.phone})", color = TextSecondaryDark, fontSize = 11.sp)
                                    }

                                    Surface(
                                        color = if (workerProfile.isSelfieVerified) BrandPrimaryGreenDim else BrandAmberDim,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (workerProfile.isSelfieVerified) "Level 3: Full Verified" else "Pending Review",
                                            color = if (workerProfile.isSelfieVerified) BrandPrimaryGreen else BrandAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("• Level 1: Mobile OTP Verified: ${workerProfile.isPhoneVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Level 2: Google Identity Verified: ${workerProfile.isGoogleVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Level 3: Face Selfie Verification: ${workerProfile.isSelfieVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Trust Score: ${workerProfile.trustScore}/100 (${workerProfile.trustLevel.label})", color = BrandPrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { viewModel.toggleWorkerVerification() },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (workerProfile.isSelfieVerified) BrandAmberDim else BrandPrimaryGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (workerProfile.isSelfieVerified) "Revoke Face Selfie Verification" else "Approve Level 3 Face Selfie",
                                        color = if (workerProfile.isSelfieVerified) BrandAmber else Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = businessProfile.businessName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text("GSTIN: ${businessProfile.gstin}", color = TextSecondaryDark, fontSize = 11.sp)
                                    }

                                    Surface(
                                        color = if (businessProfile.isLocationVerified) BrandPrimaryGreenDim else BrandAmberDim,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = if (businessProfile.isLocationVerified) "Level 3: Physical Venue Verified" else "Pending Geofence",
                                            color = if (businessProfile.isLocationVerified) BrandPrimaryGreen else BrandAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("• Level 1: Mobile Phone Verified: ${businessProfile.isPhoneVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Level 2: Google Workspace Org: ${businessProfile.isGoogleVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Level 3: Physical Location Geofenced: ${businessProfile.isLocationVerified}", color = Color.LightGray, fontSize = 11.sp)
                                Text("• Reliability Score: ${businessProfile.reliabilityScore}% (0 Fake Job Flags)", color = BrandPrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = { viewModel.toggleBusinessVerification() },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (businessProfile.isLocationVerified) BrandAmberDim else BrandPrimaryGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (businessProfile.isLocationVerified) "Toggle Re-Inspection" else "Approve Physical Venue Verification",
                                        color = if (businessProfile.isLocationVerified) BrandAmber else Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Anti-Fake Jobs Audit
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(jobs, key = { it.id }) { job ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = job.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Surface(
                                        color = BrandPrimaryGreenDim,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Deposit Escrowed: ₹${job.securityDeposit.toInt()}",
                                            color = BrandPrimaryGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "${job.businessName} • ${job.location} (Zone: ${job.zone})",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Wage: ${job.wage} ${job.unit}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Hired: ${job.workersHired}/${job.workersRequired} workers", color = BrandSecondaryCyan, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Suspensions & Bans
                Column(modifier = Modifier.weight(1f)) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "COMMUNITY SAFETY ACTION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Instantly suspend bad actors, repeated no-shows, or fraudulent job posters across all Hyderabad zones.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    viewModel.banUser("Vikram S.", "3 Repeated No-Shows without notice")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Ban: Suspend Flagged Worker", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("SUSPENDED ACCOUNTS:", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontWeight = FontWeight.Bold))

                    Spacer(modifier = Modifier.height(8.dp))

                    if (bannedUsers.isEmpty()) {
                        Text("No active suspensions", color = TextSecondaryDark, fontSize = 12.sp)
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(bannedUsers) { ban ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = ban, color = Color.White, fontSize = 12.sp)
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
