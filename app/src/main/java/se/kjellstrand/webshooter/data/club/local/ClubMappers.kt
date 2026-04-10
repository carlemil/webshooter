package se.kjellstrand.webshooter.data.club.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

fun ClubData.toEntity(gson: Gson): ClubEntity = ClubEntity(
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
    adminsJson = gson.toJson(admins),
    usersJson = gson.toJson(users)
)

fun ClubEntity.toDomain(gson: Gson): ClubInfoResponse = ClubInfoResponse(
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
        admins = gson.fromJson(adminsJson, object : TypeToken<List<ClubMember>>() {}.type),
        users = gson.fromJson(usersJson, object : TypeToken<List<ClubMember>>() {}.type)
    )
)
