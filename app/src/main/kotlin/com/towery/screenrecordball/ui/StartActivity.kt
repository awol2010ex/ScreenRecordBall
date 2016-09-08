package com.towery.screenrecordball.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import com.towery.screenrecordball.R
import com.towery.screenrecordball.service.StartFloatBallService
import org.jetbrains.anko.*
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        relativeLayout {
            lparams {
                width = matchParent
                height = matchParent
            }
            padding =dip(16)

            button(R.string.start_float_ball){
                lparams {
                    width = wrapContent
                    height = wrapContent
                    centerInParent()
                }

            }.onClick {
               startBallService()
            }
        }
    }


    fun startBallService() {
        val intent = Intent(this, StartFloatBallService::class.java)
        startService(intent)
        finish()
    }
}
