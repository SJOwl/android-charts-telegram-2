package au.sjowl.lib.view.charts.telegram

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.core.view.children
import androidx.core.view.forEach
import au.sjowl.lib.view.charts.telegram.data.ChartsData
import au.sjowl.lib.view.charts.telegram.names.ChartItem
import au.sjowl.lib.view.charts.telegram.names.RoundTitledCheckbox
import au.sjowl.lib.view.charts.telegram.params.ChartColors
import au.sjowl.lib.view.charts.telegram.params.ChartConfig
import kotlinx.android.synthetic.main.chart_layout.view.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import org.jetbrains.anko.layoutInflater
import org.jetbrains.anko.margin
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.wrapContent

class TelegramChartView : LinearLayout {

    var chartsData: ChartsData = ChartsData()
        set(value) {
            field = value
            value.initTimeWindow()
            value.columns.values.forEach { it.calculateExtremums() }
            titleTextView.text = value.title
            setTimeIntervalTitle()
            setChartNames()
            chartOverview.chartsData = chartsData
            chartViewContainer.chartsData = chartsData
            axisTime.chartsData = chartsData
            setZoomMode()
        }

    /**
     * zoomIn: true if zoom in, false if zoom out
     */
    var onZoomListener: ((chartsData: ChartsData, zoomIn: Boolean) -> Unit)? = null

    private var colors = ChartColors(context)

    private var animValue = 1f

    private val floatValueAnimator = ValueAnimator().apply {
        setFloatValues(1f, 0f)
        duration = ChartConfig.animDuration
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            val v = animatedValue as Float
            if (v != animValue) {
                animValue = v
                onAnimate(v)
            }
        }
    }

    private val onChartNameClick = { chartItem: ChartItem, checked: Boolean ->
        onAnimate(floatValueAnimator) {
            chartsData.columns[chartItem.chartId]!!.enabled = checked
        }
    }

    private val onChartNameLongClick = { chartItem: ChartItem ->
        this@TelegramChartView.chartNames.children.forEach { (it as RoundTitledCheckbox).checked = it.chart!!.chartId == chartItem.chartId }
        onAnimate(floatValueAnimator) {
            chartsData.columns.values.forEach { it.enabled = it.id == chartItem.chartId }
        }
    }

    override fun onDetachedFromWindow() {
        floatValueAnimator.cancel()
        floatValueAnimator.removeAllUpdateListeners()
        super.onDetachedFromWindow()
    }

    fun updateTheme() {
        colors = ChartColors(context)
        titleTextView.textColor = colors.chartTitle
        timeIntervalTextView.textColor = colors.chartTitle
        chartViewContainer.updateTheme(colors)
        axisTime.updateTheme(colors)
        chartOverview.updateTheme(colors)
        chartRoot.backgroundColor = colors.background
        chartNames.forEach { (it as ThemedView).updateTheme(colors) }
        zoomOutTextView.tint(colors.zoomOut)
    }

    private fun setZoomMode() {
        var animateTo = if (chartsData.isZoomed) 1f else 0f
        zoomOutTextView.animate()
            .alpha(animateTo)
            .scaleX(animateTo)
            .scaleY(animateTo)
            .setDuration(ChartConfig.animDuration)
        animateTo = if (chartsData.isZoomed) 0f else 1f
        titleTextView.animate()
            .alpha(animateTo)
            .scaleX(animateTo)
            .scaleY(animateTo)
            .setDuration(ChartConfig.animDuration)
    }

    private fun onAnimate(v: Float) {
        chartOverview.onAnimateValues(v)
        chartViewContainer.onAnimateValues(v)
    }

    private fun setChartNames() {
        chartNames.removeAllViews()
        chartsData.columns.values.forEach {
            chartNames.addView(RoundTitledCheckbox(context).apply {
                layoutParams = ViewGroup.MarginLayoutParams(wrapContent, wrapContent).apply {
                    margin = context.dip(5)
                }
                bind(
                    ChartItem(it.id, it.name, it.color, it.enabled),
                    onChartNameClick,
                    onChartNameLongClick
                )
            })
        }
    }

    private fun setTimeIntervalTitle() {
        timeIntervalTextView.text = DateFormatter.intervalFormat(chartsData.timeStart, chartsData.timeEnd)
    }

    private fun onAnimate(animator: ValueAnimator, block: () -> Unit) {
        chartOverview.updateStartPoints()
        chartViewContainer.updateStartPoints()

        block.invoke()

        chartOverview.updateFinishState()
        chartViewContainer.updateFinishState()

        animator.start()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        context.layoutInflater.inflate(R.layout.chart_layout, this)
        chartOverview.onTimeIntervalChanged = {
            setTimeIntervalTitle()
            chartViewContainer.onTimeIntervalChanged()
            axisTime.onTimeIntervalChanged()
        }
        chartViewContainer.onPopupClicked = { onZoomListener?.invoke(chartsData, true) }
        zoomOutTextView.onClick { onZoomListener?.invoke(chartsData, false) }
    }

    constructor(context: Context) : super(context) {
        init(context, null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }
}