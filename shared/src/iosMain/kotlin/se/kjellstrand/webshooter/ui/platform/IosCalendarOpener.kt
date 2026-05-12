package se.kjellstrand.webshooter.ui.platform

import io.github.aakira.napier.Napier

/**
 * iOS calendar integration is a TODO. EventKit would be the proper path
 * (request access + EKEventStore.saveEvent), but it's its own slice;
 * for now this is a no-op so the shared UI compiles on iOS.
 */
class IosCalendarOpener : CalendarOpener {
    override fun addEvent(
        title: String,
        description: String?,
        location: String?,
        beginEpochMillis: Long,
        endEpochMillis: Long,
        allDay: Boolean,
    ) {
        Napier.w("IosCalendarOpener.addEvent is not yet implemented — '$title' was not added.")
    }
}
