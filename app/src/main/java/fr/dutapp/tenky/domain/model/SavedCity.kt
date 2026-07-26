package fr.dutapp.tenky.domain.model

/** A city the user has pinned to the "All cities" screen. */
data class SavedCity(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val state: String? = null,
) {
    val coordinates: Coordinates get() = Coordinates(latitude, longitude)

    /** "Lyon, Auvergne-Rhône-Alpes, FR" — enough to tell duplicates apart. */
    val qualifiedName: String
        get() = listOfNotNull(name, state, country).joinToString(", ")
}
