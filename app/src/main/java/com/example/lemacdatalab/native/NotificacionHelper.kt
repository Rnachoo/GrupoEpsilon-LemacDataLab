package com.example.lemacdatalab.native

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.lemacdatalab.R

object NotificacionHelper {

    private const val CHANNEL_REGISTROS = "registros"
    private const val ID_REGISTRO_GUARDADO = 1

    // false si el usuario rechazó el permiso o apagó las notificaciones de la app
    fun notificacionesActivas(context: Context): Boolean {
        val permisoConcedido = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
        return permisoConcedido && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    @Suppress("UNUSED_PARAMETER")
    fun mostrarAlertaRegistroGuardado(context: Context, tipoFormulario: String, racha: Int) {
        if (!notificacionesActivas(context)) return
        crearCanal(context)

        val abrirApp = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        // versión que se ve en la pantalla de bloqueo
        val versionPublica = NotificationCompat.Builder(context, CHANNEL_REGISTROS)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("LemacDataLab")
            .setContentText("Tienes una notificación")
            .build()

        val notificacion = NotificationCompat.Builder(context, CHANNEL_REGISTROS)
            .setSmallIcon(R.drawable.ic_launcher_monochrome)
            .setContentTitle("LemacDataLab")
            .setContentText("Tienes un recordatorio pendiente")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(versionPublica)
            .setContentIntent(abrirApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(ID_REGISTRO_GUARDADO, notificacion)
        } catch (e: SecurityException) {

        }
    }

    private fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CHANNEL_REGISTROS,
            "Registros guardados",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = "Aviso al guardar un formulario de autorregistro" }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }
}
