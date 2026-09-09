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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
    onLogoClick: (() -> Unit)? = null
) {
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
            // Official DailyCrew Brand Logo & Identity
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = onLogoClick != null) { onLogoClick?.invoke() }
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(BrandDeepBlueContainer)
                        .border(1.dp, BrandSuccessGreen.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                        .padding(3.dp)
                        .testTag("brand_badge_logo"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_dailycrew_brand_logo),
                        contentDescription = "DailyCrew Official Brand Logo",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DAILYCREW",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.2.sp,
                                fontSize = 17.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(BrandSuccessGreen)
                        )
                    }
                    Text(
                        text = "Verified People. Trusted Work.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandSuccessGreen.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Quick Role Switcher Chip & Action Icons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // AI Insights button
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

                // Role Toggle Chip with Dropdown Menu
                var roleMenuExpanded by remember { mutableStateOf(false) }

                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentRole) {
                            AppRole.WORKER -> BrandPrimaryGreenDim
                            AppRole.OWNER -> BrandSecondaryCyanDim
                            AppRole.ADMIN -> BrandSecondaryCyanDim
                            AppRole.SUPER_ADMIN -> BrandAmberDim
                        },
                        border = BorderStroke(
                            1.dp,
                            when (currentRole) {
                                AppRole.WORKER -> BrandPrimaryGreen
                                AppRole.OWNER -> BrandSecondaryCyan
                                AppRole.ADMIN -> BrandSecondaryCyan
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
                                    AppRole.WORKER -> "👷 Worker"
                                    AppRole.OWNER -> "🏪 Owner"
                                    AppRole.ADMIN -> "🛡️ Admin"
                                    AppRole.SUPER_ADMIN -> "⚙️ Super Admin"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentRole) {
                                        AppRole.WORKER -> BrandPrimaryGreen
                                        AppRole.OWNER -> BrandSecondaryCyan
                                        AppRole.ADMIN -> BrandSecondaryCyan
                                        AppRole.SUPER_ADMIN -> BrandAmber
                                    },
                                    fontSize = 11.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Role",
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
                        DropdownMenuItem(
                            text = { Text("👷 Worker Marketplace", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleChange(AppRole.WORKER)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🏪 Business Owner (Hirer)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleChange(AppRole.OWNER)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🛡️ Admin Moderation Center", color = BrandSecondaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleChange(AppRole.ADMIN)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("⚙️ Super Admin Platform Engine", color = BrandAmber, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleChange(AppRole.SUPER_ADMIN)
                            }
                        )
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
    onDismiss: () -> Unit,
    onConfirmWithdraw: (Double) -> Unit
) {
    var amountText by remember { mutableStateOf("1000") }
    var upiId by remember { mutableStateOf("ramesh@icici") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = BrandCardDark,
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("withdraw_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Instant UPI Wallet Payout",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Available to withdraw: ₹${balance.toInt()}",
                    style = MaterialTheme.typography.bodySmall.copy(color = BrandPrimaryGreen)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("Registered UPI ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondaryDark)
                    }
                    Button(
                        onClick = {
                            val amt = amountText.toDoubleOrNull() ?: 500.0
                            onConfirmWithdraw(amt)
                        },
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPrimaryGreen,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Transfer Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
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
                        onSubmit(reason, target, details.ifBlank { "Details logged via in-app report." })
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
