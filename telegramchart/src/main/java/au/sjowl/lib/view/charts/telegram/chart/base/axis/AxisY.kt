package au.sjowl.lib.view.charts.telegram.chart.base.axis

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import au.sjowl.lib.view.charts.telegram.data.ChartsData
import au.sjowl.lib.view.charts.telegram.other.SLog
import au.sjowl.lib.view.charts.telegram.params.BasePaints
import au.sjowl.lib.view.charts.telegram.params.ChartConfig
import au.sjowl.lib.view.charts.telegram.params.ChartLayoutParams

open class AxisY(val v: View) {

    val intervals = ChartConfig.yIntervals

    open var chartsData: ChartsData = ChartsData()

    val chartLayoutParams = ChartLayoutParams(v.context)

    val height get() = v.height

    open val drawGrid = true

    open val windowMin get() = chartsData.windowMin

    open val windowMax get() = chartsData.windowMax

    open val alphaOldPoints get() = ((animScale) * 255).toInt()

    open val alphaNewPoints get() = ((1 - animScale) * 255).toInt()

    protected var paints = AxisPaints(v.context)

    protected var pointsFrom = Points(intervals)

    protected var pointsTo = Points(intervals)

    protected val valueFormatter = ValueFormatter()

    protected var animScale = 0f

    protected var animScroll = 0f

    protected open var textOffset = chartLayoutParams.paddingHorizontal * 1f

    protected var mh = 0f

    protected var isScrolling = false

    protected var isScaling = false

    protected var lastWindowMin = 0

    protected var lastWindowMax = 0

    private var kY = 0f

    open fun onAnimationScrollStart() {
        isScrolling = true
        isScaling = false

        lastWindowMin = windowMin
        lastWindowMax = windowMax

        for (i in 0..intervals) {
            pointsTo.canvasFrom[i] = canvasY(pointsTo.valuesTo[i])
        }
    }

    open fun onAnimateScroll(value: Float) {
        animScroll = value
        setVals()
        for (i in 0..intervals) {
            pointsTo.canvasTo[i] = canvasY(pointsTo.valuesTo[i])
        }
        pointsTo.calcCurrent(animScroll, v.width, chartLayoutParams.paddingHorizontal, chartLayoutParams.paddingHorizontal)
    }

    fun isIntervalChanged(): Boolean {
        return !(windowMin == lastWindowMin && windowMax == lastWindowMax)
    }

    /**
     * remember current all points
     */
    open fun onAnimationScaleStart() {
        setVals()
        isScaling = true
        isScrolling = false

        lastWindowMin = windowMin
        lastWindowMax = windowMax

        calcPointsFrom()
        calcPointsTo()
    }

    open fun onAnimateScale(value: Float) {
        animScale = value
        // update alpha
        pointsTo.calcCurrent(animScale, v.width, chartLayoutParams.paddingHorizontal, chartLayoutParams.paddingHorizontal)
        pointsFrom.calcCurrent(animScale, v.width, chartLayoutParams.paddingHorizontal, chartLayoutParams.paddingHorizontal)
    }

    open fun ky(): Float = (mh - chartLayoutParams.paddingTop) / chartsData.windowValueInterval

    open fun canvasY(value: Int) = mh - kY * (value - windowMin)

    fun drawTitlesFrom(canvas: Canvas) {
        val x = textOffset
        for (i in 0..intervals) {
            val y = pointsFrom.currentCanvas[i] - chartLayoutParams.paddingTextBottom
            canvas.drawText(valueFormatter.format(pointsFrom.valuesFrom[i]), x, y, paints.paintChartText)
        }
    }

    fun drawTitlesTo(canvas: Canvas) {
        val x = textOffset
        for (i in 0..intervals) {
            val y = pointsTo.currentCanvas[i] - chartLayoutParams.paddingTextBottom
            canvas.drawText(valueFormatter.format(pointsTo.valuesTo[i]), x, y, paints.paintChartText)
        }
    }

    open fun drawGrid(canvas: Canvas) {
        if (!drawGrid) return
        if (isScaling) {
            paints.paintGrid.alpha = ((animScale) * 25).toInt()
            canvas.drawLines(pointsFrom.gridPoints, paints.paintGrid)

            paints.paintGrid.alpha = ((1f - animScale) * 25).toInt()
            canvas.drawLines(pointsTo.gridPoints, paints.paintGrid)
        }
        if (isScrolling) {
            paints.paintGrid.alpha = 25
            canvas.drawLines(pointsTo.gridPoints, paints.paintGrid)
        }
    }

    open fun drawMarks(canvas: Canvas) {
        val x = textOffset
        if (isScaling) {
            if (!pointsFrom.valuesTo.contentEquals(pointsTo.valuesTo)) {
                // old
                paints.paintChartText.alpha = alphaOldPoints
                drawTitlesFrom(canvas)
                // new
                paints.paintChartText.alpha = alphaNewPoints
                drawTitlesTo(canvas)
            } else {
                paints.paintChartText.alpha = 255
                drawTitlesTo(canvas)
            }
        }
        if (isScrolling) {
            paints.paintChartText.alpha = 255
            drawTitlesTo(canvas)
        }
    }

    open fun updateTheme(context: Context) {
        this.paints = AxisPaints(v.context)
    }

    fun draw(canvas: Canvas) {
        drawGrid(canvas)
        drawMarks(canvas)
    }

    private fun calcPointsFrom() {
        for (i in 0..intervals) {
            pointsFrom.valuesFrom[i] = pointsTo.valuesTo[i]
            pointsFrom.valuesTo[i] = pointsTo.valuesTo[i]
            pointsFrom.canvasFrom[i] = pointsTo.canvasTo[i]
            pointsFrom.canvasTo[i] = canvasY(pointsTo.valuesTo[i])
        }
    }

    private fun calcPointsTo() {
        pointsTo.valuesTo = valueFormatter.rawMarksFromRange(windowMin, windowMax, intervals).toIntArray()
        for (i in 0..intervals) {
            pointsTo.canvasTo[i] = canvasY(pointsTo.valuesTo[i])
            pointsTo.canvasFrom[i] = pointsTo.canvasTo[i]
            pointsTo.valuesFrom[i] = pointsTo.valuesTo[i]
        }
    }

    private fun setVals() {
        mh = v.height * 1f - chartLayoutParams.paddingBottom
        kY = ky()
    }

    inner class Points(cap: Int) {

        val capacity = cap + 1

        var canvasFrom = FloatArray(capacity)
        var canvasTo = FloatArray(capacity)
        var valuesFrom = IntArray(capacity)
        var valuesTo = IntArray(capacity)
        var gridPoints = FloatArray(capacity shl 2)
        val currentCanvas = FloatArray(capacity)

        init {
            for (i in 0..cap) {
                canvasFrom[i] = 1500f
                canvasTo[i] = 1500f
            }
        }

        fun calcCurrent(v: Float, width: Int, paddingLeft: Float, paddingRight: Float) {
            for (i in 0 until capacity) {
                currentCanvas[i] = canvasTo[i] - (canvasTo[i] - canvasFrom[i]) * v
                val j = i * 4
                gridPoints[j] = paddingLeft * 1f
                gridPoints[j + 1] = currentCanvas[i]
                gridPoints[j + 2] = width - paddingRight * 1f
                gridPoints[j + 3] = currentCanvas[i]
            }
        }

        fun print(msg: String) {
            SLog.d("******************* $msg")
            SLog.d("canvasFrom = ${floatArrayS(canvasFrom)}")
            SLog.d("canvasTo = ${floatArrayS(canvasTo)}")
            SLog.d("valuesFrom = ${intArrayS(valuesFrom)}")
            SLog.d("valuesTo = ${intArrayS(valuesTo)}")
            SLog.d("gridPoints = ${floatArrayS(gridPoints)}")
        }
    }

    class AxisPaints(context: Context) : BasePaints(context) {

        val paintGrid = simplePaint().apply {
            color = colors.gridLines
            style = Paint.Style.STROKE
            strokeWidth = dimensions.gridWidth
            strokeCap = Paint.Cap.ROUND
        }

        val paintChartText = antiAliasPaint().apply {
            color = colors.chartText
            textSize = dimensions.axisTextHeight
        }
    }
}

private fun floatArrayS(a: FloatArray): String {
    var s = "["
    for (i in 0 until a.size) {
        s += "${a[i]}, "
    }
    s += "]"
    return s
}

private fun intArrayS(a: IntArray): String {
    var s = "["
    for (i in 0 until a.size) {
        s += "${a[i]}, "
    }
    s += "]"
    return s
}