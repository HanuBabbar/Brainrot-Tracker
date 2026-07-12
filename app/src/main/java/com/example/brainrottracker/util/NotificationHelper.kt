package com.example.brainrottracker.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat

class NotificationHelper(private val context: Context) {

    fun getContext(): Context = context

    companion object {
        private const val CHANNEL_ID = "brainrot_limit_channel"
        private const val SERVICE_CHANNEL_ID = "brainrot_service_channel"
        private const val NOTIFICATION_ID = 1001
        const val PERSISTENT_NOTIFICATION_ID = 1002
        private const val DISABLED_NOTIFICATION_ID = 1003
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Usage Limits"
            val descriptionText = "Notifications for when you reach your daily brainrot limit"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            
            val serviceName = "Tracker Service"
            val serviceDesc = "Ongoing notification while the tracker is active"
            val serviceImportance = NotificationManager.IMPORTANCE_LOW
            val serviceChannel = NotificationChannel(SERVICE_CHANNEL_ID, serviceName, serviceImportance).apply {
                description = serviceDesc
                enableVibration(false)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun sendLimitReachedNotification(currentCount: Int, vibrate: Boolean) {
        Log.d("BrainrotTracker", "Attempting to show notification for count: $currentCount")
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for now
            .setContentTitle("Limit Reached! 🧠")
            .setContentText("You've swiped $currentCount videos today. Time to take a break?")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, builder.build())

        if (vibrate) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(500)
            }
        }
    }

    fun buildPersistentServiceNotification(disableIntentAction: String, dismissedIntentAction: String): android.app.Notification {
        val intent = android.content.Intent(disableIntentAction).apply {
            setPackage(context.packageName)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val deleteIntent = android.content.Intent(dismissedIntentAction).apply {
            setPackage(context.packageName)
        }
        val pendingDeleteIntent = android.app.PendingIntent.getBroadcast(
            context,
            0,
            deleteIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .setContentTitle("Brainrot Tracker Active")
            .setContentText("Tracker is monitoring usage. Disable for banking apps.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setDeleteIntent(pendingDeleteIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Disable App",
                pendingIntent
            )
            
        val notification = builder.build()
        notification.flags = notification.flags or android.app.Notification.FLAG_NO_CLEAR
        return notification
    }

    fun showAppDisabledNotification() {
        val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_close_clear_cancel)
            .setContentTitle("Brainrot Tracker Disabled")
            .setContentText("App is disabled and not tracking doomscrolling.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.ic_menu_preferences,
                "Enable in Settings",
                pendingIntent
            )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(DISABLED_NOTIFICATION_ID, builder.build())
    }
}
