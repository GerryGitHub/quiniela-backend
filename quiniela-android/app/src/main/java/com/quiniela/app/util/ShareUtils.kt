package com.quiniela.app.util

import android.content.Context
import android.content.Intent

object ShareUtils {
    const val APP_URL = "https://qgol.app"

    fun shareQuiniela(context: Context, nombre: String, codigo: String) {
        val mensaje = "¡Únete a mi quiniela \"$nombre\" en QGol!\nUsa el código: $codigo\n\n$APP_URL"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, mensaje)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir con"))
    }
}
