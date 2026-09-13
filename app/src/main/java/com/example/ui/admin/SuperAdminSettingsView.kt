package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.DailyCrewViewModel

@Composable
fun SuperAdminSettingsView(viewModel: DailyCrewViewModel) {
    val currentSettings by viewModel.platformSettings.collectAsState()
    val scrollState = rememberScrollState()

    var commissionRate by remember(currentSettings) { mutableFloatStateOf(currentSettings.commissionRatePercent.toFloat()) }
    var depositAmount by remember(currentSettings) { mutableFloatStateOf(currentSettings.securityDepositPerJob.toFloat()) }
    var workerWithdrawalPercent by remember(currentSettings) { mutableFloatStateOf(currentSettings.workerWithdrawalCommissionPercent.toFloat()) }
    var ownerSuretyRefundPercent by remember(currentSettings) { mutableFloatStateOf(currentSettings.ownerSuretyRefundPercent.toFloat()) }
    var workerReferralBonusPercent by remember(currentSettings) { mutableFloatStateOf(currentSettings.workerReferralBonusPercent.toFloat()) }
    var ownerReferralBonusPercent by remember(currentSettings) { mutableFloatStateOf(currentSettings.ownerReferralBonusPercent.toFloat()) }
    var autoEscrow by remember(currentSettings) { mutableStateOf(currentSettings.autoEscrowPayoutEnabled) }
    var penaltyScore by remember(currentSettings) { mutableIntStateOf(currentSettings.antiNoShowPenaltyScore) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("super_admin_settings_view")
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
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = BrandAmber,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Super Admin Platform Engine",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
                Text(
                    text = "Global marketplace rules, economics, and pilot territory configuration",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            Surface(
                color = BrandAmberDim,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "SYSTEM ROOT",
                    color = BrandAmber,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Commission Setting Card (Strict 5% - 8% mandate to prevent commission rejection)
        Card(
            shape = RoundedCornerShape(14.dp),
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
                            text = "Marketplace Commission Engine",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Mandate: 5% to 8% (Anti-Rejection Policy)",
                            color = BrandPrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = BrandPrimaryGreenDim,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", commissionRate)}%",
                            color = BrandPrimaryGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = commissionRate,
                    onValueChange = { commissionRate = it },
                    valueRange = 5.0f..8.0f,
                    steps = 5,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandPrimaryGreen,
                        activeTrackColor = BrandPrimaryGreen,
                        inactiveTrackColor = BrandCardBorderDark
                    )
                )

                Text(
                    text = "Current split on ₹1,000 shift: Worker receives ₹${(1000 * (1 - commissionRate / 100)).toInt()}, Platform fee is ₹${(1000 * (commissionRate / 100)).toInt()}.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Anti-Fake Job Security Deposit
        Card(
            shape = RoundedCornerShape(14.dp),
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
                            text = "Anti-Fake Job Security Deposit",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Refundable to business upon shift completion",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = BrandSecondaryCyanDim,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "₹${depositAmount.toInt()}",
                            color = BrandSecondaryCyan,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Slider(
                    value = depositAmount,
                    onValueChange = { depositAmount = it },
                    valueRange = 100f..1500f,
                    steps = 14,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandSecondaryCyan,
                        activeTrackColor = BrandSecondaryCyan,
                        inactiveTrackColor = BrandCardBorderDark
                    )
                )

                Text(
                    text = "Requires business to deposit ₹${depositAmount.toInt()} into DailyCrew escrow before publishing any shift. Drastically eliminates fake posts.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Owner Surety Escrow System Configuration
        Card(
            shape = RoundedCornerShape(14.dp),
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
                            text = "Owner Surety System (₹300 Escrow)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Refund: 73.2% (₹219.60) | Platform Revenue: 26.8% (₹80.40)",
                            color = BrandSecondaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                        color = BrandSecondaryCyanDim,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", ownerSuretyRefundPercent)}% / ${String.format("%.1f", 100f - ownerSuretyRefundPercent)}%",
                            color = BrandSecondaryCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "On shift completion, ₹${String.format("%.2f", 300.0 * (ownerSuretyRefundPercent / 100.0))} is returned to owner, and ₹${String.format("%.2f", 300.0 * ((100.0 - ownerSuretyRefundPercent) / 100.0))} is recognized as DailyCrew platform revenue.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Worker Payment & Commission Rail
        Card(
            shape = RoundedCornerShape(14.dp),
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
                            text = "Worker Withdrawal Commission",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Commission deducted strictly at withdrawal time",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = BrandPrimaryGreenDim,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${String.format("%.1f", workerWithdrawalPercent)}%",
                            color = BrandPrimaryGreen,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Worker keeps ${String.format("%.1f", 100f - workerWithdrawalPercent)}% of earnings. Platform takes ${String.format("%.1f", workerWithdrawalPercent)}% on payout (or 0% if referral pass is active).",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Referral Program Economics Configuration
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Referral Program Reward Rails",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Surface(
                    color = BrandSurfaceDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Worker Referral Bonus (to Worker A)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("2.3% of platform commission generated from Worker B", color = TextSecondaryDark, fontSize = 10.sp)
                        }
                        Text("${String.format("%.1f", workerReferralBonusPercent)}%", color = BrandPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Surface(
                    color = BrandSurfaceDark,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Owner Referral Bonus (to Owner A)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("3.0% of DailyCrew's surety revenue from Owner B", color = TextSecondaryDark, fontSize = 10.sp)
                        }
                        Text("${String.format("%.1f", ownerReferralBonusPercent)}%", color = BrandSecondaryCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Text(
                    text = "Both referred workers and owners receive 2 commission-free shifts / withdrawals upon onboarding.",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Phase 1 Launch Territory Lock
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Phase 1 Launch Pilot Territory (Hyderabad)",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Cold-start prevention: Restrict workforce liquidity to validated density cluster",
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Uppal", "Habsiguda", "Tarnaka", "Nacharam").forEach { zone ->
                        Surface(
                            color = BrandSurfaceDark,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(zone, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Anti No-Show Penalties
        Card(
            shape = RoundedCornerShape(14.dp),
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
                            text = "Anti No-Show Penalty Deduction",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Deducted from Trust Score on unexcused absence",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )
                    }

                    Text("-$penaltyScore pts", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Instant Auto-Escrow Payouts", color = Color.White, fontSize = 13.sp)
                    Switch(
                        checked = autoEscrow,
                        onCheckedChange = { autoEscrow = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandPrimaryGreen,
                            checkedTrackColor = BrandPrimaryGreenDim
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                viewModel.updatePlatformSettings(
                    currentSettings.copy(
                        commissionRatePercent = commissionRate.toDouble(),
                        securityDepositPerJob = depositAmount.toDouble(),
                        workerWithdrawalCommissionPercent = workerWithdrawalPercent.toDouble(),
                        ownerSuretyRefundPercent = ownerSuretyRefundPercent.toDouble(),
                        ownerSuretyPlatformCommissionPercent = (100.0 - ownerSuretyRefundPercent.toDouble()),
                        workerReferralBonusPercent = workerReferralBonusPercent.toDouble(),
                        ownerReferralBonusPercent = ownerReferralBonusPercent.toDouble(),
                        autoEscrowPayoutEnabled = autoEscrow,
                        antiNoShowPenaltyScore = penaltyScore
                    )
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("btn_save_super_admin_settings")
        ) {
            Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Platform Settings Live", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
