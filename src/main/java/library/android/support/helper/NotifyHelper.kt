/*
 * Created by EJun on 2023
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package library.android.support.helper

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.util.Log
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import library.android.support.R

object NotifyHelper {
    private var CHANNEL_ID = ""
    private var CHANNEL_NAME = ""

    fun init(channelId: String = "", channelName: String = "") {
        CHANNEL_ID = channelId
        CHANNEL_NAME = channelName
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun performHaptic(context: Context, vibrationEffect: VibrationEffect? = null, isVibration: Boolean) {
        if (isVibration) vibrationEffect?.let { AndroidHelper.getInstance().performHapticFeedback(context, it) }
    }

    fun createPendingIntent(context: Context, intent: Intent, requestCode: Int = 100): PendingIntent? {
        val pendingIntent = if (Build.VERSION.SDK_INT >= 34) {
            PendingIntent.getActivity(
                context, requestCode, intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT,
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        return pendingIntent
    }


    fun createNotificationWithForeground(
        context: Context,
        pendingIntent: PendingIntent? = null,
        title: String,
        body: String,
        smallIcon: IconCompat,
        largePic: Bitmap? = null,
        vibrationEffect: VibrationEffect? = null,
        isOnlyAlertOnce: Boolean = false,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        isVibration: Boolean = false,
    ): Notification {
        Log.e(TAG, "createNotificationWithBigText: $CHANNEL_ID")
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannelGroup(
            NotificationChannelGroup("chats_group", "Chats"),
        )
        val notificationChannel =
            NotificationChannel(
                "service_channel", "Service Notifications",
                NotificationManager.IMPORTANCE_MIN,
            )
        notificationChannel.enableLights(false)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        notificationManager?.createNotificationChannel(notificationChannel)

        performHaptic(context, vibrationEffect, isVibration)

        val builder = NotificationCompat.Builder(context, "service_channel")

        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(smallIcon)
        builder.setLargeIcon(largePic)

        builder.setAutoCancel(isAutoCancel)
        builder.setOnlyAlertOnce(isOnlyAlertOnce)
        builder.setWhen(0)
        builder.setOngoing(isOngoing)
        builder.color = ContextCompat.getColor(context, R.color.black)
        builder.priority = NotificationManager.IMPORTANCE_HIGH
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.setContentIntent(pendingIntent)

        return builder.build()
    }

    fun createNotificationWithBigText(
        context: Context,
        pendingIntent: PendingIntent? = null,
        title: String,
        body: String,
        smallIcon: IconCompat,
        largePic: Bitmap? = null,
        vibrationEffect: VibrationEffect? = null,
        isOnlyAlertOnce: Boolean = false,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        isVibration: Boolean = false,
    ): Notification {
        Log.e(TAG, "createNotificationWithBigText: $CHANNEL_ID")
        return commonCreateNotification(
            context = context,
            pendingIntent = pendingIntent,
            title = title,
            body = body,
            smallIcon = smallIcon,
            largePic = largePic,
            vibrationEffect = vibrationEffect,
            isBigText =  true,
            isOnlyAlertOnce = isOnlyAlertOnce,
            isOngoing = isOngoing,
            isAutoCancel = isAutoCancel,
            isVibration = isVibration,
        )
    }

    fun createNotificationWithBigImg(
        context: Context,
        pendingIntent: PendingIntent? = null,
        title: String,
        body: String,
        smallIcon: IconCompat,
        largePic: Bitmap? = null,
        bigPic: Bitmap? = null,
        vibrationEffect: VibrationEffect? = null,
        isOnlyAlertOnce: Boolean = false,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        isVibration: Boolean = false,
    ): Notification {
        Log.e(TAG, "createNotificationWithBigImg: $CHANNEL_ID")
        return commonCreateNotification(
            context = context,
            pendingIntent = pendingIntent,
            title = title,
            body = body,
            smallIcon = smallIcon,
            largePic = largePic,
            bigPic = bigPic,
            vibrationEffect = vibrationEffect,
            isOnlyAlertOnce = isOnlyAlertOnce,
            isOngoing = isOngoing,
            isAutoCancel = isAutoCancel,
            isVibration = isVibration,
        )
    }

    fun createNotification(
        context: Context,
        pendingIntent: PendingIntent? = null,
        title: String,
        body: String,
        smallIcon: IconCompat,
        vibrationEffect: VibrationEffect? = null,
        isOnlyAlertOnce: Boolean = false,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        isVibration: Boolean = false,
    ): Notification {
        Log.e(TAG, "createNotification: $CHANNEL_ID")
        return commonCreateNotification(
            context = context,
            pendingIntent = pendingIntent,
            title = title,
            body = body,
            smallIcon = smallIcon,
            vibrationEffect = vibrationEffect,
            isOnlyAlertOnce = isOnlyAlertOnce,
            isOngoing = isOngoing,
            isAutoCancel = isAutoCancel,
            isVibration = isVibration,
        )
    }

    private fun commonCreateNotification(
        context: Context,
        pendingIntent: PendingIntent? = null,
        title: String,
        body: String,
        smallIcon: IconCompat,
        largePic: Bitmap? = null,
        bigPic: Bitmap? = null,
        vibrationEffect: VibrationEffect? = null,
        isOnlyAlertOnce: Boolean = false,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        isVibration: Boolean = false,
        isBigText: Boolean = false,
    ): Notification {
        if (CHANNEL_ID.isEmpty()) throw IllegalStateException("NotifyHelper.init() call, first.")

        createChannel(context)
        performHaptic(context, vibrationEffect, isVibration)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(smallIcon)
        builder.setLargeIcon(largePic)
        bigPic?.also {
            builder.setStyle(
                NotificationCompat.BigPictureStyle().bigPicture(it),
            )
        }
        if (isBigText) {
            builder.setStyle(
                NotificationCompat.BigTextStyle().bigText(body),
            )
        }

        builder.setAutoCancel(isAutoCancel)
        builder.setOnlyAlertOnce(isOnlyAlertOnce)
        builder.setWhen(0)
        builder.setOngoing(isOngoing)
        builder.color = ContextCompat.getColor(context, R.color.black)
        builder.priority = NotificationManager.IMPORTANCE_HIGH
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.setContentIntent(pendingIntent)

        return builder.build()
    }

    /**
     * notification at the end of the measurement
     *
     * @param context
     * @param title - title of the notification
     * @param content - subtext of notification
     * @return built notification
     */
    fun endingNotification(context: Context, title: String?, content: String?): Notification {

        createChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle(title)
        builder.setAutoCancel(false)
        builder.setStyle(NotificationCompat.BigTextStyle().bigText(content))

//        builder.setSmallIcon(R.drawable.ic_graph)
//        builder.color = ContextCompat.getColor(context, R.color.colorBlack)

        builder.priority = NotificationManager.IMPORTANCE_DEFAULT
        builder.setCategory(Notification.CATEGORY_SERVICE)

        return builder.build()
    }


    fun updateNotification(context: Context, id: Int, notification: Notification) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notification)
    }

    fun cancelNotification(context: Context, id: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

    fun cancelAllNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }
}
