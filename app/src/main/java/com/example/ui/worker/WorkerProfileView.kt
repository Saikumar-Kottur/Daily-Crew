package com.example.ui.worker

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.model.SuretyTransaction
import com.example.data.model.WorkerProfile
import com.example.data.model.WorkerShiftHistoryItem
import com.example.data.model.WorkerSkillBadge
import com.example.ui.components.TrustBadgePill
import com.example.ui.theme.*
import com.example.util.DailyCrewLocalization

@Composable
fun WorkerProfileView(
    worker: WorkerProfile,
    suretyTransactions: List<SuretyTransaction> = emptyList(),
    selectedLanguage: AppLanguage = worker.selectedLanguage,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onToggleStandby: () -> Unit = {},
    onOpenSuretyDialog: () -> Unit = {},
    onOpenVerificationDialog: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showDigitalIdDialog by remember { mutableStateOf(false) }
    var isViewingEscrowSubView by remember { mutableStateOf(false) }

    BackHandler(enabled = isViewingEscrowSubView) {
        isViewingEscrowSubView = false
    }

    if (isViewingEscrowSubView) {
        SecurityDepositEscrowView(
            worker = worker,
            transactions = suretyTransactions,
            onBack = { isViewingEscrowSubView = false },
            onOpenTopUpDialog = onOpenSuretyDialog
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("worker_profile_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Hero Identity & Trust Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worker_profile_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar with Online / Standby Glow & Verified Badge
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(BrandPrimaryGreen, BrandSecondaryCyan)
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(BrandSurfaceDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = worker.avatarInitials,
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BrandPrimaryGreen
                                    )
                                )
                            }
                        }

                        // Verified Green Check Badge
                        Surface(
                            shape = CircleShape,
                            color = BrandPrimaryGreen,
                            border = BorderStroke(2.dp, BrandSurfaceDark),
                            modifier = Modifier.size(26.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified Identity",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = worker.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${worker.workerIdTag} • Member since ${worker.memberSince}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = BrandSecondaryCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = worker.currentZone,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trust Badge Pill (Tier & Score)
                    TrustBadgePill(tier = worker.badgeTier, score = worker.trustScore)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Standby Mode Status & Quick Toggle
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (worker.isAvailableNow) BrandPrimaryGreenDim else BrandSurfaceDark,
                        border = BorderStroke(
                            1.dp,
                            if (worker.isAvailableNow) BrandPrimaryGreen else BrandCardBorderDark
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onToggleStandby() }
                            .testTag("worker_profile_standby_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (worker.isAvailableNow) BrandPrimaryGreen else TextSecondaryDark,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (worker.isAvailableNow) "⚡ Standby Active: Available for Instant Shifts" else "Standby Mode Paused (Tap to activate)",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (worker.isAvailableNow) BrandPrimaryGreen else TextSecondaryDark,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metrics Row (Rating, Jobs, On-Time, Trust)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem(valStr = "⭐ ${worker.rating}", label = DailyCrewLocalization.get("metric_rating", selectedLanguage))
                            MetricItem(valStr = "${worker.completedJobs}", label = DailyCrewLocalization.get("metric_shifts_done", selectedLanguage))
                            MetricItem(valStr = "⏱️ ${worker.onTimeRate}%", label = DailyCrewLocalization.get("metric_ontime", selectedLanguage))
                            MetricItem(valStr = "${worker.trustScore}/100", label = DailyCrewLocalization.get("metric_trust_score", selectedLanguage))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector at Profile Section (Telugu, Hindi, English)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_language_quick_selector")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Translate,
                                        contentDescription = "Language",
                                        tint = BrandPrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = when (selectedLanguage) {
                                            AppLanguage.TELUGU -> "భాష ఎంచుకోండి (Language)"
                                            AppLanguage.HINDI -> "भाषा चुनें (Language)"
                                            AppLanguage.ENGLISH -> "Language / భాష / भाषा"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrandPrimaryGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(0.5.dp, BrandPrimaryGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = selectedLanguage.nativeName,
                                        color = BrandPrimaryGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AppLanguage.values().forEach { lang ->
                                    val isSelected = selectedLanguage == lang
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) BrandPrimaryGreen else BrandSurfaceDark,
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) BrandPrimaryGreen else BrandCardBorderDark
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onSelectLanguage(lang) }
                                            .testTag("quick_lang_btn_${lang.code}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${lang.flagEmoji} ${lang.nativeName}",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.Black else Color.White,
                                                fontSize = 12.sp,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = lang.displayName,
                                                color = if (isSelected) Color.Black.copy(alpha = 0.7f) else TextSecondaryDark,
                                                fontSize = 10.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Button: View Verified Digital ID Pass
                    OutlinedButton(
                        onClick = { showDigitalIdDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandPrimaryGreen),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandPrimaryGreen
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_view_digital_id_pass")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "View Verified Digital ID Pass",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Button: View Security Deposit & Escrow Ledger
                    OutlinedButton(
                        onClick = { isViewingEscrowSubView = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSecondaryCyan),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandSecondaryCyan
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_profile_surety_escrow")
                    ) {
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
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = BrandSecondaryCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Security Deposit & Escrow Ledger",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "₹${worker.suretyEscrowBalance.toInt()} in Escrow • ${if (worker.lockedSuretyAmount > 0) "₹${worker.lockedSuretyAmount.toInt()} Active Hold" else "All Available"}",
                                        fontSize = 11.sp,
                                        color = BrandSecondaryCyan
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section Tabs: [0: Verification Status], [1: Work History], [2: Skill Badges & XP], [3: Escrow Ledger]
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = BrandCardDark,
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabPill(
                        title = DailyCrewLocalization.get("tab_verification", selectedLanguage),
                        icon = Icons.Default.VerifiedUser,
                        isSelected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_worker_verification")
                    )
                    TabPill(
                        title = DailyCrewLocalization.get("tab_history", selectedLanguage),
                        icon = Icons.Default.WorkHistory,
                        isSelected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_worker_history")
                    )
                    TabPill(
                        title = DailyCrewLocalization.get("tab_badges", selectedLanguage),
                        icon = Icons.Default.Stars,
                        isSelected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_worker_badges")
                    )
                    TabPill(
                        title = DailyCrewLocalization.get("tab_escrow", selectedLanguage),
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_worker_escrow_ledger")
                    )
                    TabPill(
                        title = DailyCrewLocalization.get("tab_settings", selectedLanguage),
                        icon = Icons.Default.Settings,
                        isSelected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_worker_settings")
                    )
                }
            }
        }

        // TAB CONTENT
        when (selectedTab) {
            0 -> {
                // VERIFICATION STATUS TAB
                item {
                    VerificationTabContent(
                        worker = worker,
                        onOpenVerificationDialog = onOpenVerificationDialog
                    )
                }
            }
            1 -> {
                // WORK HISTORY TAB
                item {
                    WorkHistoryTabContent(worker = worker)
                }
            }
            2 -> {
                // SKILL BADGES & XP TAB
                item {
                    SkillBadgesTabContent(worker = worker)
                }
            }
            3 -> {
                // SECURITY DEPOSIT & ESCROW LEDGER TAB
                item {
                    EscrowTabInlineContent(
                        worker = worker,
                        transactions = suretyTransactions,
                        onOpenFullSubView = { isViewingEscrowSubView = true },
                        onOpenSuretyDialog = onOpenSuretyDialog
                    )
                }
            }
            4 -> {
                // SETTINGS & LANGUAGE TAB
                item {
                    SettingsTabContent(
                        worker = worker,
                        selectedLanguage = selectedLanguage,
                        onSelectLanguage = onSelectLanguage
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
    }

    // Official Verified Digital ID Pass Modal
    if (showDigitalIdDialog) {
        VerifiedDigitalIdDialog(
            worker = worker,
            onDismiss = { showDigitalIdDialog = false }
        )
    }
}

// ---------------- SUB-COMPONENTS & TABS ----------------

@Composable
private fun MetricItem(valStr: String, label: String) {
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
private fun TabPill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandPrimaryGreen else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else TextSecondaryDark,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.Black else TextSecondaryDark,
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }
    }
}

// ---------------- TAB 0: VERIFICATION STATUS ----------------

@Composable
private fun VerificationTabContent(
    worker: WorkerProfile,
    onOpenVerificationDialog: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Room Database Verification Live Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(
                1.5.dp,
                if (worker.isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (worker.isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (worker.isVerifiedInDb) Icons.Default.Verified else Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (worker.isVerifiedInDb) "DATABASE VERIFIED" else "PENDING ID UPLOAD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (worker.isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "DailyCrew Room SQLite Database",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryGreen.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (worker.isVerifiedInDb) "Active in DB" else "Upload Required",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Primary Document", fontSize = 10.sp, color = TextSecondaryDark)
                        Text(text = "${worker.verifiedDocumentType} (${worker.aadhaarMasked})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Database Record ID", fontSize = 10.sp, color = TextSecondaryDark)
                        Text(
                            text = worker.verifiedDbRecordId,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = onOpenVerificationDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryGreen,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_worker_upload_verification")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (worker.isVerifiedInDb) "View / Update Identification Documents" else "Upload Identification Details to DB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Trust Shield Assurance Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = BrandPrimaryGreenDim,
            border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(BrandPrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "100% Verified Workforce Guarantee",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "All 5 identity checkpoints authenticated with zero negative records on national registries.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandPrimaryGreen,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }

        Text(
            text = "Identity & Security Checkpoints",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // 5 Levels of Verification
        VerificationCheckItem(
            levelNumber = "1",
            title = "Phone OTP Verification",
            detail = "${worker.phone} (Verified via 6-digit cryptographic OTP)",
            isVerified = worker.isPhoneVerified,
            issuedBy = "Telecom Carrier Gateway"
        )

        VerificationCheckItem(
            levelNumber = "2",
            title = "Google Account / SSO Authentication",
            detail = "${worker.email} (Linked enterprise identity)",
            isVerified = worker.isGoogleVerified,
            issuedBy = "Google OAuth2 SSO"
        )

        VerificationCheckItem(
            levelNumber = "3",
            title = "Government ID (UIDAI Aadhaar / PAN)",
            detail = "Aadhaar Card: ${worker.aadhaarMasked} (Biometrics Verified)",
            isVerified = worker.isAadhaarVerified,
            issuedBy = "UIDAI National Registry"
        )

        VerificationCheckItem(
            levelNumber = "4",
            title = "State Police Background Clearance",
            detail = "Certificate #${worker.policeVerificationId} (Zero Criminal History)",
            isVerified = worker.isPoliceVerified,
            issuedBy = "Telangana State Police Department"
        )

        VerificationCheckItem(
            levelNumber = "5",
            title = "Live Biometric Face Liveness",
            detail = "3D Liveness Selfie Confirmed (${worker.faceMatchConfidence}% match confidence)",
            isVerified = worker.isSelfieVerified,
            issuedBy = "DailyCrew AI Vision Engine"
        )

        VerificationCheckItem(
            levelNumber = "6",
            title = "SQLite Room Database Integrity Record",
            detail = "Table: verification_records • ID: ${worker.verifiedDbRecordId} (${if (worker.isVerifiedInDb) "Verified & Synchronized" else "Pending Upload"})",
            isVerified = worker.isVerifiedInDb,
            issuedBy = "DailyCrew Room SQLite DB"
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Platform Trust & Anti-No-Show Safeguards",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TrustFeatureRow(
                    icon = Icons.Default.GpsFixed,
                    title = "GPS Geofence Shift Check-In",
                    desc = "Check-in only permitted within 100 meters of registered job premises."
                )
                HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)
                TrustFeatureRow(
                    icon = Icons.Default.ThumbUp,
                    title = "Zero Shift Abandonment Record",
                    desc = "Maintained 100% completion across all 47 accepted shifts over 12 months."
                )
                HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)
                TrustFeatureRow(
                    icon = Icons.Default.CurrencyRupee,
                    title = "Instant Escrow Payout Entitlement",
                    desc = "High trust score enables automated UPI release upon supervisor QR checkout."
                )
            }
        }
    }
}

@Composable
private fun VerificationCheckItem(
    levelNumber: String,
    title: String,
    detail: String,
    isVerified: Boolean,
    issuedBy: String
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, if (isVerified) BrandPrimaryGreen.copy(alpha = 0.4f) else BrandCardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isVerified) BrandPrimaryGreen else BrandSurfaceDark,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "L$levelNumber",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isVerified) Color.Black else TextSecondaryDark,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = "Authority: $issuedBy",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandSecondaryCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isVerified) BrandPrimaryGreenDim else BrandSurfaceDark,
                border = BorderStroke(1.dp, if (isVerified) BrandPrimaryGreen else TextSecondaryDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (isVerified) BrandPrimaryGreen else TextSecondaryDark,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isVerified) "VERIFIED" else "PENDING",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isVerified) BrandPrimaryGreen else TextSecondaryDark
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TrustFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = BrandSecondaryCyan,
            modifier = Modifier
                .size(18.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
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

// ---------------- TAB 1: WORK HISTORY ----------------

@Composable
private fun WorkHistoryTabContent(worker: WorkerProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Workforce Track Record",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TrackRecordStat(label = "Total Hours", value = "${worker.totalHoursWorked} hrs")
                    TrackRecordStat(label = "Completed", value = "${worker.completedJobs} Shifts")
                    TrackRecordStat(label = "Re-Hire Rate", value = "${worker.repeatHireRate}%")
                    TrackRecordStat(label = "No-Shows", value = "0")
                }
            }
        }

        Text(
            text = "Completed Shifts & Supervisor Reviews",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        worker.workHistory.forEach { item ->
            WorkHistoryCard(item = item)
        }
    }
}

@Composable
private fun TrackRecordStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = BrandPrimaryGreen
            )
        )
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
private fun WorkHistoryCard(item: WorkerShiftHistoryItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("work_history_item_${item.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Role title + Payout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.roleTitle,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "${item.businessName} • ${item.category}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandSecondaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandPrimaryGreenDim,
                    border = BorderStroke(1.dp, BrandPrimaryGreen)
                ) {
                    Text(
                        text = "₹${item.earningsAmount.toInt()}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Shift Details (Date, duration, location)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${item.date} • ${item.shiftDuration}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = TextSecondaryDark,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = item.location,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Supervisor Review Quote Card
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BrandSurfaceDark,
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "⭐ ${item.ratingGiven}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BrandAmber
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Supervisor Review",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreenDim
                        ) {
                            Text(
                                text = item.payoutStatus,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandPrimaryGreen,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "\"${item.supervisorReview}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Verified Strengths Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.keyStrengths.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandSecondaryCyanDim,
                        border = BorderStroke(0.5.dp, BrandSecondaryCyan.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "✓ $tag",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandSecondaryCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ---------------- TAB 2: SKILL BADGES & XP ----------------

@Composable
private fun SkillBadgesTabContent(worker: WorkerProfile) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Gamification Level & XP Progress Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏆 Level 8 Elite Crew",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                        Text(
                            text = "${worker.xpPoints} / 4,000 XP (550 XP to Level 9 Master)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 11.sp
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandAmberDim,
                        border = BorderStroke(1.dp, BrandAmber)
                    ) {
                        Text(
                            text = "🔥 ${worker.streakDays}-Day Streak",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // XP Progress Bar
                LinearProgressIndicator(
                    progress = { worker.xpPoints / 4000f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = BrandAmber,
                    trackColor = BrandSurfaceDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active Elite Perks
                Text(
                    text = "Unlocked Platinum Elite Privileges:",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ElitePerkChip("⚡ Instant Auto-Hire")
                    ElitePerkChip("💸 0% Service Fee")
                    ElitePerkChip("🚀 Priority Dispatch")
                }
            }
        }

        Text(
            text = "Accredited Skill Badges & Endorsements",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // List of Skill Badges
        worker.skillBadgesList.forEach { badge ->
            SkillBadgeCard(badge = badge)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Platform Achievement Medals",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        // Achievement Medals Row / Grid
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AchievementMedalTile(
                emoji = "⚡",
                title = "Fast Responder",
                desc = "Acknowledged and confirmed standby shifts in under 3 minutes.",
                awardedTag = "Top 3% Speed"
            )
            AchievementMedalTile(
                emoji = "🛡️",
                title = "Zero No-Show Guarantee",
                desc = "Completed 47 out of 47 bookings without a single cancellation.",
                awardedTag = "100% Attendance"
            )
            AchievementMedalTile(
                emoji = "🌟",
                title = "Top 5% Hospitality Star",
                desc = "Maintained 4.85+ star employer satisfaction over past 6 months.",
                awardedTag = "Elite Tier"
            )
            AchievementMedalTile(
                emoji = "⏱️",
                title = "Punctual Master",
                desc = "Checked in within geofence 10+ minutes prior to shift start 44 times.",
                awardedTag = "98% Punctual"
            )
        }
    }
}

@Composable
private fun ElitePerkChip(title: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = BrandPrimaryGreenDim,
        border = BorderStroke(0.5.dp, BrandPrimaryGreen.copy(alpha = 0.5f))
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                color = BrandPrimaryGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun SkillBadgeCard(badge: WorkerSkillBadge) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, if (badge.isAccredited) BrandPrimaryGreen.copy(alpha = 0.4f) else BrandCardBorderDark),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("skill_badge_card_${badge.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BrandSurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    if (badge.isAccredited) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Accredited",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Text(
                    text = "${badge.level} • ${badge.category}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandSecondaryCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = "Endorsed by: ${badge.verifiedBy}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 10.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandSurfaceDark,
                    border = BorderStroke(1.dp, BrandCardBorderDark)
                ) {
                    Text(
                        text = "👍 ${badge.endorsements}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = badge.dateAwarded,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun AchievementMedalTile(
    emoji: String,
    title: String,
    desc: String,
    awardedTag: String
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
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
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
                        color = BrandAmberDim
                    ) {
                        Text(
                            text = awardedTag,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
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

// ---------------- OFFICIAL VERIFIED DIGITAL ID PASS DIALOG ----------------

@Composable
private fun VerifiedDigitalIdDialog(
    worker: WorkerProfile,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandSurfaceDark,
            border = BorderStroke(2.dp, BrandPrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("verified_digital_id_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Brand & Security Ribbon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreen,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "DC",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp,
                                    color = Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DAILYCREW SECURE PASS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.sp
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Official Government & Police Verification Header
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandPrimaryGreenDim,
                    border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GOVERNMENT & POLICE VERIFIED WORKER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimaryGreen,
                                fontSize = 10.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar & Identification
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(BrandPrimaryGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = worker.avatarInitials,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = worker.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "ID: ${worker.workerIdTag}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandSecondaryCyan,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Verification Stamp Checklist
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandCardDark,
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IdStampRow("UIDAI Aadhaar Linked", worker.aadhaarMasked, true)
                        IdStampRow("Police Clearance", worker.policeVerificationId, true)
                        IdStampRow("Biometric Face Match", "${worker.faceMatchConfidence}% Confidence", true)
                        IdStampRow("Trust Rating", "${worker.trustScore}/100 Platinum", true)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Holographic QR Code Box
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    modifier = Modifier
                        .size(130.dp)
                        .padding(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "Security QR Code",
                                tint = Color.Black,
                                modifier = Modifier.size(90.dp)
                            )
                            Text(
                                text = "SCAN TO VERIFY",
                                color = Color.Black,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Present this digital pass to site supervisor upon venue check-in.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Done",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun IdStampRow(label: String, value: String, isPassed: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (isPassed) BrandPrimaryGreen else TextSecondaryDark,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color.White,
                    fontSize = 11.sp
                )
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = BrandPrimaryGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        )
    }
}

@Composable
private fun EscrowTabInlineContent(
    worker: WorkerProfile,
    transactions: List<SuretyTransaction>,
    onOpenFullSubView: () -> Unit,
    onOpenSuretyDialog: () -> Unit
) {
    val availableBalance = (worker.suretyEscrowBalance - worker.lockedSuretyAmount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Escrow Balance Summary Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f)),
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
                            text = "CURRENT ESCROW BALANCE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondaryDark,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "₹${worker.suretyEscrowBalance.toInt()}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandPrimaryGreenDim
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
                                text = "100% Escrow Backed",
                                color = BrandPrimaryGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Available Balance",
                                color = TextSecondaryDark,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "₹${availableBalance.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.3f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Locked (Holds)",
                                color = TextSecondaryDark,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "₹${worker.lockedSuretyAmount.toInt()}",
                                color = BrandAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenFullSubView,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandSecondaryCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_open_full_escrow_subview")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "View Full Ledger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenSuretyDialog,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_escrow_topup_inline")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Add / Manage",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Platform Commitment Status
        Card(
            shape = RoundedCornerShape(14.dp),
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
                    Text(
                        text = "Platform Commitment Status",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BrandPrimaryGreenDim
                    ) {
                        Text(
                            text = "Active Commitment",
                            color = BrandPrimaryGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (worker.lockedSuretyAmount > 0) {
                    Text(
                        text = "• ₹${worker.lockedSuretyAmount.toInt()} committed for VIP Event Host & Usher (Apex Global Summit). Releases immediately upon GPS check-in.",
                        color = BrandAmber,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                } else {
                    Text(
                        text = "• No shift commitments locked. Full escrow reserve is active and available.",
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "• On-Time Check-In Guarantee: ${worker.onTimeRate}% with 0 recorded no-shows.",
                    color = BrandSecondaryCyan,
                    fontSize = 11.sp
                )
            }
        }

        // Recent Transactions Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Past Escrow Transactions",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            TextButton(onClick = onOpenFullSubView) {
                Text(
                    text = "View All (${transactions.size})",
                    color = BrandSecondaryCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Recent transaction items
        transactions.take(3).forEach { txn ->
            SuretyTransactionItemCard(txn = txn)
        }
    }
}

// ---------------- TAB 4: SETTINGS & LANGUAGE ----------------

@Composable
private fun SettingsTabContent(
    worker: WorkerProfile,
    selectedLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    var smsAlertsEnabled by remember { mutableStateOf(true) }
    var pushAlertsEnabled by remember { mutableStateOf(true) }
    var emergencySirenEnabled by remember { mutableStateOf(true) }
    var selectedRadiusKm by remember { mutableFloatStateOf(10f) }
    var upiIdInput by remember { mutableStateOf("ramesh@oksbi") }
    var isSavedNotification by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("worker_settings_tab_content"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Language Card (Featured at top of Settings)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.5.dp, BrandPrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("card_language_settings")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandPrimaryGreen.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = DailyCrewLocalization.get("language_settings_title", selectedLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = DailyCrewLocalization.get("language_settings_subtitle", selectedLanguage),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryGreenDim,
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "${selectedLanguage.flagEmoji} ${selectedLanguage.nativeName}",
                            color = BrandPrimaryGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // The 3 Language Cards: English, Telugu (తెలుగు), Hindi (हिन्दी)
                AppLanguage.values().forEach { lang ->
                    val isSelected = selectedLanguage == lang
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) BrandCardDark else BrandBackgroundDark,
                        border = BorderStroke(
                            width = if (isSelected) 1.8.dp else 1.dp,
                            color = if (isSelected) BrandPrimaryGreen else BrandCardBorderDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectLanguage(lang) }
                            .testTag("lang_selection_card_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectLanguage(lang) },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = BrandPrimaryGreen,
                                        unselectedColor = TextSecondaryDark
                                    )
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = lang.nativeName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) BrandPrimaryGreen else Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = BrandSurfaceDark
                                        ) {
                                            Text(
                                                text = lang.displayName,
                                                color = TextSecondaryDark,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = lang.scriptSample,
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            if (isSelected) {
                                Surface(
                                    shape = CircleShape,
                                    color = BrandPrimaryGreen,
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color.Black,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Shift & Payout Notification Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = DailyCrewLocalization.get("section_notifications", selectedLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // SMS Alert Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DailyCrewLocalization.get("opt_sms_alerts", selectedLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Receive direct SMS to ${worker.phone} when new shifts match your skills",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = smsAlertsEnabled,
                        onCheckedChange = { smsAlertsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BrandPrimaryGreen
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BrandCardBorderDark, thickness = 0.5.dp)

                // Push Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DailyCrewLocalization.get("opt_push_notifs", selectedLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Priority dispatch notifications for ₹800+ banquet and catering gigs",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = pushAlertsEnabled,
                        onCheckedChange = { pushAlertsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BrandPrimaryGreen
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BrandCardBorderDark, thickness = 0.5.dp)

                // Emergency Siren Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = DailyCrewLocalization.get("opt_emergency_sound", selectedLanguage),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = "Play audible siren for 30-min urgent restaurant replacements (+₹150 bonus)",
                            fontSize = 10.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Switch(
                        checked = emergencySirenEnabled,
                        onCheckedChange = { emergencySirenEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = BrandAmber
                        )
                    )
                }
            }
        }

        // Work Territory & GPS Commute Radius
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = DailyCrewLocalization.get("section_territory", selectedLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${DailyCrewLocalization.get("label_pilot_zone", selectedLanguage)}: ${worker.currentZone}",
                        fontSize = 12.sp,
                        color = BrandSecondaryCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "${DailyCrewLocalization.get("label_travel_radius", selectedLanguage)}: ${selectedRadiusKm.toInt()} km",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5f, 10f, 15f, 25f).forEach { radius ->
                        val isRadSel = selectedRadiusKm == radius
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isRadSel) BrandSecondaryCyan else BrandCardDark,
                            border = BorderStroke(1.dp, if (isRadSel) BrandSecondaryCyan else BrandCardBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedRadiusKm = radius }
                        ) {
                            Text(
                                text = "${radius.toInt()} km",
                                color = if (isRadSel) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bank UPI Payout Account Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = DailyCrewLocalization.get("section_payout", selectedLanguage),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "All instant escrow earnings & tips will be disbursed directly via UPI 2.0 rails.",
                    fontSize = 11.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = upiIdInput,
                    onValueChange = { upiIdInput = it },
                    label = { Text(DailyCrewLocalization.get("label_upi_id", selectedLanguage)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = BrandPrimaryGreen
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_worker_settings_upi")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isSavedNotification = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_save_worker_settings")
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = DailyCrewLocalization.get("btn_save_preferences", selectedLanguage),
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                if (isSavedNotification) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryGreenDim,
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DailyCrewLocalization.get("settings_saved_success", selectedLanguage),
                                color = BrandPrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}


