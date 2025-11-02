package cut.the.crap.qreverywhere

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import cut.the.crap.qreverywhere.di.getAllModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class QrApplication : Application() , CameraXConfig.Provider {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        // Start Koin
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@QrApplication)
            modules(getAllModules())
        }
    }

    override fun getCameraXConfig(): CameraXConfig {
        return Camera2Config.defaultConfig()
    }
}