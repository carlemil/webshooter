package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupClub
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupUser
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupWeaponClass

class MockSignups {
    val signups = listOf(
        CompetitionSignupEntry(id = 1, user = CompetitionSignupUser(name = "Erik", lastname = "Svensson"), club = CompetitionSignupClub(name = "Kullens PK"), weaponclass = CompetitionSignupWeaponClass(classname = "C1", classnameGeneral = "C")),
        CompetitionSignupEntry(id = 2, user = CompetitionSignupUser(name = "Anna", lastname = "Johansson"), club = CompetitionSignupClub(name = "Kullens PK"), weaponclass = CompetitionSignupWeaponClass(classname = "B1", classnameGeneral = "B")),
        CompetitionSignupEntry(id = 3, user = CompetitionSignupUser(name = "Lars", lastname = "Nilsson"), club = CompetitionSignupClub(name = "Hässleholms PSK"), weaponclass = CompetitionSignupWeaponClass(classname = "A1", classnameGeneral = "A")),
        CompetitionSignupEntry(id = 4, user = CompetitionSignupUser(name = "Maria", lastname = "Karlsson"), club = CompetitionSignupClub(name = "Lunds PSK"), weaponclass = CompetitionSignupWeaponClass(classname = "R1", classnameGeneral = "R")),
        CompetitionSignupEntry(id = 5, user = CompetitionSignupUser(name = "Olof", lastname = "Bergström"), club = CompetitionSignupClub(name = "Kullens PK"), weaponclass = CompetitionSignupWeaponClass(classname = "CVÄ", classnameGeneral = "Cvä")),
        CompetitionSignupEntry(id = 6, user = CompetitionSignupUser(name = "Gustav", lastname = "Lindgren"), club = CompetitionSignupClub(name = "Malmö PSK"), weaponclass = CompetitionSignupWeaponClass(classname = "C3", classnameGeneral = "C")),
        CompetitionSignupEntry(id = 7, user = CompetitionSignupUser(name = "Sofia", lastname = "Andersson"), club = CompetitionSignupClub(name = "Helsingborgs PK"), weaponclass = CompetitionSignupWeaponClass(classname = "B2", classnameGeneral = "B"))
    )
}
