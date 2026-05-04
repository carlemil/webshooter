package se.kjellstrand.webshooter.data.competitionsignups.local

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupClub
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupUser
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupWeaponClass

fun CompetitionSignupEntry.toEntity(competitionId: Long, json: Json): CompetitionSignupEntity =
    CompetitionSignupEntity(
        id = id,
        competitionId = competitionId,
        userJson = json.encodeToString(user),
        clubJson = json.encodeToString(club),
        weaponClassJson = json.encodeToString(weaponclass)
    )

fun CompetitionSignupEntity.toDomain(json: Json): CompetitionSignupEntry =
    CompetitionSignupEntry(
        id = id,
        user = json.decodeFromString<CompetitionSignupUser?>(userJson),
        club = json.decodeFromString<CompetitionSignupClub?>(clubJson),
        weaponclass = json.decodeFromString<CompetitionSignupWeaponClass?>(weaponClassJson)
    )
