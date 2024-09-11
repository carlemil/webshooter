package se.kjellstrand.webshooter.data.competitions.remote.parsing

enum class Logo(val value: String) {
    Club("club"),
    Webshooter("webshooter");

    companion object {
        public fun fromValue(value: String): Logo = when (value) {
            "club"       -> Club
            "webshooter" -> Webshooter
            else         -> throw IllegalArgumentException()
        }
    }
}