package se.kjellstrand.webshooter.data

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class AuthCookieJar : CookieJar {
    private val cookieStore: MutableMap<String, Cookie> = mutableMapOf()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        println("saveFromResponse: $cookies")
        for (cookie in cookies) {
            val key = "${cookie.domain}${cookie.path}${cookie.name}"
            cookieStore[key] = cookie
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        println("loadForRequest: $cookieStore")
        return cookieStore.values.filter { cookie ->
            url.host.endsWith(cookie.domain) && url.encodedPath.startsWith(cookie.path)
        }
    }

    fun getAllCookies(): String {
        val cookies = cookieStore.values.map { cookie ->
            "${cookie.name}=${cookie.value}"
        }.joinToString(separator = "; ")
        return cookies
    }

    fun getSessionCookies(): String {
        val cookies = cookieStore.values.map { cookie ->
            "${cookie.name}=${cookie.value}"
        }.filter { it == "XSRF-TOKEN" || it == "laravel_session" }.joinToString(separator = "; ")
        return cookies
    }
}