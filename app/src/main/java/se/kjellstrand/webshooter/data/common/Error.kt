package se.kjellstrand.webshooter.data.common

sealed interface Error

sealed class UserError : Error {
    data object IOError : UserError()
    data class HttpError(val statusCode: Int = 0) : UserError()
    data object UnknownError : UserError()
}