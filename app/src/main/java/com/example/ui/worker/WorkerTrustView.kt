package com.example.ui.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.WorkerProfile
import com.example.ui.components.TrustBadgePill
import com.example.ui.theme.*

@Composable
fun WorkerTrustView(
    worker: WorkerProfile
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("worker_trust_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Worker Identity & Trust Header Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(BrandPrimaryGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = worker.avatarInitials,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aadhaar & Police Verified Worker",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandPrimaryGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    TrustBadgePill(tier = worker.badgeTier, score = worker.trustScore)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ScoreStatItem(valStr = "⭐ ${worker.rating}", label = "Rating")
                            ScoreStatItem(valStr = "${worker.completedJobs}", label = "Jobs Done")
                            ScoreStatItem(valStr = "⏱️ ${worker.onTimeRate}%", label = "On-Time Rate")
                            ScoreStatItem(valStr = "${worker.trustScore}/100", label = "Trust Score")
                        }
                    }
                }
            }
        }

        // Trust Checklist
        item {
            Text(
                text = "Trust & Verification Checklist",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TrustCheckTile(
                    title = "Government ID (Aadhaar/PAN) Linked",
                    desc = "Biometric identity authenticated with zero discrepancies.",
                    isPassed = worker.isAadhaarVerified
                )
                TrustCheckTile(
                    title = "Police Background Clearance",
                    desc = "Verified clean criminal record on national registry.",
                    isPassed = worker.isPoliceVerified
                )
                TrustCheckTile(
                    title = "Zero No-Show Record (Past 30 Days)",
                    desc = "Maintained 100% adherence to all accepted shifts.",
                    isPassed = true
                )
                TrustCheckTile(
                    title = "Top Rated Catering & Hospitality Worker",
                    desc = "Consistently achieved 4.8+ star supervisor reviews.",
                    isPassed = true
                )
            }
        }

        // Skills & Experience Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Verified Competencies & Skills",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        worker.skills.take(3).forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandSecondaryCyanDim,
                                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = BrandSecondaryCyan,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Languages: ${worker.languages.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Experience: ${worker.experience}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun ScoreStatItem(valStr: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valStr,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                color = TextSecondaryDark,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun TrustCheckTile(
    title: String,
    desc: String,
    isPassed: Boolean
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isPassed) BrandPrimaryGreen else TextSecondaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
