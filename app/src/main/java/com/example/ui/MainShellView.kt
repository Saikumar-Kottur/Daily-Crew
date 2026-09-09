package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AppRole
import com.example.notification.DailyCrewNotificationService
import com.example.ui.admin.AdminDashboardView
import com.example.ui.admin.SuperAdminSettingsView
import com.example.ui.common.AiMatchingScreen
import com.example.ui.common.NotificationsScreen
import com.example.ui.components.*
import com.example.ui.owner.EmergencyHiringDialog
import com.example.ui.owner.OwnerApplicantsView
import com.example.ui.owner.OwnerBusinessTrustView
import com.example.ui.owner.OwnerDashboardView
import com.example.ui.owner.OwnerPostJobView
import com.example.ui.splash.DailyCrewSplashScreen
import com.example.ui.theme.*
import com.example.ui.worker.WorkerCheckInView
import com.example.ui.worker.WorkerEarningsView
import com.example.ui.worker.WorkerJobsView
import com.example.ui.worker.WorkerTrustView
import com.example.viewmodel.DailyCrewViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellView(
    viewModel: DailyCrewViewModel,
    notificationService: DailyCrewNotificationService? = null
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.showSnackbar("Push notifications enabled! You'll receive instant shift & event alerts.")
        } else {
            viewModel.showSnackbar("Notifications disabled. You can enable them anytime from Settings.")
        }
    }

    var dismissedPermissionBanner by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredJobs by viewModel.filteredJobs.collectAsStateWithLifecycle()
    val workerProfile by viewModel.workerProfile.collectAsStateWithLifecycle()
    val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()
    val applicants by viewModel.applicants.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val aiRecommendedJobs by viewModel.aiRecommendedJobs.collectAsStateWithLifecycle()
    val aiTopApplicants by viewModel.aiTopApplicants.collectAsStateWithLifecycle()

    var showNotificationsScreen by remember { mutableStateOf(false) }
    var showAiMatchingScreen by remember { mutableStateOf(false) }
    var showSplashScreen by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Launch snackbars from ViewModel
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    if (showSplashScreen) {
        DailyCrewSplashScreen(
            onContinue = { showSplashScreen = false }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("daily_crew_main_scaffold"),
            containerColor = BrandBackgroundDark,
            topBar = {
                DailyCrewTopBar(
                    currentRole = uiState.role,
                    onRoleChange = { newRole ->
                        showNotificationsScreen = false
                        showAiMatchingScreen = false
                        viewModel.switchRole(newRole)
                    },
                    unreadNotifCount = notifications.count { !it.isRead },
                    onNotifClick = {
                        showNotificationsScreen = !showNotificationsScreen
                        showAiMatchingScreen = false
                    },
                    onAiInsightsClick = {
                        showAiMatchingScreen = !showAiMatchingScreen
                        showNotificationsScreen = false
                    },
                    onLogoClick = {
                        showSplashScreen = true
                    }
                )
            },
        bottomBar = {
            NavigationBar(
                containerColor = BrandSurfaceDark,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                when (uiState.role) {
                    AppRole.WORKER -> {
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 0 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(0)
                            },
                            icon = { Icon(Icons.Default.Work, contentDescription = "Shifts") },
                            label = { Text("Shifts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandPrimaryGreen,
                                indicatorColor = BrandPrimaryGreen,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_worker_shifts")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 1 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(1)
                            },
                            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Check-In") },
                            label = { Text("Check-In", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandPrimaryGreen,
                                indicatorColor = BrandPrimaryGreen,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_worker_checkin")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 2 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(2)
                            },
                            icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet") },
                            label = { Text("Wallet", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandPrimaryGreen,
                                indicatorColor = BrandPrimaryGreen,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_worker_wallet")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 3 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(3)
                            },
                            icon = { Icon(Icons.Default.VerifiedUser, contentDescription = "Trust Score") },
                            label = { Text("Trust", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandPrimaryGreen,
                                indicatorColor = BrandPrimaryGreen,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_worker_trust")
                        )
                    }
                    AppRole.OWNER -> {
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 0 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(0)
                            },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Shifts") },
                            label = { Text("Shifts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_owner_shifts")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 1 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(1)
                            },
                            icon = { Icon(Icons.Default.AddCircle, contentDescription = "Post Shift") },
                            label = { Text("Post Shift", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_owner_post_shift")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 2 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(2)
                            },
                            icon = { Icon(Icons.Default.People, contentDescription = "Applicants") },
                            label = { Text("Applicants", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_owner_applicants")
                        )
                        NavigationBarItem(
                            selected = uiState.bottomNavIndex == 3 && !showAiMatchingScreen && !showNotificationsScreen,
                            onClick = {
                                showNotificationsScreen = false
                                showAiMatchingScreen = false
                                viewModel.setBottomNavIndex(3)
                            },
                            icon = { Icon(Icons.Default.Shield, contentDescription = "Business Trust") },
                            label = { Text("Credibility", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            ),
                            modifier = Modifier.testTag("nav_owner_trust")
                        )
                    }
                    AppRole.ADMIN -> {
                        NavigationBarItem(
                            selected = true,
                            onClick = {},
                            icon = { Icon(Icons.Default.Gavel, contentDescription = "Moderation") },
                            label = { Text("Moderation & Bans", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            )
                        )
                        NavigationBarItem(
                            selected = false,
                            onClick = { viewModel.switchRole(AppRole.SUPER_ADMIN) },
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Super Admin") },
                            label = { Text("Platform Settings", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandAmber,
                                indicatorColor = BrandAmber,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            )
                        )
                    }
                    AppRole.SUPER_ADMIN -> {
                        NavigationBarItem(
                            selected = false,
                            onClick = { viewModel.switchRole(AppRole.ADMIN) },
                            icon = { Icon(Icons.Default.Gavel, contentDescription = "Admin Moderation") },
                            label = { Text("Moderation Center", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandSecondaryCyan,
                                indicatorColor = BrandSecondaryCyan,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            )
                        )
                        NavigationBarItem(
                            selected = true,
                            onClick = {},
                            icon = { Icon(Icons.Default.Tune, contentDescription = "Super Admin Engine") },
                            label = { Text("Super Admin Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = BrandAmber,
                                indicatorColor = BrandAmber,
                                unselectedIconColor = TextSecondaryDark,
                                unselectedTextColor = TextSecondaryDark
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = BrandCardDark,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Notification Permission Banner for Android 13+
            if (!hasNotificationPermission && !dismissedPermissionBanner && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Surface(
                    color = BrandSecondaryCyanDim,
                    border = BorderStroke(1.dp, BrandSecondaryCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = BrandSecondaryCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Enable push alerts to get instant nearby shifts & live check-in updates.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandSecondaryCyan),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Enable", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { dismissedPermissionBanner = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = TextSecondaryDark)
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    showNotificationsScreen -> {
                        NotificationsScreen(
                            currentRole = uiState.role,
                            notifications = notifications,
                            hasNotificationPermission = hasNotificationPermission,
                            onRequestPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            onTestJobAlert = { viewModel.sendTestJobOpeningAlert() },
                            onTestApplicantAlert = { viewModel.sendTestApplicantAlert() },
                            onTestCheckInAlert = { viewModel.sendTestCheckInAlert() },
                            onClose = { showNotificationsScreen = false }
                        )
                    }
                showAiMatchingScreen -> {
                    AiMatchingScreen(
                        currentRole = uiState.role,
                        recommendedJobs = aiRecommendedJobs,
                        topApplicants = aiTopApplicants,
                        onSelectJob = { job ->
                            showAiMatchingScreen = false
                            viewModel.openJobDetails(job)
                        },
                        onHireApplicant = { appId ->
                            viewModel.hireApplicant(appId)
                        }
                    )
                }
                uiState.role == AppRole.WORKER -> {
                    when (uiState.bottomNavIndex) {
                        0 -> WorkerJobsView(
                            jobs = filteredJobs,
                            workerProfile = workerProfile,
                            searchQuery = uiState.searchQuery,
                            onSearchChange = { viewModel.setSearchQuery(it) },
                            selectedCategory = uiState.selectedCategory,
                            onSelectCategory = { viewModel.selectCategory(it) },
                            filterRadiusKm = uiState.filterRadiusKm,
                            onRadiusChange = { viewModel.setRadius(it) },
                            selectedZone = uiState.selectedZoneFilter,
                            onSelectZone = { viewModel.setZoneFilter(it) },
                            filterEmergencyOnly = uiState.filterEmergencyOnly,
                            onToggleEmergency = { viewModel.toggleEmergencyFilter() },
                            onToggleStandby = { viewModel.toggleWorkerStandby() },
                            onJobClick = { viewModel.openJobDetails(it) },
                            onApplyJob = { viewModel.applyForJob(it) }
                        )
                        1 -> WorkerCheckInView(
                            worker = workerProfile,
                            onOpenCheckInScanner = { viewModel.openCheckInScanner(true) },
                            onCheckOut = { viewModel.performWorkerCheckOut() }
                        )
                        2 -> WorkerEarningsView(
                            worker = workerProfile,
                            transactions = transactions,
                            onWithdrawClick = { viewModel.openWithdrawDialog(true) },
                            onReportDisputeClick = { viewModel.openDisputeDialog(true) }
                        )
                        3 -> WorkerTrustView(worker = workerProfile)
                    }
                }
                uiState.role == AppRole.OWNER -> {
                    when (uiState.bottomNavIndex) {
                        0 -> OwnerDashboardView(
                            business = businessProfile,
                            applicants = applicants,
                            onTriggerPayout = { amt -> viewModel.triggerOwnerAutoPayout(amt) },
                            onNavigateToPostJob = { viewModel.setBottomNavIndex(1) },
                            onOpenEmergencyHiring = { viewModel.openEmergencyHiringDialog(true) }
                        )
                        1 -> OwnerPostJobView(
                            onPublishJob = { title, cat, wage, count, dress, instructions, loc, time ->
                                viewModel.postJob(title, cat, wage, count, dress, instructions, loc, time)
                            }
                        )
                        2 -> OwnerApplicantsView(
                            applicants = applicants,
                            onHireWorker = { viewModel.hireApplicant(it) },
                            onRejectWorker = { viewModel.rejectApplicant(it) },
                            onChatWithWorker = { viewModel.openChatWithApplicant(it) }
                        )
                        3 -> OwnerBusinessTrustView(business = businessProfile)
                    }
                }
                uiState.role == AppRole.ADMIN -> {
                    AdminDashboardView(viewModel = viewModel)
                }
                uiState.role == AppRole.SUPER_ADMIN -> {
                    SuperAdminSettingsView(viewModel = viewModel)
                }
            }

            // Dialogs & Modals
            uiState.selectedJobForDetails?.let { job ->
                JobDetailsDialog(
                    job = job,
                    onDismiss = { viewModel.closeJobDetails() },
                    onApply = { viewModel.applyForJob(it) }
                )
            }

            if (uiState.isCheckInScannerOpen) {
                CheckInScannerDialog(
                    onDismiss = { viewModel.openCheckInScanner(false) },
                    onConfirmCheckIn = { viewModel.performWorkerCheckIn() }
                )
            }

            if (uiState.isWithdrawDialogOpen) {
                WithdrawDialog(
                    balance = workerProfile.walletBalance,
                    onDismiss = { viewModel.openWithdrawDialog(false) },
                    onConfirmWithdraw = { viewModel.withdrawWallet(it) }
                )
            }

            if (uiState.isDisputeDialogOpen) {
                DisputeDialog(
                    onDismiss = { viewModel.openDisputeDialog(false) },
                    onSubmit = { reason, target, details ->
                        viewModel.submitDispute(reason, target, details)
                    }
                )
            }

            if (uiState.isEmergencyHiringDialogOpen) {
                EmergencyHiringDialog(
                    onDismiss = { viewModel.openEmergencyHiringDialog(false) },
                    onBroadcast = { title, cat, wage, count ->
                        viewModel.postEmergency30MinHiring(title, cat, wage, count)
                    }
                )
            }

            // Interactive Chat Overlay
            if (uiState.isChatSheetOpen && uiState.activeChatApplicant != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        ChatSheet(
                            applicant = uiState.activeChatApplicant!!,
                            messages = uiState.chatMessages,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onClose = { viewModel.closeChatSheet() }
                        )
                    }
                }
            }
        }
    }
}
}
}
