package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.WeaponGroup
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

class MockCompetitions {
    val competitions = Competitions(
        currentPage = 1,
        data = listOf(
            Datum(
                id = 1,
                name = "Kretsfält 5 + KM Fält R Trollenäs",
                allowTeams = 1,
                website = "https://archerycompetition.com",
                contactName = "Jane Smith",
                contactVenue = "Central Park",
                contactCity = "New York",
                lat = 40.785091,
                lng = -73.968285,
                contactEmail = "contact@archerycompetition.com",
                contactTelephone = "555-1234",
                googleMaps = "",
                description = "Kullens PK hälsar välkomna till kretsfält med mästerskap i C.\n" +
                        "Se mer info i separat inbjudan. (klicka på webbplatsen till höger)\n" +
                        "Max 3 starter.",
                resultsType = ResultsType.FIELD,
                date = "2023-12-15",
                signupsOpeningDate = "2023-11-01",
                signupsClosingDate = "2023-12-01",
                weaponGroups = listOf(
                    WeaponGroup(
                        id = 1, name = ClassnameGeneral.A
                    ), WeaponGroup(
                        id = 2, name = ClassnameGeneral.B
                    ), WeaponGroup(
                        id = 3, name = ClassnameGeneral.C
                    ), WeaponGroup(
                        id = 4, name = ClassnameGeneral.R
                    ), WeaponGroup(
                        id = 5, name = ClassnameGeneral.Cvä
                    )
                ),
                signupsCount = 150,
                patrolsCount = 10,
                status = "Open",
                statusHuman = "Open for Registration",
                startTimeHuman = "08:00",
                finalTimeHuman = "17:00",
                allowSignupsAfterClosingDateHuman = "Behöver godkännas. 0 kr avgift",
                resultsTypeHuman = "Fält",
                competitionType = CompetitionType(
                    id = 1,
                    name = "Fält"
                ),
                weaponClasses = listOf(),
                userSignups = listOf(
                    Usersignup(
                        id = 1L,
                        weaponClassesID = 1L,
                        startTimeHuman = "08:00 AM",
                        endTimeHuman = "10:00 AM"
                    ),
                    Usersignup(
                        id = 2L,
                        weaponClassesID = 21L,
                        startTimeHuman = "09:00 AM",
                        endTimeHuman = "11:00 AM"
                    )
                ),
                club = Club(
                    id = 1,
                    name = "NY Archery Club"
                )
            ),
        ),
        lastPage = 1,
        total = 1,
        status = "Open",
        competitionTypes = listOf(
            CompetitionType(
                id = 1,
                name = "Indoor"
            )
        )
    )
}
