package com.example.syslearning.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

/**
 * Anillo circular decorativo.
 * Se usa en la tarjeta de la actividad DISPONIBLE para darle uso
 */
class AnilloProgresoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progreso = 0.7f // 0f..1f

    private val paintFondo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        color = Color.parseColor("#40FFFFFF")
    }

    private val paintProgreso = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 12f
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }

    private val rect = RectF()

    fun setProgreso(valor: Float) {
        progreso = valor.coerceIn(0.05f, 1f) // mínimo 5% para que siempre se vea algo de arco
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val stroke = paintFondo.strokeWidth
        rect.set(stroke / 2f, stroke / 2f, width - stroke / 2f, height - stroke / 2f)

        canvas.drawArc(rect, 0f, 360f, false, paintFondo)
        canvas.drawArc(rect, -90f, 360f * progreso, false, paintProgreso)
    }
}