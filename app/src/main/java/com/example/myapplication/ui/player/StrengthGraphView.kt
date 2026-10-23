package com.example.myapplication.ui.player

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R
import kotlin.math.roundToInt

class StrengthGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<Pair<Int, Double>> = emptyList()

    private val linePath = Path()
    private val fillPath = Path()

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(3f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1f)
        color = Color.parseColor("#33888888")
        pathEffect = DashPathEffect(floatArrayOf(dpToPx(4f), dpToPx(4f)), 0f)
    }

    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dpToPx(1.5f)
        color = Color.parseColor("#66888888")
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = spToPx(10f)
        color = Color.parseColor("#888888")
        textAlign = Paint.Align.CENTER
    }

    private val yLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = spToPx(10f)
        color = Color.parseColor("#888888")
        textAlign = Paint.Align.RIGHT
    }

    private val pointOuterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val pointInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val valueTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = spToPx(10f)
        color = Color.parseColor("#222222")
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val emptyTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = spToPx(14f)
        color = Color.GRAY
        textAlign = Paint.Align.CENTER
    }

    private var primaryColor: Int = Color.BLUE
    private var textColor: Int = Color.BLACK

    init {
        val typedValue = TypedValue()
        val theme = context.theme

        if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            primaryColor = typedValue.data
        } else {
            primaryColor = ContextCompat.getColor(context, R.color.meeple_blue_light)
        }

        if (theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)) {
            val colorSec = typedValue.data
            labelPaint.color = colorSec
            yLabelPaint.color = colorSec
        }

        if (theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)) {
            textColor = typedValue.data
            valueTextPaint.color = textColor
        }

        linePaint.color = primaryColor
        pointOuterPaint.color = primaryColor
    }

    fun setData(points: List<Pair<Int, Double>>) {
        this.dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        if (dataPoints.isEmpty()) {
            canvas.drawText("Sin partidas registradas", width / 2f, height / 2f, emptyTextPaint)
            return
        }

        val paddingLeft = dpToPx(42f)
        val paddingRight = dpToPx(20f)
        val paddingTop = dpToPx(28f)
        val paddingBottom = dpToPx(32f)

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        if (graphWidth <= 0 || graphHeight <= 0) return

        // 1. Calculate Y range (Strength / Fuerza)
        val strengths = dataPoints.map { it.second }
        var minY = strengths.minOrNull() ?: 1200.0
        var maxY = strengths.maxOrNull() ?: 1200.0

        if (minY == maxY) {
            minY -= 50.0
            maxY += 50.0
        }

        val rangeY = maxY - minY
        val yMargin = (rangeY * 0.15).coerceAtLeast(15.0)
        val yMinBound = (minY - yMargin).coerceAtLeast(0.0)
        val yMaxBound = maxY + yMargin
        val totalYRange = yMaxBound - yMinBound

        // 2. Calculate X range & Step (Matches / Partidas)
        val maxMatch = dataPoints.maxOf { it.first }.coerceAtLeast(1)
        val xStep = calculateXStep(maxMatch)

        fun getX(matchNum: Int): Float {
            return if (maxMatch == 1) {
                paddingLeft + graphWidth / 2f
            } else {
                paddingLeft + ((matchNum - 1).toFloat() / (maxMatch - 1).toFloat()) * graphWidth
            }
        }

        fun getY(strengthVal: Double): Float {
            val ratio = (strengthVal - yMinBound) / totalYRange
            return (paddingTop + graphHeight) - (ratio.toFloat() * graphHeight)
        }

        // 3. Draw Grid lines & Y-axis labels
        val yTicksCount = 4
        for (i in 0 until yTicksCount) {
            val fraction = i.toFloat() / (yTicksCount - 1)
            val valY = yMinBound + fraction * totalYRange
            val canvasY = getY(valY)

            canvas.drawLine(paddingLeft, canvasY, width - paddingRight, canvasY, gridPaint)
            canvas.drawText(valY.roundToInt().toString(), paddingLeft - dpToPx(6f), canvasY + dpToPx(3.5f), yLabelPaint)
        }

        // 4. Draw X-axis labels & grid lines
        val xTicks = mutableListOf<Int>()
        if (maxMatch <= 7) {
            for (m in 1..maxMatch) {
                xTicks.add(m)
            }
        } else {
            var curr = xStep
            while (curr <= maxMatch) {
                xTicks.add(curr)
                curr += xStep
            }
            if (xTicks.size > 7) {
                val stepFilter = (xTicks.size / 6) + 1
                val filtered = xTicks.filterIndexed { index, _ -> index % stepFilter == 0 }
                xTicks.clear()
                xTicks.addAll(filtered)
            }
            if (xTicks.isNotEmpty() && xTicks.last() != maxMatch && xTicks.size < 7 && (maxMatch - xTicks.last()) >= xStep / 2) {
                xTicks.add(maxMatch)
            }
        }

        for (matchNum in xTicks) {
            val canvasX = getX(matchNum)
            canvas.drawLine(canvasX, paddingTop, canvasX, height - paddingBottom, gridPaint)
            canvas.drawText(matchNum.toString(), canvasX, height - paddingBottom + dpToPx(16f), labelPaint)
        }

        // Axis Titles
        canvas.drawText("Partidas", paddingLeft + graphWidth / 2f, height - dpToPx(4f), labelPaint)

        // Draw solid X and Y axes
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint)
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint)

        // 5. Draw Line Graph & Area Fill
        if (dataPoints.isNotEmpty()) {
            linePath.reset()
            fillPath.reset()

            val firstX = getX(dataPoints.first().first)
            val firstY = getY(dataPoints.first().second)

            linePath.moveTo(firstX, firstY)
            fillPath.moveTo(firstX, height - paddingBottom)
            fillPath.lineTo(firstX, firstY)

            for (i in 1 until dataPoints.size) {
                val px = getX(dataPoints[i].first)
                val py = getY(dataPoints[i].second)
                linePath.lineTo(px, py)
                fillPath.lineTo(px, py)
            }

            val lastX = getX(dataPoints.last().first)
            fillPath.lineTo(lastX, height - paddingBottom)
            fillPath.close()

            val gradient = LinearGradient(
                0f, paddingTop,
                0f, height - paddingBottom,
                adjustAlpha(primaryColor, 0.35f),
                adjustAlpha(primaryColor, 0.02f),
                Shader.TileMode.CLAMP
            )
            fillPaint.shader = gradient
            canvas.drawPath(fillPath, fillPaint)

            canvas.drawPath(linePath, linePaint)

            // 6. Draw Points & Value Callouts
            for (i in dataPoints.indices) {
                val (matchNum, strengthVal) = dataPoints[i]
                val px = getX(matchNum)
                val py = getY(strengthVal)

                canvas.drawCircle(px, py, dpToPx(5f), pointOuterPaint)
                canvas.drawCircle(px, py, dpToPx(2.5f), pointInnerPaint)

                val isMin = strengthVal == minY
                val isMax = strengthVal == maxY
                val isFirst = i == 0
                val isLast = i == dataPoints.size - 1

                if (dataPoints.size <= 7 || isFirst || isLast || isMin || isMax) {
                    val labelText = strengthVal.roundToInt().toString()
                    canvas.drawText(labelText, px, py - dpToPx(8f), valueTextPaint)
                }
            }
        }
    }

    private fun calculateXStep(maxMatches: Int): Int {
        if (maxMatches <= 7) return 1
        val niceSteps = listOf(2, 5, 10, 15, 20, 25, 50, 100, 200, 500, 1000)
        for (step in niceSteps) {
            if (maxMatches / step <= 6) {
                return step
            }
        }
        return (maxMatches / 6).coerceAtLeast(1)
    }

    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).roundToInt()
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    private fun spToPx(sp: Float): Float {
        return sp * context.resources.displayMetrics.scaledDensity
    }
}
