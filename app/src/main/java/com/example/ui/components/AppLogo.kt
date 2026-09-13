package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

enum class AppLogoSize(
    val iconBoxSize: Dp,
    val iconSize: Dp,
    val titleFontSize: TextUnit,
    val taglineFontSize: TextUnit,
    val letterSpacing: TextUnit,
    val cornerRadius: Dp
) {
    COMPACT(
        iconBoxSize = 38.dp,
        iconSize = 28.dp,
        titleFontSize = 16.sp,
        taglineFontSize = 9.sp,
        letterSpacing = 1.2.sp,
        cornerRadius = 10.dp
    ),
    STANDARD(
        iconBoxSize = 48.dp,
        iconSize = 36.dp,
        titleFontSize = 20.sp,
        taglineFontSize = 11.sp,
        letterSpacing = 1.8.sp,
        cornerRadius = 12.dp
    ),
    LARGE(
        iconBoxSize = 72.dp,
        iconSize = 56.dp,
        titleFontSize = 26.sp,
        taglineFontSize = 13.sp,
        letterSpacing = 2.4.sp,
        cornerRadius = 18.dp
    ),
    HERO(
        iconBoxSize = 140.dp,
        iconSize = 120.dp,
        titleFontSize = 32.sp,
        taglineFontSize = 15.sp,
        letterSpacing = 3.sp,
        cornerRadius = 28.dp
    )
}

/**
 * Reusable, modern, and enterprise-grade AppLogo widget for DAILYCREW.
 * Encapsulates the signature Deep Blue and Green Success visual identity.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    size: AppLogoSize = AppLogoSize.STANDARD,
    showText: Boolean = true,
    showTagline: Boolean = true,
    titleText: String = "DAILYCREW",
    customTagline: String? = null,
    badgeText: String? = null,
    isHeroLayoutVertical: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(size.cornerRadius))
            .clickable { onClick() }
    } else {
        modifier
    }

    if (isHeroLayoutVertical) {
        Column(
            modifier = clickableModifier.testTag("app_logo_vertical"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LogoIconBadge(size = size)

            if (showText) {
                Spacer(modifier = Modifier.height(16.dp))
                LogoWordmark(size = size, titleText = titleText, badgeText = badgeText, isCentered = true)

                if (showTagline) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LogoTagline(
                        size = size,
                        customTagline = customTagline,
                        isCentered = true
                    )
                }
            }
        }
    } else {
        Row(
            modifier = clickableModifier.testTag("app_logo_horizontal"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoIconBadge(size = size)

            if (showText) {
                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    LogoWordmark(size = size, titleText = titleText, badgeText = badgeText, isCentered = false)

                    if (showTagline) {
                        Spacer(modifier = Modifier.height(2.dp))
                        LogoTagline(
                            size = size,
                            customTagline = customTagline,
                            isCentered = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LogoIconBadge(size: AppLogoSize) {
    Box(
        modifier = Modifier
            .size(size.iconBoxSize)
            .clip(RoundedCornerShape(size.cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BrandDeepBlueContainer,
                        BrandDeepBlue,
                        BrandDeepBlueDark
                    )
                )
            )
            .border(
                BorderStroke(
                    width = if (size == AppLogoSize.HERO) 2.5.dp else 1.5.dp,
                    color = BrandSuccessGreen.copy(alpha = 0.85f)
                ),
                RoundedCornerShape(size.cornerRadius)
            )
            .padding(if (size == AppLogoSize.HERO) 12.dp else 4.dp)
            .testTag("app_logo_dc_icon"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(
                id = if (size == AppLogoSize.HERO || size == AppLogoSize.LARGE) {
                    R.drawable.ic_dailycrew_logo_hero
                } else {
                    R.drawable.ic_dailycrew_brand_logo
                }
            ),
            contentDescription = "DAILYCREW DC Icon",
            modifier = Modifier.size(size.iconSize)
        )
    }
}

@Composable
private fun LogoWordmark(
    size: AppLogoSize,
    titleText: String,
    badgeText: String?,
    isCentered: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCentered) Arrangement.Center else Arrangement.Start
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                color = Color.White,
                fontSize = size.titleFontSize,
                letterSpacing = size.letterSpacing
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Vibrant Green Verified Trust indicator dot
        Box(
            modifier = Modifier
                .size(if (size == AppLogoSize.HERO) 10.dp else 7.dp)
                .clip(CircleShape)
                .background(BrandSuccessGreen)
        )

        if (!badgeText.isNullOrBlank()) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandDeepBlueContainer,
                border = BorderStroke(1.dp, BrandSuccessGreen.copy(alpha = 0.6f))
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandSuccessGreen,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    ),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun LogoTagline(
    size: AppLogoSize,
    customTagline: String?,
    isCentered: Boolean
) {
    Text(
        text = customTagline ?: "Verified People. Trusted Work.",
        style = MaterialTheme.typography.bodySmall.copy(
            color = BrandSuccessGreen.copy(alpha = 0.95f),
            fontWeight = FontWeight.SemiBold,
            fontSize = size.taglineFontSize,
            letterSpacing = 0.4.sp
        )
    )
}
