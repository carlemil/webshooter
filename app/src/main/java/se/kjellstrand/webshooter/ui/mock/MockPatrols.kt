package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupClub
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupUser
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupWeaponClass

class MockPatrols {
    val patrols = listOf(
        PatrolEntry(
            id = 1, sortorder = 1, startTimeHuman = "08:00", endTimeHuman = "09:30", patrolSize = 15,
            signups = listOf(
                PatrolSignupEntry(id = 101, lane = 1, user = PatrolSignupUser(userId = 1, name = "Erik", lastname = "Svensson"), club = PatrolSignupClub(name = "Kullens PK"), weaponclass = PatrolSignupWeaponClass(classname = "C1", classnameGeneral = "C")),
                PatrolSignupEntry(id = 102, lane = 2, user = PatrolSignupUser(userId = 2, name = "Anna", lastname = "Johansson"), club = PatrolSignupClub(name = "Kullens PK"), weaponclass = PatrolSignupWeaponClass(classname = "B1", classnameGeneral = "B")),
                PatrolSignupEntry(id = 103, lane = 3, user = PatrolSignupUser(userId = 3, name = "Lars", lastname = "Nilsson"), club = PatrolSignupClub(name = "Hässleholms PSK"), weaponclass = PatrolSignupWeaponClass(classname = "A1", classnameGeneral = "A"))
            )
        ),
        PatrolEntry(
            id = 2, sortorder = 2, startTimeHuman = "09:30", endTimeHuman = "11:00", patrolSize = 15,
            signups = listOf(
                PatrolSignupEntry(id = 201, lane = 1, user = PatrolSignupUser(userId = 4, name = "Maria", lastname = "Karlsson"), club = PatrolSignupClub(name = "Lunds PSK"), weaponclass = PatrolSignupWeaponClass(classname = "R1", classnameGeneral = "R")),
                PatrolSignupEntry(id = 202, lane = 2, user = PatrolSignupUser(userId = 5, name = "Olof", lastname = "Bergström"), club = PatrolSignupClub(name = "Kullens PK"), weaponclass = PatrolSignupWeaponClass(classname = "CVÄ", classnameGeneral = "Cvä"))
            )
        ),
        PatrolEntry(
            id = 3, sortorder = 3, startTimeHuman = "11:00", endTimeHuman = "12:30", patrolSize = 15,
            signups = listOf(
                PatrolSignupEntry(id = 301, lane = 1, user = PatrolSignupUser(userId = 6, name = "Gustav", lastname = "Lindgren"), club = PatrolSignupClub(name = "Malmö PSK"), weaponclass = PatrolSignupWeaponClass(classname = "C3", classnameGeneral = "C")),
                PatrolSignupEntry(id = 302, lane = 2, user = PatrolSignupUser(userId = 7, name = "Sofia", lastname = "Andersson"), club = PatrolSignupClub(name = "Helsingborgs PK"), weaponclass = PatrolSignupWeaponClass(classname = "B2", classnameGeneral = "B")),
                PatrolSignupEntry(id = 303, lane = 3, user = PatrolSignupUser(userId = 8, name = "Oscar", lastname = "Pettersson"), club = PatrolSignupClub(name = "Hässleholms PSK"), weaponclass = PatrolSignupWeaponClass(classname = "A2", classnameGeneral = "A"))
            )
        ),
        PatrolEntry(
            id = 4, sortorder = 4, startTimeHuman = "12:30", endTimeHuman = "14:00", patrolSize = 15,
            signups = listOf(
                PatrolSignupEntry(id = 401, lane = 1, user = PatrolSignupUser(userId = 9, name = "Ebba", lastname = "Magnusson"), club = PatrolSignupClub(name = "Lunds PSK"), weaponclass = PatrolSignupWeaponClass(classname = "C1", classnameGeneral = "C")),
                PatrolSignupEntry(id = 402, lane = 2, user = PatrolSignupUser(userId = 10, name = "Hugo", lastname = "Eriksson"), club = PatrolSignupClub(name = "Kullens PK"), weaponclass = PatrolSignupWeaponClass(classname = "R2", classnameGeneral = "R"))
            )
        ),
        PatrolEntry(
            id = 5, sortorder = 5, startTimeHuman = "14:00", endTimeHuman = "15:30", patrolSize = 15,
            signups = listOf(
                PatrolSignupEntry(id = 501, lane = 1, user = PatrolSignupUser(userId = 11, name = "Wilma", lastname = "Olsson"), club = PatrolSignupClub(name = "Malmö PSK"), weaponclass = PatrolSignupWeaponClass(classname = "B1", classnameGeneral = "B")),
                PatrolSignupEntry(id = 502, lane = 2, user = PatrolSignupUser(userId = 12, name = "Axel", lastname = "Jonsson"), club = PatrolSignupClub(name = "Helsingborgs PK"), weaponclass = PatrolSignupWeaponClass(classname = "CVÄ", classnameGeneral = "Cvä")),
                PatrolSignupEntry(id = 503, lane = 3, user = PatrolSignupUser(userId = 13, name = "Nils", lastname = "Holm"), club = PatrolSignupClub(name = "Kullens PK"), weaponclass = PatrolSignupWeaponClass(classname = "C2", classnameGeneral = "C"))
            )
        )
    )
}
