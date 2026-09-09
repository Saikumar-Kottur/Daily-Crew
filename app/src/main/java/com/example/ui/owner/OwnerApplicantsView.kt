package com.example.ui.owner

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApplicantStatus
import com.example.data.model.JobApplicant
import com.example.ui.theme.*

@Composable
fun OwnerApplicantsView(
    applicants: List<JobApplicant>,
    onHireWorker: (String) -> Unit,
    onRejectWorker: (String) -> Unit,
    onChatWithWorker: (JobApplicant) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_applicants_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Shift Applicants",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Sorted by Trust Score & On-Time Performance",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandSecondaryCyanDim
            ) {
                Text(
                    text = "${applicants.size} Available",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandSecondaryCyan,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(applicants, key = { it.id }) { applicant ->
                ApplicantCard(
                    applicant = applicant,
                    onHire = { onHireWorker(applicant.id) },
                    onReject = { onRejectWorker(applicant.id) },
                    onChat = { onChatWithWorker(applicant) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}

@Composable
private fun ApplicantCard(
    applicant: JobApplicant,
    onHire: () -> Unit,
    onReject: () -> Unit,
    onChat: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("applicant_card_${applicant.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Worker name, verified check, and rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryGreenDim),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = applicant.workerName.take(2).uppercase(),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = BrandPrimaryGreen
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
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Worker",
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "Applying for: ${applicant.jobTitle}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

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
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                }
                ApplicantStatus.HIRED -> {
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
                            modifier = Modifier.size(42.dp)
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
