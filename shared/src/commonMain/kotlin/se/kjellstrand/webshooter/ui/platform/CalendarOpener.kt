package se.kjellstrand.webshooter.ui.platform

interface CalendarOpener {
    fun addEvent(
        title: String,
        description: String?,
        location: String?,
        beginEpochMillis: Long,
        endEpochMillis: Long,
        allDay: Boolean,
    )
}
