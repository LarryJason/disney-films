package fr.example.tuenolarryjason.disneyfilms.models

data class UserFilm(
    val filmTitle: String = "",
    val isWatched: Boolean = false,
    val wantToWatch: Boolean = false,
    val ownOnDVD: Boolean = false,
    val wantToGetRidOf: Boolean = false
)
