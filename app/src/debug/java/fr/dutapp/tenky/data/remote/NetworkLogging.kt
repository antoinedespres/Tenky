package fr.dutapp.tenky.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

/**
 * Debug builds log one line per request.
 *
 * `appid` is redacted so the API key never reaches logcat, where any app with
 * read access — or anyone looking over a shoulder — could pick it up.
 */
internal fun OkHttpClient.Builder.installNetworkLogging(): OkHttpClient.Builder =
    addInterceptor(
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
            redactQueryParams("appid")
        },
    )
