package se.kjellstrand.webshooter.data.competitionsignups.local

import com.google.gson.Gson
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupClub
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupUser
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupWeaponClass

fun CompetitionSignupEntry.toEntity(competitionId: Long, gson: Gson): CompetitionSignupEntity =
    CompetitionSignupEntity(
        id = id,
        competitionId = competitionId,
        userJson = gson.toJson(user),
        clubJson = gson.toJson(club),
        weaponClassJson = gson.toJson(weaponclass)
    )

fun CompetitionSignupEntity.toDomain(gson: Gson): CompetitionSignupEntry =
    CompetitionSignupEntry(
        id = id,
        user = gson.fromJson(userJson, CompetitionSignupUser::class.java),
        club = gson.fromJson(clubJson, CompetitionSignupClub::class.java),
        weaponclass = gson.fromJson(weaponClassJson, CompetitionSignupWeaponClass::class.java)
    )
