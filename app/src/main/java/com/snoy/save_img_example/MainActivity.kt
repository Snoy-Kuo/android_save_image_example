package com.snoy.save_img_example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.snoy.save_img_example.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(
            "RDTest",
            "current Thread count= " + Thread.getAllStackTraces().size
                    + ", Thread activeCount= " + Thread.activeCount()
                    + ", process count= " + Runtime.getRuntime().availableProcessors()
        )
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}