package se.kjellstrand.webshooter

import android.app.Application
import android.os.StrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ShooterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .detectActivityLeaks()
                    .detectLeakedRegistrationObjects()
                    .detectFileUriExposure()
                    .detectCleartextNetwork()
                    .detectContentUriWithoutPermission()
                    // Intentionally omitting detectUntaggedSockets() -
                    // triggered by Google's datatransport library (Firebase/Crashlytics)
                    // which we cannot control.
                    .penaltyLog()
                    .build()
            )
        }
    }
}