package com.towery.screenrecordball.ui

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import org.jetbrains.anko._LinearLayout

/**
 * Created by ZY on 2016/8/10.
 * 实现的效果为：
 * 双击后小球内的波浪高度从零增加到目标进度值targetProgress
 * 单击后波浪呈现上下浮动且波动渐小的效果
 */
class ProgressBall(context: Context?, function: () -> _LinearLayout) : View(context) {

    //view的宽度
     val ball_width = 200
    //view的高度
     val ball_height = 200
    //最大进度值
     val maxProgress = 100
    //当前进度值
     var currentProgress = 0
    //目标进度值
    var targetProgress = 70
    //是否为单击
    var isSingleTop: Boolean = false
    //设定波浪总的起伏次数
    var Count = 20
    //当前起伏次数
    var currentCount: Int = 0
    //初始振幅大小
    var startAmplitude = 15
    //波浪周期性出现的次数
    var cycleCount = width / (startAmplitude * 4) + 1


    var  ball_handler: Handler = Handler();

    var bitmap: Bitmap = Bitmap.createBitmap(ball_width, ball_height, Bitmap.Config.ARGB_8888);
    var bitmapCanvas = Canvas(bitmap)
    var path: Path? = null;

    var ballPaint: Paint? = null;

    var progressPaint: Paint? = null;

    var textPaint: Paint? = null;


    var gestureDetector: GestureDetector? = null;

    private val doubleTapRunnable = DoubleTapRunnable()
    private val singleTapRunnable = SingleTapRunnable()
    init{
        //初始化小球画笔
        ballPaint = Paint()
        ballPaint!!.isAntiAlias=true
        ballPaint!!.color=(Color.argb(0xff, 0x3a, 0x8c, 0x6c))
        //初始化（波浪）进度条画笔
        progressPaint = Paint()
        progressPaint!!.setAntiAlias(true)
        progressPaint!!.setColor(Color.argb(0xff, 0x4e, 0xc9, 0x63))
        progressPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_IN))
        //初始化文字画笔
        textPaint = Paint()
        textPaint!!.setAntiAlias(true)
        textPaint!!.setColor(Color.WHITE)
        textPaint!!.setTextSize(25f)

        path = Path()




//手势监听
        //重点在于将单击和双击操作分隔开
        val listener = object : GestureDetector.SimpleOnGestureListener() {
            //双击
            override fun onDoubleTap(e: MotionEvent): Boolean {
                //当前波浪起伏次数为零，说明“单击效果”没有影响到现在
                if (currentCount === 0) {
                    //当前进度为零或者已达到目标进度值，说明“双击效果”没有影响到现在，此时可以允许进行双击操作
                    if (currentProgress === 0 || currentProgress === targetProgress) {
                        currentProgress = 0
                        isSingleTop = false
                        startDoubleTapAnimation()
                    }
                }
                return super.onDoubleTap(e)
            }

            //单击
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                //当前进度值等于目标进度值，且当前波动次数为零，则允许进行单击操作
                if (currentProgress === targetProgress && currentCount === 0) {
                    isSingleTop = true
                    startSingleTapAnimation()
                }
                return super.onSingleTapConfirmed(e)
            }
        }
        gestureDetector = GestureDetector(context, listener)
        setOnTouchListener { v, event -> gestureDetector!!.onTouchEvent(event) }


        //接受点击操作
        setClickable(true);
    }


    internal inner class DoubleTapRunnable : Runnable {
        override fun run() {
            if (currentProgress < targetProgress) {
                invalidate()
                handler.postDelayed(doubleTapRunnable, 50)
                currentProgress++
            } else {
                handler.removeCallbacks(doubleTapRunnable)
            }
        }
    }
    //开启双击动作动画
    fun startDoubleTapAnimation() {
        ball_handler.postDelayed(doubleTapRunnable, 50)
    }

    internal inner class SingleTapRunnable : Runnable {
        override fun run() {
            if (currentCount < Count) {
                invalidate()
                currentCount++
                ball_handler.postDelayed(singleTapRunnable, 100)
            } else {
                ball_handler.removeCallbacks(singleTapRunnable)
                currentCount = 0
            }
        }
    }

    //开启单击动作动画
    fun startSingleTapAnimation() {
        ball_handler.postDelayed(singleTapRunnable, 100)
    }


    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(ball_width, ball_height)
    }

    protected override fun onDraw(canvas: Canvas) {
        //绘制圆形
        bitmapCanvas.drawCircle((ball_width / 2).toFloat(), (ball_height / 2).toFloat(), (ball_width / 2).toFloat(), ballPaint)
        path!!.reset()
        //高度随当前进度值的变化而变化
        val y = (1 - currentProgress.toFloat() / maxProgress) * height
        //属性PorterDuff.Mode.SRC_IN代表了progressPaint只显示与下层层叠的部分，
        //所以以下四点虽然连起来是个矩形，可呈现出来的依然是圆形
        //右上角
        path!!.moveTo(ball_width.toFloat(), y)
        //右下角
        path!!.lineTo(ball_width.toFloat(), ball_height.toFloat())
        //左下角
        path!!.lineTo(0f, height.toFloat())
        //左上角
        path!!.lineTo(0f, y)
        //绘制顶部波浪
        if (!isSingleTop) {
            //是双击
            //根据当前进度大小调整振幅大小，有逐渐减小的趋势
            val tempAmplitude = (1 - currentProgress.toFloat() / targetProgress) * startAmplitude
            for (i in 0..cycleCount - 1) {
                path!!.rQuadTo(startAmplitude.toFloat(), tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
                path!!.rQuadTo(startAmplitude.toFloat(), -tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
            }
        } else {
            //是单击
            //根据当前次数调整振幅大小，有逐渐减小的趋势
            val tempAmplitude = (1 - currentCount.toFloat() / Count) * startAmplitude
            //因为想要形成波浪上下起伏的效果，所以根据currentCount的奇偶性来变化贝塞尔曲线转折点位置
            if (currentCount % 2 === 0) {
                for (i in 0..cycleCount - 1) {
                    path!!.rQuadTo(startAmplitude.toFloat(), tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
                    path!!.rQuadTo(startAmplitude.toFloat(), -tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
                }
            } else {
                for (i in 0..cycleCount - 1) {
                    path!!.rQuadTo(startAmplitude.toFloat(), -tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
                    path!!.rQuadTo(startAmplitude.toFloat(), tempAmplitude, (2 * startAmplitude).toFloat(), 0f)
                }
            }
        }
        path!!.close()
        bitmapCanvas.drawPath(path, progressPaint)
        val text = (currentProgress.toFloat() / maxProgress * 100f).toString() + "%"
        val textWidth = textPaint!!.measureText(text)
        val metrics = textPaint!!.fontMetrics
        val baseLine = ball_height / 2 - (metrics.ascent + metrics.descent)
        bitmapCanvas.drawText(text, ball_width / 2 - textWidth / 2, baseLine, textPaint)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}