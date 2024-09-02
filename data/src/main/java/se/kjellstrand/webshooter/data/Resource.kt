package se.kjellstrand.webshooter.data

typealias RootError = Error

sealed interface Resource<T, E: RootError> {
    data class Success<T, E: RootError>(val data: T) : Resource<T, E>
    data class Error<T, E: RootError>(val error: E) : Resource<T, E>
    data class Loading<T, E: RootError>(val isLoading: Boolean = true) : Resource<T, E>
}