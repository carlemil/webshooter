package se.kjellstrand.webshooter.ui.platform

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract

class AndroidCalendarOpener(private val context: Context) : CalendarOpener {
    override fun addEvent(
        title: String,
        description: String?,
        location: String?,
        beginEpochMillis: Long,
        endEpochMillis: Long,
        allDay: Boolean,
    ) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            if (description != null) putExtra(CalendarContract.Events.DESCRIPTION, description)
            if (location != null) putExtra(CalendarContract.Events.EVENT_LOCATION, location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginEpochMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endEpochMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
