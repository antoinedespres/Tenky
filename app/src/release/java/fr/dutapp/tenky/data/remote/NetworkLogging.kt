package fr.dutapp.tenky.data.remote

import okhttp3.OkHttpClient

/**
 * Release builds do not log network traffic, and the logging interceptor is not
 * packaged at all — it is a `debugImplementation` dependency.
 */
internal fun OkHttpClient.Builder.installNetworkLogging(): OkHttpClient.Builder = this
