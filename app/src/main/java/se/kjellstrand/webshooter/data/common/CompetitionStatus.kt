package se.kjellstrand.webshooter.data.common

enum class CompetitionStatus(val status: String) {
    OPEN("open"),
    ALL("all"),
    COMPLETED("completed"),
    UPCOMING("upcoming"),
    CLOSED("closed")
}