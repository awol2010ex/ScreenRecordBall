package com.towery.screenrecordball.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.towery.screenrecordball.manager.ViewManager

/** 启动悬浮球服务
 * Created by User on 2016/9/7.
 */
class StartFloatBallService :Service(){
    var manager: ViewManager? = null;
    override fun onBind(p0: Intent?): IBinder? {
       return null
    }

    override fun onDestroy() {
        super.onDestroy()
        manager!!.hideFloatBall()
    }

    override fun onCreate() {

        manager = ViewManager.getInstance(this)
        manager!!.showFloatBall()
        super.onCreate()
    }

}