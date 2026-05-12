package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.results.remote.StationResult

class MockShooterResult {
    val stationResults = listOf(
        StationResult(id = 1, figureHits = 5, hits = 8, points = 2),
        StationResult(id = 2, figureHits = 4, hits = 7, points = 1),
        StationResult(id = 3, figureHits = 6, hits = 9, points = 3),
        StationResult(id = 4, figureHits = 3, hits = 6, points = 0),
        StationResult(id = 5, figureHits = 6, hits = 9, points = 0)
    )
}
