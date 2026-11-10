package es.brujula.jitai.core.jitai

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
import es.brujula.jitai.MainActivity
import es.brujula.jitai.R

/**
 * Emisor de notificaciones locales neutras y discretas (DAFO A1 → CAME AF1).
 * Nunca incluye términos de sustancias, sexualidad ni coordenadas en pantalla de bloqueo.
 */
class BrujulaNotifier(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "brujula_orientation_channel"
        private const val NOTIFICATION_ID = 4102
    }

    init {
        ensureChannel()
    }

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Orientación personal discreta",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos neutros acordados (máximo 2 al día, respetando horas de silencio)."
            setShowBadge(false)
            lockscreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun sendNeutralOrientationPrompt(
        discreetMode: Boolean,
        customNeutralSubtitle: String? = null
    ): Boolean {
        if (!hasNotificationPermission()) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_destination", "checkin")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (discreetMode) {
            "BRÚJULA · Nota personal"
        } else {
            "BRÚJULA · Momento de orientación"
        }
        val body = if (discreetMode) {
            "Tienes una pausa breve disponible cuando te venga bien."
        } else {
            customNeutralSubtitle ?: "¿Te apetece comprobar cómo te encuentras en 20 segundos?"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_compass)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
            true
        }.getOrDefault(false)
    }
}
