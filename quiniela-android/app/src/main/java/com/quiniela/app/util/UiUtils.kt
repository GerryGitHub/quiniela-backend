package com.quiniela.app.util

import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.quiniela.app.R

object UiUtils {

    fun showSuccessSnackbar(view: View, message: String) {
        showSnackbar(view, message, R.color.success, R.color.white)
    }

    fun showErrorSnackbar(view: View, message: String) {
        showSnackbar(view, message, R.color.error, R.color.white)
    }

    fun showWarningSnackbar(view: View, message: String) {
        showSnackbar(view, message, R.color.warning, R.color.black)
    }

    private fun showSnackbar(view: View, message: String, bgColor: Int, textColor: Int) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val sbView = snackbar.view
        sbView.setBackgroundColor(ContextCompat.getColor(view.context, bgColor))
        sbView.alpha = 0.95f
        sbView.elevation = 8f

        val params = sbView.layoutParams as? androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        params?.let {
            it.setMargins(16.dpToPx(view.context), 0, 16.dpToPx(view.context), 16.dpToPx(view.context))
            sbView.layoutParams = it
        }

        val radius = 12.dpToPx(view.context).toFloat()
        val background = sbView.background
        if (background is GradientDrawable) {
            background.cornerRadius = radius
        } else {
            val shape = GradientDrawable()
            shape.cornerRadius = radius
            shape.setColor(ContextCompat.getColor(view.context, bgColor))
            sbView.background = shape
        }

        val tv = sbView.findViewById<android.widget.TextView>(com.google.android.material.R.id.snackbar_text)
        tv.setTextColor(ContextCompat.getColor(view.context, textColor))
        tv.typeface = Typeface.DEFAULT_BOLD
        tv.textSize = 14f
        tv.maxLines = 3
        tv.gravity = Gravity.CENTER
        tv.textAlignment = View.TEXT_ALIGNMENT_CENTER

        snackbar.show()
    }

    fun startLivePulse(dot: View) {
        dot.animate().cancel()
        dot.alpha = 1f
        dot.scaleX = 1f
        dot.scaleY = 1f
        dot.animate()
            .alpha(0.3f)
            .scaleX(1.6f)
            .scaleY(1.6f)
            .setDuration(800)
            .withLayer()
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .setRepeatCount(android.view.animation.Animation.INFINITE)
            .setRepeatMode(android.view.animation.Animation.REVERSE)
            .start()
    }

    fun fadeInView(view: View, duration: Long = 300) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .withLayer()
            .start()
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
