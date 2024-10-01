package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.Signup
import se.kjellstrand.webshooter.data.results.remote.Status
import se.kjellstrand.webshooter.data.results.remote.StdMedal
import se.kjellstrand.webshooter.data.results.remote.User

class MockResults {
    val results = listOf(
        Result(
            id = 16342,
            competitionsID = 196,
            signupsID = 19257,
            placement = 8,
            price = null,
            figureHits = 24,
            hits = 39,
            points = 6,
            weaponclassesID = 29,
            stdMedal = null,
            signup = Signup(
                id = 19257,
                competitionsID = 196,
                patrolsID = 4108,
                patrolsFinalsID = 0,
                laneFinals = 0,
                patrolsDistinguishID = 0,
                laneDistinguish = 0,
                startTime = "2024-09-29 09:00:00",
                endTime = "2024-09-29 10:45:00",
                lane = 5,
                weaponclassesID = 29,
                registrationFee = 100,
                invoicesID = 5922,
                clubsID = 73,
                startBefore = "00:00:00",
                startAfter = "00:00:00",
                firstLastPatrol = null,
                shareWeaponWith = 0,
                participateOutOfCompetition = null,
                excludeFromStandardmedal = null,
                sharePatrolWith = 0,
                shootNotSimultaneouslyWith = 0,
                note = null,
                requiresApproval = 0,
                isApprovedBy = 0,
                createdBy = 132,
                createdAt = "2024-08-30T14:41:41.000000Z",
                specialWishes = "",
                firstLastPatrolHuman = "-",
                startTimeHuman = "09:00",
                endTimeHuman = "10:45",
                user = User(
                    name = "Alf",
                    lastname = "Andersson",
                    shootingCardNumber = "5953",
                    gradeField = "2",
                    gradeTrackshooting = "2",
                    apiToken = "",
                    userID = 1625372658,
                    fullname = "Alf Andersson",
                    clubsID = 73,
                    status = Status.Active,
                    clubs = listOf()
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 29,
                weaponGroupsID = 6,
                classname = "CVÄ",
                championship = 1,
                classnameGeneral = ClassnameGeneral.Cvä,
                pivot = null
            ),
            results = listOf(),
            resultsDistinguish = emptyList(),
            resultsFinals = emptyList()
        ),
        Result(
            id = 16343,
            competitionsID = 196,
            signupsID = 19957,
            placement = 3,
            price = null,
            figureHits = 30,
            hits = 48,
            points = 39,
            weaponclassesID = 23,
            stdMedal = StdMedal.S,
            signup = Signup(
                id = 19957,
                competitionsID = 196,
                patrolsID = 4108,
                patrolsFinalsID = 0,
                laneFinals = 0,
                patrolsDistinguishID = 0,
                laneDistinguish = 0,
                startTime = "2024-09-29 09:00:00",
                endTime = "2024-09-29 10:45:00",
                lane = 3,
                weaponclassesID = 23,
                registrationFee = 100,
                invoicesID = 5931,
                clubsID = 191,
                startBefore = "00:00:00",
                startAfter = "00:00:00",
                firstLastPatrol = null,
                shareWeaponWith = 0,
                participateOutOfCompetition = null,
                excludeFromStandardmedal = null,
                sharePatrolWith = 0,
                shootNotSimultaneouslyWith = 0,
                note = null,
                requiresApproval = 0,
                isApprovedBy = 0,
                createdBy = 552,
                createdAt = "2024-09-18T17:42:31.000000Z",
                specialWishes = "",
                firstLastPatrolHuman = "-",
                startTimeHuman = "09:00",
                endTimeHuman = "10:45",
                user = User(
                    name = "Martin",
                    lastname = "Nordborg",
                    shootingCardNumber = "35826",
                    gradeField = "3",
                    gradeTrackshooting = "2",
                    apiToken = "",
                    userID = 846904506,
                    fullname = "Martin Nordborg",
                    clubsID = 191,
                    status = Status.Active,
                    clubs = listOf()
                ),
                club = null
            ),
            weaponClass = WeaponClass(
                id = 23,
                weaponGroupsID = 3,
                classname = "C3",
                championship = 1,
                classnameGeneral = ClassnameGeneral.C,
                pivot = null
            ),
            results = listOf(),
            resultsDistinguish = emptyList(),
            resultsFinals = emptyList()
        )
    )
}
