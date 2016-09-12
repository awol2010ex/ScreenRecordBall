package com.towery.screenrecordball.ui

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.view.View
import com.towery.screenrecordball.R

/** 悬浮球
 * Created by User on 2016/9/7.
 */
class FloatBall(context: Context?) : View(context) {
    val ball_width=150;//悬浮球长宽
    val ball_height=150 ;//悬浮球长宽
    var ball_text ="0s";////悬浮球文本

    var currentProgress :Int =0 ;//录像秒数

    var startRecordingRunable:StartRecordingRunable=StartRecordingRunable() //显示录了几秒
    var isDrag:Boolean=false //是否正在拖动

    var ballPaint: Paint

    var textPaint: Paint

    var bitmap: Bitmap //显示的图片

    public var isRecording:Boolean=false;//是否正在录像


    var  record_handler: Handler = Handler();//录像中
    init{

        ballPaint = Paint()
        ballPaint.color=(Color.GRAY)
        ballPaint.setAntiAlias(true)

        textPaint = Paint()
        textPaint.textSize=25f
        textPaint.color=Color.WHITE
        textPaint.isAntiAlias=true
        textPaint.setFakeBoldText(true)

        val src = BitmapFactory.decodeResource(resources, R.drawable.recorder_ball)
        //将图片裁剪到指定大小
        bitmap = Bitmap.createScaledBitmap(src, ball_width, ball_height, true)

    }


    //设置录像长宽
    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(ball_width, ball_height)
    }

    protected override fun onDraw(canvas: Canvas) {

        if (isRecording) {
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

    //设置是否正在录像
    fun setIsRecording(isRecording :Boolean){
        this.isRecording = isRecording

        invalidate()

        handler.postDelayed(startRecordingRunable, 1000)
    }
    //设置是否正在录像
    fun getIsRecording(): Boolean{
        return this.isRecording
    }

    //显示录了几秒
    inner class StartRecordingRunable : Runnable {
        override fun run() {
            if (isRecording) {
                invalidate()
                handler.postDelayed(startRecordingRunable, 1000)
                currentProgress++
                ball_text =currentProgress.toString()+"s"
            } else {
                ball_text="0s"
                currentProgress=0
                handler.removeCallbacks(startRecordingRunable)
            }
        }
    }

}