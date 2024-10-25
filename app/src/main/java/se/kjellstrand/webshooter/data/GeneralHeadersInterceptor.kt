package se.kjellstrand.webshooter.data

import okhttp3.Interceptor
import okhttp3.Response

class GeneralHeadersInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header(
                    "Accept-Language",
                    "en,en-GB;q=0.9,sv-SE;q=0.8,sv;q=0.7,ru-KZ;q=0.6,ru;q=0.5,en-US;q=0.4"
                )
                .header("Connection", "keep-alive")
                .header("DNT", "1")
                .header("Host", "webshooter.se")
                .header("Referer", "https://webshooter.se/app/")
                .header("Sec-Fetch-Dest", "empty")
                .header("Sec-Fetch-Mode", "cors")
                .header("Sec-Fetch-Site", "same-origin")
                .header(
                    "User-agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36"
                )
                .header("X-Requested-With", "XMLHttpRequest")
                .header(
                    "sec-ch-ua",
                    "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\""
                )
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "Windows")
                .build()
        )
    }
}
