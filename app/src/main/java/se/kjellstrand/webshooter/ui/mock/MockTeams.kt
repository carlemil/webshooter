package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamPivot
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupClub
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupEntry
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupUser
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamWeaponGroup

class MockTeams {
    val teams = listOf(
        TeamEntry(
            id = 1, name = "Kullens PK Lag 1",
            weapongroup = TeamWeaponGroup(id = 3, name = "C"),
            signups = listOf(
                TeamSignupEntry(id = 101, pivot = TeamPivot(position = 1), user = TeamSignupUser(userId = 1, name = "Erik", lastname = "Svensson"), club = TeamSignupClub(name = "Kullens PK")),
                TeamSignupEntry(id = 102, pivot = TeamPivot(position = 2), user = TeamSignupUser(userId = 2, name = "Anna", lastname = "Johansson"), club = TeamSignupClub(name = "Kullens PK")),
                TeamSignupEntry(id = 103, pivot = TeamPivot(position = 3), user = TeamSignupUser(userId = 3, name = "Lars", lastname = "Nilsson"), club = TeamSignupClub(name = "Kullens PK"))
            )
        ),
        TeamEntry(
            id = 2, name = "Hässleholms PSK Lag 1",
            weapongroup = TeamWeaponGroup(id = 3, name = "C"),
            signups = listOf(
                TeamSignupEntry(id = 201, pivot = TeamPivot(position = 1), user = TeamSignupUser(userId = 4, name = "Maria", lastname = "Karlsson"), club = TeamSignupClub(name = "Hässleholms PSK")),
                TeamSignupEntry(id = 202, pivot = TeamPivot(position = 2), user = TeamSignupUser(userId = 5, name = "Olof", lastname = "Bergström"), club = TeamSignupClub(name = "Hässleholms PSK"))
            )
        ),
        TeamEntry(
            id = 3, name = "Lunds PSK Lag 1",
            weapongroup = TeamWeaponGroup(id = 1, name = "A"),
            signups = listOf(
                TeamSignupEntry(id = 301, pivot = TeamPivot(position = 1), user = TeamSignupUser(userId = 6, name = "Gustav", lastname = "Lindgren"), club = TeamSignupClub(name = "Lunds PSK")),
                TeamSignupEntry(id = 302, pivot = TeamPivot(position = 2), user = TeamSignupUser(userId = 7, name = "Sofia", lastname = "Andersson"), club = TeamSignupClub(name = "Lunds PSK")),
                TeamSignupEntry(id = 303, pivot = TeamPivot(position = 3), user = TeamSignupUser(userId = 8, name = "Oscar", lastname = "Pettersson"), club = TeamSignupClub(name = "Lunds PSK"))
            )
        ),
        TeamEntry(
            id = 4, name = "Malmö PSK Lag 1",
            weapongroup = TeamWeaponGroup(id = 2, name = "B"),
            signups = listOf(
                TeamSignupEntry(id = 401, pivot = TeamPivot(position = 1), user = TeamSignupUser(userId = 9, name = "Ebba", lastname = "Magnusson"), club = TeamSignupClub(name = "Malmö PSK")),
                TeamSignupEntry(id = 402, pivot = TeamPivot(position = 2), user = TeamSignupUser(userId = 10, name = "Hugo", lastname = "Eriksson"), club = TeamSignupClub(name = "Malmö PSK"))
            )
        ),
        TeamEntry(
            id = 5, name = "Helsingborgs PK Lag 1",
            weapongroup = TeamWeaponGroup(id = 4, name = "R"),
            signups = listOf(
                TeamSignupEntry(id = 501, pivot = TeamPivot(position = 1), user = TeamSignupUser(userId = 11, name = "Wilma", lastname = "Olsson"), club = TeamSignupClub(name = "Helsingborgs PK")),
                TeamSignupEntry(id = 502, pivot = TeamPivot(position = 2), user = TeamSignupUser(userId = 12, name = "Axel", lastname = "Jonsson"), club = TeamSignupClub(name = "Helsingborgs PK")),
                TeamSignupEntry(id = 503, pivot = TeamPivot(position = 3), user = TeamSignupUser(userId = 13, name = "Nils", lastname = "Holm"), club = TeamSignupClub(name = "Helsingborgs PK"))
            )
        )
    )
}
