package au.sjowl.lib.view.charts.telegram.chart.linear

import android.content.Context
import android.util.AttributeSet
import au.sjowl.lib.view.charts.telegram.chart.base.BaseChartContainer
import au.sjowl.lib.view.charts.telegram.chart.base.ChartPointerPopup

class LinearChartContainer : BaseChartContainer {

    override fun init() {
        axisY = LineAxisView(context)
        chart = LinearChartView(context)
        pointerPopup = ChartPointerPopup(context)
        tint = LineTintView(context)
        super.init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }
}