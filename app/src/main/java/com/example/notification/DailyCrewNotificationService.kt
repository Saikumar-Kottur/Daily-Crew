package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.JobApplicant
import com.example.data.model.JobPosting

class DailyCrewNotificationService(private val context: Context) {

    companion object {
        const val CHANNEL_WORKER_JOBS = "channel_worker_job_alerts"
        const val CHANNEL_OWNER_EVENTS = "channel_owner_events_alerts"

        private const val NOTIF_ID_JOB_OFFSET = 1000
        private const val NOTIF_ID_APPLICANT_OFFSET = 2000
        private const val NOTIF_ID_CHECKIN_OFFSET = 3000
        private const val NOTIF_ID_CHECKOUT_OFFSET = 4000
    }

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val systemNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                    ?: return

            // Channel 1: Worker Jobs Channel
            val workerChannel = NotificationChannel(
                CHANNEL_WORKER_JOBS,
                "Nearby Job Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts workers instantly when new daily shifts and gigs open nearby"
                enableLights(true)
                enableVibration(true)
            }

            // Channel 2: Owner Events Channel
            val ownerChannel = NotificationChannel(
                CHANNEL_OWNER_EVENTS,
                "Incoming Applications & Check-Ins",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts business owners about new applications, worker arrivals, and QR check-ins"
                enableLights(true)
                enableVibration(true)
            }

            systemNotificationManager.createNotificationChannel(workerChannel)
            systemNotificationManager.createNotificationChannel(ownerChannel)
        }
    }

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            notificationManager.areNotificationsEnabled()
        }
    }

    /**
     * Alerts workers about new nearby job openings
     */
    fun notifyNewJobAlert(job: JobPosting): Boolean {
        if (!hasPermission()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("job_id", job.id)
            putExtra("target_role", "WORKER")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            job.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_WORKER_JOBS)
            .setSmallIcon(R.drawable.ic_notif_work)
            .setContentTitle("⚡ New Shift Nearby: ${job.title}")
            .setContentText("${job.businessName} • ${job.wage} ${job.unit} • ${job.distanceKm} km away")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "${job.businessName} posted an urgent shift!\n" +
                            "• Wage: ${job.wage} ${job.unit}\n" +
                            "• Location: ${job.location} (${job.distanceKm} km)\n" +
                            "• Time: ${job.time}\n" +
                            "• Dress Code: ${job.dressCode}"
                )
            )
            .setColor(0xFF00E676.toInt()) // Brand primary green
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notifId = NOTIF_ID_JOB_OFFSET + (System.currentTimeMillis() % 1000).toInt()
            notificationManager.notify(notifId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Alerts business owners about incoming applicant
     */
    fun notifyNewApplicantAlert(applicant: JobApplicant, jobTitle: String): Boolean {
        if (!hasPermission()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("applicant_id", applicant.id)
            putExtra("target_role", "OWNER")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            applicant.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWNER_EVENTS)
            .setSmallIcon(R.drawable.ic_notif_applicant)
            .setContentTitle("📋 New Applicant: ${applicant.workerName}")
            .setContentText("Applied for $jobTitle • Trust Score: ${applicant.trustScore}/100 • ${applicant.rating}★")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Verified candidate applied for $jobTitle:\n" +
                            "• Worker: ${applicant.workerName}\n" +
                            "• Trust Score: ${applicant.trustScore}/100 (Aadhaar Verified)\n" +
                            "• On-time Reliability: ${applicant.onTimeRate} • Completed: ${applicant.jobsCompleted} shifts"
                )
            )
            .setColor(0xFF00B0FF.toInt()) // Brand secondary cyan
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notifId = NOTIF_ID_APPLICANT_OFFSET + (System.currentTimeMillis() % 1000).toInt()
            notificationManager.notify(notifId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Alerts business owners when a worker checks in at the venue
     */
    fun notifyWorkerCheckInAlert(
        workerName: String,
        jobTitle: String,
        venueName: String,
        time: String
    ): Boolean {
        if (!hasPermission()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_role", "OWNER")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (workerName + venueName).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWNER_EVENTS)
            .setSmallIcon(R.drawable.ic_notif_checkin)
            .setContentTitle("📍 Worker Checked In: $workerName")
            .setContentText("Geo-Fence & QR verified for $jobTitle at $venueName ($time)")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Crew member $workerName arrived & verified at venue:\n" +
                            "• Venue: $venueName\n" +
                            "• Role: $jobTitle\n" +
                            "• Arrival Time: $time\n" +
                            "• Geo-Fence & QR Proof: Authenticated (Active)"
                )
            )
            .setColor(0xFF00E676.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notifId = NOTIF_ID_CHECKIN_OFFSET + (System.currentTimeMillis() % 1000).toInt()
            notificationManager.notify(notifId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }

    /**
     * Alerts business owners when a worker finishes their shift
     */
    fun notifyWorkerCheckOutAlert(
        workerName: String,
        jobTitle: String,
        venueName: String,
        shiftPay: Double
    ): Boolean {
        if (!hasPermission()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_role", "OWNER")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            (workerName + venueName + "out").hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OWNER_EVENTS)
            .setSmallIcon(R.drawable.ic_notif_checkin)
            .setContentTitle("🏁 Shift Completed: $workerName")
            .setContentText("$workerName finished shift at $venueName. Payout queued: ₹${shiftPay.toInt()}")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Shift checkout logged:\n" +
                            "• Worker: $workerName\n" +
                            "• Role: $jobTitle\n" +
                            "• Venue: $venueName\n" +
                            "• Auto-Escrow Payout: ₹${shiftPay.toInt()} ready for instant release"
                )
            )
            .setColor(0xFFFFAB00.toInt()) // Brand amber
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notifId = NOTIF_ID_CHECKOUT_OFFSET + (System.currentTimeMillis() % 1000).toInt()
            notificationManager.notify(notifId, notification)
            return true
        } catch (e: SecurityException) {
            return false
        }
    }
}
