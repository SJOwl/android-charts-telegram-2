package au.sjowl.lib.view.charts.telegram.chart.singlebar

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import au.sjowl.lib.view.charts.telegram.chart.base.AbstractChart
import au.sjowl.lib.view.charts.telegram.chart.base.BaseChartView
import au.sjowl.lib.view.charts.telegram.chart.stack.StackBarChart
import au.sjowl.lib.view.charts.telegram.data.ChartData
import au.sjowl.lib.view.charts.telegram.data.ChartsData

class SingleBarChartView : BaseChartView {

    override fun drawPointerLine(canvas: Canvas) = Unit

    override fun calcExtremums() = chartsData.calcSingleBarWindowExtremums()

    override fun provideChart(it: ChartData, value: ChartsData): AbstractChart {
        return StackBarChart(it, paints, chartLayoutParams, chartsData)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}