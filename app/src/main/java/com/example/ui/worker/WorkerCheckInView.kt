package com.example.ui.worker

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*

@Composable
fun WorkerCheckInView(
    worker: WorkerProfile,
    onOpenCheckInScanner: () -> Unit,
    onCheckOut: () -> Unit
) {
    val isCheckedIn = worker.isCheckedIn

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = if (isCheckedIn) 1.0f else 0.95f,
        targetValue = if (isCheckedIn) 1.08f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
            .testTag("worker_checkin_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large circular animated radar
        Box(
            modifier = Modifier
                .size(150.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(BrandCardDark)
                .border(
                    3.dp,
                    if (isCheckedIn) BrandAmber else BrandPrimaryGreen,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCheckedIn) Icons.Default.Timer else Icons.Default.TouchApp,
                contentDescription = null,
                tint = if (isCheckedIn) BrandAmber else BrandPrimaryGreen,
                modifier = Modifier.size(68.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Shift Status Header
        Text(
            text = if (isCheckedIn) "Active Shift: Royal Feast Caterers" else "Ready for your shift?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isCheckedIn)
                "Geo-Fence Verified • Checked-In at ${worker.checkInTimestamp ?: "5:02 PM Today"}\nShift timer running. Maintain uniform & code of conduct."
            else
                "Perform Check-In via QR scanner or venue GPS when you reach the business venue.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Big Primary Action Button
        Button(
            onClick = {
                if (isCheckedIn) onCheckOut() else onOpenCheckInScanner()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("worker_checkin_checkout_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCheckedIn) BrandErrorRed else BrandPrimaryGreen,
                contentColor = if (isCheckedIn) Color.White else Color.Black
            )
        ) {
            Icon(
                imageVector = if (isCheckedIn) Icons.Default.Logout else Icons.Default.QrCodeScanner,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isCheckedIn) "QR CHECK-OUT SHIFT" else "VERIFY & CHECK-IN (QR / GPS)",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Verification Status Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCardDark),
            border = BorderStroke(1.dp, BrandCardBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Shift Check-In Protocols",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                CheckInProtocolTile(
                    icon = Icons.Default.LocationOn,
                    title = "GPS Geo-Fence Matching",
                    desc = "Check-in allowed within 100m radius of banquet hall venue."
                )

                CheckInProtocolTile(
                    icon = Icons.Default.QrCode,
                    title = "Business Venue QR Scan",
                    desc = "Scan the DailyCrew manager QR code placed at reception or supervisor desk."
                )

                CheckInProtocolTile(
                    icon = Icons.Default.Security,
                    title = "Timestamp Proof & Payout Lock",
                    desc = "Guarantees wage release automatically upon supervisor shift closure."
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun CheckInProtocolTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
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
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
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
