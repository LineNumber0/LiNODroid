package ma.oprojet.linodroid.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.text.Html
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import dagger.hilt.android.qualifiers.ApplicationContext
import ma.oprojet.linodroid.R
import ma.oprojet.linodroid.view.MainActivity
import javax.inject.Inject

class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val channelId = "linodroid_new_posts_channel"
    private val postsChannelDescription = "LiNODroid Notifications for new blog posts"
    private val postsChannelName = "LiNODroid new articles"

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.linodroid_notif}")

            val attentionPattern = longArrayOf(
                0,    // no delay
                40,   // quick attention tap
                120,  // pause (creates tension)
                120,  // strong main pulse
                80,   // short pause
                60    // sharp finish
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                channelId,
                postsChannelName,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = postsChannelDescription
                enableLights(true)
                setSound(soundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = attentionPattern
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        postId: Int,
        title: String,
        excerpt: String,
        imageBitmap: Bitmap?
    ) {

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("postId", postId)
            putExtra("from_notification", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            postId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cleanExcerpt = Html.fromHtml(excerpt, FROM_HTML_MODE_LEGACY).toString()

        val notifTitle = context.getString(R.string.notifTitle, title)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(notifTitle)
            .setContentText(cleanExcerpt)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (imageBitmap != null) {
            builder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .bigLargeIcon(null)
            )
        } else {
            builder.setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(cleanExcerpt)
            )
        }

        NotificationManagerCompat.from(context)
            .notify(postId, builder.build())
    }


}