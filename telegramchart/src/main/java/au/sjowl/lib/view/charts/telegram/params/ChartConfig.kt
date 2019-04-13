package au.sjowl.lib.view.charts.telegram.params

import android.view.animation.AccelerateInterpolator

object ChartConfig {
    const val animDuration = 1000L
    val yIntervals = 5

    fun interpolator() = AccelerateInterpolator()
}