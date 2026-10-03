package se.kjellstrand.webshooter.data.settings

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Flatten a JSON object into form fields the way the webshooter.se web
 * client does (jQuery `$.param`): nested objects become `key[sub]`, arrays
 * `key[0]`, `null` becomes an empty string.
 *
 * The web client saves the profile by PUTting the *whole* user object it
 * loaded, including fields this app has no UI for (`grade_military`,
 * `shooting_card_year`, …). Sending those back unchanged keeps a save from
 * the app from blanking them on the server.
 */
fun JsonObject.toFormFields(skipKeys: Set<String> = emptySet()): Map<String, String> {
    val out = LinkedHashMap<String, String>()
    for ((key, value) in this) {
        if (key !in skipKeys) flatten(key, value, out)
    }
    return out
}

private fun flatten(prefix: String, element: JsonElement, out: MutableMap<String, String>) {
    when (element) {
        is JsonNull -> out[prefix] = ""
        is JsonPrimitive -> out[prefix] = element.content
        is JsonObject -> element.forEach { (k, v) -> flatten("$prefix[$k]", v, out) }
        is JsonArray -> element.forEachIndexed { i, v -> flatten("$prefix[$i]", v, out) }
    }
}
