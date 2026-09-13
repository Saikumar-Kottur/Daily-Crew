package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.DailyCrewLocalization
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyCrewTopBar(
    currentRole: AppRole,
    onRoleChange: (AppRole) -> Unit,
    unreadNotifCount: Int = 2,
    onNotifClick: () -> Unit,
    onAiInsightsClick: () -> Unit,
    onReferClick: (() -> Unit)? = null,
    onVerificationClick: (() -> Unit)? = null,
    onLogoClick: (() -> Unit)? = null,
    onLogout: () -> Unit = {}
) {
    val isStaffRole = currentRole == AppRole.ADMIN || currentRole == AppRole.SUPER_ADMIN

    Surface(
        color = BrandSurfaceDark,
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Reusable Official DailyCrew AppLogo Widget
            AppLogo(
                size = AppLogoSize.COMPACT,
                showText = true,
                showTagline = true,
                onClick = onLogoClick,
                modifier = Modifier.testTag("brand_badge_logo")
            )

            // Role Badge & Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // ID Verification Status & Upload Button
                if (onVerificationClick != null && !isStaffRole) {
                    IconButton(
                        onClick = onVerificationClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("verification_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "ID Verification & Database Status",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Refer & Earn Button
                if (onReferClick != null) {
                    IconButton(
                        onClick = onReferClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("refer_earn_topbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = "Refer & Earn",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // AI Insights button (for public roles)
                if (!isStaffRole) {
                    IconButton(
                        onClick = onAiInsightsClick,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("ai_insights_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Matching & Insights",
                            tint = BrandAmber,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Notification Bell
                    BadgedBox(
                        badge = {
                            if (unreadNotifCount > 0) {
                                Badge(
                                    containerColor = BrandPrimaryGreen,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = "$unreadNotifCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = onNotifClick,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("notification_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsNone,
                                contentDescription = "Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))
                }

                // Role Toggle Chip with Clean Public/Staff Context Menu
                var roleMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentRole) {
                            AppRole.WORKER -> BrandPrimaryGreenDim
                            AppRole.OWNER -> BrandSecondaryCyanDim
                            AppRole.ADMIN -> BrandRedDim
                            AppRole.SUPER_ADMIN -> BrandAmberDim
                        },
                        border = BorderStroke(
                            1.dp,
                            when (currentRole) {
                                AppRole.WORKER -> BrandPrimaryGreen
                                AppRole.OWNER -> BrandSecondaryCyan
                                AppRole.ADMIN -> BrandRed
                                AppRole.SUPER_ADMIN -> BrandAmber
                            }
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { roleMenuExpanded = true }
                            .testTag("role_switch_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentRole) {
                                    AppRole.WORKER -> "👷 Worker Mode"
                                    AppRole.OWNER -> "🏪 Employer Mode"
                                    AppRole.ADMIN -> "🛡️ Staff Admin"
                                    AppRole.SUPER_ADMIN -> "⚙️ Super Admin"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentRole) {
                                        AppRole.WORKER -> BrandPrimaryGreen
                                        AppRole.OWNER -> BrandSecondaryCyan
                                        AppRole.ADMIN -> BrandRed
                                        AppRole.SUPER_ADMIN -> BrandAmber
                                    },
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "User Menu",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                        modifier = Modifier.background(BrandSurfaceDark)
                    ) {
                        if (!isStaffRole) {
                            // PUBLIC USER MENU (Worker <-> Owner toggle, plus Logout)
                            if (currentRole == AppRole.WORKER) {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Storefront,
                                                contentDescription = null,
                                                tint = BrandSecondaryCyan,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Switch to Hiring Mode",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        roleMenuExpanded = false
                                        onRoleChange(AppRole.OWNER)
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Engineering,
                                                contentDescription = null,
                                                tint = BrandPrimaryGreen,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                "Switch to Worker Mode",
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        roleMenuExpanded = false
                                        onRoleChange(AppRole.WORKER)
                                    }
                                )
                            }

                            Divider(color = BrandCardBorderDark, modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = null,
                                            tint = BrandRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Log Out",
                                            color = BrandRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    onLogout()
                                }
                            )
                        } else {
                            // STAFF / ADMIN INTERNAL PORTAL MENU
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "🛡️ Dispute Moderation Center",
                                        color = BrandSecondaryCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    onRoleChange(AppRole.ADMIN)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "⚙️ Super Admin Platform Engine",
                                        color = BrandAmber,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    onRoleChange(AppRole.SUPER_ADMIN)
                                }
                            )

                            Divider(color = BrandCardBorderDark, modifier = Modifier.padding(vertical = 4.dp))

                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ExitToApp,
                                            contentDescription = null,
                                            tint = BrandRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Exit Staff Portal & Sign Out",
                                            color = BrandRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrustBadgePill(tier: BadgeTier, score: Int, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(tier.badgeColorHex).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color(tier.badgeColorHex)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = tier.title,
                tint = Color(tier.badgeColorHex),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${tier.title} • $score/100",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(tier.badgeColorHex),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun JobDetailsDialog(
    job: JobPosting,
    onDismiss: () -> Unit,
    onApply: (String) -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("job_details_dialog")
        ) {
            LazyColumn(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreenDim
                        ) {
                            Text(
                                text = job.category.label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BrandPrimaryGreen,
                                    fontWeight = FontWeight.Bold
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondaryDark
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = job.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = job.businessName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BrandSecondaryCyan,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Business",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "⭐ ${job.rating}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandAmber,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                item {
                    HorizontalDivider(color = BrandCardBorderDark)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "WAGE OFFERED",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${job.wage} ${job.unit}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = BrandPrimaryGreen,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CREW REQUIRED",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${job.workersHired}/${job.workersRequired} Filled",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("WORKER SURETY HOLD", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "₹${job.suretyRequired.toInt()} (Refunded on check-in)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = BrandAmber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("OWNER SURETY ESCROW", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Security, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "₹${job.securityDeposit.toInt()} Secured",
                                        style = MaterialTheme.typography.bodySmall.copy(color = BrandPrimaryGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = BrandSecondaryCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${job.date} • ${job.time}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = BrandAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${job.location} (${job.distanceKm} km away)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Dress Code Requirement",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandAmberDim,
                        border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checkroom,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = job.dressCode,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Supervisor Instructions",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = job.instructions,
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondaryDark)
                    )
                }

                item {
                    // Google Maps Navigation Intent Button
                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:0,0?q=${Uri.encode(job.location)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                // Fallback browser maps
                                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(job.location)}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("google_maps_nav_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BrandSecondaryCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Navigate",
                            tint = BrandSecondaryCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Navigate with Google Maps",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = BrandSecondaryCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                item {
                    // Apply Button
                    Button(
                        onClick = { onApply(job.id) },
                        enabled = !job.isApplied,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (job.isApplied) Color.Gray else BrandPrimaryGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("job_details_apply_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (job.isApplied) "Applied (Pending Owner Review)" else "ACCEPT SHIFT NOW",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInScannerDialog(
    onDismiss: () -> Unit,
    onConfirmCheckIn: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("check_in_scanner_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Venue Geo-Fence & QR Check-In",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive Radar Animation & QR Scanner Frame
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(2.dp, BrandPrimaryGreen, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Scanner",
                        tint = BrandPrimaryGreen,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BrandPrimaryGreenDim
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS",
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GPS Coordinates: 17.4156° N, 78.4357° E (Within 50m)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BrandPrimaryGreen,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Shift: Royal Feast Caterers • Banjara Hills\nTimestamp will be logged cryptographically to guarantee attendance score.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextSecondaryDark,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onConfirmCheckIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_check_in_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandPrimaryGreen,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.DoneAll, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONFIRM QR CHECK-IN",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
                    )
                }
            }
        }
    }
}

@Composable
fun WithdrawDialog(
    balance: Double,
    freePassesCount: Int = 0,
    commissionRatePercent: Double = 9.7,
    registeredUpiId: String = "ramesh@icici",
    defaultAccountHolder: String = "Ramesh Kumar",
    defaultAccountNumber: String = "5010042918234",
    defaultIfsc: String = "HDFC0001234",
    onReferClick: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (amount: Double, method: String, destination: String) -> Unit
) {
    // 0: UPI ID, 1: Bank Account
    var selectedMethodIndex by remember { mutableIntStateOf(0) }
    var amountText by remember { mutableStateOf("1000") }

    // UPI state
    var upiId by remember { mutableStateOf(registeredUpiId) }

    // Bank state
    var accountHolderName by remember { mutableStateOf(defaultAccountHolder) }
    var accountNumber by remember { mutableStateOf(defaultAccountNumber) }
    var confirmAccountNumber by remember { mutableStateOf(defaultAccountNumber) }
    var ifscCode by remember { mutableStateOf(defaultIfsc) }
    var isAccountRevealed by remember { mutableStateOf(false) }
    var isSavingsAccount by remember { mutableStateOf(true) }

    // Helper for IFSC Bank Name detection
    fun resolveBankName(ifsc: String): String {
        val clean = ifsc.trim().uppercase()
        return when {
            clean.startsWith("HDFC") -> "HDFC Bank"
            clean.startsWith("SBIN") -> "State Bank of India (SBI)"
            clean.startsWith("ICIC") -> "ICICI Bank"
            clean.startsWith("UTIB") || clean.startsWith("AXIS") -> "Axis Bank"
            clean.startsWith("PUNB") -> "Punjab National Bank"
            clean.startsWith("BARB") -> "Bank of Baroda"
            clean.startsWith("KKBK") -> "Kotak Mahindra Bank"
            clean.startsWith("YESB") -> "Yes Bank"
            clean.startsWith("IDFB") -> "IDFC FIRST Bank"
            clean.startsWith("CNRB") -> "Canara Bank"
            clean.length >= 4 -> "${clean.take(4)} Scheduled Bank"
            else -> "Indian Scheduled Bank"
        }
    }

    val withdrawAmount = amountText.toDoubleOrNull()
    val isValidAmount = withdrawAmount != null && withdrawAmount > 0.0 && withdrawAmount <= balance
    val isCommissionFree = freePassesCount > 0
    val commissionFee = if (isCommissionFree || withdrawAmount == null) 0.0 else (withdrawAmount * (commissionRatePercent / 100.0))
    val netDisbursed = if (withdrawAmount != null) (withdrawAmount - commissionFee).coerceAtLeast(0.0) else 0.0

    // Validation
    val isUpiValid = upiId.contains("@") && upiId.length >= 5
    val isBankMatch = accountNumber.isNotBlank() && accountNumber == confirmAccountNumber
    val isIfscValid = ifscCode.trim().length == 11
    val isBankValid = accountHolderName.isNotBlank() && accountNumber.length >= 9 && isBankMatch && isIfscValid

    val canProceed = isValidAmount && (if (selectedMethodIndex == 0) isUpiValid else isBankValid)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .heightIn(max = 700.dp)
                .padding(vertical = 12.dp)
                .testTag("withdraw_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Withdraw Earnings",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Instant 24x7 Escrow & Wallet Payout",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Available Balance Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandPrimaryGreenDim,
                    border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Available Wallet Balance",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                            )
                        }
                        Text(
                            text = "₹${balance.toInt()}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BrandPrimaryGreen
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Referral Commission Waiver Banner
                if (isCommissionFree) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandPrimaryGreenDim,
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🎉 Referral Waiver Pass Active: 0% Commission!",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimaryGreen
                                    )
                                )
                                Text(
                                    text = "$freePassesCount zero-commission withdrawals remaining. You save $commissionRatePercent%!",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.85f))
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandAmberDim,
                        border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = BrandAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "$commissionRatePercent% Commission Applied",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BrandAmber
                                        )
                                    )
                                    Text(
                                        text = "Refer 1 worker: Get 2 Free Withdrawals (Save $commissionRatePercent%) + 2.3% on their earnings!",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.8f))
                                    )
                                }
                            }
                            if (onReferClick != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = onReferClick,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandAmber,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("Refer & Save", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PAYOUT METHOD SELECTOR TABS
                Text(
                    text = "SELECT PAYOUT METHOD",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // UPI ID Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMethodIndex == 0) BrandPrimaryGreen.copy(alpha = 0.15f) else BrandSurfaceDark,
                        border = BorderStroke(
                            if (selectedMethodIndex == 0) 2.dp else 1.dp,
                            if (selectedMethodIndex == 0) BrandPrimaryGreen else BrandCardBorderDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedMethodIndex = 0 }
                            .testTag("tab_withdraw_upi")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = null,
                                tint = if (selectedMethodIndex == 0) BrandPrimaryGreen else TextSecondaryDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "UPI ID (VPA)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedMethodIndex == 0) BrandPrimaryGreen else Color.White
                            )
                            Text(
                                text = "Instant 0s Payout",
                                fontSize = 10.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }

                    // Bank Account Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selectedMethodIndex == 1) BrandSecondaryCyan.copy(alpha = 0.15f) else BrandSurfaceDark,
                        border = BorderStroke(
                            if (selectedMethodIndex == 1) 2.dp else 1.dp,
                            if (selectedMethodIndex == 1) BrandSecondaryCyan else BrandCardBorderDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedMethodIndex = 1 }
                            .testTag("tab_withdraw_bank")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = if (selectedMethodIndex == 1) BrandSecondaryCyan else TextSecondaryDark,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Bank Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedMethodIndex == 1) BrandSecondaryCyan else Color.White
                            )
                            Text(
                                text = "Direct IMPS Deposit",
                                fontSize = 10.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AMOUNT INPUT & QUICK CHIPS
                Text(
                    text = "WITHDRAWAL AMOUNT",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
                    label = { Text("Amount in Rupees (₹)") },
                    leadingIcon = {
                        Text(
                            text = "₹",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = BrandPrimaryGreen,
                            modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                        )
                    },
                    trailingIcon = {
                        if (amountText.isNotBlank()) {
                            IconButton(onClick = { amountText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    isError = withdrawAmount != null && (withdrawAmount <= 0 || withdrawAmount > balance),
                    supportingText = {
                        if (withdrawAmount != null && withdrawAmount > balance) {
                            Text("Exceeds available balance (₹${balance.toInt()})", color = BrandRed)
                        } else if (withdrawAmount != null && withdrawAmount <= 0) {
                            Text("Please enter an amount greater than ₹0", color = BrandRed)
                        } else if (isCommissionFree) {
                            Text("🎉 ₹0 Platform Fee • Referral Pass Applied (Saved $commissionRatePercent%)", color = BrandPrimaryGreen)
                        } else {
                            Text("$commissionRatePercent% Platform Fee: ₹${"%.2f".format(commissionFee)} deducted at withdrawal", color = BrandAmber)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_withdraw_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark,
                        focusedContainerColor = BrandSurfaceDark,
                        unfocusedContainerColor = BrandSurfaceDark
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Amount Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("500", "1000", "2000").forEach { quickVal ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandSurfaceDark,
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { amountText = quickVal }
                        ) {
                            Text(
                                text = "₹$quickVal",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }

                    // Withdraw All Chip
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimaryGreenDim,
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable { amountText = balance.toInt().toString() }
                    ) {
                        Text(
                            text = "All (₹${balance.toInt()})",
                            color = BrandPrimaryGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // CONDITIONAL FORM: UPI ID vs BANK ACCOUNT
                if (selectedMethodIndex == 0) {
                    // UPI DETAILS FORM
                    Text(
                        text = "UPI DETAILS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = upiId,
                        onValueChange = { upiId = it.trim() },
                        label = { Text("Virtual Payment Address (UPI ID)") },
                        placeholder = { Text("username@okhdfcbank") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (isUpiValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Valid UPI ID",
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_withdraw_upi_id"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedContainerColor = BrandSurfaceDark,
                            unfocusedContainerColor = BrandSurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick UPI Suffixes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("@okhdfcbank", "@okaxis", "@paytm", "@ybl").forEach { suffix ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = BrandSurfaceDark,
                                border = BorderStroke(1.dp, BrandCardBorderDark),
                                modifier = Modifier.clickable {
                                    val prefix = upiId.substringBefore("@").ifBlank { "9876543210" }
                                    upiId = "$prefix$suffix"
                                }
                            ) {
                                Text(
                                    text = suffix,
                                    fontSize = 11.sp,
                                    color = TextSecondaryDark,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Trust Note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandSurfaceDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "NPCI UPI Rail: 0–15 second settlement directly to your phone's default UPI banking app.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                } else {
                    // BANK ACCOUNT DETAILS FORM
                    Text(
                        text = "BANK ACCOUNT DETAILS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Holder Name
                    OutlinedTextField(
                        value = accountHolderName,
                        onValueChange = { accountHolderName = it },
                        label = { Text("Account Holder Name") },
                        placeholder = { Text("Ramesh Kumar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        supportingText = {
                            Text("Name as registered in Bank Passbook / Aadhaar", fontSize = 10.sp, color = TextSecondaryDark)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bank_account_holder"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandSecondaryCyan,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedContainerColor = BrandSurfaceDark,
                            unfocusedContainerColor = BrandSurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Account Number
                    OutlinedTextField(
                        value = accountNumber,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                accountNumber = input
                            }
                        },
                        label = { Text("Bank Account Number") },
                        placeholder = { Text("5010042918234") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isAccountRevealed = !isAccountRevealed }) {
                                Icon(
                                    imageVector = if (isAccountRevealed) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (isAccountRevealed) "Hide" else "Show",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (isAccountRevealed) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bank_account_number"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandSecondaryCyan,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedContainerColor = BrandSurfaceDark,
                            unfocusedContainerColor = BrandSurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Confirm Account Number
                    OutlinedTextField(
                        value = confirmAccountNumber,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                confirmAccountNumber = input
                            }
                        },
                        label = { Text("Re-enter Account Number") },
                        placeholder = { Text("5010042918234") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.CheckCircleOutline,
                                contentDescription = null,
                                tint = if (isBankMatch && confirmAccountNumber.isNotEmpty()) BrandPrimaryGreen else BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (confirmAccountNumber.isNotEmpty()) {
                                Icon(
                                    imageVector = if (isBankMatch) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (isBankMatch) BrandPrimaryGreen else BrandRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        supportingText = {
                            if (confirmAccountNumber.isNotEmpty() && !isBankMatch) {
                                Text("Account numbers do not match", color = BrandRed, fontSize = 10.sp)
                            } else if (isBankMatch && confirmAccountNumber.isNotEmpty()) {
                                Text("Account numbers match", color = BrandPrimaryGreen, fontSize = 10.sp)
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bank_confirm_account_number"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandSecondaryCyan,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedContainerColor = BrandSurfaceDark,
                            unfocusedContainerColor = BrandSurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // IFSC Code Input with live bank resolver
                    OutlinedTextField(
                        value = ifscCode,
                        onValueChange = { input ->
                            if (input.length <= 11) {
                                ifscCode = input.uppercase()
                            }
                        },
                        label = { Text("IFSC Code (11 characters)") },
                        placeholder = { Text("HDFC0001234") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Pin,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (isIfscValid) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_bank_ifsc_code"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandSecondaryCyan,
                            unfocusedBorderColor = BrandCardBorderDark,
                            focusedContainerColor = BrandSurfaceDark,
                            unfocusedContainerColor = BrandSurfaceDark
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Auto-detected Bank Name Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandSecondaryCyanDim,
                        border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Detected Bank: ${resolveBankName(ifscCode)} • IMPS Active",
                                color = BrandSecondaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Account Type Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Savings Account
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSavingsAccount) BrandSecondaryCyan.copy(alpha = 0.2f) else BrandSurfaceDark,
                            border = BorderStroke(
                                1.dp,
                                if (isSavingsAccount) BrandSecondaryCyan else BrandCardBorderDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSavingsAccount = true }
                                .testTag("btn_account_type_savings")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSavingsAccount,
                                    onClick = { isSavingsAccount = true },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandSecondaryCyan),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Savings A/C",
                                    color = if (isSavingsAccount) BrandSecondaryCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Current Account
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (!isSavingsAccount) BrandSecondaryCyan.copy(alpha = 0.2f) else BrandSurfaceDark,
                            border = BorderStroke(
                                1.dp,
                                if (!isSavingsAccount) BrandSecondaryCyan else BrandCardBorderDark
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSavingsAccount = false }
                                .testTag("btn_account_type_current")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = !isSavingsAccount,
                                    onClick = { isSavingsAccount = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = BrandSecondaryCyan),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Current A/C",
                                    color = if (!isSavingsAccount) BrandSecondaryCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // RBI IMPS Security Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BrandSurfaceDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reserve Bank of India (RBI) IMPS rail ensures 24x7 instant direct bank credit with ₹0 deduction.",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // TRANSFER SUMMARY CARD
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandSurfaceDark,
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Gross Withdrawal", color = TextSecondaryDark, fontSize = 12.sp)
                            Text(
                                text = "₹${withdrawAmount?.toInt() ?: 0}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isCommissionFree) "Platform Fee (Waived)" else "Platform Fee ($commissionRatePercent%)",
                                color = TextSecondaryDark,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isCommissionFree) "₹0.00 (100% Free Pass)" else "-₹${"%.2f".format(commissionFee)}",
                                color = if (isCommissionFree) BrandPrimaryGreen else BrandAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Net Credited to You", color = TextSecondaryDark, fontSize = 12.sp)
                            Text(
                                text = "₹${"%.2f".format(netDisbursed)}",
                                color = BrandPrimaryGreen,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Destination", color = TextSecondaryDark, fontSize = 12.sp)
                            Text(
                                text = if (selectedMethodIndex == 0) {
                                    upiId.ifBlank { "Registered UPI" }
                                } else {
                                    "${resolveBankName(ifscCode)} (A/C ***${accountNumber.takeLast(4)})"
                                },
                                color = if (selectedMethodIndex == 0) BrandPrimaryGreen else BrandSecondaryCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Remaining Balance", color = TextSecondaryDark, fontSize = 12.sp)
                            val remaining = if (withdrawAmount != null) (balance - withdrawAmount).coerceAtLeast(0.0) else balance
                            Text(
                                text = "₹${remaining.toInt()}",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ACTIONS ROW: Cancel & Confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandCardBorderDark)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }

                    Button(
                        onClick = {
                            if (canProceed && withdrawAmount != null) {
                                val method = if (selectedMethodIndex == 0) "UPI" else "Bank Account"
                                val destination = if (selectedMethodIndex == 0) {
                                    upiId
                                } else {
                                    "${resolveBankName(ifscCode)} (A/C ***${accountNumber.takeLast(4)})"
                                }
                                onConfirmWithdraw(withdrawAmount, method, destination)
                            }
                        },
                        enabled = canProceed,
                        modifier = Modifier
                            .weight(1.8f)
                            .height(48.dp)
                            .testTag("btn_confirm_withdraw"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMethodIndex == 0) BrandPrimaryGreen else BrandSecondaryCyan,
                            contentColor = Color.Black,
                            disabledContainerColor = Color(0xFF1E2A2E),
                            disabledContentColor = Color.Gray
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedMethodIndex == 0) "Transfer via UPI" else "Transfer to Bank",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

// Backward-compatible overload for single-parameter callers
@Composable
fun WithdrawDialog(
    balance: Double,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (Double) -> Unit
) {
    WithdrawDialog(
        balance = balance,
        onDismiss = onDismiss,
        onConfirmWithdraw = { amount, _, _ ->
            onConfirmWithdraw(amount)
        }
    )
}

@Composable
fun DisputeDialog(
    onDismiss: () -> Unit,
    onSubmit: (reason: String, target: String, details: String) -> Unit
) {
    var reason by remember { mutableStateOf("Payment Not Received") }
    var target by remember { mutableStateOf("Royal Feast Caterers") }
    var details by remember { mutableStateOf("") }

    val reasons = listOf(
        "Payment Not Received",
        "Fake Job Posting",
        "Unsafe Workplace Conditions",
        "Worker No-show",
        "Incomplete Work Delivery"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("dispute_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "File a Dispute Ticket",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "DailyCrew Trust & Mediation Council reviews GPS logs and shift timestamps.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Dispute Category", style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark))
                Spacer(modifier = Modifier.height(6.dp))

                // Reason selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    reasons.take(3).forEach { r ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (reason == r) BrandErrorRed.copy(alpha = 0.2f) else BrandSurfaceDark,
                            border = BorderStroke(1.dp, if (reason == r) BrandErrorRed else BrandCardBorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reason = r }
                        ) {
                            Text(
                                text = r,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (reason == r) BrandErrorRed else Color.White,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Entity Involved (Business or Worker)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Evidence Description & Incident Details") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onSubmit(
                            reason,
                            target.ifBlank { "DailyCrew Platform Mediation" },
                            details.ifBlank { "Details logged via in-app report." }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandErrorRed,
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SUBMIT EVIDENCE & LOG DISPUTE", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatSheet(
    applicant: JobApplicant,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        color = BrandSurfaceDark,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .testTag("applicant_chat_sheet")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Header
            Surface(
                color = BrandCardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BrandSecondaryCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = applicant.workerName.take(2).uppercase(),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = applicant.workerName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Applying for ${applicant.jobTitle} • Trust ${applicant.trustScore}%",
                            style = MaterialTheme.typography.bodySmall.copy(color = BrandPrimaryGreen)
                        )
                    }
                }
            }

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages) { msg ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (msg.isFromMe) BrandSecondaryCyan else BrandCardDark,
                            border = if (!msg.isFromMe) BorderStroke(1.dp, BrandCardBorderDark) else null,
                            modifier = Modifier.widthIn(max = 280.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = if (msg.isFromMe) Color.Black else Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (msg.isFromMe) Color.Black.copy(alpha = 0.6f) else TextSecondaryDark,
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            // Input bar
            Surface(
                color = BrandCardDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Type instruction or question...", color = TextSecondaryDark) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandSecondaryCyan,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                onSendMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(BrandSecondaryCyan)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    }
}

// ---------------- Surety / Escrow Management Dialog ----------------

@Composable
fun SuretyEscrowDialog(
    workerProfile: WorkerProfile,
    suretyTransactions: List<SuretyTransaction>,
    onDismiss: () -> Unit,
    onTopUp: (Double) -> Unit,
    onWithdraw: (Double) -> Unit
) {
    var topUpInput by remember { mutableStateOf("100") }
    var selectedPreset by remember { mutableStateOf(100.0) }
    val availableToWithdraw = (workerProfile.suretyEscrowBalance - workerProfile.lockedSuretyAmount).coerceAtLeast(0.0)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.5.dp, BrandAmber.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("surety_escrow_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(BrandAmberDim),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = BrandAmber,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Surety Escrow Reserve",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                )
                                Text(
                                    text = "Anti-No-Show Trust Guarantee",
                                    style = MaterialTheme.typography.labelSmall.copy(color = BrandAmber)
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                        }
                    }
                }

                // Balance Summary Cards
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL SURETY BALANCE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondaryDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "₹${workerProfile.suretyEscrowBalance.toInt()}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = BrandPrimaryGreen,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "LOCKED FOR ACTIVE SHIFTS",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondaryDark,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "₹${workerProfile.lockedSuretyAmount.toInt()}",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            color = BrandAmber,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BrandCardBorderDark)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Available to Withdraw:",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White)
                                )
                                Text(
                                    text = "₹${availableToWithdraw.toInt()}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BrandSecondaryCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }

                // Policy Explanation Banner
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandAmberDim.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "How Surety Escrow Protects You & Owners",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = BrandAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "• Shift Hold: ₹100 surety is locked during accepted shifts.\n• 100% Refund Condition: Automatically returned upon verified GPS/QR check-in.\n• Forfeit Policy: If you abandon a confirmed shift, the deposit compensates the owner for liquidated damages.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }

                // Quick Top-up Presets
                item {
                    Text(
                        text = "Add Surety Deposit",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(100.0, 200.0, 300.0, 500.0).forEach { amount ->
                            val isSelected = selectedPreset == amount
                            OutlinedButton(
                                onClick = {
                                    selectedPreset = amount
                                    topUpInput = amount.toInt().toString()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) BrandPrimaryGreenDim else Color.Transparent
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BrandPrimaryGreen else BrandCardBorderDark
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "₹${amount.toInt()}",
                                    color = if (isSelected) BrandPrimaryGreen else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // Action Buttons (Top Up & Withdraw)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (availableToWithdraw > 0) {
                            OutlinedButton(
                                onClick = { onWithdraw(availableToWithdraw) },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BrandSecondaryCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Withdraw", color = BrandSecondaryCyan, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                val amt = topUpInput.toDoubleOrNull() ?: selectedPreset
                                onTopUp(amt)
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimaryGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(if (availableToWithdraw > 0) 1.4f else 1f)
                                .height(48.dp)
                                .testTag("confirm_topup_surety_button")
                        ) {
                            Text("Top Up ₹${topUpInput}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        }
                    }
                }

                // Surety History
                item {
                    Text(
                        text = "Escrow Transaction History",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        suretyTransactions.take(4).forEach { txn ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrandCardDark,
                                border = BorderStroke(1.dp, BrandCardBorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = txn.title,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${txn.date} • ${txn.relatedJobTitle}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
                                        )
                                    }

                                    Text(
                                        text = "${if (txn.type == "DEPOSIT" || txn.type == "UNLOCKED_REFUNDED") "+" else "-"}₹${txn.amount.toInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (txn.type == "FORFEITED") Color.Red else BrandPrimaryGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- Owner Edit Job Dialog ----------------

@Composable
fun EditJobDialog(
    job: JobPosting,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        category: JobCategory,
        wageAmount: Double,
        workersRequired: Int,
        time: String,
        dressCode: String,
        location: String,
        instructions: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(job.title) }
    var wageStr by remember { mutableStateOf(job.wageAmount.toInt().toString()) }
    var countStr by remember { mutableStateOf(job.workersRequired.toString()) }
    var time by remember { mutableStateOf(job.time) }
    var dressCode by remember { mutableStateOf(job.dressCode) }
    var location by remember { mutableStateOf(job.location) }
    var instructions by remember { mutableStateOf(job.instructions) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandPrimaryGreen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("edit_job_dialog")
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Shift Details",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Shift Title") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = wageStr,
                            onValueChange = { wageStr = it },
                            label = { Text("Wage (₹)") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = BrandCardBorderDark
                            )
                        )
                        OutlinedTextField(
                            value = countStr,
                            onValueChange = { countStr = it },
                            label = { Text("Workers Needed") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = BrandCardBorderDark
                            )
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Shift Time & Timing") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = dressCode,
                        onValueChange = { dressCode = it },
                        label = { Text("Dress Code Requirement") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Venue Location") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = instructions,
                        onValueChange = { instructions = it },
                        label = { Text("Instructions for Crew") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandPrimaryGreen,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )
                }

                item {
                    Button(
                        onClick = {
                            val wage = wageStr.toDoubleOrNull() ?: job.wageAmount
                            val count = countStr.toIntOrNull() ?: job.workersRequired
                            onSave(title, job.category, wage, count, time, dressCode, location, instructions)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimaryGreen,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_job_changes_button")
                    ) {
                        Text("Save & Update Shift", fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

// ---------------- Report No-Show Dialog (Surety Forfeit) ----------------

@Composable
fun ReportNoShowDialog(
    applicant: JobApplicant,
    onDismiss: () -> Unit,
    onSubmit: (reason: String) -> Unit
) {
    var selectedReason by remember {
        mutableStateOf("Worker did not arrive at venue and stopped answering calls.")
    }
    var additionalNotes by remember { mutableStateOf("") }

    val presetReasons = listOf(
        "Worker did not arrive at venue and stopped answering calls.",
        "Cancelled with less than 1 hour notice before shift.",
        "Left venue during active shift without supervisor permission.",
        "Unprofessional conduct / refused assigned duties."
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.5.dp, Color.Red.copy(alpha = 0.7f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("report_no_show_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Report Worker No-Show",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Surface(
                    color = Color.Red.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Candidate: ${applicant.workerName} • Shift: ${applicant.jobTitle}\n\nUnder DailyCrew Surety Escrow Rules, reporting a verified no-show immediately forfeits the candidate's ₹100 surety deposit directly into your business account as liquidated damages compensation.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Text(
                    text = "Select Primary Reason:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )

                presetReasons.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) BrandCardDark else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color.Red else BrandCardBorderDark
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                    ) {
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) Color.White else TextSecondaryDark,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Button(
                    onClick = { onSubmit("$selectedReason ${additionalNotes.trim()}") },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_no_show_claim_button")
                ) {
                    Text("Confirm No-Show & Claim ₹100", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- Rate Worker Dialog ----------------

@Composable
fun RateWorkerDialog(
    applicant: JobApplicant,
    onDismiss: () -> Unit,
    onSubmitRating: (rating: Double, feedback: String) -> Unit
) {
    var rating by remember { mutableStateOf(5.0) }
    var feedback by remember { mutableStateOf("") }
    val tags = listOf("Punctual", "Hardworking", "Well Groomed", "Polite", "Would Rehire")
    val selectedTags = remember { mutableStateListOf<String>("Punctual", "Hardworking") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.5.dp, BrandAmber),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("rate_worker_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rate ${applicant.workerName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Text(
                    text = "Shift completed at ${applicant.jobTitle}. Your rating updates the worker's DailyCrew trust score and unlocks repeat bonus opportunities.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )

                // Star rating row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star.toDouble() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star Stars",
                                tint = BrandAmber,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Quick praise chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    tags.take(3).forEach { tag ->
                        val isChecked = selectedTags.contains(tag)
                        FilterChip(
                            selected = isChecked,
                            onClick = {
                                if (isChecked) selectedTags.remove(tag) else selectedTags.add(tag)
                            },
                            label = { Text(tag, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    placeholder = { Text("Write brief performance comment (optional)...", color = TextSecondaryDark) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandAmber,
                        unfocusedBorderColor = BrandCardBorderDark
                    )
                )

                Button(
                    onClick = {
                        val comment = if (feedback.isNotBlank()) feedback else selectedTags.joinToString(", ")
                        onSubmitRating(rating, comment)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandAmber,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_rating_button")
                ) {
                    Text("Submit Rating ($rating★)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------- Refer & Earn Rewards Dialog ----------------

@Composable
fun ReferAndEarnDialog(
    role: AppRole,
    worker: WorkerProfile,
    business: BusinessProfile,
    selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit,
    onReferWorker: (name: String, phone: String) -> Unit,
    onSimulateWorkerFriendShift: (friendId: String) -> Unit,
    onReferOwner: (name: String, phone: String) -> Unit,
    onSimulateOwnerSuretyBonus: (ownerId: String) -> Unit,
    onApplyCode: (code: String) -> Unit
) {
    val isWorker = role == AppRole.WORKER
    val referralCode = if (isWorker) worker.referralCode else business.referralCode
    val freePasses = if (isWorker) worker.freeCommissionWithdrawalsCount else business.freeCommissionJobsCount
    val totalReferrals = if (isWorker) worker.totalReferralsCount else business.totalReferralsCount
    val totalEarned = if (isWorker) worker.totalReferralEarnings else business.totalReferralEarnings

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var inviteName by remember { mutableStateOf("") }
    var invitePhone by remember { mutableStateOf("") }
    var redeemCodeText by remember { mutableStateOf("") }
    var codeCopied by remember { mutableStateOf(false) }
    var linkCopied by remember { mutableStateOf(false) }

    val roleParam = if (isWorker) "worker" else "business"
    val referralLink = "https://dailycrew.app/invite?ref=$referralCode&role=$roleParam"
    val downloadAppLink = "https://dailycrew.app/download?ref=$referralCode"

    val shareText = if (isWorker) {
        "🚀 Join DailyCrew with my link & get 2 FREE Zero-Commission Payouts!\n\nDailyCrew provides verified daily hospitality & retail shifts with instant daily payouts and safety escrow.\n\n📲 1. Download App: $downloadAppLink\n🎁 2. Sign up using my referral link or code: $referralLink\nReferral Code: $referralCode\n\nStart working verified shifts with 100% daily earnings!"
    } else {
        "🏢 Join DailyCrew to hire verified staff & manage daily shift operations!\n\n🎁 Use my referral link to get 2 ZERO-COMMISSION shifts (100% of ₹300 surety fee refunded):\n\n📲 1. Download Business App: $downloadAppLink\n🎁 2. Sign up using my link or code: $referralLink\nReferral Code: $referralCode\n\nVerified daily crew with standby guarantee & swift settlements!"
    }

    val shareViaChooser: (String) -> Unit = { textToShare ->
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Join DailyCrew & Get Zero-Commission Passes!")
            putExtra(Intent.EXTRA_TEXT, textToShare)
        }
        val chooser = Intent.createChooser(sendIntent, "Share DailyCrew Link via...")
        context.startActivity(chooser)
    }

    val shareViaWhatsApp: (String) -> Unit = { textToShare ->
        try {
            val waIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
                setPackage("com.whatsapp")
            }
            context.startActivity(waIntent)
        } catch (e: Exception) {
            shareViaChooser(textToShare)
        }
    }

    val shareViaInstagram: (String) -> Unit = { textToShare ->
        try {
            val igIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, textToShare)
                setPackage("com.instagram.android")
            }
            context.startActivity(igIntent)
        } catch (e: Exception) {
            shareViaChooser(textToShare)
        }
    }

    val shareViaSms: (String, String) -> Unit = { textToShare, phoneNumber ->
        try {
            val uri = if (phoneNumber.isNotBlank()) Uri.parse("smsto:$phoneNumber") else Uri.parse("sms:")
            val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", textToShare)
            }
            context.startActivity(smsIntent)
        } catch (e: Exception) {
            shareViaChooser(textToShare)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 750.dp)
                .padding(vertical = 8.dp)
                .testTag("refer_earn_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = BrandPrimaryGreenDim,
                            border = BorderStroke(1.dp, BrandPrimaryGreen),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CardGiftcard,
                                    contentDescription = null,
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = DailyCrewLocalization.get("refer_title", selectedLanguage),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (isWorker) "Zero-Fee Withdrawals & 2.3% Friend Earnings" else "Zero-Fee Shifts & 3.0% Surety Commission",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Banner
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSurfaceDark,
                    border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$freePasses",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BrandPrimaryGreen
                                )
                            )
                            Text(
                                text = if (isWorker) "0% Free Payouts" else "0% Free Shifts",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = BrandCardBorderDark
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalReferrals",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BrandSecondaryCyan
                                )
                            )
                            Text(
                                text = "People Referred",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        VerticalDivider(
                            modifier = Modifier.height(36.dp),
                            color = BrandCardBorderDark
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "₹${"%.2f".format(totalEarned)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = BrandAmber
                                )
                            )
                            Text(
                                text = "Bonus Earned",
                                fontSize = 11.sp,
                                color = TextSecondaryDark,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Your Referral Code & App Download Link Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = BrandSurfaceDark,
                    border = BorderStroke(1.dp, BrandPrimaryGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Top row: Referral Code
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "YOUR REFERRAL CODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondaryDark,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = referralCode,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = BrandPrimaryGreen,
                                        letterSpacing = 1.5.sp
                                    )
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(referralCode))
                                    codeCopied = true
                                    linkCopied = false
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = BrandPrimaryGreenDim,
                                    contentColor = BrandPrimaryGreen
                                ),
                                border = BorderStroke(1.dp, BrandPrimaryGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("btn_copy_referral_code")
                            ) {
                                Icon(
                                    imageVector = if (codeCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = DailyCrewLocalization.get(if (codeCopied) "code_copied" else "copy_code", selectedLanguage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)

                        // Direct App Download & Invite Link Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = null,
                                        tint = BrandSecondaryCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = DailyCrewLocalization.get("download_app_link", selectedLanguage).uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandSecondaryCyan,
                                        letterSpacing = 0.8.sp
                                    )
                                }

                                Text(
                                    text = "Auto-Applies Code",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BrandAmber
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BrandCardDark,
                                border = BorderStroke(1.dp, BrandCardBorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = downloadAppLink,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        FilledTonalButton(
                                            onClick = {
                                                val dlIntent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadAppLink))
                                                try {
                                                    context.startActivity(dlIntent)
                                                } catch (e: Exception) {
                                                    shareViaChooser(downloadAppLink)
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = BrandPrimaryGreen,
                                                contentColor = Color.Black
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .testTag("btn_download_app_link")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Download,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = DailyCrewLocalization.get("download_app_btn", selectedLanguage),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(downloadAppLink))
                                                linkCopied = true
                                                codeCopied = false
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = BrandSecondaryCyan,
                                                contentColor = Color.Black
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(32.dp)
                                                .testTag("btn_copy_referral_link")
                                        ) {
                                            Icon(
                                                imageVector = if (linkCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                                                contentDescription = null,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = DailyCrewLocalization.get(if (linkCopied) "link_copied" else "copy_link", selectedLanguage),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Primary Share Link Action Button (Universal Mobile Apps Chooser)
                        Button(
                            onClick = { shareViaChooser(shareText) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimaryGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("btn_share_referral_all_apps")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = DailyCrewLocalization.get("share_link_label", selectedLanguage),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick 1-Tap Social Share Channels (WhatsApp, Insta, SMS)
                        Text(
                            text = "OR 1-TAP SHARE VIA SOCIAL APPS:",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondaryDark,
                            letterSpacing = 0.8.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // WhatsApp
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF25D366)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { shareViaWhatsApp(shareText) }
                                    .testTag("btn_share_whatsapp")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = "WhatsApp",
                                        tint = Color(0xFF25D366),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = DailyCrewLocalization.get("share_whatsapp", selectedLanguage),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF25D366)
                                    )
                                }
                            }

                            // Instagram
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE1306C).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFE1306C)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { shareViaInstagram(shareText) }
                                    .testTag("btn_share_instagram")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Instagram",
                                        tint = Color(0xFFE1306C),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = DailyCrewLocalization.get("share_instagram", selectedLanguage),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE1306C)
                                    )
                                }
                            }

                            // SMS / Messages
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0288D1).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF0288D1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { shareViaSms(shareText, "") }
                                    .testTag("btn_share_sms")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = "SMS",
                                        tint = Color(0xFF0288D1),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = DailyCrewLocalization.get("share_sms", selectedLanguage),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0288D1)
                                    )
                                }
                            }
                        }

                        // App Download & Referral Lifecycle Explanation
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.3f),
                            border = BorderStroke(0.5.dp, BrandCardBorderDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "📲 When your friend opens your link & downloads the app:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "1. Your link takes them directly to download DailyCrew.\n2. Referral code $referralCode is auto-applied at signup.\n3. Both of you get 2 zero-commission passes + ${if (isWorker) "2.3% lifetime bonus" else "3.0% surety bonus"}!",
                                    fontSize = 10.sp,
                                    color = TextSecondaryDark,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Benefit Highlights
                Text(
                    text = DailyCrewLocalization.get("referral_perks", selectedLanguage).uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                if (isWorker) {
                    // Worker Perks
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandSurfaceDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Benefits for Friend (Worker B): First 2 withdrawals are 100% commission-free (0% platform deduction, save 9.7% each time!).",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Benefits for You (Worker A): Earn 2.3% of platform commission generated from Worker B. (e.g. Worker B withdraws ₹1,000 ➔ Normal Commission ₹97 ➔ Reward to Worker A is ₹2.23).",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your safety surety: ₹100 safety surety is stored in escrow and 100% refunded on safe shift checkout.",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    // Owner Perks
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandSurfaceDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Owner Surety System: ₹300 surety fee in escrow. On job completion: 73.2% (₹219.60) refunded to owner, 26.8% (₹80.40) DailyCrew platform revenue.",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Benefits for Referred Owner (Owner B): First 2 completed jobs receive full ₹300 surety refund (0% platform deduction).",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Benefits for You (Owner A): Earn 3.0% of DailyCrew's surety revenue generated from Owner B (3.0% of ₹80.40 platform revenue = ₹2.41 per job).",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Invite New Person Form
                Text(
                    text = if (isWorker) "INVITE A WORKER FRIEND" else "INVITE A BUSINESS OWNER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inviteName,
                            onValueChange = { inviteName = it },
                            placeholder = { Text(if (isWorker) "Friend Name" else "Business Name", color = TextSecondaryDark, fontSize = 12.sp) },
                            modifier = Modifier.weight(1.1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = BrandCardBorderDark
                            )
                        )

                        OutlinedTextField(
                            value = invitePhone,
                            onValueChange = { invitePhone = it },
                            placeholder = { Text("Phone (Optional)", color = TextSecondaryDark, fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = BrandCardBorderDark
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inviteName.isNotBlank()) {
                                    if (isWorker) {
                                        onReferWorker(inviteName, invitePhone)
                                    } else {
                                        onReferOwner(inviteName, invitePhone)
                                    }
                                    inviteName = ""
                                    invitePhone = ""
                                }
                            },
                            enabled = inviteName.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimaryGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_add_invite_record")
                        ) {
                            Text("+ Add (+2 Free)", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                val customMsg = if (inviteName.isNotBlank()) {
                                    "Hey $inviteName!\n\n$shareText"
                                } else {
                                    shareText
                                }
                                if (invitePhone.isNotBlank()) {
                                    shareViaSms(customMsg, invitePhone)
                                } else {
                                    shareViaWhatsApp(customMsg)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = Color(0xFF25D366).copy(alpha = 0.2f),
                                contentColor = Color(0xFF25D366)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF25D366)),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("btn_send_link_to_friend")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Send Link", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Redeem a friend's code
                Text(
                    text = "REDEEM REFERRAL CODE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = redeemCodeText,
                        onValueChange = { redeemCodeText = it.uppercase() },
                        placeholder = { Text("Enter 6-digit invite code", color = TextSecondaryDark) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = BrandAmber,
                            unfocusedBorderColor = BrandCardBorderDark
                        )
                    )

                    Button(
                        onClick = {
                            if (redeemCodeText.isNotBlank()) {
                                onApplyCode(redeemCodeText)
                                redeemCodeText = ""
                            }
                        },
                        enabled = redeemCodeText.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandAmber,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("Apply Code", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Referral History & Real-Time Test Simulation
                Text(
                    text = if (isWorker) "YOUR REFERRED FRIENDS (${worker.referralFriends.size})" else "YOUR REFERRED BUSINESSES (${business.referralOwners.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isWorker) {
                    worker.referralFriends.forEach { friend ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandSurfaceDark,
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = friend.name,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${friend.shiftsCompleted} shifts completed • ₹${friend.friendEarnings.toInt()} earned",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = "Your 2.3% Bonus: ₹${"%.2f".format(friend.bonusEarned)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandPrimaryGreen
                                    )
                                }

                                Button(
                                    onClick = { onSimulateWorkerFriendShift(friend.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandPrimaryGreenDim,
                                        contentColor = BrandPrimaryGreen
                                    ),
                                    border = BorderStroke(1.dp, BrandPrimaryGreen),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("⚡ Test +2.3%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    business.referralOwners.forEach { owner ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandSurfaceDark,
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = owner.businessName,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${owner.jobsPosted} shifts completed",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark
                                    )
                                    Text(
                                        text = "Your 3.0% Commission: ₹${"%.2f".format(owner.bonusEarned)}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BrandSecondaryCyan
                                    )
                                }

                                Button(
                                    onClick = { onSimulateOwnerSuretyBonus(owner.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = BrandSecondaryCyanDim,
                                        contentColor = BrandSecondaryCyan
                                    ),
                                    border = BorderStroke(1.dp, BrandSecondaryCyan),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("⚡ Test +3.0%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

