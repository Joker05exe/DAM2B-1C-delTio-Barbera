package cat.institut.dam.contactes

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/**
 * Utilitat per crear el canal de notificacions i mostrar la notificació
 * amb el nombre de contactes carregats.
 */
object Notificacions {

    private const val CANAL_ID = "canal_contactes"
    private const val NOTIFICACIO_ID = 1

    /** Mostra una notificació indicant quants [quantitat] contactes s'han carregat. */
    fun mostraContactesCarregats(context: Context, quantitat: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val tePermis = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!tePermis) return
        }

        creaCanalSiCal(context)

        val notificacio = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_notificacio)
            .setContentTitle(context.getString(R.string.notificacio_titol))
            .setContentText(context.getString(R.string.notificacio_text, quantitat))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICACIO_ID, notificacio)
    }

    private fun creaCanalSiCal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(CANAL_ID) != null) return

        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.canal_notificacions),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager.createNotificationChannel(canal)
    }
}
