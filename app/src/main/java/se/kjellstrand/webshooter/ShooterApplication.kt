package se.kjellstrand.webshooter

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import se.kjellstrand.webshooter.ui.login.SecurePrefs

@HiltAndroidApp
class ShooterApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SecurePrefs.init(this)
    }
}