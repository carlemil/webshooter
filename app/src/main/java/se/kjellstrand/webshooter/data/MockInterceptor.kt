package se.kjellstrand.webshooter.data

import android.content.Context
import okhttp3.Headers.Companion.headersOf
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import se.kjellstrand.webshooter.BuildConfig
import se.kjellstrand.webshooter.R
import java.io.IOException

open class MockInterceptor(
    private val context: Context,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        if (!MockModeManager.isMockMode) {
            return chain.proceed(chain.request())
        }

        val request = chain.request()

        val responseString = when (request.url.encodedPath) {
            "/" -> "No data available"
            "/app/" -> "No data available"
            "/api/v4.1.9/oauth/token" -> getTextFromRaw(R.raw.api_v4_1_9_oauth_token)
            "/api/v4.1.9/competitions" -> {
                if (request.url.queryParameter("usersignup") == "1") {
                    getTextFromRaw(R.raw.myentries)
                } else {
                    getTextFromRaw(R.raw.competitions)
                }
            }
            "/api/v4.1.9/competitions/208/results" -> getTextFromRaw(R.raw.results_208)
            "/api/v4.1.9/authenticate/user" -> getTextFromRaw(R.raw.authenticate_user)
            "/api/v4.1.9/authenticate/updatePassword" -> "{}"
            "/api/v4.1.9/clubs/getUserClub" -> getTextFromRaw(R.raw.clubs_get_user_club)
            else -> ""
        }

        val responseCode = when {
            responseString.isEmpty() -> 404
            else -> 200
        }

        val responseHeaders = when (request.url.encodedPath) {
            "/" -> headersOf("header", "headertest")
            else -> headersOf()
        }
        return Response.Builder()
            .code(responseCode)
            .message(responseString)
            .body(responseString.toResponseBody("application/json".toMediaType()))
            .headers(responseHeaders)
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .build()
    }

    private fun getTextFromRaw(raw: Int): String {
        try {
            val inputStream = context.resources.openRawResource(raw)
            return inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw e
        }
    }
}