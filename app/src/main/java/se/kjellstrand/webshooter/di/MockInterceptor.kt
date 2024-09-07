package se.kjellstrand.webshooter.di

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import se.kjellstrand.webshooter.R
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MockInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Intercept request and provide mock data for certain endpoints
        val responseString = when (request.url.encodedPath) {
            "/api/v4.1.9/oauth/token" -> getTextFromRaw(R.raw.api_v4_1_9_oauth_token)
            "/getUserDetails" -> "{ \"id\": \"1\", \"name\": \"Mock User\", \"email\": \"mock@example.com\" }"
            else -> throw IllegalArgumentException("Unknown request path: ${request.url.encodedPath}")
        }

        return Response.Builder()
            .code(200)
            .message(responseString)
            .body(ResponseBody.create("application/json".toMediaType(), responseString))
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
            // Handle the exception
        }
    }
}