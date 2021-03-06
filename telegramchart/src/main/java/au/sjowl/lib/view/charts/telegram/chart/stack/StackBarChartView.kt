package au.sjowl.lib.view.charts.telegram.chart.stack

import android.content.Context
import android.util.AttributeSet
import au.sjowl.lib.view.charts.telegram.chart.base.AbstractChart
import au.sjowl.lib.view.charts.telegram.chart.base.BaseChartView
import au.sjowl.lib.view.charts.telegram.data.ChartData
import au.sjowl.lib.view.charts.telegram.data.ChartsData

class StackBarChartView : BaseChartView {

    override fun calcExtremums() = chartsData.calcStackedWindowExtremums()

    override fun provideChart(it: ChartData, value: ChartsData): AbstractChart {
        return StackBarChart(it, value, chartLayoutParams)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
}