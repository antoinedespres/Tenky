package fr.dutapp.tenky.domain.model

/** Why a request could not be served. Each case maps to a user-facing message. */
enum class WeatherError {
    /** No key was supplied at build time. */
    MissingApiKey,

    /** The key was rejected (HTTP 401) — wrong, revoked, or not yet activated. */
    InvalidApiKey,

    /** Device is offline, or the host could not be reached. */
    Network,

    /** Too many calls for the current plan (HTTP 429). */
    RateLimited,

    /** The place has no data, or the search returned nothing (HTTP 404). */
    NotFound,

    /** Anything else, including malformed responses. */
    Unknown,
}

/** A success/failure pair used across the data layer. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>

    data class Failure(val error: WeatherError) : DataResult<Nothing>
}

inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> = when (this) {
    is DataResult.Success -> DataResult.Success(transform(data))
    is DataResult.Failure -> this
}

fun <T> DataResult<T>.getOrNull(): T? = (this as? DataResult.Success)?.data
