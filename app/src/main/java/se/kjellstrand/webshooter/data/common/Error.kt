package se.kjellstrand.webshooter.data.common

sealed interface Error

enum class UserError : Error {
    IOError,
    HttpError,
    UnknownError
}