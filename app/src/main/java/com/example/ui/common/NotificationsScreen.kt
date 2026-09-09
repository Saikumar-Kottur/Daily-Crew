package com.example.ui.common

import android.Manifest
import android.os.Build
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
import com.example.data.model.AppRole
import com.example.data.model.NotificationItem
import com.example.ui.theme.*

@Composable
fun NotificationsScreen(
    currentRole: AppRole,
    notifications: List<NotificationItem>,
    hasNotificationPermission: Boolean,
    onRequestPermission: () -> Unit,
    onTestJobAlert: () -> Unit,
    onTestApplicantAlert: () -> Unit,
    onTestCheckInAlert: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("notifications_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Notifications & Alerts",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (hasNotificationPermission) BrandPrimaryGreenDim else BrandAmberDim,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, if (hasNotificationPermission) BrandPrimaryGreen.copy(alpha = 0.4f) else BrandAmber.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = if (hasNotificationPermission) "Live Push Active" else "Permission Required",
                            color = if (hasNotificationPermission) BrandPrimaryGreen else BrandAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Text(
                    text = "High-priority push channels for daily shifts and owner check-in events",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                )
            }

            IconButton(onClick = onClose) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondaryDark)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // System Notification Service Channel Status Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = BrandSecondaryCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DailyCrew System Notification Service",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (!hasNotificationPermission) {
                        Button(
                            onClick = onRequestPermission,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreen),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_grant_notif_permission")
                        ) {
                            Text("Enable", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Configured Channels: Nearby Job Openings (Worker) & Applications & Check-Ins (Owner). Supports instant sound, vibration, and deep-linking.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Simulation Controls
                Text(
                    text = "SIMULATE NOTIFICATIONS:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentRole == AppRole.WORKER) {
                        Button(
                            onClick = onTestJobAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimaryGreenDim),
                            border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_simulate_job_alert")
                        ) {
                            Icon(
                                Icons.Default.Work,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Nearby Job Alert",
                                color = BrandPrimaryGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onTestApplicantAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSecondaryCyanDim),
                            border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_simulate_applicant_alert")
                        ) {
                            Icon(
                                Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Applicant Alert",
                                color = BrandSecondaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = onTestCheckInAlert,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandAmberDim),
                            border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_simulate_checkin_alert")
                        ) {
                            Icon(
                                Icons.Default.PinDrop,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Check-In Alert",
                                color = BrandAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "EVENT LOG & NOTIFICATION HISTORY",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TextSecondaryDark,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications right now", color = TextSecondaryDark)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
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
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (notif.type) {
                                            "PAYOUT" -> BrandPrimaryGreenDim
                                            "CHECKIN" -> BrandSecondaryCyanDim
                                            "ALERT" -> BrandAmberDim
                                            else -> BrandCardBorderDark
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (notif.type) {
                                        "PAYOUT" -> Icons.Default.Payments
                                        "CHECKIN" -> Icons.Default.QrCode
                                        "ALERT" -> Icons.Default.WarningAmber
                                        else -> Icons.Default.Work
                                    },
                                    contentDescription = null,
                                    tint = when (notif.type) {
                                        "PAYOUT" -> BrandPrimaryGreen
                                        "CHECKIN" -> BrandSecondaryCyan
                                        "ALERT" -> BrandAmber
                                        else -> Color.White
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                Text(
                                    text = notif.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = TextSecondaryDark,
                                        fontSize = 11.sp
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = notif.time,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}
