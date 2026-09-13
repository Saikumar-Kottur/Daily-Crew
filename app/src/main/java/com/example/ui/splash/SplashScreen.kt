package com.example.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.AppLogo
import com.example.ui.components.AppLogoSize
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DailyCrewSplashScreen(
    onContinue: () -> Unit
) {
    // Entrance animations
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        // Auto navigate after 2.5 seconds if user doesn't tap earlier
        delay(2500)
        onContinue()
    }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.7f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "content_alpha"
    )

    // Infinite breathing glow for the trust shield
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandDeepBlueDark,
                        BrandDeepBlue,
                        Color(0xFF040A10)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .testTag("dailycrew_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .alpha(alpha)
        ) {
            Spacer(modifier = Modifier.weight(0.8f))

            // Prominent DailyCrew Logo Widget with Deep Blue and Green Success Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(glowScale)
                    .testTag("splash_app_logo_container")
            ) {
                AppLogo(
                    size = AppLogoSize.HERO,
                    isHeroLayoutVertical = true,
                    showText = true,
                    showTagline = true,
                    customTagline = "Verified People. Trusted Work.",
                    badgeText = "VERIFIED",
                    modifier = Modifier.testTag("splash_dailycrew_logo")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Value Proposition Chip in Deep Blue & Success Green Border
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BrandDeepBlueContainer,
                border = BorderStroke(1.dp, BrandDeepBlueBorder),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BrandSuccessGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hyperlocal On-Demand Workforce Ecosystem",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Key Trust Pillars in Deep Blue Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandDeepBlueCard),
                border = BorderStroke(1.dp, BrandDeepBlueBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SplashFeatureItem(
                        icon = Icons.Default.Security,
                        title = "Zero-Fraud Escrow Guarantee",
                        subtitle = "Anti-fake job deposits & automated wage disbursal"
                    )
                    SplashFeatureItem(
                        icon = Icons.Default.Speed,
                        title = "30-Min Emergency Staffing",
                        subtitle = "Instant broadcast to verified standby talent"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enter Platform CTA Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("splash_continue_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandSuccessGreen,
                    contentColor = BrandDeepBlueDark
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "ENTER DAILYCREW",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = BrandDeepBlueDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Subtle Footer
            Text(
                text = "Hyderabad Pilot • Uppal • Habsiguda • Tarnaka • Nacharam",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SplashFeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BrandSuccessGreenDim),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BrandSuccessGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            )
        }
    }
}
