package se.kjellstrand.webshooter.data

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow




class SessionManager constructor() {

    sealed class SessionEvent {
        data object Expired : SessionEvent()
    }

    private val _events = MutableSharedFlow<SessionEvent>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    fun emitSessionExpired() {
        _events.tryEmit(SessionEvent.Expired)
    }
}
