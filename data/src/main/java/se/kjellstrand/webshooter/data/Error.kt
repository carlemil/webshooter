package se.kjellstrand.webshooter.data

sealed interface Error

enum class UserError : Error {
    IOError,
    HttpError,
    UnknownError
}