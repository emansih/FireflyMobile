package xyz.hisname.fireflyiii.ui.base

import android.graphics.*
import android.graphics.drawable.ShapeDrawable

class ViewDrawable(private val text: String): ShapeDrawable() {

    private var textPaint: Paint = paint.apply {
        color = Color.GRAY
        isAntiAlias = true
        isFakeBoldText = true
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
    }

    init {
        paint.color = Color.GRAY
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val rect = bounds
        val count = canvas.save()
        canvas.translate(rect.left.toFloat(), rect.top.toFloat())
        val width =  rect.width()
        val height = rect.height()
        textPaint.textSize = width.coerceAtMost(height).div(2).toFloat()
        textPaint.color = Color.WHITE
        canvas.drawText(text, width.div(2).toFloat(),
            height.div(2).minus(((textPaint.descent() + textPaint.ascent()) / 2)), textPaint)
        canvas.restoreToCount(count)
    }

    override fun setAlpha(alpha: Int) {
        textPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        textPaint.colorFilter = colorFilter
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getIntrinsicHeight(): Int {
        return -1
    }

    override fun getIntrinsicWidth(): Int {
        return -1
    }
}