package se.kjellstrand.webshooter.ui.platform

import io.github.aakira.napier.Napier
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.EventKit.EKEntityType
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKit.EKSpan
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970

/**
 * iOS EventKit-backed calendar opener. Mirrors the Android version's UX
 * goal of "add an event to the user's calendar" but writes directly via
 * EKEventStore.saveEvent rather than presenting Apple's event editor.
 *
 * Access is requested with the deprecated `requestAccessToEntityType` so
 * the same code path works back to iOS 13. On iOS 17+ this still works
 * (it's been preserved for compatibility) provided `NSCalendarsUsageDescription`
 * is in Info.plist.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosCalendarOpener : CalendarOpener {
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
            // Passing null for the NSError out-pointer — error details are
            // lost but logging a failure is still useful.
            val saved = store.saveEvent(event, EKSpan.EKSpanThisEvent, null)
            if (saved) {
                Napier.i("Added '$title' to calendar")
            } else {
                Napier.w("Calendar save failed for '$title'")
            }
        }
    }
}
