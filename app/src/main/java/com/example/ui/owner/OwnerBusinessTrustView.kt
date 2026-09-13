package com.example.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.model.BusinessProfile
import com.example.ui.theme.*

@Composable
fun OwnerBusinessTrustView(
    business: BusinessProfile,
    selectedLanguage: AppLanguage = business.selectedLanguage,
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onOpenVerificationDialog: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("owner_trust_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Language Quick Selection Card for Business Owner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("owner_language_selector_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (selectedLanguage) {
                                    AppLanguage.TELUGU -> "యాప్ భాష (Language)"
                                    AppLanguage.HINDI -> "ऐप भाषा (Language)"
                                    AppLanguage.ENGLISH -> "Language / భాష / भाषा"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandSecondaryCyan.copy(alpha = 0.15f),
                            border = BorderStroke(0.5.dp, BrandSecondaryCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${selectedLanguage.flagEmoji} ${selectedLanguage.nativeName}",
                                color = BrandSecondaryCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.values().forEach { lang ->
                            val isSelected = selectedLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) BrandSecondaryCyan else BrandCardDark,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) BrandSecondaryCyan else BrandCardBorderDark
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onSelectLanguage(lang) }
                                    .testTag("owner_lang_btn_${lang.code}")
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
        }
        // Business Score Overview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BrandSecondaryCyan,
                        modifier = Modifier.size(54.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Business Trust Score",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )

                    Text(
                        text = "Verified Marketplace Partner • Banjara Hills, Hyderabad",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BrandCardDark,
                        border = BorderStroke(1.dp, BrandCardBorderDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            BusinessStatItem(valStr = "⭐ ${business.rating}", label = "Worker Rating")
                            BusinessStatItem(valStr = "💰 ${business.reliabilityScore}%", label = "Payment Reliability")
                            BusinessStatItem(valStr = "🟢 ${business.fakeJobFlags}", label = "Dispute Flags")
                        }
                    }
                }
            }
        }

        // Room Database Business Verification Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(
                    1.5.dp,
                    if (business.isVerifiedInDb) BrandSecondaryCyan else BrandPrimaryGreen
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
                                color = if (business.isVerifiedInDb) BrandSecondaryCyan else BrandPrimaryGreen,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (business.isVerifiedInDb) Icons.Default.Verified else Icons.Default.UploadFile,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (business.isVerifiedInDb) "DATABASE VERIFIED ENTITY" else "PENDING BUSINESS ID UPLOAD",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (business.isVerifiedInDb) BrandSecondaryCyan else BrandPrimaryGreen,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = business.businessName,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BrandSecondaryCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (business.isVerifiedInDb) "Room DB Active" else "Upload Required",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSecondaryCyan,
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
                            Text(text = "Primary Document / GSTIN", fontSize = 10.sp, color = TextSecondaryDark)
                            Text(text = "${business.verifiedDocumentType} (${business.gstin})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Room DB Record ID", fontSize = 10.sp, color = TextSecondaryDark)
                            Text(
                                text = business.verifiedDbRecordId,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BrandSecondaryCyan
                            )
                        }
                    }

                    Button(
                        onClick = onOpenVerificationDialog,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandSecondaryCyan,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("btn_owner_upload_verification")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (business.isVerifiedInDb) "View / Update Business ID & GSTIN" else "Upload Business ID & Mark Verified in DB",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Trust Factors Checklist
        item {
            Text(
                text = "Verification & Credibility Badges",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BusinessCheckTile(
                    title = "GSTIN Business Authenticity",
                    desc = "Taxpayer identifier 36AAAAA0000A1Z5 verified by Government portal.",
                    isPassed = business.isVerified
                )
                BusinessCheckTile(
                    title = "Geo-Verified Physical Venue",
                    desc = "Banjara Hills banquet premises confirmed via Google Maps & GPS pin.",
                    isPassed = true
                )
                BusinessCheckTile(
                    title = "Escrow Auto-Disbursement Record",
                    desc = "Zero pending wage defaults; 184 completed worker payouts settled.",
                    isPassed = true
                )
                BusinessCheckTile(
                    title = "Safe Workplace Certification",
                    desc = "DailyCrew safety standards, hygienic staff area, and provided meals.",
                    isPassed = true
                )
            }
        }

        // Owner Security Deposit & Escrow Ledger
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_owner_escrow_ledger")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Security Deposit & Escrow Ledger",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BrandPrimaryGreenDim
                        ) {
                            Text(
                                text = "RBI Escrow Backed",
                                color = BrandPrimaryGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
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
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Current Escrow Balance",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹1,800",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "6 shifts backed",
                                    color = BrandSecondaryCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BrandCardDark,
                            border = BorderStroke(1.dp, BrandCardBorderDark),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Commitment Deposit",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                                Text(
                                    text = "₹300 / Job",
                                    color = BrandPrimaryGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Cancellation Guarantee",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Past Security Deposit Commitments:",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        OwnerEscrowTxnRow(
                            title = "Security Deposit Locked (6 Jobs)",
                            amount = "₹1,800",
                            status = "ACTIVE ESCROW",
                            isCredit = false
                        )
                        OwnerEscrowTxnRow(
                            title = "Escrow Payout Settled (Grand Hotel Banquet)",
                            amount = "₹1,250",
                            status = "SETTLED TO WORKER",
                            isCredit = true
                        )
                        OwnerEscrowTxnRow(
                            title = "Deposit Refund (Pre-acceptance cancel)",
                            amount = "₹300",
                            status = "REFUNDED",
                            isCredit = true
                        )
                    }
                }
            }
        }

        // Escrow Policy Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = BrandPrimaryGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DailyCrew Trust Guarantee",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = Color.White)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Businesses with 95%+ Payment Reliability receive 3x more applicant responses and priority ranking in the Nearby Shifts feed for top-tier workers.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
private fun BusinessStatItem(valStr: String, label: String) {
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
private fun BusinessCheckTile(title: String, desc: String, isPassed: Boolean) {
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
            Icon(
                imageVector = if (isPassed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isPassed) BrandSecondaryCyan else TextSecondaryDark,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
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
}

@Composable
private fun OwnerEscrowTxnRow(
    title: String,
    amount: String,
    status: String,
    isCredit: Boolean
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BrandCardDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = status,
                    color = if (isCredit) BrandPrimaryGreen else BrandSecondaryCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = amount,
                color = if (isCredit) BrandPrimaryGreen else BrandAmber,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

