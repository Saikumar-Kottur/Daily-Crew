package com.example.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.data.model.JobApplicant
import com.example.data.model.ApplicantStatus
import com.example.data.model.JobPosting
import com.example.ui.theme.*

@Composable
fun OwnerDashboardView(
    business: BusinessProfile,
    applicants: List<JobApplicant>,
    jobs: List<JobPosting> = emptyList(),
    onTriggerPayout: (Double) -> Unit,
    onNavigateToPostJob: () -> Unit,
    onOpenEmergencyHiring: () -> Unit,
    onEditJob: (JobPosting) -> Unit = {},
    onReferClick: () -> Unit = {}
) {
    val checkedInCount = applicants.count { it.status == ApplicantStatus.CHECKED_IN }
    val standbyWorkers = applicants.filter { it.isStandbyReady }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Business Identity Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandSecondaryCyanDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = BrandSecondaryCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = business.businessName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Business",
                                        tint = BrandPrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "GSTIN: ${business.gstin} • ${business.category}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreenDim
                        ) {
                            Text(
                                text = "Zone: ${business.zone}",
                                color = BrandPrimaryGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = BrandCardBorderDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anti-Fake Escrow Balance:", color = TextSecondaryDark, fontSize = 11.sp)
                        }
                        Text(
                            text = "₹${business.escrowSecurityDepositBalance.toInt()} (Refundable)",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Referral & Surety Split Perks Banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (business.freeCommissionJobsCount > 0) BrandSecondaryCyanDim else BrandCardDark
                ),
                border = BorderStroke(1.dp, if (business.freeCommissionJobsCount > 0) BrandSecondaryCyan else BrandCardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReferClick() }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BrandSecondaryCyan,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (business.freeCommissionJobsCount > 0)
                                        "🎉 ${business.freeCommissionJobsCount} Zero-Fee Shifts Active (100% Surety Refund)!"
                                    else
                                        "🎁 Refer an Owner = 2 Zero-Fee Shifts!",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "Surety Split: ₹300 deposit -> 73.2% (₹219.60) refund | 27.8% fee",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                                )
                            }
                        }

                        Button(
                            onClick = onReferClick,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSecondaryCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share Link", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Earn 3.0% commission on surety fees of referred owners. Total earned: ₹${"%.2f".format(business.totalReferralEarnings)}",
                        fontSize = 11.sp,
                        color = BrandSecondaryCyan
                    )
                }
            }
        }

        // Emergency 30-Min Staffing Banner
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = BrandAmberDim),
                border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "⚡ 30-Min Emergency Staffing",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandAmber
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Staff no-show or sudden banquet rush? Broadcast to standby workers nearby.",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = onOpenEmergencyHiring,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandAmber),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("btn_broadcast_emergency")
                    ) {
                        Text("Hire Now", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        // Standby Crew Live Radar (Nearby verified crew on standby)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Standby Crew Radar (${standbyWorkers.size} Online)",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Text("Arrival: < 30 mins", color = BrandPrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                standbyWorkers.forEach { crew ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.width(180.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = crew.workerName.substringBefore(" "),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Surface(
                                    color = BrandPrimaryGreenDim,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "${crew.trustScore} TS",
                                        color = BrandPrimaryGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = crew.role,
                                color = TextSecondaryDark,
                                fontSize = 10.sp,
                                maxLines = 1
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("★ ${crew.rating}", color = BrandAmber, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${crew.distanceKm} km away", color = Color.LightGray, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Active Shift Management
        item {
            Text(
                text = "Active Shift Management",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Tonight Shift (Senior Catering Stewards)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "5 Workers Required • 5:00 PM - 1:00 AM",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandAmberDim
                        ) {
                            Text(
                                text = "$checkedInCount Checked-In",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BrandCardBorderDark)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Worker Check-in Status Tiles
                    applicants.take(3).forEach { applicant ->
                        val isHere = applicant.status == ApplicantStatus.CHECKED_IN
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isHere) Icons.Default.CheckCircle else Icons.Default.AccessTimeFilled,
                                    contentDescription = null,
                                    tint = if (isHere) BrandPrimaryGreen else TextSecondaryDark,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = applicant.workerName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = applicant.role,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }

                            Text(
                                text = if (isHere) applicant.checkInTime ?: "Checked-In" else "Not Arrived Yet",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (isHere) BrandPrimaryGreen else BrandAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val activeJob = jobs.firstOrNull { it.businessName == business.businessName } ?: jobs.firstOrNull()
                    if (activeJob != null) {
                        OutlinedButton(
                            onClick = { onEditJob(activeJob) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(42.dp)
                                .testTag("btn_edit_job_shift"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.8f))
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Shift Requirements & Pay",
                                color = BrandAmber,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Auto Payout Trigger Button
                    OutlinedButton(
                        onClick = { onTriggerPayout(2550.0) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("complete_shift_payout_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandSecondaryCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Payments, contentDescription = null, tint = BrandSecondaryCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Disburse Shift Auto-Payout (₹2,550)",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandSecondaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }

        // Hiring Analytics Stats
        item {
            Text(
                text = "Hiring Analytics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Workers Hired",
                    value = "${business.totalWorkersHired}",
                    icon = Icons.Default.People,
                    color = BrandPrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Reliability",
                    value = "${business.reliabilityScore}%",
                    icon = Icons.Default.VerifiedUser,
                    color = BrandSecondaryCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Payouts Disbursed",
                    value = "₹${"%,.0f".format(business.totalPayoutsDisbursed / 1000)}k",
                    icon = Icons.Default.AccountBalance,
                    color = BrandAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Post Job CTA
        item {
            Button(
                onClick = onNavigateToPostJob,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("quick_post_job_cta"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandSecondaryCyan,
                    contentColor = Color.Black
                )
            ) {
                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("POST SCHEDULED REQUIREMENT", fontWeight = FontWeight.ExtraBold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 10.sp
                )
            )
        }
    }
}
