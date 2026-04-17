package com.skydev.canvastest

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.skydev.canvastest.data.objectbox.MyObjectBox
import com.skydev.canvastest.data.objectbox.ObjectBox
import dagger.hilt.android.HiltAndroidApp
import io.objectbox.BoxStore
import javax.inject.Inject

@HiltAndroidApp
class MainApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()



    override fun onCreate() {
        super.onCreate()
        ObjectBox.init(this)
    }
}