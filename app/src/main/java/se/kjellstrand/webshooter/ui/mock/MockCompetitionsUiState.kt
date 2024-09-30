package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.ClassnameGeneral
import se.kjellstrand.webshooter.data.common.Club
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.common.Logo
import se.kjellstrand.webshooter.data.common.WeaponGroup
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.Translations
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup
import se.kjellstrand.webshooter.ui.competitions.CompetitionsUiState

class MockCompetitionsUiState {
    val uiState = CompetitionsUiState(
        competitions = Competitions(
            currentPage = 1,
            data = listOf(
                Datum(
                    id = 1,
                    championshipsID = 1,
                    isPublic = 1,
                    resultsIsPublic = 1,
                    patrolsIsPublic = 1,
                    organizerID = 1,
                    organizerType = "Club",
                    invoicesRecipientID = 1,
                    invoicesRecipientType = "Club",
                    name = "Kretsfält 5 + KM Fält R Trollenäs",
                    allowTeams = 1,
                    teamsRegistrationFee = 100,
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
                    resultsType = "Standard",
                    resultsPrices = 1,
                    resultsComment = "",
                    date = "2023-12-15",
                    signupsOpeningDate = "2023-11-01",
                    signupsClosingDate = "2023-12-01",
                    allowSignupsAfterClosingDate = 1,
                    priceSignupsAfterClosingDate = 20,
                    approvalSignupsAfterClosingDate = 0,
                    finalTime = "17:00",
                    createdBy = 1,
                    pdfLogo = Logo.Club,
                    closedAt = null,
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
                    translations = Translations(
                        patrolsNameSingular = "Group",
                        patrolsNamePlural = "Groups",
                        patrolsListSingular = "Group List",
                        patrolsListPlural = "Groups List",
                        patrolsSize = "15",
                        patrolsLaneSingular = "Lane",
                        patrolsLanePlural = "Lanes",
                        stationsNameSingular = "Station",
                        stationsNamePlural = "Stations",
                        resultsListSingular = "Result",
                        resultsListPlural = "Results",
                        shootingCard = "Shooting Card",
                        shootingCards = "Shooting Cards",
                        signups = "Registrations"
                    ),
                    resultsTypeHuman = "Fält",
                    availableLogos = listOf(Logo.Club, Logo.Webshooter),
                    pdfLogoPath = "",
                    pdfLogoURL = "",
                    championship = null,
                    competitionType = CompetitionType(
                        id = 1,
                        name = "Fält",
                        createdAt = "2023-01-01",
                        updatedAt = "2023-01-01",
                        deletedAt = null
                    ),
                    weaponClasses = listOf(),
                    userSignups = listOf(
                        Usersignup(
                            id = 1L,
                            competitionsID = 101L,
                            patrolsID = 201L,
                            patrolsFinalsID = 301L,
                            laneFinals = 1L,
                            patrolsDistinguishID = 401L,
                            laneDistinguish = 2L,
                            startTime = "2023-10-15T08:00:00Z",
                            endTime = "2023-10-15T10:00:00Z",
                            lane = 5L,
                            weaponClassesID = 1L,
                            registrationFee = 100L,
                            invoicesID = null,
                            clubsID = 601L,
                            startBefore = "2023-10-15T07:30:00Z",
                            startAfter = "2023-10-15T08:30:00Z",
                            firstLastPatrol = null,
                            shareWeaponWith = 0L,
                            participateOutOfCompetition = null,
                            excludeFromStandardmedal = null,
                            sharePatrolWith = 0L,
                            shootNotSimultaneouslyWith = 0L,
                            note = "Prefer morning slot",
                            requiresApproval = 0L,
                            isApprovedBy = 0L,
                            createdBy = 701L,
                            createdAt = "2023-09-15T12:00:00Z",
                            specialWishes = "Vegetarian meal",
                            firstLastPatrolHuman = "First Patrol",
                            startTimeHuman = "08:00 AM",
                            endTimeHuman = "10:00 AM"
                        ),
                        Usersignup(
                            id = 2L,
                            competitionsID = 102L,
                            patrolsID = 202L,
                            patrolsFinalsID = 302L,
                            laneFinals = 2L,
                            patrolsDistinguishID = 402L,
                            laneDistinguish = 3L,
                            startTime = "2023-10-16T09:00:00Z",
                            endTime = "2023-10-16T11:00:00Z",
                            lane = 6L,
                            weaponClassesID = 21L,
                            registrationFee = 150L,
                            invoicesID = null,
                            clubsID = 602L,
                            startBefore = "2023-10-16T08:30:00Z",
                            startAfter = "2023-10-16T09:30:00Z",
                            firstLastPatrol = null,
                            shareWeaponWith = 1L,
                            participateOutOfCompetition = null,
                            excludeFromStandardmedal = null,
                            sharePatrolWith = 1L,
                            shootNotSimultaneouslyWith = 0L,
                            note = "Will arrive late",
                            requiresApproval = 1L,
                            isApprovedBy = 0L,
                            createdBy = 702L,
                            createdAt = "2023-09-16T13:00:00Z",
                            specialWishes = "Need wheelchair access",
                            firstLastPatrolHuman = "Last Patrol",
                            startTimeHuman = "09:00 AM",
                            endTimeHuman = "11:00 AM"
                        )
                    ),
                    club = Club(
                        id = 1,
                        disablePersonalInvoices = 0,
                        districtsID = 1,
                        clubsNr = "001",
                        name = "NY Archery Club",
                        email = "info@nyarcheryclub.com",
                        phone = "555-5678",
                        addressStreet = "123 Archery Lane",
                        addressStreet2 = null,
                        addressZipcode = "10001",
                        addressCity = "New York",
                        addressCountry = "USA",
                        bankgiro = null,
                        postgiro = null,
                        swish = "1234567890",
                        logo = null,
                        userHasRole = null,
                        addressCombined = "123 Archery Lane, New York, 10001",
                        addressIncomplete = false,
                        logoURL = "",
                        logoPath = ""
                    )
                ),
                // Add more Datum objects as needed for testing
            ),
            firstPageURL = "",
            from = 1,
            lastPage = 1,
            lastPageURL = "",
            links = listOf(),
            nextPageURL = null,
            path = "",
            perPage = 10,
            prevPageURL = null,
            to = 1,
            total = 1,
            search = null,
            status = "Open",
            clubsID = null,
            type = 1,
            userSignup = null,
            competitionTypes = listOf(
                CompetitionType(
                    id = 1,
                    name = "Indoor",
                    createdAt = "2023-01-01",
                    updatedAt = "2023-01-01",
                    deletedAt = null
                )
            )
        )
    )
}
