package se.kjellstrand.webshooter.ui.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class IosUrlLauncher : UrlLauncher {
    override fun openUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url) ?: return
        UIApplication.sharedApplication.openURL(nsUrl, options = emptyMap<Any?, Any?>(), completionHandler = null)
    }
}
