package se.kjellstrand.webshooter.data

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.HttpClientCall
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.plugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpMethod
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.util.InternalAPI
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteReadChannel
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.Job
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.stringWithContentsOfFile

/**
 * iOS-side counterpart to the Android `MockInterceptor`: when
 * [MockModeManager.isMockMode] is on, short-circuits outbound Ktor requests
 * with canned JSON loaded from the app bundle under `Resources/mocks/`.
 *
 * Wired via `HttpSend` so the rest of the client pipeline (cookies, auth)
 * is still consulted on real requests but bypassed entirely in mock mode.
 * Mock content lives in `iosApp/iosApp/Resources/mocks/` and is copied
 * directly from `app/src/main/res/raw/` — drift between the two is
 * accepted; consolidating to a single source is a follow-up.
 *
 * Path → resource mapping mirrors `app/.../MockInterceptor.kt` so reviewer
 * credentials (`mockuser`/`mockpassword`) drive identical behaviour on both
 * platforms.
 */
@OptIn(InternalAPI::class)
fun installIosMockInterceptor(client: HttpClient) {
    client.plugin(HttpSend).intercept { request ->
        if (!MockModeManager.isMockMode) {
            execute(request)
        } else {
            val body = mockBodyFor(request)
            val status = if (body.isEmpty()) HttpStatusCode.NotFound else HttpStatusCode.OK
            val responseData = HttpResponseData(
                statusCode = status,
                requestTime = GMTDate(),
                headers = headersOf("Content-Type", "application/json"),
                version = HttpProtocolVersion.HTTP_1_1,
                body = ByteReadChannel(body.encodeToByteArray()),
                callContext = coroutineContext + Job(),
            )
            HttpClientCall(client, request.build(), responseData)
        }
    }
}

private fun mockBodyFor(builder: HttpRequestBuilder): String {
    val path = "/" + builder.url.encodedPathSegments.filter { it.isNotEmpty() }.joinToString("/")
    val method = builder.method
    val isMyEntries = builder.url.parameters["usersignup"] == "1"

    // Static / non-id-dependent endpoints
    when {
        method == HttpMethod.Get && (path == "/" || path == "/app/") -> return "No data available"
        method == HttpMethod.Post && path == "/api/v4.1.9/oauth/token" ->
            return loadBundleResource("api_v4_1_9_oauth_token") ?: ""
        method == HttpMethod.Get && path == "/api/v4.1.9/competitions" ->
            return loadBundleResource(if (isMyEntries) "myentries" else "competitions") ?: ""
        method == HttpMethod.Get && path == "/api/v4.1.9/authenticate/user" ->
            return loadBundleResource("authenticate_user") ?: ""
        method == HttpMethod.Put && path == "/api/v4.1.9/authenticate/user" ->
            return loadBundleResource("authenticate_user") ?: ""
        method == HttpMethod.Put && path == "/api/v4.1.9/authenticate/updatePassword" -> return "{}"
        method == HttpMethod.Get && path == "/api/v4.1.9/clubs/getUserClub" ->
            return loadBundleResource("clubs_get_user_club") ?: ""

        // Standalone signup endpoints
        method == HttpMethod.Get && path == "/api/v4.1.9/signup" ->
            return loadBundleResource("signup") ?: ""
        method == HttpMethod.Post && path == "/api/v4.1.9/signup" ->
            return """{"id":99999,"status":"ok"}"""
        method == HttpMethod.Delete && SIGNUP_ID_PATH.matches(path) -> return "{}"
    }

    if (method != HttpMethod.Get) return ""

    val resourceName = when (path) {
        "/api/v4.1.9/competitions/208/results" -> "results_208"
        "/api/v4.1.9/competitions/208/signups" -> "signups_208"
        "/api/v4.1.9/competitions/208/patrols" -> "patrols_208"
        "/api/v4.1.9/competitions/208/teams" -> "teams_208"

        "/api/v4.1.9/competitions/287/results" -> "results_287"
        "/api/v4.1.9/competitions/287/signups" -> "signups_287"
        "/api/v4.1.9/competitions/287/patrols" -> "patrols_287"
        "/api/v4.1.9/competitions/287/teams" -> "teams_287"
        "/api/v4.1.9/competitions/296/results" -> "results_296"
        "/api/v4.1.9/competitions/296/signups" -> "signups_296"
        "/api/v4.1.9/competitions/296/patrols" -> "patrols_296"
        "/api/v4.1.9/competitions/296/teams" -> "teams_296"
        "/api/v4.1.9/competitions/297/results" -> "results_297"
        "/api/v4.1.9/competitions/297/signups" -> "signups_297"
        "/api/v4.1.9/competitions/297/patrols" -> "patrols_297"
        "/api/v4.1.9/competitions/297/teams" -> "teams_297"
        "/api/v4.1.9/competitions/303/results" -> "results_303"
        "/api/v4.1.9/competitions/303/signups" -> "signups_303"
        "/api/v4.1.9/competitions/303/patrols" -> "patrols_303"
        "/api/v4.1.9/competitions/303/teams" -> "teams_303"
        "/api/v4.1.9/competitions/304/results" -> "results_304"
        "/api/v4.1.9/competitions/304/signups" -> "signups_304"
        "/api/v4.1.9/competitions/304/patrols" -> "patrols_304"
        "/api/v4.1.9/competitions/304/teams" -> "teams_304"
        "/api/v4.1.9/competitions/305/results" -> "results_305"
        "/api/v4.1.9/competitions/305/signups" -> "signups_305"
        "/api/v4.1.9/competitions/305/patrols" -> "patrols_305"
        "/api/v4.1.9/competitions/305/teams" -> "teams_305"
        "/api/v4.1.9/competitions/306/results" -> "results_306"
        "/api/v4.1.9/competitions/306/signups" -> "signups_306"
        "/api/v4.1.9/competitions/306/patrols" -> "patrols_306"
        "/api/v4.1.9/competitions/306/teams" -> "teams_306"
        "/api/v4.1.9/competitions/307/results" -> "results_307"
        "/api/v4.1.9/competitions/307/signups" -> "signups_307"
        "/api/v4.1.9/competitions/307/patrols" -> "patrols_307"
        "/api/v4.1.9/competitions/307/teams" -> "teams_307"
        "/api/v4.1.9/competitions/308/results" -> "results_308"
        "/api/v4.1.9/competitions/308/signups" -> "signups_308"
        "/api/v4.1.9/competitions/308/patrols" -> "patrols_308"
        "/api/v4.1.9/competitions/308/teams" -> "teams_308"
        "/api/v4.1.9/competitions/309/results" -> "results_309"
        "/api/v4.1.9/competitions/309/signups" -> "signups_309"
        "/api/v4.1.9/competitions/309/patrols" -> "patrols_309"
        "/api/v4.1.9/competitions/309/teams" -> "teams_309"
        "/api/v4.1.9/competitions/311/results" -> "results_311"
        "/api/v4.1.9/competitions/311/signups" -> "signups_311"
        "/api/v4.1.9/competitions/311/patrols" -> "patrols_311"
        "/api/v4.1.9/competitions/311/teams" -> "teams_311"
        "/api/v4.1.9/competitions/312/results" -> "results_312"
        "/api/v4.1.9/competitions/312/signups" -> "signups_312"
        "/api/v4.1.9/competitions/312/patrols" -> "patrols_312"
        "/api/v4.1.9/competitions/312/teams" -> "teams_312"
        "/api/v4.1.9/competitions/313/results" -> "results_313"
        "/api/v4.1.9/competitions/313/signups" -> "signups_313"
        "/api/v4.1.9/competitions/313/patrols" -> "patrols_313"
        "/api/v4.1.9/competitions/313/teams" -> "teams_313"
        "/api/v4.1.9/competitions/314/results" -> "results_314"
        "/api/v4.1.9/competitions/314/signups" -> "signups_314"
        "/api/v4.1.9/competitions/314/patrols" -> "patrols_314"
        "/api/v4.1.9/competitions/314/teams" -> "teams_314"
        "/api/v4.1.9/competitions/315/results" -> "results_315"
        "/api/v4.1.9/competitions/315/signups" -> "signups_315"
        "/api/v4.1.9/competitions/315/patrols" -> "patrols_315"
        "/api/v4.1.9/competitions/315/teams" -> "teams_315"
        "/api/v4.1.9/competitions/316/results" -> "results_316"
        "/api/v4.1.9/competitions/316/signups" -> "signups_316"
        "/api/v4.1.9/competitions/316/patrols" -> "patrols_316"
        "/api/v4.1.9/competitions/316/teams" -> "teams_316"
        "/api/v4.1.9/competitions/317/results" -> "results_317"
        "/api/v4.1.9/competitions/317/signups" -> "signups_317"
        "/api/v4.1.9/competitions/317/patrols" -> "patrols_317"
        "/api/v4.1.9/competitions/317/teams" -> "teams_317"
        "/api/v4.1.9/competitions/318/results" -> "results_318"
        "/api/v4.1.9/competitions/318/signups" -> "signups_318"
        "/api/v4.1.9/competitions/318/patrols" -> "patrols_318"
        "/api/v4.1.9/competitions/318/teams" -> "teams_318"
        "/api/v4.1.9/competitions/319/results" -> "results_319"
        "/api/v4.1.9/competitions/319/signups" -> "signups_319"
        "/api/v4.1.9/competitions/319/patrols" -> "patrols_319"
        "/api/v4.1.9/competitions/319/teams" -> "teams_319"
        "/api/v4.1.9/competitions/320/results" -> "results_320"
        "/api/v4.1.9/competitions/320/signups" -> "signups_320"
        "/api/v4.1.9/competitions/320/patrols" -> "patrols_320"
        "/api/v4.1.9/competitions/320/teams" -> "teams_320"
        "/api/v4.1.9/competitions/321/results" -> "results_321"
        "/api/v4.1.9/competitions/321/signups" -> "signups_321"
        "/api/v4.1.9/competitions/321/patrols" -> "patrols_321"
        "/api/v4.1.9/competitions/321/teams" -> "teams_321"
        "/api/v4.1.9/competitions/322/results" -> "results_322"
        "/api/v4.1.9/competitions/322/signups" -> "signups_322"
        "/api/v4.1.9/competitions/322/patrols" -> "patrols_322"
        "/api/v4.1.9/competitions/322/teams" -> "teams_322"
        "/api/v4.1.9/competitions/324/results" -> "results_324"
        "/api/v4.1.9/competitions/324/signups" -> "signups_324"
        "/api/v4.1.9/competitions/324/patrols" -> "patrols_324"
        "/api/v4.1.9/competitions/324/teams" -> "teams_324"
        "/api/v4.1.9/competitions/325/results" -> "results_325"
        "/api/v4.1.9/competitions/325/signups" -> "signups_325"
        "/api/v4.1.9/competitions/325/patrols" -> "patrols_325"
        "/api/v4.1.9/competitions/325/teams" -> "teams_325"
        "/api/v4.1.9/competitions/326/results" -> "results_326"
        "/api/v4.1.9/competitions/326/signups" -> "signups_326"
        "/api/v4.1.9/competitions/326/patrols" -> "patrols_326"
        "/api/v4.1.9/competitions/326/teams" -> "teams_326"

        else -> return ""
    }
    return loadBundleResource(resourceName) ?: ""
}

@OptIn(ExperimentalForeignApi::class)
private fun loadBundleResource(name: String): String? {
    val path = NSBundle.mainBundle.pathForResource(name, "txt", "mocks") ?: run {
        Napier.w("Mock resource not found in bundle: mocks/$name.txt", tag = TAG)
        return null
    }
    @Suppress("CAST_NEVER_SUCCEEDS")
    return NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as String?
}

private val SIGNUP_ID_PATH = Regex("""^/api/v4\.1\.9/signup/\d+$""")

private const val TAG = "IosMockInterceptor"
