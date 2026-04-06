package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.charts.ChartDataPoint
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.ui.screens.charts.ShooterChartInfo

class MockCharts {
    val precisionDataPoints = listOf(
        ChartDataPoint(1L, "SM Precision 2025", "2025-03-15", 92.5, "A", "precision"),
        ChartDataPoint(2L, "DM Precision 2025", "2025-05-20", 88.3, "A", "precision"),
        ChartDataPoint(3L, "KM Precision 2025", "2025-08-10", 95.1, "A", "precision"),
        ChartDataPoint(4L, "Höstskyttet 2025", "2025-10-05", 91.7, "A", "precision"),
    )

    val fieldDataPoints = listOf(
        ChartDataPoint(5L, "Fälttävling Vår", "2025-04-12", 78.2, "B", "field"),
        ChartDataPoint(6L, "Sommarfältet", "2025-06-28", 82.6, "B", "field"),
        ChartDataPoint(7L, "Höstfältet", "2025-09-14", 85.0, "B", "field"),
    )

    val militaryDataPoints = listOf(
        ChartDataPoint(8L, "Snabbmatch Vår", "2025-04-05", 145.0, "C", "military"),
        ChartDataPoint(9L, "Snabbmatch Sommar", "2025-07-12", 152.3, "C", "military"),
    )

    val allChartData = mapOf(
        "precision" to precisionDataPoints,
        "field" to fieldDataPoints,
        "military" to militaryDataPoints
    )

    val availableResultsTypes = listOf("precision", "field", "military")

    val availableWeaponClasses = listOf("A", "B", "C")

    val comparedShooter1 = ShooterChartInfo(
        name = "Anna Svensson",
        chartData = listOf(
            ChartDataPoint(1L, "SM Precision 2025", "2025-03-15", 89.0, "A", "precision"),
            ChartDataPoint(2L, "DM Precision 2025", "2025-05-20", 93.2, "A", "precision"),
            ChartDataPoint(3L, "KM Precision 2025", "2025-08-10", 90.5, "A", "precision"),
        )
    )

    val comparedShooter2 = ShooterChartInfo(
        name = "Erik Johansson",
        chartData = listOf(
            ChartDataPoint(1L, "SM Precision 2025", "2025-03-15", 86.7, "A", "precision"),
            ChartDataPoint(2L, "DM Precision 2025", "2025-05-20", 91.0, "A", "precision"),
        )
    )

    val comparedShooters = mapOf(
        101L to comparedShooter1,
        102L to comparedShooter2
    )

    val clubMembers = listOf(
        ClubMember(userId = 101L, name = "Anna", lastname = "Svensson", fullname = "Anna Svensson"),
        ClubMember(userId = 102L, name = "Erik", lastname = "Johansson", fullname = "Erik Johansson"),
        ClubMember(userId = 103L, name = "Maria", lastname = "Nilsson", fullname = "Maria Nilsson"),
        ClubMember(userId = 104L, name = "Anders", lastname = "Karlsson", fullname = "Anders Karlsson"),
        ClubMember(userId = 105L, name = "Lisa", lastname = "Andersson", fullname = "Lisa Andersson"),
    )
}
