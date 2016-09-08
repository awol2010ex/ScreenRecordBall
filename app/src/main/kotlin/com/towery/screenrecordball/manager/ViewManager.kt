package com.towery.screenrecordball.manager

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Point
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import com.towery.screenrecordball.ui.FloatBall
import com.towery.screenrecordball.ui.FloatMenu
import org.jetbrains.anko.onTouch

/**
 * Created by User on 2016/9/7.
 */
class ViewManager(val context : Context){

    val windowManager: WindowManager by lazy{
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    };
    var floatBall: FloatBall? = null
    var floatMenu: FloatMenu? = null

    private var floatBallParams: WindowManager.LayoutParams? = null

    private var floatMenuParams: WindowManager.LayoutParams? = null


    init{
        floatBall =  FloatBall(context);
        floatMenu =  FloatMenu(context);
        val touchListener = object : OnTouchListener {
            internal var startX: Float = 0.toFloat()
            internal var startY: Float = 0.toFloat()
            internal var tempX: Float = 0.toFloat()
            internal var tempY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = event.rawX
                        startY = event.rawY

                        tempX = event.rawX
                        tempY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val x = event.rawX - startX
                        val y = event.rawY - startY
                        //计算偏移量，刷新视图
                        floatBallParams!!.x = floatBallParams!!.x.plus(x).toInt()
                        floatBallParams!!.y = floatBallParams!!.y.plus(y).toInt()
                        floatBall!!.setDragState(true)
                        windowManager.updateViewLayout(floatBall, floatBallParams)
                        startX = event.rawX
                        startY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                        //判断松手时View的横坐标是靠近屏幕哪一侧，将View移动到依靠屏幕
                        var endX = event.rawX
                        val endY = event.rawY
                        if (endX <  getScreenWidth() / 2) {
                            endX = 0f
                        } else {
                            endX = getScreenWidth().toFloat() - floatBall!!.width
                        }
                        floatBallParams!!.x = endX.toInt()
                        floatBall!!.setDragState(false)
                        windowManager.updateViewLayout(floatBall, floatBallParams)
                        //如果初始落点与松手落点的坐标差值超过6个像素，则拦截该点击事件
                        //否则继续传递，将事件交给OnClickListener函数处理
                        if (Math.abs(endX - tempX) > 6 && Math.abs(endY - tempY) > 6) {
                            return true
                        }
                    }
                }
                return false
            }
        }
        val clickListener = object : View.OnClickListener {

            override fun onClick(v: View) {
                windowManager.removeView(floatBall)
                showFloatMenu()
                floatMenu!!.startAnimation()
            }
        }
        floatBall!!.setOnTouchListener(touchListener)
        floatBall!!.setOnClickListener(clickListener)
    }



    companion object {
        var manager : ViewManager? = null

        fun getInstance(context: Context): ViewManager {
              if(manager==null) manager = ViewManager(context)

              return manager as ViewManager
        }
    }

    //获取屏幕宽度
    fun getScreenWidth(): Int {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return point.x
    }

    //获取屏幕高度
    fun getScreenHeight(): Int {
        val point = Point()
        windowManager.defaultDisplay.getSize(point)
        return point.y
    }

    //获取状态栏高度
    fun getStatusHeight(): Int {
        try {
            val c = Class.forName("com.android.internal.R\$dimen")
            val `object` = c.newInstance()
            val field = c.getField("status_bar_height")
            val x = field.get(`object`) as Int

            return context.resources.getDimensionPixelSize(x)
        } catch (e: Exception) {
            return 0
        }

    }

    //显示浮动小球
    fun showFloatBall() {
        if (floatBallParams == null) {
            floatBallParams = WindowManager.LayoutParams()
            floatBallParams!!.width = floatBall!!.width
            floatBallParams!!.height = floatBall!!.height - getStatusHeight()
            floatBallParams!!.gravity = Gravity.TOP or Gravity.LEFT
            floatBallParams!!.type = WindowManager.LayoutParams.TYPE_TOAST
            floatBallParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            floatBallParams!!.format = PixelFormat.RGBA_8888
        }
        windowManager.addView(floatBall, floatBallParams)
    }

    //显示底部菜单
    private fun showFloatMenu() {
        if (floatMenuParams == null) {
            floatMenuParams = WindowManager.LayoutParams()
            floatMenuParams!!.width = getScreenWidth()
            floatMenuParams!!.height = getScreenHeight() - getStatusHeight()
            floatMenuParams!!.gravity = Gravity.BOTTOM
            floatMenuParams!!.type = WindowManager.LayoutParams.TYPE_TOAST
            floatMenuParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            floatMenuParams!!.format = PixelFormat.RGBA_8888
        }
        windowManager.addView(floatMenu, floatMenuParams)
    }

    //隐藏底部菜单
    fun hideFloatMenu() {
        if (floatMenu != null) {
            windowManager.removeView(floatMenu)
        }
    }
}