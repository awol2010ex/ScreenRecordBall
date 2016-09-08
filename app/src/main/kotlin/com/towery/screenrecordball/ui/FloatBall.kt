package com.towery.screenrecordball.ui

import android.content.Context
import android.graphics.*
import android.view.View
import com.towery.screenrecordball.R

/** 悬浮球
 * Created by User on 2016/9/7.
 */
class FloatBall(context: Context?) : View(context) {
    val ball_width=150;
    val ball_height=150 ;
    val ball_text ="50%";
    var isDrag:Boolean=false

    var ballPaint: Paint

    var textPaint: Paint

    var bitmap: Bitmap
    init{

        ballPaint = Paint()
        ballPaint.color=(Color.GRAY)
        ballPaint.setAntiAlias(true)

        textPaint = Paint()
        textPaint.textSize=25f
        textPaint.color=Color.WHITE
        textPaint.isAntiAlias=true
        textPaint.setFakeBoldText(true)

        val src = BitmapFactory.decodeResource(resources, R.drawable.ninja)
        //将图片裁剪到指定大小
        bitmap = Bitmap.createScaledBitmap(src, ball_width, ball_height, true)
    }


    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(ball_width, ball_height)
    }

    protected override fun onDraw(canvas: Canvas) {
        if (!isDrag) {
            canvas.drawCircle((ball_width / 2).toFloat(), (ball_height / 2).toFloat(), (ball_width / 2).toFloat(), ballPaint)
            val textWidth = textPaint.measureText(ball_text)
            val fontMetrics = textPaint.fontMetrics
            val dy = -(fontMetrics.descent + fontMetrics.ascent) / 2
            canvas.drawText(ball_text, ball_width / 2 - textWidth / 2, ball_height / 2 + dy, textPaint)
        } else {
            //正在被拖动时则显示指定图片
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }
    }

    //设置当前移动状态
    fun setDragState(isDrag: Boolean) {
        this.isDrag = isDrag
        invalidate()
    }
}