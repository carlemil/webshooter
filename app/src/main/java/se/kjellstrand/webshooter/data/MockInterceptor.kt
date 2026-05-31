package se.kjellstrand.webshooter.data

import android.content.Context
import okhttp3.Headers.Companion.headersOf
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
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
        val method = request.method
        val path = request.url.encodedPath

        val responseString: String = when {
            // Static / non-id-dependent endpoints
            method == "GET" && (path == "/" || path == "/app/") -> "No data available"
            method == "POST" && path == "/api/v4.1.9/oauth/token" ->
                getTextFromRaw(R.raw.api_v4_1_9_oauth_token)
            method == "GET" && path == "/api/v4.1.9/competitions" -> {
                if (request.url.queryParameter("usersignup") == "1") {
                    getTextFromRaw(R.raw.myentries)
                } else {
                    getTextFromRaw(R.raw.competitions)
                }
            }
            method == "GET" && path == "/api/v4.1.9/authenticate/user" ->
                getTextFromRaw(R.raw.authenticate_user)
            method == "PUT" && path == "/api/v4.1.9/authenticate/user" ->
                getTextFromRaw(R.raw.authenticate_user)
            method == "PUT" && path == "/api/v4.1.9/authenticate/updatePassword" -> "{}"
            method == "GET" && path == "/api/v4.1.9/clubs/getUserClub" ->
                getTextFromRaw(R.raw.clubs_get_user_club)

            // Standalone signup endpoints
            method == "GET" && path == "/api/v4.1.9/signup" -> getTextFromRaw(R.raw.signup)
            method == "POST" && path == "/api/v4.1.9/signup" -> """{"id":99999,"status":"ok"}"""
            method == "DELETE" && SIGNUP_ID_PATH.matches(path) -> "{}"

            // Per-competition detail endpoints (hardcoded per id)
            method == "GET" -> when (path) {
                "/api/v4.1.9/competitions/208/results" -> getTextFromRaw(R.raw.results_208)
                "/api/v4.1.9/competitions/208/signups" -> getTextFromRaw(R.raw.signups_208)
                "/api/v4.1.9/competitions/208/patrols" -> getTextFromRaw(R.raw.patrols_208)
                "/api/v4.1.9/competitions/208/teams" -> getTextFromRaw(R.raw.teams_208)

                "/api/v4.1.9/competitions/287/results" -> getTextFromRaw(R.raw.results_287)
                "/api/v4.1.9/competitions/287/signups" -> getTextFromRaw(R.raw.signups_287)
                "/api/v4.1.9/competitions/287/patrols" -> getTextFromRaw(R.raw.patrols_287)
                "/api/v4.1.9/competitions/287/teams" -> getTextFromRaw(R.raw.teams_287)
                "/api/v4.1.9/competitions/296/results" -> getTextFromRaw(R.raw.results_296)
                "/api/v4.1.9/competitions/296/signups" -> getTextFromRaw(R.raw.signups_296)
                "/api/v4.1.9/competitions/296/patrols" -> getTextFromRaw(R.raw.patrols_296)
                "/api/v4.1.9/competitions/296/teams" -> getTextFromRaw(R.raw.teams_296)
                "/api/v4.1.9/competitions/297/results" -> getTextFromRaw(R.raw.results_297)
                "/api/v4.1.9/competitions/297/signups" -> getTextFromRaw(R.raw.signups_297)
                "/api/v4.1.9/competitions/297/patrols" -> getTextFromRaw(R.raw.patrols_297)
                "/api/v4.1.9/competitions/297/teams" -> getTextFromRaw(R.raw.teams_297)
                "/api/v4.1.9/competitions/303/results" -> getTextFromRaw(R.raw.results_303)
                "/api/v4.1.9/competitions/303/signups" -> getTextFromRaw(R.raw.signups_303)
                "/api/v4.1.9/competitions/303/patrols" -> getTextFromRaw(R.raw.patrols_303)
                "/api/v4.1.9/competitions/303/teams" -> getTextFromRaw(R.raw.teams_303)
                "/api/v4.1.9/competitions/304/results" -> getTextFromRaw(R.raw.results_304)
                "/api/v4.1.9/competitions/304/signups" -> getTextFromRaw(R.raw.signups_304)
                "/api/v4.1.9/competitions/304/patrols" -> getTextFromRaw(R.raw.patrols_304)
                "/api/v4.1.9/competitions/304/teams" -> getTextFromRaw(R.raw.teams_304)
                "/api/v4.1.9/competitions/305/results" -> getTextFromRaw(R.raw.results_305)
                "/api/v4.1.9/competitions/305/signups" -> getTextFromRaw(R.raw.signups_305)
                "/api/v4.1.9/competitions/305/patrols" -> getTextFromRaw(R.raw.patrols_305)
                "/api/v4.1.9/competitions/305/teams" -> getTextFromRaw(R.raw.teams_305)
                "/api/v4.1.9/competitions/306/results" -> getTextFromRaw(R.raw.results_306)
                "/api/v4.1.9/competitions/306/signups" -> getTextFromRaw(R.raw.signups_306)
                "/api/v4.1.9/competitions/306/patrols" -> getTextFromRaw(R.raw.patrols_306)
                "/api/v4.1.9/competitions/306/teams" -> getTextFromRaw(R.raw.teams_306)
                "/api/v4.1.9/competitions/307/results" -> getTextFromRaw(R.raw.results_307)
                "/api/v4.1.9/competitions/307/signups" -> getTextFromRaw(R.raw.signups_307)
                "/api/v4.1.9/competitions/307/patrols" -> getTextFromRaw(R.raw.patrols_307)
                "/api/v4.1.9/competitions/307/teams" -> getTextFromRaw(R.raw.teams_307)
                "/api/v4.1.9/competitions/308/results" -> getTextFromRaw(R.raw.results_308)
                "/api/v4.1.9/competitions/308/signups" -> getTextFromRaw(R.raw.signups_308)
                "/api/v4.1.9/competitions/308/patrols" -> getTextFromRaw(R.raw.patrols_308)
                "/api/v4.1.9/competitions/308/teams" -> getTextFromRaw(R.raw.teams_308)
                "/api/v4.1.9/competitions/309/results" -> getTextFromRaw(R.raw.results_309)
                "/api/v4.1.9/competitions/309/signups" -> getTextFromRaw(R.raw.signups_309)
                "/api/v4.1.9/competitions/309/patrols" -> getTextFromRaw(R.raw.patrols_309)
                "/api/v4.1.9/competitions/309/teams" -> getTextFromRaw(R.raw.teams_309)
                "/api/v4.1.9/competitions/311/results" -> getTextFromRaw(R.raw.results_311)
                "/api/v4.1.9/competitions/311/signups" -> getTextFromRaw(R.raw.signups_311)
                "/api/v4.1.9/competitions/311/patrols" -> getTextFromRaw(R.raw.patrols_311)
                "/api/v4.1.9/competitions/311/teams" -> getTextFromRaw(R.raw.teams_311)
                "/api/v4.1.9/competitions/312/results" -> getTextFromRaw(R.raw.results_312)
                "/api/v4.1.9/competitions/312/signups" -> getTextFromRaw(R.raw.signups_312)
                "/api/v4.1.9/competitions/312/patrols" -> getTextFromRaw(R.raw.patrols_312)
                "/api/v4.1.9/competitions/312/teams" -> getTextFromRaw(R.raw.teams_312)
                "/api/v4.1.9/competitions/313/results" -> getTextFromRaw(R.raw.results_313)
                "/api/v4.1.9/competitions/313/signups" -> getTextFromRaw(R.raw.signups_313)
                "/api/v4.1.9/competitions/313/patrols" -> getTextFromRaw(R.raw.patrols_313)
                "/api/v4.1.9/competitions/313/teams" -> getTextFromRaw(R.raw.teams_313)
                "/api/v4.1.9/competitions/314/results" -> getTextFromRaw(R.raw.results_314)
                "/api/v4.1.9/competitions/314/signups" -> getTextFromRaw(R.raw.signups_314)
                "/api/v4.1.9/competitions/314/patrols" -> getTextFromRaw(R.raw.patrols_314)
                "/api/v4.1.9/competitions/314/teams" -> getTextFromRaw(R.raw.teams_314)
                "/api/v4.1.9/competitions/315/results" -> getTextFromRaw(R.raw.results_315)
                "/api/v4.1.9/competitions/315/signups" -> getTextFromRaw(R.raw.signups_315)
                "/api/v4.1.9/competitions/315/patrols" -> getTextFromRaw(R.raw.patrols_315)
                "/api/v4.1.9/competitions/315/teams" -> getTextFromRaw(R.raw.teams_315)
                "/api/v4.1.9/competitions/316/results" -> getTextFromRaw(R.raw.results_316)
                "/api/v4.1.9/competitions/316/signups" -> getTextFromRaw(R.raw.signups_316)
                "/api/v4.1.9/competitions/316/patrols" -> getTextFromRaw(R.raw.patrols_316)
                "/api/v4.1.9/competitions/316/teams" -> getTextFromRaw(R.raw.teams_316)
                "/api/v4.1.9/competitions/317/results" -> getTextFromRaw(R.raw.results_317)
                "/api/v4.1.9/competitions/317/signups" -> getTextFromRaw(R.raw.signups_317)
                "/api/v4.1.9/competitions/317/patrols" -> getTextFromRaw(R.raw.patrols_317)
                "/api/v4.1.9/competitions/317/teams" -> getTextFromRaw(R.raw.teams_317)
                "/api/v4.1.9/competitions/318/results" -> getTextFromRaw(R.raw.results_318)
                "/api/v4.1.9/competitions/318/signups" -> getTextFromRaw(R.raw.signups_318)
                "/api/v4.1.9/competitions/318/patrols" -> getTextFromRaw(R.raw.patrols_318)
                "/api/v4.1.9/competitions/318/teams" -> getTextFromRaw(R.raw.teams_318)
                "/api/v4.1.9/competitions/319/results" -> getTextFromRaw(R.raw.results_319)
                "/api/v4.1.9/competitions/319/signups" -> getTextFromRaw(R.raw.signups_319)
                "/api/v4.1.9/competitions/319/patrols" -> getTextFromRaw(R.raw.patrols_319)
                "/api/v4.1.9/competitions/319/teams" -> getTextFromRaw(R.raw.teams_319)
                "/api/v4.1.9/competitions/320/results" -> getTextFromRaw(R.raw.results_320)
                "/api/v4.1.9/competitions/320/signups" -> getTextFromRaw(R.raw.signups_320)
                "/api/v4.1.9/competitions/320/patrols" -> getTextFromRaw(R.raw.patrols_320)
                "/api/v4.1.9/competitions/320/teams" -> getTextFromRaw(R.raw.teams_320)
                "/api/v4.1.9/competitions/321/results" -> getTextFromRaw(R.raw.results_321)
                "/api/v4.1.9/competitions/321/signups" -> getTextFromRaw(R.raw.signups_321)
                "/api/v4.1.9/competitions/321/patrols" -> getTextFromRaw(R.raw.patrols_321)
                "/api/v4.1.9/competitions/321/teams" -> getTextFromRaw(R.raw.teams_321)
                "/api/v4.1.9/competitions/322/results" -> getTextFromRaw(R.raw.results_322)
                "/api/v4.1.9/competitions/322/signups" -> getTextFromRaw(R.raw.signups_322)
                "/api/v4.1.9/competitions/322/patrols" -> getTextFromRaw(R.raw.patrols_322)
                "/api/v4.1.9/competitions/322/teams" -> getTextFromRaw(R.raw.teams_322)
                "/api/v4.1.9/competitions/324/results" -> getTextFromRaw(R.raw.results_324)
                "/api/v4.1.9/competitions/324/signups" -> getTextFromRaw(R.raw.signups_324)
                "/api/v4.1.9/competitions/324/patrols" -> getTextFromRaw(R.raw.patrols_324)
                "/api/v4.1.9/competitions/324/teams" -> getTextFromRaw(R.raw.teams_324)
                "/api/v4.1.9/competitions/325/results" -> getTextFromRaw(R.raw.results_325)
                "/api/v4.1.9/competitions/325/signups" -> getTextFromRaw(R.raw.signups_325)
                "/api/v4.1.9/competitions/325/patrols" -> getTextFromRaw(R.raw.patrols_325)
                "/api/v4.1.9/competitions/325/teams" -> getTextFromRaw(R.raw.teams_325)
                "/api/v4.1.9/competitions/326/results" -> getTextFromRaw(R.raw.results_326)
                "/api/v4.1.9/competitions/326/signups" -> getTextFromRaw(R.raw.signups_326)
                "/api/v4.1.9/competitions/326/patrols" -> getTextFromRaw(R.raw.patrols_326)
                "/api/v4.1.9/competitions/326/teams" -> getTextFromRaw(R.raw.teams_326)

                else -> ""
            }

            else -> ""
        }

        val responseCode = if (responseString.isEmpty()) 404 else 200
        val responseHeaders = when (path) {
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

    companion object {
        private val SIGNUP_ID_PATH = Regex("""^/api/v4\.1\.9/signup/\d+$""")
    }
}
