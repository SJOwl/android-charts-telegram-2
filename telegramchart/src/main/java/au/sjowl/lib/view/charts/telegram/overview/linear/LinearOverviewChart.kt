package au.sjowl.lib.view.charts.telegram.overview.linear

import au.sjowl.lib.view.charts.telegram.chart.base.BaseChartView
import au.sjowl.lib.view.charts.telegram.chart.linear.LinearChart
import au.sjowl.lib.view.charts.telegram.data.ChartData
import au.sjowl.lib.view.charts.telegram.data.ChartsData
import au.sjowl.lib.view.charts.telegram.params.ChartLayoutParams

open class LinearOverviewChart(
    chartData: ChartData,
    paints: BaseChartView.ChartViewPaints,
    chartLayoutParams: ChartLayoutParams,
    chartsData: ChartsData
) : LinearChart(chartData, paints, chartLayoutParams, chartsData) {

    override fun calculateInnerBorders() {
        innerTimeIndexEnd = chartsData.times.size - 1
        innerTimeIndexStart = 0
    }

    override fun timeIndexStart() = 0
    override fun timeIndexEnd() = chartsData.times.size - 1
    override fun y(index: Int) = mh - kY * (chartData.values[index] - chartsData.chartsMin)
    override fun ky() = 1f * (h - chartLayoutParams.paddingBottom - chartLayoutParams.paddingTop) / chartsData.chartValueInterval

    // todo draw lines instead of path
}