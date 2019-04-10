package au.sjowl.lib.view.charts.telegram.chart.container

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import au.sjowl.lib.view.charts.telegram.chart.axis.AxisY
import au.sjowl.lib.view.charts.telegram.chart.chartview.BaseChartView
import au.sjowl.lib.view.charts.telegram.chart.pointer.ChartPointerPopup
import au.sjowl.lib.view.charts.telegram.data.ChartsData
import au.sjowl.lib.view.charts.telegram.other.ThemedView
import au.sjowl.lib.view.charts.telegram.other.setVisible
import org.jetbrains.anko.sdk27.coroutines.onClick

/**
 * Base class for all chart containers
 */
// todo remove linear-specific stuff
abstract class BaseChartContainer : FrameLayout, ThemedView {

    var chartsData: ChartsData = ChartsData()
        set(value) {
            field = value
            axisY.chartsData = value
            pointerPopup.chartsData = value
            chart.chartsData = value
            drawPointer = false

            onTimeIntervalChanged()
        }

    var onPopupClicked: (() -> Unit)? = null
    // todo double init here and in child
    protected open var chart: BaseChartView = BaseChartView(context)

    protected open var pointerPopup: ChartPointerPopup = ChartPointerPopup(context)

    protected open var axisY: AxisY = AxisY(context)

    protected var drawPointer = false
        set(value) {
            chart.drawPointer = value
            chart.invalidate()
            pointerPopup.setVisible(value)
            pointerPopup.invalidate()
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                updateTimeIndexFromX(event.x)
                drawPointer = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                invalidate()
            }
        }
        return true
    }

    override fun updateTheme() {
        axisY.updateTheme()
        chart.updateTheme()
        pointerPopup.updateTheme()
    }

    open fun onChartStateChanged() { // todo replace with observables of chartsData
        axisY.anim()
        chart.onChartStateChanged()
    }

    open fun onTimeIntervalChanged() { // todo replace with observables of chartsData
        drawPointer = false
        axisY.adjustValuesRange()
        axisY.onTimeIntervalChanged()
        chart.onTimeIntervalChanged()
    }

    protected open fun init() {
        addView(chart)
        addView(axisY)
        addView(pointerPopup)
        pointerPopup.onClick { onPopupClicked?.invoke() }
    }

    private fun updateTimeIndexFromX(x: Float) {
        val t = chartsData.pointerTimeIndex

        chart.updateTimeIndexFromX(x)

        if (t != chartsData.pointerTimeIndex) {
            pointerPopup.updatePoints(x, measuredWidth)
            invalidate()
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
}