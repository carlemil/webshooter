package se.kjellstrand.webshooter.data.club.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.club.remote.ClubData
import se.kjellstrand.webshooter.data.club.remote.ClubInfoResponse
import se.kjellstrand.webshooter.data.club.remote.ClubMember

private fun String?.nullIfLiteralNull(): String? = if (this == "null") null else this

fun ClubData.sanitizeNullStrings(): ClubData = copy(
    phone = phone.nullIfLiteralNull(),
    addressStreet = addressStreet.nullIfLiteralNull(),
    addressZipcode = addressZipcode.nullIfLiteralNull(),
    addressCity = addressCity.nullIfLiteralNull(),
    addressCountry = addressCountry.nullIfLiteralNull(),
    bankgiro = bankgiro.nullIfLiteralNull(),
    postgiro = postgiro.nullIfLiteralNull(),
    swish = swish.nullIfLiteralNull()
)

fun ClubData.toEntity(json: Json): ClubEntity = ClubEntity(
    id = id,
    clubsNr = clubsNr,
    name = name,
    email = email,
    phone = phone,
    addressStreet = addressStreet,
    addressZipcode = addressZipcode,
    addressCity = addressCity,
    addressCountry = addressCountry,
    bankgiro = bankgiro,
    postgiro = postgiro,
    swish = swish,
    adminsJson = json.encodeToString(admins),
    usersJson = json.encodeToString(users)
)

fun ClubEntity.toDomain(json: Json): ClubInfoResponse = ClubInfoResponse(
    club = ClubData(
        id = id,
        clubsNr = clubsNr,
        name = name,
        email = email,
        phone = phone,
        addressStreet = addressStreet,
        addressZipcode = addressZipcode,
        addressCity = addressCity,
        addressCountry = addressCountry,
        bankgiro = bankgiro,
        postgiro = postgiro,
        swish = swish,
        admins = json.decodeFromString<List<ClubMember>>(adminsJson),
        users = json.decodeFromString<List<ClubMember>>(usersJson)
    )
)
