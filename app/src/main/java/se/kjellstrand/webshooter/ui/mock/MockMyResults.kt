package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.mysignups.remote.SignupCompetition
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.data.mysignups.remote.SignupPatrol
import se.kjellstrand.webshooter.data.mysignups.remote.SignupResultsPlacement
import se.kjellstrand.webshooter.data.mysignups.remote.SignupTeam
import se.kjellstrand.webshooter.data.mysignups.remote.SignupWeaponClass
import se.kjellstrand.webshooter.ui.screens.myresults.ResultStats
import se.kjellstrand.webshooter.ui.screens.myresults.SummaryRow

class MockMyResults {
    private fun makeEntry(
        id: Long,
        compId: Long,
        compName: String,
        compDate: String,
        weaponClassname: String,
        weaponGeneral: String,
        placement: Int,
        points: Long,
        medal: String? = null
    ) = SignupEntry(
        id = id,
        competitionsId = compId,
        weaponClassesId = 23,
        patrolsId = 1,
        startTime = "2024-09-29 09:00:00",
        endTime = "2024-09-29 10:45:00",
        lane = 3,
        note = null,
        registrationFee = 100,
        specialWishes = "",
        startTimeHuman = "09:00",
        endTimeHuman = "10:45",
        competition = SignupCompetition(
            id = compId,
            name = compName,
            date = compDate,
            status = "Closed",
            statusHuman = "Avslutad",
            contactName = "Tävlingsledare",
            contactCity = "Mölle",
            resultsType = "field",
            resultsTypeHuman = "Fält"
        ),
        weaponclass = SignupWeaponClass(id = 23, classname = weaponClassname, classnameGeneral = weaponGeneral),
        patrol = SignupPatrol(id = 1, competitionsId = compId, startTimeHuman = "09:00", endTimeHuman = "10:45"),
        team = emptyList(),
        resultsPlacements = SignupResultsPlacement(id = id, placement = placement, stdMedal = medal, points = points)
    )

    val entries = listOf(
        makeEntry(1, 196, "Kretsfält 5 Trollenäs", "2024-09-29", "C1", "C", 3, 39, "S"),
        makeEntry(2, 197, "Höstfält Kullens PK", "2024-10-15", "C1", "C", 5, 32),
        makeEntry(3, 198, "SM Fält 2024", "2024-11-02", "C3", "C", 12, 28),
        makeEntry(4, 199, "Vårfält Lund", "2025-03-20", "B1", "B", 2, 45, "G"),
        makeEntry(5, 200, "Kretsfält 1 Helsingborg", "2025-04-05", "C1", "C", 1, 48, "G"),
        makeEntry(6, 201, "Sommarfält Malmö", "2025-06-14", "R1", "R", 8, 22)
    )

    val groupedEntries = entries.groupBy { it.competition.date.substring(0, 4) }

    val resultStats = mapOf(
        1L to ResultStats(stationCount = 8, hits = 39, figureHits = 24),
        2L to ResultStats(stationCount = 8, hits = 35, figureHits = 20),
        3L to ResultStats(stationCount = 10, hits = 42, figureHits = 28),
        4L to ResultStats(stationCount = 8, hits = 45, figureHits = 30),
        5L to ResultStats(stationCount = 8, hits = 48, figureHits = 32),
        6L to ResultStats(stationCount = 8, hits = 30, figureHits = 18)
    )

    val summaryRows = listOf(
        SummaryRow(weaponClass = "C1", competitionType = "Fält", count = 3, avgScore = 39.7, avgHits = 40.7, avgX = 0.0, figureHits = 25.3, totalScore = 119.0, totalHits = 122, totalFigureHits = 76, medalScore = 2),
        SummaryRow(weaponClass = "C3", competitionType = "Fält", count = 1, avgScore = 28.0, avgHits = 42.0, avgX = 0.0, figureHits = 28.0, totalScore = 28.0, totalHits = 42, totalFigureHits = 28, medalScore = 0),
        SummaryRow(weaponClass = "B1", competitionType = "Fält", count = 1, avgScore = 45.0, avgHits = 45.0, avgX = 0.0, figureHits = 30.0, totalScore = 45.0, totalHits = 45, totalFigureHits = 30, medalScore = 1),
        SummaryRow(weaponClass = "R1", competitionType = "Fält", count = 1, avgScore = 22.0, avgHits = 30.0, avgX = 0.0, figureHits = 18.0, totalScore = 22.0, totalHits = 30, totalFigureHits = 18, medalScore = 0)
    )
}
