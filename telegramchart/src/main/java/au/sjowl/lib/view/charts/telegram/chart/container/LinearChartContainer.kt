package au.sjowl.lib.view.charts.telegram.chart.container

import android.content.Context
import android.util.AttributeSet
import au.sjowl.lib.view.charts.telegram.chart.chartview.LinearChartView

class LinearChartContainer : BaseChartContainer {

    override fun init() {
        chart = LinearChartView(context)
        super.init()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }
}