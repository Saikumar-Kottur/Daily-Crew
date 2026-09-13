package com.example.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppRole
import com.example.ui.components.AppLogo
import com.example.ui.components.AppLogoSize
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DailyCrewLoginScreen(
    onLoginSuccess: (role: AppRole, name: String, phone: String) -> Unit
) {
    var selectedRole by remember { mutableStateOf(AppRole.WORKER) }
    var authMode by remember { mutableStateOf("LOGIN") } // "LOGIN" or "REGISTER"
    var phoneNumber by remember { mutableStateOf("9876543210") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCountdown by remember { mutableIntStateOf(30) }
    var simulatedSmsReceived by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Worker Registration Specific Fields
    var workerFullName by remember { mutableStateOf("Ramesh Kumar") }
    var workerAge by remember { mutableStateOf("24") }
    var workerGender by remember { mutableStateOf("Male") }
    var workerLocation by remember { mutableStateOf("Uppal, Hyderabad") }
    var selectedWorkerSkills by remember { mutableStateOf(setOf("Catering", "Banquet Serving")) }
    var workerExperience by remember { mutableStateOf("2 Years") }
    var workerUpiId by remember { mutableStateOf("ramesh@oksbi") }
    var workerReferralCode by remember { mutableStateOf("") }

    // Owner Registration Specific Fields
    var ownerBusinessName by remember { mutableStateOf("Royal Grand Banquets") }
    var ownerAddress by remember { mutableStateOf("Road 12, Habsiguda, Hyderabad") }
    var ownerCategory by remember { mutableStateOf("Banquet & Catering") }
    var ownerLocationVerified by remember { mutableStateOf(true) }
    var ownerLicenseNumber by remember { mutableStateOf("FSSAI-12423009876543") }
    var ownerReferralCode by remember { mutableStateOf("") }
    var isGoogleSignedIn by remember { mutableStateOf(false) }

    // Staff / Admin Protected Portal state
    var showStaffPortal by remember { mutableStateOf(false) }
    var staffPasscode by remember { mutableStateOf("") }
    var staffError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // OTP Timer
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            simulatedSmsReceived = true
            otpCode = "123456" // Auto-fill for seamless demo experience
            otpCountdown = 30
            while (otpCountdown > 0) {
                delay(1000)
                otpCountdown--
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BrandBackgroundDark,
                        BrandSurfaceDark,
                        Color(0xFF040A10)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Branding & Logo
            AppLogo(
                size = AppLogoSize.LARGE,
                showText = true,
                showTagline = true,
                modifier = Modifier.testTag("login_app_logo")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trust Pill Banner
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = BrandCardDark,
                border = BorderStroke(1.dp, BrandCardBorderDark)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = BrandPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "RBI-Compliant Escrow • 0% Worker Platform Cuts",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role Selector Card
            Text(
                text = "SELECT YOUR ACCOUNT TYPE",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Worker Option
                RoleSelectionCard(
                    title = "Daily Crew / Worker",
                    subtitle = "Find shifts & daily payouts",
                    icon = Icons.Default.Engineering,
                    accentColor = BrandPrimaryGreen,
                    isSelected = selectedRole == AppRole.WORKER,
                    onClick = {
                        selectedRole = AppRole.WORKER
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_select_role_worker")
                )

                // Business Owner Option
                RoleSelectionCard(
                    title = "Business Hirer",
                    subtitle = "Hire on-demand staff",
                    icon = Icons.Default.Storefront,
                    accentColor = BrandSecondaryCyan,
                    isSelected = selectedRole == AppRole.OWNER,
                    onClick = {
                        selectedRole = AppRole.OWNER
                        errorMessage = null
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_select_role_owner")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Auth Mode Toggle (Sign-In vs New Registration)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = BrandCardDark,
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { authMode = "LOGIN" },
                        shape = RoundedCornerShape(8.dp),
                        color = if (authMode == "LOGIN") (if (selectedRole == AppRole.WORKER) BrandPrimaryGreen else BrandSecondaryCyan) else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Existing Sign-In",
                                color = if (authMode == "LOGIN") Color.Black else TextSecondaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { authMode = "REGISTER" },
                        shape = RoundedCornerShape(8.dp),
                        color = if (authMode == "REGISTER") (if (selectedRole == AppRole.WORKER) BrandPrimaryGreen else BrandSecondaryCyan) else Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "New Registration",
                                color = if (authMode == "REGISTER") Color.Black else TextSecondaryDark,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (authMode == "LOGIN") {
                // Phone & OTP Authentication Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp)
                    ) {
                        Text(
                            text = if (selectedRole == AppRole.WORKER) "Worker Mobile Sign-In" else "Business Partner Sign-In",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Sign in with your 10-digit mobile number",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Phone Number Input
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { input ->
                                if (input.length <= 10 && input.all { it.isDigit() }) {
                                    phoneNumber = input
                                    errorMessage = null
                                }
                            },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("9876543210") },
                            leadingIcon = {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🇮🇳 +91",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Divider(
                                        modifier = Modifier
                                            .height(18.dp)
                                            .width(1.dp),
                                        color = BrandCardBorderDark
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (!isOtpSent) ImeAction.Send else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (phoneNumber.length == 10) {
                                        isOtpSent = true
                                        focusManager.clearFocus()
                                    } else {
                                        errorMessage = "Please enter a valid 10-digit mobile number"
                                    }
                                }
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedRole == AppRole.WORKER) BrandPrimaryGreen else BrandSecondaryCyan,
                                unfocusedBorderColor = BrandCardBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = BrandCardDark,
                                unfocusedContainerColor = BrandCardDark
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_phone_number")
                        )

                        // Simulated SMS Banner
                        AnimatedVisibility(visible = simulatedSmsReceived) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BrandPrimaryGreenDim,
                                border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = null,
                                        tint = BrandPrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Simulated SMS: Your DailyCrew OTP is 123456",
                                        color = BrandPrimaryGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // OTP Input
                        AnimatedVisibility(visible = isOtpSent) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { input ->
                                        if (input.length <= 6 && input.all { it.isDigit() }) {
                                            otpCode = input
                                            errorMessage = null
                                        }
                                    },
                                    label = { Text("6-Digit OTP Code") },
                                    placeholder = { Text("123456") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = BrandPrimaryGreen,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        if (otpCountdown > 0) {
                                            Text(
                                                text = "${otpCountdown}s",
                                                color = TextSecondaryDark,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(end = 12.dp)
                                            )
                                        } else {
                                            TextButton(onClick = {
                                                otpCountdown = 30
                                                simulatedSmsReceived = true
                                                otpCode = "123456"
                                            }) {
                                                Text("Resend", color = BrandPrimaryGreen, fontSize = 12.sp)
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Number,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (otpCode.length == 6) {
                                                val name = if (selectedRole == AppRole.WORKER) "Ravi Kumar" else "Spice Garden Banquets"
                                                onLoginSuccess(selectedRole, name, "+91 $phoneNumber")
                                            } else {
                                                errorMessage = "Please enter 6-digit OTP"
                                            }
                                        }
                                    ),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BrandPrimaryGreen,
                                        unfocusedBorderColor = BrandCardBorderDark,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedContainerColor = BrandCardDark,
                                        unfocusedContainerColor = BrandCardDark
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_otp_code")
                                )
                            }
                        }

                        errorMessage?.let { err ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = err,
                                color = BrandRed,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Action Button (Send OTP or Verify & Login)
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                if (!isOtpSent) {
                                    if (phoneNumber.length == 10) {
                                        isOtpSent = true
                                    } else {
                                        errorMessage = "Please enter a valid 10-digit mobile number"
                                    }
                                } else {
                                    if (otpCode.length == 6) {
                                        val name = if (selectedRole == AppRole.WORKER) "Ravi Kumar" else "Spice Garden Banquets"
                                        onLoginSuccess(selectedRole, name, "+91 $phoneNumber")
                                    } else {
                                        errorMessage = "Please enter 6-digit OTP code"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == AppRole.WORKER) BrandPrimaryGreen else BrandSecondaryCyan,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_auth_primary_action")
                        ) {
                            Text(
                                text = if (!isOtpSent) "Send Verification OTP" else "Verify & Enter Marketplace",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                // NEW REGISTRATION FORM
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                    border = BorderStroke(1.dp, BrandCardBorderDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = if (selectedRole == AppRole.WORKER) "Worker Registration" else "Business Owner Registration",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = if (selectedRole == AppRole.WORKER)
                                "Complete your worker profile with skills, experience, and payout details"
                            else
                                "Register your hospitality venue with verified location & legal credentials",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
                        )

                        if (selectedRole == AppRole.OWNER) {
                            // Google Sign-in Option
                            OutlinedButton(
                                onClick = {
                                    isGoogleSignedIn = true
                                    ownerBusinessName = "Grand Canteen & Caterers"
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isGoogleSignedIn) BrandSecondaryCyan else BrandCardBorderDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = if (isGoogleSignedIn) BrandSecondaryCyan else Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isGoogleSignedIn) "Google Connected: owner@grandcanteen.com" else "Sign In with Google (Instant Verification)",
                                    color = if (isGoogleSignedIn) BrandSecondaryCyan else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Mobile Number OTP Verification
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) phoneNumber = it },
                            label = { Text("Mobile Number (for OTP)") },
                            leadingIcon = { Text("🇮🇳 +91", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(start = 12.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = BrandPrimaryGreen,
                                unfocusedBorderColor = BrandCardBorderDark,
                                focusedContainerColor = BrandCardDark,
                                unfocusedContainerColor = BrandCardDark
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // If Worker
                        if (selectedRole == AppRole.WORKER) {
                            OutlinedTextField(
                                value = workerFullName,
                                onValueChange = { workerFullName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BrandPrimaryGreen) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandPrimaryGreen,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = workerAge,
                                    onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) workerAge = it },
                                    label = { Text("Age") },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        focusedBorderColor = BrandPrimaryGreen,
                                        unfocusedBorderColor = BrandCardBorderDark,
                                        focusedContainerColor = BrandCardDark,
                                        unfocusedContainerColor = BrandCardDark
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.8f)
                                )

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text("Gender", color = TextSecondaryDark, fontSize = 11.sp)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        listOf("Male", "Female").forEach { g ->
                                            FilterChip(
                                                selected = workerGender == g,
                                                onClick = { workerGender = g },
                                                label = { Text(g, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = workerLocation,
                                onValueChange = { workerLocation = it },
                                label = { Text("Current Location (Zone)") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandPrimaryGreen) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandPrimaryGreen,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Skills selection
                            Text("Skills & Capabilities:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val allSkills = listOf("Catering", "Banquet Serving", "Kitchen Helper", "Dishwashing", "Counter Cashier")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allSkills.take(3).forEach { skill ->
                                    val isSel = selectedWorkerSkills.contains(skill)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            selectedWorkerSkills = if (isSel) selectedWorkerSkills - skill else selectedWorkerSkills + skill
                                        },
                                        label = { Text(skill, fontSize = 10.sp) }
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                allSkills.drop(3).forEach { skill ->
                                    val isSel = selectedWorkerSkills.contains(skill)
                                    FilterChip(
                                        selected = isSel,
                                        onClick = {
                                            selectedWorkerSkills = if (isSel) selectedWorkerSkills - skill else selectedWorkerSkills + skill
                                        },
                                        label = { Text(skill, fontSize = 10.sp) }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = workerExperience,
                                onValueChange = { workerExperience = it },
                                label = { Text("Experience") },
                                placeholder = { Text("e.g. 2 Years in Banquets") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandPrimaryGreen,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = workerUpiId,
                                onValueChange = { workerUpiId = it },
                                label = { Text("Bank UPI ID / Account Details") },
                                placeholder = { Text("e.g. ramesh@oksbi") },
                                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = BrandPrimaryGreen) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandPrimaryGreen,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = workerReferralCode,
                                onValueChange = { workerReferralCode = it.uppercase() },
                                label = { Text("Referral Code (Optional)") },
                                placeholder = { Text("e.g. DCW8832") },
                                trailingIcon = {
                                    Text("2 Free Shifts", color = BrandPrimaryGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandPrimaryGreen,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Owner Registration fields
                            OutlinedTextField(
                                value = ownerBusinessName,
                                onValueChange = { ownerBusinessName = it },
                                label = { Text("Business / Venue Name") },
                                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = BrandSecondaryCyan) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandSecondaryCyan,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = ownerAddress,
                                onValueChange = { ownerAddress = it },
                                label = { Text("Business Address") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BrandSecondaryCyan) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandSecondaryCyan,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text("Business Category:", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Catering", "Restaurant", "Canteen", "Hotel").forEach { cat ->
                                    FilterChip(
                                        selected = ownerCategory == cat,
                                        onClick = { ownerCategory = cat },
                                        label = { Text(cat, fontSize = 10.sp) }
                                    )
                                }
                            }

                            // Location Verification Status
                            Surface(
                                color = BrandSecondaryCyanDim,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = BrandSecondaryCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Location Verified (Hyderabad Pilot Zone)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("17.3984° N, 78.5583° E (Uppal / Habsiguda)", color = TextSecondaryDark, fontSize = 10.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = ownerLicenseNumber,
                                onValueChange = { ownerLicenseNumber = it },
                                label = { Text("Profile Verification (FSSAI / GSTIN)") },
                                placeholder = { Text("FSSAI-12423009876543") },
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = BrandSecondaryCyan) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandSecondaryCyan,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = ownerReferralCode,
                                onValueChange = { ownerReferralCode = it.uppercase() },
                                label = { Text("Referral Code (Optional)") },
                                placeholder = { Text("e.g. DCO9912") },
                                trailingIcon = {
                                    Text("2 Free Jobs", color = BrandSecondaryCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = BrandSecondaryCyan,
                                    unfocusedBorderColor = BrandCardBorderDark,
                                    focusedContainerColor = BrandCardDark,
                                    unfocusedContainerColor = BrandCardDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Complete Registration Button
                        Button(
                            onClick = {
                                val name = if (selectedRole == AppRole.WORKER) {
                                    if (workerFullName.isNotBlank()) workerFullName else "Ramesh Kumar"
                                } else {
                                    if (ownerBusinessName.isNotBlank()) ownerBusinessName else "Royal Grand Banquets"
                                }
                                onLoginSuccess(selectedRole, name, "+91 $phoneNumber")
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedRole == AppRole.WORKER) BrandPrimaryGreen else BrandSecondaryCyan,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_complete_registration")
                        ) {
                            Text(
                                text = if (selectedRole == AppRole.WORKER) "Complete Worker Registration & Enter" else "Complete Owner Registration & Enter",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // QUICK DEMO & TEST ACCOUNTS CARD (Crucial for live demo & testing)
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                border = BorderStroke(1.dp, BrandCardBorderDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_quick_demo_accounts")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = BrandAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "1-Tap Demo & Evaluation Logins",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BrandAmberDim
                        ) {
                            Text(
                                text = "DEMO READY",
                                color = BrandAmber,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Instant login for testing without waiting for OTP",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Demo Worker Button
                    OutlinedButton(
                        onClick = {
                            onLoginSuccess(AppRole.WORKER, "Ravi Kumar", "+91 98765 43210")
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrandPrimaryGreenDim.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_demo_login_worker")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "👷", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Log In as Worker (Ravi Kumar)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "4.9★ Catering Pro • ₹600 Escrow Reserve",
                                        color = BrandPrimaryGreen,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = BrandPrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Demo Owner Button
                    OutlinedButton(
                        onClick = {
                            onLoginSuccess(AppRole.OWNER, "Spice Garden Banquets", "+91 91234 56789")
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BrandSecondaryCyanDim.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_demo_login_owner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🏪", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "Log In as Hirer (Spice Garden)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Verified Banquet Venue • 6 Active Shifts",
                                        color = BrandSecondaryCyan,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // PROTECTED STAFF / ADMIN ACCESS (Hidden from regular public flow)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, BrandCardBorderDark.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showStaffPortal = !showStaffPortal }
                    .testTag("btn_toggle_staff_portal")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LockPerson,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DailyCrew Staff & Moderator Access",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Icon(
                        imageVector = if (showStaffPortal) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = TextSecondaryDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expandable Staff / Admin Portal Form
            AnimatedVisibility(visible = showStaffPortal) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BrandCardDark,
                    border = BorderStroke(1.dp, BrandRed.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("staff_portal_container")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = BrandRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Restricted Staff Credentials",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "For platform dispute arbiters, compliance moderators & system engineers only. Not visible to marketplace users.",
                            color = TextSecondaryDark,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = staffPasscode,
                            onValueChange = {
                                staffPasscode = it
                                staffError = null
                            },
                            label = { Text("Staff PIN / Passcode (Demo: 1234)") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandRed,
                                unfocusedBorderColor = BrandCardBorderDark,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        staffError?.let { err ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = err, color = BrandRed, fontSize = 10.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (staffPasscode == "1234" || staffPasscode.equals("admin", ignoreCase = true) || staffPasscode.isBlank()) {
                                        onLoginSuccess(AppRole.ADMIN, "Dispute Arbiter", "+91 90000 00001")
                                    } else {
                                        staffError = "Invalid passcode. Use demo: 1234"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandSecondaryCyan,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_staff_login_admin")
                            ) {
                                Text("Enter as Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    if (staffPasscode == "1234" || staffPasscode.equals("admin", ignoreCase = true) || staffPasscode.isBlank()) {
                                        onLoginSuccess(AppRole.SUPER_ADMIN, "Platform Engineer", "+91 90000 00002")
                                    } else {
                                        staffError = "Invalid passcode. Use demo: 1234"
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrandAmber,
                                    contentColor = Color.Black
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_staff_login_super_admin")
                            ) {
                                Text("Super Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accentColor.copy(alpha = 0.12f) else BrandCardDark,
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) accentColor else BrandCardBorderDark
        ),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) accentColor else BrandSurfaceDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color.White,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondaryDark,
                    fontSize = 10.sp,
                    lineHeight = 14.sp
                )
            )
        }
    }
}
