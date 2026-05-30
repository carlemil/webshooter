package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.StdMedal
import se.kjellstrand.webshooter.data.results.remote.User

class MockResults {
    val results = listOf(
        Result(
            id = 16342,
            signupsID = 19257,
            placement = 8,
            figureHits = 24,
            hits = 39,
            points = 6,
            stdMedal = null,
            signup = Signup(
                user = User(
                    name = "Alf",
                    lastname = "Andersson",
                    userID = 1625372658,
                    fullname = "Alf Andersson"
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 29,
                classname = "CVÄ",
                classnameGeneral = ClassnameGeneral.Cvä
            ),
            results = listOf()
        ),
        Result(
            id = 16343,
            signupsID = 19957,
            placement = 3,
            figureHits = 30,
            hits = 48,
            points = 39,
            stdMedal = StdMedal.S,
            signup = Signup(
                user = User(
                    name = "Martin",
                    lastname = "Nordborg",
                    userID = 846904506,
                    fullname = "Martin Nordborg"
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 23,
                classname = "C3",
                classnameGeneral = ClassnameGeneral.C
            ),
            results = listOf()
        ),
        Result(
            id = 16344,
            signupsID = 19958,
            placement = 3,
            figureHits = 29,
            hits = 46,
            points = 19,
            stdMedal = StdMedal.S,
            signup = Signup(
                user = User(
                    name = "Olof",
                    lastname = "Olofson",
                    userID = 846904507,
                    fullname = "Olof Olofson"
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 23,
                classname = "C1",
                classnameGeneral = ClassnameGeneral.C
            ),
            results = listOf()
        ),
        Result(
            id = 16345,
            signupsID = 19959,
            placement = 3,
            figureHits = 20,
            hits = 41,
            points = 9,
            stdMedal = StdMedal.S,
            signup = Signup(
                user = User(
                    name = "Bosse",
                    lastname = "Borgare",
                    userID = 846904508,
                    fullname = "Bosse Borgare"
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 23,
                classname = "C3",
                classnameGeneral = ClassnameGeneral.C
            ),
            results = listOf()
        )
    )
}
