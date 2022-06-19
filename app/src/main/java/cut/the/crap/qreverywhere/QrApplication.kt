package cut.the.crap.qreverywhere

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QrApplication : Application() {

    override fun onCreate() {
        super.onCreate()

//        Timber.plant(Timber.DebugTree())
    }
}