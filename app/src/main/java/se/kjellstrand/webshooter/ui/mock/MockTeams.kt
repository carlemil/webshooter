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
            id = 1, competitionsId = 196, clubsId = 73, name = "Kullens PK Lag 1", weapongroupsId = 3,
            weapongroup = TeamWeaponGroup(id = 3, name = "C", displayname = "C-klass"),
            signups = listOf(
                TeamSignupEntry(id = 101, lane = 1, weaponclassesId = 23, pivot = TeamPivot(teamsId = 1, signupsId = 101, position = 1), user = TeamSignupUser(userId = 1, name = "Erik", lastname = "Svensson", fullname = "Erik Svensson"), club = TeamSignupClub(name = "Kullens PK")),
                TeamSignupEntry(id = 102, lane = 2, weaponclassesId = 23, pivot = TeamPivot(teamsId = 1, signupsId = 102, position = 2), user = TeamSignupUser(userId = 2, name = "Anna", lastname = "Johansson", fullname = "Anna Johansson"), club = TeamSignupClub(name = "Kullens PK")),
                TeamSignupEntry(id = 103, lane = 3, weaponclassesId = 23, pivot = TeamPivot(teamsId = 1, signupsId = 103, position = 3), user = TeamSignupUser(userId = 3, name = "Lars", lastname = "Nilsson", fullname = "Lars Nilsson"), club = TeamSignupClub(name = "Kullens PK"))
            )
        ),
        TeamEntry(
            id = 2, competitionsId = 196, clubsId = 191, name = "Hässleholms PSK Lag 1", weapongroupsId = 3,
            weapongroup = TeamWeaponGroup(id = 3, name = "C", displayname = "C-klass"),
            signups = listOf(
                TeamSignupEntry(id = 201, lane = 1, weaponclassesId = 23, pivot = TeamPivot(teamsId = 2, signupsId = 201, position = 1), user = TeamSignupUser(userId = 4, name = "Maria", lastname = "Karlsson", fullname = "Maria Karlsson"), club = TeamSignupClub(name = "Hässleholms PSK")),
                TeamSignupEntry(id = 202, lane = 2, weaponclassesId = 23, pivot = TeamPivot(teamsId = 2, signupsId = 202, position = 2), user = TeamSignupUser(userId = 5, name = "Olof", lastname = "Bergström", fullname = "Olof Bergström"), club = TeamSignupClub(name = "Hässleholms PSK"))
            )
        ),
        TeamEntry(
            id = 3, competitionsId = 196, clubsId = 85, name = "Lunds PSK Lag 1", weapongroupsId = 1,
            weapongroup = TeamWeaponGroup(id = 1, name = "A", displayname = "A-klass"),
            signups = listOf(
                TeamSignupEntry(id = 301, lane = 1, weaponclassesId = 11, pivot = TeamPivot(teamsId = 3, signupsId = 301, position = 1), user = TeamSignupUser(userId = 6, name = "Gustav", lastname = "Lindgren", fullname = "Gustav Lindgren"), club = TeamSignupClub(name = "Lunds PSK")),
                TeamSignupEntry(id = 302, lane = 2, weaponclassesId = 11, pivot = TeamPivot(teamsId = 3, signupsId = 302, position = 2), user = TeamSignupUser(userId = 7, name = "Sofia", lastname = "Andersson", fullname = "Sofia Andersson"), club = TeamSignupClub(name = "Lunds PSK")),
                TeamSignupEntry(id = 303, lane = 3, weaponclassesId = 11, pivot = TeamPivot(teamsId = 3, signupsId = 303, position = 3), user = TeamSignupUser(userId = 8, name = "Oscar", lastname = "Pettersson", fullname = "Oscar Pettersson"), club = TeamSignupClub(name = "Lunds PSK"))
            )
        ),
        TeamEntry(
            id = 4, competitionsId = 196, clubsId = 120, name = "Malmö PSK Lag 1", weapongroupsId = 2,
            weapongroup = TeamWeaponGroup(id = 2, name = "B", displayname = "B-klass"),
            signups = listOf(
                TeamSignupEntry(id = 401, lane = 1, weaponclassesId = 15, pivot = TeamPivot(teamsId = 4, signupsId = 401, position = 1), user = TeamSignupUser(userId = 9, name = "Ebba", lastname = "Magnusson", fullname = "Ebba Magnusson"), club = TeamSignupClub(name = "Malmö PSK")),
                TeamSignupEntry(id = 402, lane = 2, weaponclassesId = 15, pivot = TeamPivot(teamsId = 4, signupsId = 402, position = 2), user = TeamSignupUser(userId = 10, name = "Hugo", lastname = "Eriksson", fullname = "Hugo Eriksson"), club = TeamSignupClub(name = "Malmö PSK"))
            )
        ),
        TeamEntry(
            id = 5, competitionsId = 196, clubsId = 95, name = "Helsingborgs PK Lag 1", weapongroupsId = 4,
            weapongroup = TeamWeaponGroup(id = 4, name = "R", displayname = "R-klass"),
            signups = listOf(
                TeamSignupEntry(id = 501, lane = 1, weaponclassesId = 27, pivot = TeamPivot(teamsId = 5, signupsId = 501, position = 1), user = TeamSignupUser(userId = 11, name = "Wilma", lastname = "Olsson", fullname = "Wilma Olsson"), club = TeamSignupClub(name = "Helsingborgs PK")),
                TeamSignupEntry(id = 502, lane = 2, weaponclassesId = 27, pivot = TeamPivot(teamsId = 5, signupsId = 502, position = 2), user = TeamSignupUser(userId = 12, name = "Axel", lastname = "Jonsson", fullname = "Axel Jonsson"), club = TeamSignupClub(name = "Helsingborgs PK")),
                TeamSignupEntry(id = 503, lane = 3, weaponclassesId = 27, pivot = TeamPivot(teamsId = 5, signupsId = 503, position = 3), user = TeamSignupUser(userId = 13, name = "Nils", lastname = "Holm", fullname = "Nils Holm"), club = TeamSignupClub(name = "Helsingborgs PK"))
            )
        )
    )
}
