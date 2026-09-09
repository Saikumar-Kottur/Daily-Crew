package com.example.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.data.model.JobApplicant
import com.example.data.model.JobPosting
import com.example.ui.theme.*

@Composable
fun AiMatchingScreen(
    currentRole: AppRole,
    recommendedJobs: List<JobPosting>,
    topApplicants: List<JobApplicant>,
    onSelectJob: (JobPosting) -> Unit,
    onHireApplicant: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("ai_matching_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "DailyCrew AI Engine",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandAmberDim
                        ) {
                            Text(
                                text = "Active Neural Matching",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (currentRole == AppRole.WORKER)
                            "AI analyzed your 47 completed shifts, 98% attendance, and hospitality skills to rank the highest-paying, nearest shifts."
                        else
                            "AI verified candidates with zero no-show history and high banquet captain reviews for your shift tonight.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }
        }

        // Market Insights Cards
        item {
            Text(
                text = "Market Demand & Reliability Insights",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AiInsightCard(
                    title = "Peak Hiring Window: 4:30 PM - 1:30 AM",
                    tag = "High Demand",
                    desc = "Banquet and hotel caterers in Banjara Hills & Hitec City are offering 20% higher shift bonuses this weekend.",
                    color = BrandPrimaryGreen
                )
                AiInsightCard(
                    title = "Reliability Prediction: 98.6% On-Time",
                    tag = "Platinum Tier",
                    desc = "Workers with Aadhaar & Police clearance show zero reported shift abandonment in last 6 months.",
                    color = BrandSecondaryCyan
                )
                AiInsightCard(
                    title = "Hot Skill Requirement: Banquet Stewards",
                    tag = "Trend Alert",
                    desc = "Catering agencies require 140+ stewards this Saturday. Book shifts early to secure preference.",
                    color = BrandAmber
                )
            }
        }

        // Recommendations List
        item {
            Text(
                text = if (currentRole == AppRole.WORKER) "AI Recommended Shifts For You" else "AI Top-Ranked Verified Candidates",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        if (currentRole == AppRole.WORKER) {
            items(recommendedJobs.take(3), key = { it.id }) { job ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(job.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                            Text("${job.wage} ${job.unit}", style = MaterialTheme.typography.labelMedium.copy(color = BrandPrimaryGreen, fontWeight = FontWeight.Bold))
                        }
                        Text("${job.businessName} • ${job.location}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onSelectJob(job) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("View Match Details & Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(topApplicants.take(3), key = { it.id }) { applicant ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(applicant.workerName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(14.dp))
                            }
                            Text("Trust ${applicant.trustScore}%", style = MaterialTheme.typography.labelSmall.copy(color = BrandPrimaryGreen, fontWeight = FontWeight.Bold))
                        }
                        Text("${applicant.jobsCompleted} Shifts Done • ${applicant.onTimeRate}% On-Time Attendance", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onHireApplicant(applicant.id) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSecondaryCyan, contentColor = Color.Black),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Instant Hire Verified Candidate", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun AiInsightCard(
    title: String,
    tag: String,
    desc: String,
    color: Color
) {
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
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = tag,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 12.sp
                )
            )
        }
    }
}
