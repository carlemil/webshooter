package se.kjellstrand.webshooter.ui.screens.charts.seriespoints

import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.data.seriespoints.CompetitionSeries

enum class WeaponClassGroup(val prefix: Char) {
    A('A'), B('B'), C('C');

    fun matches(weaponClass: String): Boolean =
        weaponClass.isNotEmpty() && weaponClass[0].equals(prefix, ignoreCase = true)
}

data class SeriesPointsUiState(
    val currentUserId: Long = 0L,
    val selectedUserId: Long = 0L,
    val selectedUserName: String = "",
    val competitions: List<CompetitionSeries> = emptyList(),
    val selectedGroup: WeaponClassGroup? = WeaponClassGroup.C,
    val availableGroups: Set<WeaponClassGroup> = emptySet(),
    val selectedYear: Int = 0,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val clubMembers: List<ClubMember> = emptyList(),
    val allParticipants: List<Participant> = emptyList(),
    val searchQuery: String = "",
    val showSearchDialog: Boolean = false
) {
    val availableYears: List<Int>
        get() = competitions
            .mapNotNull { it.date.take(4).toIntOrNull() }
            .distinct()
            .sortedDescending()

    val filteredCompetitions: List<CompetitionSeries>
        get() {
            val nonZero = competitions.filter { comp ->
                comp.seriesPoints.any { it > 0 }
            }
            val byYear = if (selectedYear == 0) nonZero
            else nonZero.filter { it.date.startsWith("$selectedYear-") }
            return selectedGroup?.let { group ->
                byYear.filter { group.matches(it.weaponClass) }
            } ?: byYear
        }

    val precisionClubMembers: List<ClubMember>
        get() {
            val participantIds = allParticipants.map { it.userId }.toSet()
            return clubMembers.filter { it.userId in participantIds }
        }

    data class TrendLine(
        val fromX: Float,
        val fromY: Float,
        val toX: Float,
        val toY: Float,
    )

    val seriesAverage: Float?
        get() {
            val all = filteredCompetitions.flatMap { it.seriesPoints }
            if (all.isEmpty()) return null
            return all.average().toFloat()
        }

    val seriesTrend: TrendLine?
        get() {
            val pairs = filteredCompetitions.flatMap { comp ->
                comp.seriesPoints.mapIndexed { i, p -> (i + 1).toFloat() to p.toFloat() }
            }
            if (pairs.size < 2) return null
            val xs = pairs.map { it.first }
            val ys = pairs.map { it.second }
            val meanX = xs.average().toFloat()
            val meanY = ys.average().toFloat()
            val num = xs.zip(ys)
                .sumOf { (x, y) -> ((x - meanX) * (y - meanY)).toDouble() }
                .toFloat()
            val den = xs
                .sumOf { ((it - meanX) * (it - meanX)).toDouble() }
                .toFloat()
            if (den == 0f) return null
            val slope = num / den
            val intercept = meanY - slope * meanX
            val firstX = xs.min()
            val lastX = xs.max()
            return TrendLine(
                fromX = firstX,
                fromY = slope * firstX + intercept,
                toX = lastX,
                toY = slope * lastX + intercept
            )
        }
}
