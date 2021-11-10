package me.proton.android.pass

import android.app.Application
import android.os.Build
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp
import me.proton.android.pass.log.AppLogTag.STRICT_MODE
import me.proton.android.pass.log.PassKeyLogger
import me.proton.android.pass.log.deviceInfo
import me.proton.core.util.kotlin.Logger
import timber.log.Timber
import java.util.concurrent.Executors

@HiltAndroidApp
class App: Application() {

    private val logger: Logger = PassKeyLogger

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        logger.deviceInfo()
    }

    private fun strictMode() {
        val threadPolicyBuilder = StrictMode.ThreadPolicy.Builder()
            .detectAll()
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectAll()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            threadPolicyBuilder.penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                logger.e(STRICT_MODE, violation)
            }
            vmPolicyBuilder.penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                logger.e(STRICT_MODE, violation)
            }
        } else {
            threadPolicyBuilder.penaltyLog()
            vmPolicyBuilder.penaltyLog()
        }
        StrictMode.setThreadPolicy(threadPolicyBuilder.build())
        StrictMode.setVmPolicy(vmPolicyBuilder.build())
    }
}
