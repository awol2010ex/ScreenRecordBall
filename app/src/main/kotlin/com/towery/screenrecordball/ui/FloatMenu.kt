package com.towery.screenrecordball.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.support.v7.widget.LinearLayoutCompat
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.LinearLayout
import com.towery.screenrecordball.R
import com.towery.screenrecordball.manager.ViewManager
import org.jetbrains.anko.*

/**
 * Created by User on 2016/9/8.
 */
class FloatMenu(context: Context?) : LinearLayout(context) {


    private var animation: TranslateAnimation

    init {

        animation = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0f)
        animation.setDuration(500)
        animation.setFillAfter(true)

        relativeLayout{
            lparams(
                    width=matchParent,
                    height=matchParent
            )
            linearLayout{


                backgroundColor= Color.parseColor("#556f7f8f")
                isClickable()
                gravity=Gravity.CENTER
                orientation =LinearLayout.VERTICAL

                animation =animation


                textView("测试"){

                    lparams {
                        width= wrapContent
                        height= wrapContent
                        topMargin=dip(10)
                    }

                    gravity=Gravity.CENTER
                }


                com.towery.screenrecordball.ui.ProgressBall(context) {
                    lparams {
                        width = wrapContent
                        height = wrapContent
                        margin = dip(20)
                    }
                }

            }.lparams(matchParent,matchParent){
                alignParentBottom()
            }
            onTouch { view, motionEvent: MotionEvent ->
                   val manager = ViewManager.getInstance(context!!)
                   manager.showFloatBall()
                    manager.hideFloatMenu()
                    false

            }
        }
    }

    fun startAnimation() {
        animation.start()
    }
}