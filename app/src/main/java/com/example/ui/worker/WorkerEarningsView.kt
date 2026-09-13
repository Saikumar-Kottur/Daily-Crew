package com.example.ui.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.model.AppLanguage
import com.example.data.model.PayoutTransaction
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*
import com.example.util.DailyCrewLocalization

@Composable
fun WorkerEarningsView(
    worker: WorkerProfile,
    transactions: List<PayoutTransaction>,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    onWithdrawClick: () -> Unit,
    onReportDisputeClick: () -> Unit,
    onReferClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("worker_earnings_screen")
    ) {
        // Main Earnings Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total Earnings (This Month)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimaryGreenDim
                    ) {
                        Text(
                            text = "DailyCrew Escrow Verified",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BrandPrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹${"%,.2f".format(worker.walletBalance)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = BrandPrimaryGreen,
                        fontSize = 34.sp
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BrandCardBorderDark)
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Completed Shifts", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Text(
                            "${worker.completedJobs} shifts",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Surety Returned", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Text(
                            "100% (₹100/shift)",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandPrimaryGreen)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pending Payout", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                        Text(
                            "₹${"%,.0f".format(worker.pendingPayout)}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BrandAmber)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Instant Withdraw Button (UPI & Bank Account)
                Button(
                    onClick = onWithdrawClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("withdraw_upi_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WITHDRAW VIA UPI OR BANK A/C",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = BrandPrimaryGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (worker.freeCommissionWithdrawalsCount > 0)
                            "Referral Pass Active: 0% Platform Fee (${worker.freeCommissionWithdrawalsCount} free left)"
                        else
                            "9.7% Platform Fee at withdrawal • 100% Surety Refunded",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (worker.freeCommissionWithdrawalsCount > 0) BrandPrimaryGreen else TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Referral & Commission Banner Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (worker.freeCommissionWithdrawalsCount > 0) BrandPrimaryGreenDim else BrandAmberDim,
            border = BorderStroke(1.dp, if (worker.freeCommissionWithdrawalsCount > 0) BrandPrimaryGreen.copy(alpha = 0.5f) else BrandAmber.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onReferClick() }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (worker.freeCommissionWithdrawalsCount > 0) BrandPrimaryGreen else BrandAmber,
                        modifier = Modifier.size(34.dp)
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
                            text = if (worker.freeCommissionWithdrawalsCount > 0)
                                "🎁 ${worker.freeCommissionWithdrawalsCount} Zero-Fee Withdrawals Active!"
                            else
                                "🎁 Refer a Worker = 2 Free Withdrawals!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Save 9.7% commission + earn 2.3% on friends' earnings (₹${"%.2f".format(worker.totalReferralEarnings)} earned)",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onReferClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (worker.freeCommissionWithdrawalsCount > 0) BrandPrimaryGreen else BrandAmber,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DailyCrewLocalization.get("copy_link", selectedLanguage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Shift Payouts Header & Dispute Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Shift Payouts",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Text(
                text = "Report Issue",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = BrandErrorRed,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .clickable { onReportDisputeClick() }
                    .padding(4.dp)
                    .testTag("report_payout_dispute")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Payout Transactions List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(transactions, key = { it.id }) { tx ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (tx.isCredit) BrandPrimaryGreenDim else BrandSecondaryCyanDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (tx.isCredit) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = if (tx.isCredit) BrandPrimaryGreen else BrandSecondaryCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = tx.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = "${tx.date} • ${tx.businessOrWorker}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = (if (tx.isCredit) "+ ₹" else "- ₹") + "%,.0f".format(tx.amount),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (tx.isCredit) BrandPrimaryGreen else Color.White
                                )
                            )
                            Text(
                                text = tx.status,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandSecondaryCyan,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }
}
