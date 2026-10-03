package se.kjellstrand.webshooter.data.settings

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileFormFieldsTest {

    private fun fields(raw: String, skip: Set<String> = emptySet()) =
        Json.parseToJsonElement(raw).jsonObject.toFormFields(skip)

    @Test
    fun `scalars keep their value and null becomes empty`() {
        assertEquals(
            mapOf("name" to "Anna", "birthday" to "1976", "is_admin" to "false", "grade_military" to ""),
            fields("""{"name":"Anna","birthday":1976,"is_admin":false,"grade_military":null}""")
        )
    }

    @Test
    fun `nested objects and arrays use bracket keys`() {
        assertEquals(
            mapOf("clubs[0][id]" to "7", "clubs[0][district][name]" to "Skåne", "tags[1]" to "b", "tags[0]" to "a"),
            fields("""{"clubs":[{"id":7,"district":{"name":"Skåne"}}],"tags":["a","b"]}""")
        )
    }

    @Test
    fun `skipped keys are left out entirely`() {
        assertEquals(
            mapOf("name" to "Anna"),
            fields("""{"name":"Anna","clubs":[{"id":7}]}""", skip = setOf("clubs"))
        )
    }
}
