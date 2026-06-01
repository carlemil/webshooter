package se.kjellstrand.webshooter.data.common

sealed interface Error

sealed class UserError : Error {
    data object IOError : UserError()
    /**
     * Carries the endpoint path alongside the status code so error
     * triage knows *which* endpoint failed without grepping repository
     * source. Optional to keep existing call sites that didn't have a
     * URL handy compatible.
     */
    data class HttpError(
        val statusCode: Int = 0,
        val path: String? = null,
    ) : UserError()
    data object UnknownError : UserError()
}