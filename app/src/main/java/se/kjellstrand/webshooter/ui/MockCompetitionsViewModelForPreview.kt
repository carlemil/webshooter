package se.kjellstrand.webshooter.ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import se.kjellstrand.webshooter.data.competitions.remote.ClassnameGeneral
import se.kjellstrand.webshooter.data.competitions.remote.Club
import se.kjellstrand.webshooter.data.competitions.remote.Competitions
import se.kjellstrand.webshooter.data.competitions.remote.Competitiontype
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.Logo
import se.kjellstrand.webshooter.data.competitions.remote.Translations
import se.kjellstrand.webshooter.data.competitions.remote.Weapongroup

class MockCompetitionsViewModelForPreview : ICompetitionsViewModel {
    private val _uiState = MutableStateFlow(
        CompetitionsUiState(
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
                        name = "Annual Archery Competition",
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
                        description = "Join us for the annual archery competition!",
                        resultsType = "Standard",
                        resultsPrices = 1,
                        resultsComment = "",
                        date = "2023-12-15",
                        signupsOpeningDate = "2023-11-01",
                        signupsClosingDate = "2023-12-01",
                        allowSignupsAfterClosingDate = 0,
                        priceSignupsAfterClosingDate = 0,
                        approvalSignupsAfterClosingDate = 0,
                        finalTime = "17:00",
                        createdBy = 1,
                        pdfLogo = Logo.Club,
                        closedAt = null,
                        weapongroups = listOf(
                            Weapongroup(
                                id = 1, name = ClassnameGeneral.A
                            )
                        ),
                        signupsCount = 150,
                        patrolsCount = 10,
                        status = "Open",
                        statusHuman = "Open for Registration",
                        startTimeHuman = "08:00",
                        finalTimeHuman = "17:00",
                        allowSignupsAfterClosingDateHuman = "No",
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
                            shootingcard = "Shooting Card",
                            shootingcards = "Shooting Cards",
                            signups = "Registrations"
                        ),
                        resultsTypeHuman = "Standard",
                        availableLogos = listOf(Logo.Club, Logo.Webshooter),
                        pdfLogoPath = "",
                        pdfLogoURL = "",
                        championship = null,
                        competitiontype = Competitiontype(
                            id = 1,
                            name = "Indoor",
                            createdAt = "2023-01-01",
                            updatedAt = "2023-01-01",
                            deletedAt = null
                        ),
                        weaponclasses = listOf(),
                        usersignups = listOf(),
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
                usersignup = null,
                competitiontypes = listOf(
                    Competitiontype(
                        id = 1,
                        name = "Indoor",
                        createdAt = "2023-01-01",
                        updatedAt = "2023-01-01",
                        deletedAt = null
                    )
                )
            )
        )
    )
    override val uiState: StateFlow<CompetitionsUiState> = _uiState.asStateFlow()
}
