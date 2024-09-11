package se.kjellstrand.webshooter.data.competitions.remote

import se.kjellstrand.webshooter.data.competitions.remote.parsing.ClassnameGeneral
import se.kjellstrand.webshooter.data.competitions.remote.parsing.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.parsing.Logo

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon

private fun <T> Klaxon.convert(k: kotlin.reflect.KClass<*>, fromJson: (JsonValue) -> T, toJson: (T) -> String, isUnion: Boolean = false) =
    this.converter(object: Converter {
        @Suppress("UNCHECKED_CAST")
        override fun toJson(value: Any)        = toJson(value as T)
        override fun fromJson(jv: JsonValue)   = fromJson(jv) as Any
        override fun canConvert(cls: Class<*>) = cls == k.java || (isUnion && cls.superclass == k.java)
    })

private val klaxon = Klaxon()
    .convert(Logo::class,             { Logo.fromValue(it.string!!) },             { "\"${it.value}\"" })
    .convert(ClassnameGeneral::class, { ClassnameGeneral.fromValue(it.string!!) }, { "\"${it.value}\"" })


data class CompetitionsResponse (
    val competitions: Competitions
) {
    public fun toJson() = klaxon.toJsonString(this)

    companion object {
        public fun fromJson(json: String) = klaxon.parse<CompetitionsResponse>(json)
    }
}