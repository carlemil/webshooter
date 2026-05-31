package se.kjellstrand.webshooter.ui.platform

import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLComponents
import platform.Foundation.NSURLQueryItem
import platform.MessageUI.MFMailComposeResult
import platform.MessageUI.MFMailComposeViewController
import platform.MessageUI.MFMailComposeViewControllerDelegateProtocol
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * iOS URL launcher. Most schemes hand off to `UIApplication.openURL`, but
 * `mailto:` is special-cased to present `MFMailComposeViewController` from
 * the MessageUI framework. The plain `openURL` route silently no-ops on
 * Simulator (no Mail.app is installed) and also fails on devices where the
 * user has uninstalled Mail, which is what made the "Skicka förslag" menu
 * item look dead. The in-app composer works on both, provided the user has
 * at least one mail account configured in iOS Settings.
 *
 * Falls back to `openURL` if `canSendMail` returns false or the mailto URL
 * can't be parsed — matches the previous behavior in that case.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosUrlLauncher : UrlLauncher {
    // Mirror IosCalendarOpener: MFMailComposeViewController.mailComposeDelegate
    // is a weak property, so we hold a strong reference here until the
    // composer dismisses and the delegate fires.
    private var pendingMailDelegate: MailComposeDelegate? = null

    override fun openUrl(url: String) {
        if (url.startsWith("mailto:")) {
            handleMailto(url)
            return
        }
        // `geo:` is Android-only; iOS has no registered handler so openURL
        // silently no-ops. Re-route to the maps.apple.com universal URL,
        // which Apple Maps intercepts (and falls back to Safari otherwise).
        val effectiveUrl = if (url.startsWith("geo:")) geoToAppleMaps(url) ?: url else url
        val nsUrl = NSURL.URLWithString(effectiveUrl) ?: return
        UIApplication.sharedApplication.openURL(
            nsUrl,
            options = emptyMap<Any?, Any?>(),
            completionHandler = null
        )
    }

    private fun geoToAppleMaps(geoUrl: String): String? {
        val latLng = geoUrl.removePrefix("geo:")
            .substringBefore("?")
            .takeIf { it.isNotBlank() } ?: return null
        return "http://maps.apple.com/?ll=$latLng&q=$latLng"
    }

    private fun handleMailto(mailtoUrl: String) {
        val components = NSURLComponents.componentsWithString(mailtoUrl) ?: return
        val recipient = components.path?.takeIf { it.isNotBlank() } ?: return
        @Suppress("UNCHECKED_CAST")
        val subject = (components.queryItems as List<NSURLQueryItem>?)
            ?.firstOrNull { it.name == "subject" }
            ?.value
        val presenter = topPresentedViewController() ?: return

        if (MFMailComposeViewController.canSendMail()) {
            val composer = MFMailComposeViewController().apply {
                setToRecipients(listOf(recipient))
                subject?.let { setSubject(it) }
            }
            val delegate = MailComposeDelegate(onDone = { pendingMailDelegate = null })
            pendingMailDelegate = delegate
            composer.mailComposeDelegate = delegate
            presenter.presentViewController(composer, animated = true, completion = null)
        } else {
            // Simulator with no mail account, or device where the user has
            // never set one up. Copy the address to the pasteboard and tell
            // them — better than the previous silent no-op.
            Napier.w("Mail composer unavailable; showing fallback alert")
            UIPasteboard.generalPasteboard.string = recipient
            val alert = UIAlertController.alertControllerWithTitle(
                title = "Inget e-postkonto",
                message = "Konfigurera ett konto i Inställningar > Mail för att skicka direkt. " +
                    "Adressen $recipient har kopierats till urklipp.",
                preferredStyle = UIAlertControllerStyleAlert
            )
            alert.addAction(
                UIAlertAction.actionWithTitle("OK", UIAlertActionStyleDefault, null)
            )
            presenter.presentViewController(alert, animated = true, completion = null)
        }
    }

    private fun topPresentedViewController(): UIViewController? {
        var vc: UIViewController? = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (vc?.presentedViewController != null) {
            vc = vc.presentedViewController
        }
        return vc
    }
}

@OptIn(BetaInteropApi::class)
private class MailComposeDelegate(
    private val onDone: () -> Unit,
) : NSObject(), MFMailComposeViewControllerDelegateProtocol {
    override fun mailComposeController(
        controller: MFMailComposeViewController,
        didFinishWithResult: MFMailComposeResult,
        error: NSError?,
    ) {
        controller.dismissViewControllerAnimated(true) { onDone() }
    }
}
