package se.kjellstrand.webshooter.ui.platform

import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.darwin.NSObject

/**
 * iOS EventKit-backed calendar opener. Presents the system's
 * [EKEventEditViewController] modally so the user can review and confirm
 * the event — matches the Android picker UX where the system calendar app
 * opens its own editor on top of the WebShooter activity.
 *
 * Falls back to a silent `saveEvent` write only if no presenting view
 * controller can be located (e.g. mid-launch or background) — better to
 * still capture the entry than drop it.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosCalendarOpener : CalendarOpener {
    // Hold a strong reference to the delegate so it isn't deallocated mid-flight.
    private var pendingDelegate: EventEditDelegate? = null

    override fun addEvent(
        title: String,
        description: String?,
        location: String?,
        beginEpochMillis: Long,
        endEpochMillis: Long,
        allDay: Boolean,
    ) {
        val store = EKEventStore()
        store.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, error ->
            if (!granted || error != null) {
                Napier.w("Calendar access denied: ${error?.localizedDescription ?: "no permission"}")
                return@requestAccessToEntityType
            }
            val event = EKEvent.eventWithEventStore(store).apply {
                this.title = title
                this.notes = description
                this.location = location
                startDate = NSDate.dateWithTimeIntervalSince1970(beginEpochMillis / 1000.0)
                endDate = NSDate.dateWithTimeIntervalSince1970(endEpochMillis / 1000.0)
                this.allDay = allDay
                calendar = store.defaultCalendarForNewEvents
            }

            val presenter = topPresentedViewController()
            if (presenter == null) {
                Napier.w("No presenting UIViewController; falling back to silent save")
                runCatching { store.saveEvent(event, platform.EventKit.EKSpan.EKSpanThisEvent, null) }
                return@requestAccessToEntityType
            }

            val editVc = EKEventEditViewController().apply {
                eventStore = store
                this.event = event
            }
            val delegate = EventEditDelegate(
                onDone = { pendingDelegate = null }
            )
            pendingDelegate = delegate
            editVc.editViewDelegate = delegate
            presenter.presentViewController(editVc, animated = true, completion = null)
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
private class EventEditDelegate(
    private val onDone: () -> Unit,
) : NSObject(), EKEventEditViewDelegateProtocol {
    override fun eventEditViewController(
        controller: EKEventEditViewController,
        didCompleteWithAction: EKEventEditViewAction,
    ) {
        controller.dismissViewControllerAnimated(true) { onDone() }
    }
}
