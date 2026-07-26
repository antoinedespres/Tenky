package fr.dutapp.tenky.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * One entry of `GET /geo/1.0/direct`. Used to turn a typed city name into
 * coordinates, and to disambiguate between cities that share a name.
 */
@Serializable
data class GeocodingResultDto(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String? = null,
    val state: String? = null,
)
