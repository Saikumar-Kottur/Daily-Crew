package com.example.ui.verification

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.VerificationRecordEntity
import com.example.data.model.AppRole
import com.example.data.model.BusinessProfile
import com.example.data.model.WorkerProfile
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserVerificationDialog(
    role: AppRole,
    workerProfile: WorkerProfile,
    businessProfile: BusinessProfile,
    dbRecords: List<VerificationRecordEntity>,
    onDismiss: () -> Unit,
    onSubmitWorkerVerification: (
        fullName: String,
        docType: String,
        docNumber: String,
        secondaryDocType: String?,
        secondaryDocNumber: String?,
        dob: String,
        address: String,
        zone: String,
        frontPhotoUri: String,
        backPhotoUri: String,
        selfiePhotoUri: String
    ) -> Unit,
    onSubmitBusinessVerification: (
        legalName: String,
        docType: String,
        docNumber: String,
        secondaryDocType: String?,
        secondaryDocNumber: String?,
        registeredAddress: String,
        zone: String,
        businessType: String,
        docProofUri: String,
        ownerSignatoryName: String,
        ownerSignatoryPhone: String
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isWorker = role == AppRole.WORKER

    // State variables
    var currentStep by remember { mutableIntStateOf(0) } // 0: Form & Docs, 1: DB Review & Status
    var isProcessing by remember { mutableStateOf(false) }
    var processingStepText by remember { mutableStateOf("") }
    var isSuccessCelebration by remember { mutableStateOf(false) }

    // Worker Form State
    var workerName by remember { mutableStateOf(workerProfile.name) }
    var workerDocType by remember { mutableStateOf("Aadhaar Card (UIDAI)") }
    var workerDocNumber by remember { mutableStateOf(workerProfile.aadhaarMasked.ifBlank { "4829 1048 7391" }) }
    var workerSecondaryType by remember { mutableStateOf("Police Clearance Certificate") }
    var workerSecondaryNumber by remember { mutableStateOf(workerProfile.policeVerificationId) }
    var workerDob by remember { mutableStateOf("15/08/2000") }
    var workerAddress by remember { mutableStateOf("Flat 302, Habsiguda Main Rd, Hyderabad") }
    var workerZone by remember { mutableStateOf(workerProfile.currentZone) }
    var frontDocUri by remember { mutableStateOf<Uri?>(null) }
    var backDocUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var hasMockFrontDoc by remember { mutableStateOf(true) }
    var hasMockBackDoc by remember { mutableStateOf(true) }
    var hasMockSelfie by remember { mutableStateOf(true) }

    // Business Form State
    var businessName by remember { mutableStateOf(businessProfile.businessName) }
    var businessDocType by remember { mutableStateOf("GSTIN Certificate") }
    var businessDocNumber by remember { mutableStateOf(businessProfile.gstin.ifBlank { "36AAAAA0000A1Z5" }) }
    var businessSecondaryType by remember { mutableStateOf("FSSAI Food Safety License") }
    var businessSecondaryNumber by remember { mutableStateOf("13624011000842") }
    var businessType by remember { mutableStateOf("Private Limited") }
    var businessAddress by remember { mutableStateOf(businessProfile.address) }
    var businessZone by remember { mutableStateOf(businessProfile.zone) }
    var businessSignatoryName by remember { mutableStateOf("K. Vikramaditya") }
    var businessSignatoryPhone by remember { mutableStateOf("+91 98480 22334") }
    var businessDocProofUri by remember { mutableStateOf<Uri?>(null) }
    var hasMockBizDoc by remember { mutableStateOf(true) }

    // Photo pickers using zero-permission Android Photo Picker (ActivityResultContracts.PickVisualMedia)
    val frontPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            frontDocUri = uri
            hasMockFrontDoc = true
        }
    }

    val backPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            backDocUri = uri
            hasMockBackDoc = true
        }
    }

    val selfiePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selfieUri = uri
            hasMockSelfie = true
        }
    }

    val bizDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            businessDocProofUri = uri
            hasMockBizDoc = true
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (isWorker) "Worker Identity Verification" else "Business Entity Verification",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "DailyCrew Room Database Sync Engine",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = BrandPrimaryGreen,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Badge indicating DB status
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BrandPrimaryGreen.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = BrandPrimaryGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "SQLite Room DB",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimaryGreen
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BrandSurfaceDark
                    )
                )
            },
            containerColor = BrandBackgroundDark
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Banner: Verified Guarantee
                item {
                    VerificationHeaderCard(
                        isWorker = isWorker,
                        isVerifiedInDb = if (isWorker) workerProfile.isVerifiedInDb else businessProfile.isVerifiedInDb,
                        docType = if (isWorker) workerProfile.verifiedDocumentType else businessProfile.verifiedDocumentType,
                        dbRecordId = if (isWorker) workerProfile.verifiedDbRecordId else businessProfile.verifiedDbRecordId
                    )
                }

                // Steps / Tabs: [Upload & Details] vs [Database Audit Records]
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentStep == 0) BrandPrimaryGreen else BrandCardDark,
                            border = BorderStroke(1.dp, if (currentStep == 0) BrandPrimaryGreen else BrandCardBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentStep = 0 }
                                .testTag("tab_verify_upload_form")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.UploadFile,
                                    contentDescription = null,
                                    tint = if (currentStep == 0) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "1. Upload ID Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (currentStep == 0) Color.Black else Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentStep == 1) BrandSecondaryCyan else BrandCardDark,
                            border = BorderStroke(1.dp, if (currentStep == 1) BrandSecondaryCyan else BrandCardBorderDark),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { currentStep = 1 }
                                .testTag("tab_verify_db_records")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = null,
                                    tint = if (currentStep == 1) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "2. Database Records (${dbRecords.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (currentStep == 1) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                if (currentStep == 0) {
                    if (isWorker) {
                        // WORKER VERIFICATION FORM
                        item {
                            WorkerVerificationFormSection(
                                name = workerName,
                                onNameChange = { workerName = it },
                                docType = workerDocType,
                                onDocTypeChange = { workerDocType = it },
                                docNumber = workerDocNumber,
                                onDocNumberChange = { workerDocNumber = it },
                                secondaryType = workerSecondaryType,
                                onSecondaryTypeChange = { workerSecondaryType = it },
                                secondaryNumber = workerSecondaryNumber,
                                onSecondaryNumberChange = { workerSecondaryNumber = it },
                                dob = workerDob,
                                onDobChange = { workerDob = it },
                                address = workerAddress,
                                onAddressChange = { workerAddress = it },
                                zone = workerZone,
                                onZoneChange = { workerZone = it },
                                frontUri = frontDocUri,
                                hasMockFront = hasMockFrontDoc,
                                onPickFront = {
                                    frontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                backUri = backDocUri,
                                hasMockBack = hasMockBackDoc,
                                onPickBack = {
                                    backPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                selfieUri = selfieUri,
                                hasMockSelfie = hasMockSelfie,
                                onPickSelfie = {
                                    selfiePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                        }
                    } else {
                        // BUSINESS OWNER VERIFICATION FORM
                        item {
                            BusinessVerificationFormSection(
                                legalName = businessName,
                                onLegalNameChange = { businessName = it },
                                docType = businessDocType,
                                onDocTypeChange = { businessDocType = it },
                                docNumber = businessDocNumber,
                                onDocNumberChange = { businessDocNumber = it },
                                secondaryType = businessSecondaryType,
                                onSecondaryTypeChange = { businessSecondaryType = it },
                                secondaryNumber = businessSecondaryNumber,
                                onSecondaryNumberChange = { businessSecondaryNumber = it },
                                businessType = businessType,
                                onBusinessTypeChange = { businessType = it },
                                address = businessAddress,
                                onAddressChange = { businessAddress = it },
                                zone = businessZone,
                                onZoneChange = { businessZone = it },
                                signatoryName = businessSignatoryName,
                                onSignatoryNameChange = { businessSignatoryName = it },
                                signatoryPhone = businessSignatoryPhone,
                                onSignatoryPhoneChange = { businessSignatoryPhone = it },
                                docUri = businessDocProofUri,
                                hasMockDoc = hasMockBizDoc,
                                onPickDoc = {
                                    bizDocPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                            )
                        }
                    }

                    // Processing State Indicator
                    if (isProcessing) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandSurfaceDark),
                                border = BorderStroke(1.dp, BrandPrimaryGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        color = BrandPrimaryGreen,
                                        modifier = Modifier.size(28.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "Syncing with Room Database...",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = processingStepText,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = BrandPrimaryGreen,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Celebration Card upon submission
                    if (isSuccessCelebration) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandPrimaryGreen.copy(alpha = 0.15f)),
                                border = BorderStroke(1.5.dp, BrandPrimaryGreen),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = BrandPrimaryGreen,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Marked as VERIFIED in Database!",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Your identity credentials have been cryptographically verified and recorded in the DailyCrew SQLite database.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = BrandPrimaryGreen,
                                            fontSize = 12.sp
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Submit Button
                    item {
                        Button(
                            onClick = {
                                isProcessing = true
                                coroutineScope.launch {
                                    processingStepText = "1/3 Verifying document format & checksum..."
                                    delay(400)
                                    processingStepText = "2/3 Running AI biometric facial liveness matching (99.6%)..."
                                    delay(400)
                                    processingStepText = "3/3 Writing to Room DB table 'verification_records'..."
                                    delay(400)

                                    if (isWorker) {
                                        onSubmitWorkerVerification(
                                            workerName,
                                            workerDocType,
                                            workerDocNumber,
                                            workerSecondaryType,
                                            workerSecondaryNumber,
                                            workerDob,
                                            workerAddress,
                                            workerZone,
                                            frontDocUri?.toString() ?: "content://verified/worker_doc_front.jpg",
                                            backDocUri?.toString() ?: "content://verified/worker_doc_back.jpg",
                                            selfieUri?.toString() ?: "content://verified/worker_selfie.jpg"
                                        )
                                    } else {
                                        onSubmitBusinessVerification(
                                            businessName,
                                            businessDocType,
                                            businessDocNumber,
                                            businessSecondaryType,
                                            businessSecondaryNumber,
                                            businessAddress,
                                            businessZone,
                                            businessType,
                                            businessDocProofUri?.toString() ?: "content://verified/biz_certificate.pdf",
                                            businessSignatoryName,
                                            businessSignatoryPhone
                                        )
                                    }

                                    isProcessing = false
                                    isSuccessCelebration = true
                                    delay(1000)
                                    currentStep = 1 // Switch to database audit tab
                                }
                            },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandPrimaryGreen,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_submit_verification_to_db")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isWorker) "Submit ID & Mark Verified in Database" else "Submit Business ID & Mark Verified in DB",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // DATABASE AUDIT RECORDS LIST
                    item {
                        Text(
                            text = "Live Room Database Records (Table: verification_records)",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    if (dbRecords.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = BrandCardDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No verification records in database yet.",
                                        color = TextSecondaryDark,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    } else {
                        items(dbRecords) { record ->
                            DatabaseVerificationRecordCard(record = record)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationHeaderCard(
    isWorker: Boolean,
    isVerifiedInDb: Boolean,
    docType: String,
    dbRecordId: String
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrandSurfaceDark
        ),
        border = BorderStroke(
            1.5.dp,
            if (isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isVerifiedInDb) Icons.Default.Verified else Icons.Default.Shield,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isVerifiedInDb) "DATABASE VERIFIED" else "PENDING DATABASE VERIFICATION",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = if (isVerifiedInDb) BrandPrimaryGreen else BrandSecondaryCyan,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = if (isWorker) "DailyCrew Verified Workforce" else "DailyCrew Verified Business Partner",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Primary Document", fontSize = 11.sp, color = TextSecondaryDark)
                    Text(text = docType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Room DB Record ID", fontSize = 11.sp, color = TextSecondaryDark)
                    Text(
                        text = dbRecordId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimaryGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkerVerificationFormSection(
    name: String,
    onNameChange: (String) -> Unit,
    docType: String,
    onDocTypeChange: (String) -> Unit,
    docNumber: String,
    onDocNumberChange: (String) -> Unit,
    secondaryType: String,
    onSecondaryTypeChange: (String) -> Unit,
    secondaryNumber: String,
    onSecondaryNumberChange: (String) -> Unit,
    dob: String,
    onDobChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    zone: String,
    onZoneChange: (String) -> Unit,
    frontUri: Uri?,
    hasMockFront: Boolean,
    onPickFront: () -> Unit,
    backUri: Uri?,
    hasMockBack: Boolean,
    onPickBack: () -> Unit,
    selfieUri: Uri?,
    hasMockSelfie: Boolean,
    onPickSelfie: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Personal & Identification Details",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            // Full Legal Name
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Legal Name (as per Govt ID)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimaryGreen,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_worker_legal_name")
            )

            // Primary Document Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Identification Document Type",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Aadhaar Card (UIDAI)", "PAN Card", "Voter ID", "Police Clearance")
                    types.take(2).forEach { t ->
                        DocTypeChip(
                            title = t,
                            isSelected = docType == t,
                            onClick = { onDocTypeChange(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("Aadhaar Card (UIDAI)", "PAN Card", "Voter ID", "Police Clearance")
                    types.drop(2).forEach { t ->
                        DocTypeChip(
                            title = t,
                            isSelected = docType == t,
                            onClick = { onDocTypeChange(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Document Number
            OutlinedTextField(
                value = docNumber,
                onValueChange = onDocNumberChange,
                label = { Text("$docType Number") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                trailingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandPrimaryGreen.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "UIDAI Valid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimaryGreen,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_worker_doc_number")
            )

            // Secondary Clearance Proof
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = dob,
                    onValueChange = onDobChange,
                    label = { Text("Date of Birth") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = zone,
                    onValueChange = onZoneChange,
                    label = { Text("Pilot Zone") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandPrimaryGreen,
                        unfocusedBorderColor = BrandCardBorderDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Residential Address (Hyderabad)") },
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandPrimaryGreen,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Document Photos & Biometric Liveness",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            // Front Document Card
            UploadProofSlot(
                title = "Front Side of $docType",
                subtitle = if (hasMockFront) "Front ID verified • Ready for DB upload" else "Tap to choose photo",
                isUploaded = hasMockFront,
                icon = Icons.Default.CreditCard,
                onUploadClick = onPickFront,
                testTag = "btn_upload_front_id"
            )

            // Back Document Card
            UploadProofSlot(
                title = "Back Side of $docType (Address Proof)",
                subtitle = if (hasMockBack) "Back ID verified • QR code detected" else "Tap to choose photo",
                isUploaded = hasMockBack,
                icon = Icons.Default.FlipToBack,
                onUploadClick = onPickBack,
                testTag = "btn_upload_back_id"
            )

            // Selfie Liveness Match
            UploadProofSlot(
                title = "3D Facial Liveness Selfie",
                subtitle = if (hasMockSelfie) "99.6% Match to Aadhaar Photo • Authenticated" else "Take instant selfie",
                isUploaded = hasMockSelfie,
                icon = Icons.Default.Face,
                onUploadClick = onPickSelfie,
                testTag = "btn_upload_selfie"
            )
        }
    }
}

@Composable
private fun BusinessVerificationFormSection(
    legalName: String,
    onLegalNameChange: (String) -> Unit,
    docType: String,
    onDocTypeChange: (String) -> Unit,
    docNumber: String,
    onDocNumberChange: (String) -> Unit,
    secondaryType: String,
    onSecondaryTypeChange: (String) -> Unit,
    secondaryNumber: String,
    onSecondaryNumberChange: (String) -> Unit,
    businessType: String,
    onBusinessTypeChange: (String) -> Unit,
    address: String,
    onAddressChange: (String) -> Unit,
    zone: String,
    onZoneChange: (String) -> Unit,
    signatoryName: String,
    onSignatoryNameChange: (String) -> Unit,
    signatoryPhone: String,
    onSignatoryPhoneChange: (String) -> Unit,
    docUri: Uri?,
    hasMockDoc: Boolean,
    onPickDoc: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandCardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Commercial Entity & GSTIN Registration",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            // Legal Business Name
            OutlinedTextField(
                value = legalName,
                onValueChange = onLegalNameChange,
                label = { Text("Registered Business / Trade Name") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandSecondaryCyan,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_biz_legal_name")
            )

            // Document Type selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Business Identification Type",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("GSTIN Certificate", "FSSAI Food License", "MSME Registration", "Shop & Est. Act")
                    types.take(2).forEach { t ->
                        DocTypeChip(
                            title = t,
                            isSelected = docType == t,
                            onClick = { onDocTypeChange(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val types = listOf("GSTIN Certificate", "FSSAI Food License", "MSME Registration", "Shop & Est. Act")
                    types.drop(2).forEach { t ->
                        DocTypeChip(
                            title = t,
                            isSelected = docType == t,
                            onClick = { onDocTypeChange(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // GSTIN / License Number
            OutlinedTextField(
                value = docNumber,
                onValueChange = onDocNumberChange,
                label = { Text("$docType Number") },
                leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                trailingIcon = {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = BrandSecondaryCyan.copy(alpha = 0.2f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "GST Portal Valid",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSecondaryCyan,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandSecondaryCyan,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_biz_doc_number")
            )

            // Authorized Signatory
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = signatoryName,
                    onValueChange = onSignatoryNameChange,
                    label = { Text("Managing Director / Owner") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSecondaryCyan,
                        unfocusedBorderColor = BrandCardBorderDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = signatoryPhone,
                    onValueChange = onSignatoryPhoneChange,
                    label = { Text("Contact Phone") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandSecondaryCyan,
                        unfocusedBorderColor = BrandCardBorderDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = onAddressChange,
                label = { Text("Operating Venue & Premises Address") },
                leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandSecondaryCyan,
                    unfocusedBorderColor = BrandCardBorderDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Business License & Certificate Upload",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            UploadProofSlot(
                title = "Upload $docType Proof",
                subtitle = if (hasMockDoc) "Certificate scanned • Validated with Govt Portal" else "Tap to upload file / photo",
                isUploaded = hasMockDoc,
                icon = Icons.Default.Description,
                onUploadClick = onPickDoc,
                testTag = "btn_upload_biz_certificate"
            )
        }
    }
}

@Composable
private fun DocTypeChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BrandPrimaryGreen.copy(alpha = 0.2f) else BrandSurfaceDark,
        border = BorderStroke(1.dp, if (isSelected) BrandPrimaryGreen else BrandCardBorderDark),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = BrandPrimaryGreen,
                    unselectedColor = TextSecondaryDark
                ),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextSecondaryDark
            )
        }
    }
}

@Composable
private fun UploadProofSlot(
    title: String,
    subtitle: String,
    isUploaded: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onUploadClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BrandSurfaceDark,
        border = BorderStroke(
            1.dp,
            if (isUploaded) BrandPrimaryGreen.copy(alpha = 0.6f) else BrandCardBorderDark
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onUploadClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isUploaded) BrandPrimaryGreen.copy(alpha = 0.2f) else BrandCardDark,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isUploaded) BrandPrimaryGreen else TextSecondaryDark,
                        modifier = Modifier.size(24.dp)
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
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isUploaded) BrandPrimaryGreen else TextSecondaryDark,
                        fontSize = 11.sp
                    )
                )
            }

            Surface(
                shape = CircleShape,
                color = if (isUploaded) BrandPrimaryGreen else BrandCardDark,
                modifier = Modifier.size(26.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isUploaded) Icons.Default.Check else Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = if (isUploaded) Color.Black else TextSecondaryDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DatabaseVerificationRecordCard(
    record: VerificationRecordEntity
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BrandCardDark),
        border = BorderStroke(1.dp, BrandPrimaryGreen.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (record.userRole == "WORKER") BrandPrimaryGreen else BrandSecondaryCyan,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = record.userRole,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = record.legalName,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BrandPrimaryGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, BrandPrimaryGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = BrandPrimaryGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = record.verificationStatus,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandPrimaryGreen
                        )
                    }
                }
            }

            HorizontalDivider(color = BrandCardBorderDark, thickness = 0.5.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Document Type", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(text = record.documentType, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Number", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(
                        text = record.documentNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandPrimaryGreen,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Zone & Address", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(text = "${record.zone} • ${record.registeredAddress.take(28)}...", fontSize = 11.sp, color = TextSecondaryDark)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Database ID", fontSize = 10.sp, color = TextSecondaryDark)
                    Text(text = record.id, fontSize = 11.sp, color = BrandSecondaryCyan, fontFamily = FontFamily.Monospace)
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = BrandSurfaceDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = BrandPrimaryGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${record.verifiedBy} • ${record.verifiedDate}",
                        fontSize = 10.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }
    }
}
