package au.sjowl.lib.view.charts.telegram.chart.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import au.sjowl.lib.view.charts.telegram.data.ChartsData
import au.sjowl.lib.view.charts.telegram.other.ThemedView
import au.sjowl.lib.view.charts.telegram.other.getTextBounds
import au.sjowl.lib.view.charts.telegram.params.BasePaints
import au.sjowl.lib.view.charts.telegram.time.TimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale

open class ChartPointerPopup : View, ThemedView {

    var paints = ChartPointerPaints(context)

    var chartsData = ChartsData()
        set(value) {
            field = value
            timeFormatter = if (value.isZoomed) HourFormatter() else DayFormatter()
        }

    protected var title = ""

    protected var items = listOf<ChartPoint>()

    protected var rectRadius = paints.dimensions.pointerRadius

    protected val timeIndex get() = chartsData.pointerTimeIndex

    protected var w = 0f

    protected var h = 0f

    protected var leftBorder = 0f

    protected val r1 = Rect()

    protected val r2 = Rect()

    protected var pad = paints.dimensions.pointerPadding

    protected var mw = 0

    protected val arrowWidth = paints.dimensions.pointerArrowWidth

    protected val arrow = Arrow(paints.dimensions.pointerArrowSize.toInt())

    protected var timeFormatter: TimeFormatter = DayFormatter()

    override fun onDraw(canvas: Canvas) {
        measure()
        restrictX()

        canvas.drawRoundRect(leftBorder, pad, leftBorder + w, h + pad, rectRadius, rectRadius, paints.paintPointerBackground)

        // draw title
        paints.paintPointerTitle.getTextBounds(title, r1)
        var y = 2 * pad + r1.height()
        canvas.drawText(title, leftBorder + pad, y, paints.paintPointerTitle)

        val h0 = itemHeight()
        // draw items
        items.forEach {
            paints.paintPointerValue.color = it.color
            paints.paintPointerValue.getTextBounds(it.value, r1)
            paints.paintPointerName.getTextBounds(it.chartName, r2)

            y += h0 + pad
            canvas.drawText(it.chartName, leftBorder + pad, y, paints.paintPointerName)
            canvas.drawText(it.value, leftBorder + w - pad - r1.width(), y, paints.paintPointerValue)
        }

        if (chartsData.canBeZoomed) {
            arrow.draw(leftBorder + w - pad - arrow.size / 2, 2 * pad, canvas, paints.paintArrow)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (isInBounds(event.x, event.y)) {
                    performClick()
                    return true
                }
                return false
            }
        }
        return false
    }

    override fun updateTheme() {
        paints = ChartPointerPaints(context)
        invalidate()
    }

    fun isInBounds(x: Float, y: Float): Boolean {
        return isVisible && x in leftBorder..leftBorder + w && y in 2 * pad..2 * pad + h
    }

    open fun updatePoints(measuredWidth: Int) {
        this.mw = measuredWidth

        title = timeFormatter.format(chartsData.times[timeIndex])
        items = chartsData.charts.filter { it.enabled }
            .map { ChartPoint(it.name, it.values[timeIndex].toString(), it.color) }
    }

    fun onChartStateChanged() {
        try {
            items = chartsData.charts.filter { it.enabled }
                .map { ChartPoint(it.name, it.values[timeIndex].toString(), it.color) }
        } catch (e: Exception) {
        }
        invalidate()
    }

    protected fun itemHeight(): Int {
        paints.paintPointerValue.getTextBounds("4050", r1)
        paints.paintPointerName.getTextBounds("Apd", r2)
        return Math.max(r1.height(), r2.height())
    }

    protected fun restrictX() {
        leftBorder = chartsData.pointerTimeX - w - pad - chartsData.barHalfWidth
        if (leftBorder < pad) leftBorder = chartsData.pointerTimeX + pad + chartsData.barHalfWidth
    }

    protected open fun measure() {
        w = Math.max(
            2 * pad + (items.map {
                paints.paintPointerValue.measureText(it.value) + paints.paintPointerName.measureText(it.chartName)
            }.max() ?: 0f),
            paints.paintPointerTitle.measureText(title) + arrowWidth
        ) + 2 * pad

        val h0 = itemHeight()
        h = pad + h0 + items.size * (h0 + pad) + pad + pad / 2
    }

    class ChartPointerPaints(context: Context) : BasePaints(context) {
        val paintArrow = paint().apply {
            color = colors.gridLines
            style = Paint.Style.STROKE
            strokeWidth = dimensions.arrowWidth
            alpha = 25
            strokeCap = Paint.Cap.ROUND
        }

        val paintPointerBackground = paint().apply {
            color = colors.pointer
            setShadowLayer(5f, 0f, 2f, colors.pointerShadow)
        }

        val paintPointerTitle = paint().apply {
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            color = colors.text
            textSize = dimensions.pointerTitle
        }

        val paintPointerValue = paint().apply {
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            textSize = dimensions.pointerValueText
        }

        val paintPointerName = paint().apply {
            textSize = dimensions.pointerNameText
            color = colors.text
        }
    }

    class DayFormatter : TimeFormatter() {
        override val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
    }

    class HourFormatter : TimeFormatter() {
        override val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    }

    data class ChartPoint(
        val chartName: String,
        val value: String,
        val color: Int,
        val percent: Int = 0
    )

    class Arrow(val size: Int = 0) {
        private val points = floatArrayOf(
            0f, 0f,
            0.5f, 0.5f,
            0f, 1f
        )

        private val path = Path()

        fun draw(left: Float, top: Float, canvas: Canvas, paint: Paint) {
            path.reset()
            val dx = left
            val dy = top
            path.moveTo(points[0] * size + dx, points[1] * size + dy)
            for (i in 2 until points.size step 2) {
                path.lineTo(points[i] * size + dx, points[i + 1] * size + dy)
            }
            canvas.drawPath(path, paint)
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}
