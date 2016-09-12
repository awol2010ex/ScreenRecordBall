package com.towery.screenrecordball.ui

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.towery.screenrecordball.R
import com.towery.screenrecordball.event.MessageEvent
import com.towery.screenrecordball.service.StartFloatBallService
import com.towery.screenrecordball.thread.ScreenRecorder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jetbrains.anko.*
import java.io.File

class StartActivity : AppCompatActivity() {

    var is_opened =false //是否已开启悬浮球
    var service_intent: Intent? = null

    private val REQUEST_CODE = 1
    private val mMediaProjectionManager: MediaProjectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private var mRecorder: ScreenRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
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
        if(mRecorder != null){
            mRecorder!!.quit();
            mRecorder = null;
        }
        EventBus.getDefault().unregister(this)
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


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        if (mRecorder != null) {
            mRecorder!!.quit()
            mRecorder = null
        } else {
            val captureIntent = mMediaProjectionManager!!.createScreenCaptureIntent()
            startActivityForResult(captureIntent, REQUEST_CODE)
        }
    }


    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        val mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data)
        if (mediaProjection == null) {
            Log.e("@@", "media projection is null")
            return
        }
        // video size
        val mWidth = 1280
        val mHeight = 720
        var folder =File(Environment.getExternalStorageDirectory().path+"/screenrecordball")
        if(!folder.exists()){
            folder.mkdirs()
        }
        val file = File(Environment.getExternalStorageDirectory(),
                "screenrecordball/record-" + mWidth + "x" + mHeight + "-" + System.currentTimeMillis() + ".mp4")
        val bitrate = 6000000
        mRecorder = ScreenRecorder(mWidth, mHeight, bitrate, 1, mediaProjection, file.absolutePath)
        mRecorder!!.start()
        Toast.makeText(this, "Screen recorder is running...", Toast.LENGTH_SHORT).show()
    }

}
