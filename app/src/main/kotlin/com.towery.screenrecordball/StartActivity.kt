package com.towery.screenrecordball

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import org.jetbrains.anko.*
class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState,persistentState)

        relativeLayout {
            lparams {
                width = matchParent
                height = matchParent
            }
            padding =dip(16)
        }
    }
}
