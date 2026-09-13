package com.example.ui.worker

import androidx.compose.runtime.Composable
import com.example.data.model.WorkerProfile

/**
 * WorkerTrustView delegates directly to the comprehensive WorkerProfileView,
 * ensuring backwards compatibility while providing the full verification status,
 * work history, and skill badges.
 */
@Composable
fun WorkerTrustView(
    worker: WorkerProfile,
    onToggleStandby: () -> Unit = {},
    onOpenVerificationDialog: () -> Unit = {}
) {
    WorkerProfileView(
        worker = worker,
        onToggleStandby = onToggleStandby,
        onOpenVerificationDialog = onOpenVerificationDialog
    )
}
