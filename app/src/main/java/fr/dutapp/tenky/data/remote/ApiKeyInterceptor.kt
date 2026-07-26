package fr.dutapp.tenky.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Appends the OpenWeather `appid` to every outgoing request.
 *
 * Keeping this in one interceptor means the key is referenced exactly once in
 * the codebase, and never ends up in a URL built by hand at a call site.
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter("appid", apiKey)
            .build()
        return chain.proceed(request.newBuilder().url(url).build())
    }
}
