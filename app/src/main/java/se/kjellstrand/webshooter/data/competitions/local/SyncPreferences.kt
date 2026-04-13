package se.kjellstrand.webshooter.data.competitions.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getLastCompletedFullSyncMs(): Long =
        prefs.getLong(KEY_LAST_COMPLETED_FULL_SYNC, 0L)

    fun setLastCompletedFullSyncMs(timestampMs: Long) {
        prefs.edit { putLong(KEY_LAST_COMPLETED_FULL_SYNC, timestampMs) }
    }

    companion object {
        const val PREFS_NAME = "sync_prefs"
        const val KEY_LAST_COMPLETED_FULL_SYNC = "last_completed_full_sync_ms"
    }
}
