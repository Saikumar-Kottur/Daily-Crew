package com.example.ui.worker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SuretyTransaction
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*

/**
 * Screen / Sub-view within the Profile section that allows workers
 * to view their past security deposit transactions and current balance held in escrow,
 * helping them track their platform commitments.
 */
@Composable
fun SecurityDepositEscrowView(
    worker: WorkerProfile,
    transactions: List<SuretyTransaction>,
    onBack: () -> Unit,
    onOpenTopUpDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val availableBalance = (worker.suretyEscrowBalance - worker.lockedSuretyAmount).coerceAtLeast(0.0)

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            "HOLDS" -> transactions.filter { it.type == "LOCKED" }
            "RELEASES" -> transactions.filter { it.type == "UNLOCKED_REFUNDED" }
            "DEPOSITS" -> transactions.filter { it.type == "DEPOSIT" }
            else -> transactions
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("security_deposit_escrow_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation & Header Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("btn_back_to_profile")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Profile",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Security Deposit & Escrow",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "Commitment Tracking & Transaction Ledger",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandPrimaryGreenDim,
                    border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            text = "RBI Escrow",
                            color = BrandPrimaryGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Hero Escrow Balance Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("escrow_balance_hero_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = "CURRENT BALANCE IN ESCROW",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${worker.suretyEscrowBalance.toInt()}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 36.sp
                                )
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = BrandCardDark,
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = BrandSecondaryCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Balance Breakdown Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Available
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandCardDark,
                            border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BrandPrimaryGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Available",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${availableBalance.toInt()}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Locked in Shifts
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandCardDark,
                            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(BrandAmber)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Locked (Holds)",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "₹${worker.lockedSuretyAmount.toInt()}",
                                    color = BrandAmber,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenTopUpDialog,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandSecondaryCyan,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_escrow_add_funds")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Funds", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenTopUpDialog,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("btn_escrow_manage")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Manage / Withdraw", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Platform Commitments Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_platform_commitments")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Platform Commitments",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreenDim
                        ) {
                            Text(
                                text = "100% Reliable",
                                color = BrandPrimaryGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (worker.lockedSuretyAmount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandSurfaceDark,
                            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Shift Commitment Hold",
                                        color = BrandAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "₹${worker.lockedSuretyAmount.toInt()} Locked",
                                        color = BrandAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "VIP Event Host & Usher • Apex Global Summit",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = BrandSecondaryCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Auto-Refund Condition: Full ₹100 released upon GPS/QR check-in",
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No active shift holds currently. All ₹${worker.suretyEscrowBalance.toInt()} is available to book upcoming shifts without delay.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Commitment Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        CommitmentStat(
                            title = "Shift Capacity",
                            value = "${(worker.suretyEscrowBalance / 100).toInt()} Shifts",
                            subtitle = "₹100/shift backed"
                        )
                        CommitmentStat(
                            title = "No-Show Record",
                            value = "0 Defaults",
                            subtitle = "Clean Record"
                        )
                        CommitmentStat(
                            title = "On-Time Score",
                            value = "${worker.onTimeRate}%",
                            subtitle = "47 Verifications"
                        )
                    }
                }
            }
        }

        // Past Security Deposit Transactions Header & Filters
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Past Escrow Transactions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "${filteredTransactions.size} Records",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterPill(
                            label = "All (${transactions.size})",
                            isSelected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            tag = "filter_escrow_all"
                        )
                    }
                    item {
                        FilterPill(
                            label = "Shift Holds (${transactions.count { it.type == "LOCKED" }})",
                            isSelected = selectedFilter == "HOLDS",
                            onClick = { selectedFilter = "HOLDS" },
                            tag = "filter_escrow_holds"
                        )
                    }
                    item {
                        FilterPill(
                            label = "Releases & Refunds (${transactions.count { it.type == "UNLOCKED_REFUNDED" }})",
                            isSelected = selectedFilter == "RELEASES",
                            onClick = { selectedFilter = "RELEASES" },
                            tag = "filter_escrow_releases"
                        )
                    }
                    item {
                        FilterPill(
                            label = "Deposits (${transactions.count { it.type == "DEPOSIT" }})",
                            isSelected = selectedFilter == "DEPOSITS",
                            onClick = { selectedFilter = "DEPOSITS" },
                            tag = "filter_escrow_deposits"
                        )
                    }
                }
            }
        }

        // Transactions List
        if (filteredTransactions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandCardDark,
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No transactions found for this filter",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(filteredTransactions, key = { it.id }) { txn ->
                SuretyTransactionItemCard(txn = txn)
            }
        }

        // Surety Policy & Rules Accordion
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = BrandSecondaryCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DailyCrew Escrow & Surety Policy",
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    PolicyBullet(
                        title = "₹100 Worker Commitment Hold",
                        description = "Held temporarily when you accept a shift slot. Prevents flaky attendance so venues can rely on you."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PolicyBullet(
                        title = "Instant 100% Release upon Arrival",
                        description = "The moment you check in via GPS or QR code at the venue, the ₹100 is immediately released back to your available balance."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PolicyBullet(
                        title = "₹300 Owner Protection Escrow",
                        description = "Venues also lock ₹300 per job. If an owner cancels with short notice (<2 hours), platform escrow pays you compensation."
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun CommitmentStat(
    title: String,
    value: String,
    subtitle: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark,
                fontSize = 10.sp
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall.copy(
                color = BrandSecondaryCyan,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun FilterPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) BrandSecondaryCyan else BrandCardDark,
        border = BorderStroke(
            1.dp,
            if (isSelected) BrandSecondaryCyan else BrandCardBorderDark
        ),
        onClick = onClick,
        modifier = Modifier.testTag(tag)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.Black else TextSecondaryDark,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SuretyTransactionItemCard(
    txn: SuretyTransaction,
    modifier: Modifier = Modifier
) {
    val (icon, iconTint, amountColor, prefix, typeLabel) = when (txn.type) {
        "LOCKED" -> Quintuple(
            Icons.Default.Lock,
            BrandAmber,
            BrandAmber,
            "-",
            "SHIFT HOLD"
        )
        "UNLOCKED_REFUNDED" -> Quintuple(
            Icons.Default.CheckCircle,
            BrandPrimaryGreen,
            BrandPrimaryGreen,
            "+",
            "HOLD RELEASED"
        )
        "DEPOSIT" -> Quintuple(
            Icons.Default.AccountBalanceWallet,
            BrandSecondaryCyan,
            BrandSecondaryCyan,
            "+",
            "TOP-UP DEPOSIT"
        )
        "WITHDRAWAL" -> Quintuple(
            Icons.Default.ArrowOutward,
            Color.White,
            Color.White,
            "-",
            "WITHDRAWAL"
        )
        else -> Quintuple(
            Icons.Default.Warning,
            Color.Red,
            Color.Red,
            "-",
            "FORFEITED"
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = modifier
            .fillMaxWidth()
            .testTag("surety_transaction_card_${txn.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = iconTint.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = txn.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "$prefix₹${txn.amount.toInt()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = amountColor,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = txn.relatedJobTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandSecondaryCyan,
                            fontSize = 11.sp
                        )
                    )
                    Text(
                        text = txn.date,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 10.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = txn.note,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = iconTint.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = typeLabel,
                        color = iconTint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyBullet(
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = BrandPrimaryGreen,
            modifier = Modifier
                .size(14.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                color = TextSecondaryDark,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

private data class Quintuple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
