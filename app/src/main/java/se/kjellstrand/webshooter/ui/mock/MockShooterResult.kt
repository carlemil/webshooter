package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.results.remote.StationResult

class MockShooterResult {
    val stationResults = listOf(
        StationResult(id = 1, signupsID = 19257, finals = 0, distinguish = 0, stationsID = 1, figureHits = 5, hits = 8, points = 2, stationFigureHits = listOf(1, 1, 1, 1, 1), createdAt = "2024-09-29T10:00:00Z", updatedAt = "2024-09-29T10:00:00Z"),
        StationResult(id = 2, signupsID = 19257, finals = 0, distinguish = 0, stationsID = 2, figureHits = 4, hits = 7, points = 1, stationFigureHits = listOf(1, 1, 1, 1, 0), createdAt = "2024-09-29T10:15:00Z", updatedAt = "2024-09-29T10:15:00Z"),
        StationResult(id = 3, signupsID = 19257, finals = 0, distinguish = 0, stationsID = 3, figureHits = 6, hits = 9, points = 3, stationFigureHits = listOf(1, 1, 1, 1, 1, 1), createdAt = "2024-09-29T10:30:00Z", updatedAt = "2024-09-29T10:30:00Z"),
        StationResult(id = 4, signupsID = 19257, finals = 0, distinguish = 0, stationsID = 4, figureHits = 3, hits = 6, points = 0, stationFigureHits = listOf(1, 1, 1, 0, 0), createdAt = "2024-09-29T10:45:00Z", updatedAt = "2024-09-29T10:45:00Z"),
        StationResult(id = 5, signupsID = 19257, finals = 0, distinguish = 0, stationsID = 5, figureHits = 6, hits = 9, points = 0, stationFigureHits = listOf(1, 1, 1, 1, 1, 1), createdAt = "2024-09-29T11:00:00Z", updatedAt = "2024-09-29T11:00:00Z")
    )
}
