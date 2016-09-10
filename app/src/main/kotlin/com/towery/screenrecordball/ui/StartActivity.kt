package com.towery.screenrecordball.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import android.widget.Toast
import com.towery.screenrecordball.R
import com.towery.screenrecordball.service.StartFloatBallService
import org.jetbrains.anko.*
class StartActivity : AppCompatActivity() {

    var is_opened =false //是否已开启悬浮球
    var service_intent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        relativeLayout {
            lparams {
                width = matchParent
                height = matchParent
            }
            padding =dip(16)

            val btn=button(R.string.start_float_ball){
                lparams {
                    width = wrapContent
                    height = wrapContent
                    centerInParent()
                }

            }
            btn.onClick {
                if(!is_opened) {
                    is_opened=true
                    startBallService()

                    btn.setText(R.string.stop_float_ball)
                    moveTaskToBack(true);

                }
                else{
                    is_opened=false
                    stopBallService();
                    btn.setText(R.string.start_float_ball)
                    moveTaskToBack(true);
                }
            }

        }
    }

    override fun onDestroy() {
        stopBallService();
        super.onDestroy()
    }


    fun startBallService() {
        service_intent = Intent(this, StartFloatBallService::class.java)
        startService(service_intent)
       // finish()
    }

    fun stopBallService(){

        if(service_intent!=null){
            stopService(service_intent)
           // finish()
        }
    }

    /*再按一次停止*/
    override fun onBackPressed() {

        exit()
    }

    private var waitExit = true
    private var toast: Toast? = null
    private val mHandler = Handler()

    private fun exit() {
        if (waitExit) {
            waitExit = false
            toast = Toast.makeText(this.applicationContext, getString(R.string.press_to_exit), Toast.LENGTH_SHORT)
            toast!!.show()
            mHandler.postDelayed({waitExit = true} , 2000)
        } else {
            toast!!.cancel()
            finish()
            stopBallService();
            System.exit(0)
        }
    }

}
