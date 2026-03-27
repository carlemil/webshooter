package se.kjellstrand.webshooter.ui.screens.myresults

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry

@Composable
fun MyEntriesScreen(
    viewModel: MyResultsViewModel = hiltViewModel<MyResultsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.groupedEntries.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.signups_no_entries))
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.groupedEntries.forEach { (year, entries) ->
                    item(key = "header_$year") {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    item(key = "summary_$year") {
                        YearlySummaryCard(entries, uiState.resultStats)
                    }
                }
                item(key = "header_competitions") {
                    Text(
                        text = stringResource(R.string.my_results_competitions),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                uiState.groupedEntries.forEach { (_, entries) ->
                    val byCompetition = entries.groupBy { it.competition.id }
                        .values.sortedByDescending { it.first().competition.date }
                    items(
                        byCompetition.size,
                        key = { byCompetition[it].first().competition.id }) { index ->
                        CompetitionSignupsItem(byCompetition[index], uiState.resultStats)
                    }
                }
            }
        }
    }
}

private const val FMT_1F = "%.2f"

private data class SummaryRow(
    val weaponClass: String,
    val competitionType: String,
    val count: Int,
    val avgScore: Double,
    val avgHits: Double,
    val avgX: Double,
    val figureHits: Double,
    val totalScore: Double,
    val totalHits: Int,
    val totalFigureHits: Int,
    val medalScore: Int
)

@Composable
private fun SymbolInfoDialog(onDismiss: () -> Unit) {
    val symbols = listOf(
        stringResource(R.string.my_results_symbol_xbar) to stringResource(R.string.my_results_symbol_xbar_desc),
        stringResource(R.string.my_results_symbol_sigma) to stringResource(R.string.my_results_symbol_sigma_desc),
        stringResource(R.string.my_results_symbol_x) to stringResource(R.string.my_results_symbol_x_desc),
        stringResource(R.string.my_results_symbol_medal_pts) to stringResource(R.string.my_results_symbol_medal_pts_desc),
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.my_results_symbols_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                symbols.forEach { (symbol, desc) ->
                    Row {
                        Text(
                            text = symbol,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(0.25f)
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(0.75f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.my_results_symbols_close))
            }
        }
    )
}

@Composable
private fun YearlySummaryCard(entries: List<SignupEntry>, resultStats: Map<Long, ResultStats>) {
    var showSymbolInfo by remember { mutableStateOf(false) }
    if (showSymbolInfo) SymbolInfoDialog(onDismiss = { showSymbolInfo = false })
    val rowsByType = remember(entries, resultStats) {
        val relevantTypes = entries
            .filter { it.resultsPlacements != null }
            .map { it.competition.resultsTypeHuman }
            .distinct()
        val allRows = entries
            .filter { it.resultsPlacements != null && it.competition.resultsTypeHuman in relevantTypes }
            .groupBy { it.weaponclass.classname to it.competition.resultsTypeHuman }
            .map { (key, group) ->
                val placements = group.map { it.resultsPlacements!! }
                val avgScore = group.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull entry.resultsPlacements!!.points.toDouble()
                    entry.resultsPlacements!!.points.toDouble() / stations
                }.average()
                val hits = group.mapNotNull { entry -> resultStats[entry.id]?.hits }
                val figureHits = group.mapNotNull { entry -> resultStats[entry.id]?.figureHits }
                val avgHits = group.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 } ?: return@mapNotNull null
                    resultStats[entry.id]!!.hits.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val avgFigureHits = if (figureHits.isEmpty()) 0.0 else figureHits.map { it.toDouble() }.average()
                SummaryRow(
                    weaponClass = key.first,
                    competitionType = key.second,
                    count = group.size,
                    avgScore = avgScore,
                    avgHits = avgHits,
                    avgX = avgHits,
                    figureHits = avgFigureHits,
                    totalScore = group.sumOf { it.resultsPlacements!!.points.toDouble() },
                    totalHits = hits.sumOf { it.toInt() },
                    totalFigureHits = figureHits.sumOf { it.toInt() },
                    medalScore = placements.sumOf {
                        when (it.stdMedal) { "B" -> 1; "S" -> 2; else -> 0 }.toInt()
                    }
                )
            }
        relevantTypes.mapNotNull { type ->
            val rows = allRows.filter { it.competitionType == type }.sortedBy { it.weaponClass }
            if (rows.isEmpty()) null else {
                val typeEntries = entries.filter {
                    it.resultsPlacements != null && it.competition.resultsTypeHuman == type
                }
                val placements = typeEntries.map { it.resultsPlacements!! }
                val avgScore = typeEntries.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 }
                        ?: return@mapNotNull entry.resultsPlacements!!.points.toDouble()
                    entry.resultsPlacements!!.points.toDouble() / stations
                }.average()
                val hits = typeEntries.mapNotNull { entry -> resultStats[entry.id]?.hits }
                val figureHits = typeEntries.mapNotNull { entry -> resultStats[entry.id]?.figureHits }
                val avgHits = typeEntries.mapNotNull { entry ->
                    val stations = resultStats[entry.id]?.stationCount?.takeIf { it > 0 } ?: return@mapNotNull null
                    resultStats[entry.id]!!.hits.toDouble() / stations
                }.let { if (it.isEmpty()) 0.0 else it.average() }
                val avgFigureHits = if (figureHits.isEmpty()) 0.0 else figureHits.map { it.toDouble() }.average()
                val totalRow = SummaryRow(
                    weaponClass = rows.joinToString(", ") { it.weaponClass },
                    competitionType = type,
                    count = typeEntries.size,
                    avgScore = avgScore,
                    avgHits = avgHits,
                    avgX = avgHits,
                    figureHits = avgFigureHits,
                    totalScore = typeEntries.sumOf { it.resultsPlacements!!.points.toDouble() },
                    totalHits = hits.sumOf { it.toInt() },
                    totalFigureHits = figureHits.sumOf { it.toInt() },
                    medalScore = placements.sumOf {
                        when (it.stdMedal) { "B" -> 1; "S" -> 2; else -> 0 }.toInt()
                    }
                )
                Triple(type, rows, totalRow)
            }
        }
    }
    if (rowsByType.isEmpty()) return

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_results_summary),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showSymbolInfo = true },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(R.string.my_results_symbols_title),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            rowsByType.forEachIndexed { sectionIndex, (type, rows, totalRow) ->
                if (sectionIndex > 0) Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val isFalt = type == "Fält"
                GridRow {
                    GridCell(stringResource(R.string.my_results_summary_class), 1.5f, fontWeight = FontWeight.Bold)
                    if (isFalt) {
                        GridCell(stringResource(R.string.my_results_summary_avg_hits_falt), 1.5f, fontWeight = FontWeight.Bold)
                        GridCell(stringResource(R.string.my_results_summary_figures), 1.5f, fontWeight = FontWeight.Bold)
                    } else {
                        GridCell(stringResource(R.string.my_results_summary_avg_score), 1.5f, fontWeight = FontWeight.Bold)
                        GridCell(stringResource(R.string.my_results_summary_avg_hits_pres), 1.5f, fontWeight = FontWeight.Bold)
                    }
                    GridCell(stringResource(R.string.my_results_summary_medal_pts), 0.8f, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                rows.forEach { row ->
                    GridRow {
                        GridCell("${row.weaponClass}/${row.count}", 1.5f)
                        if (isFalt) {
                            GridCell(FMT_1F.format(row.avgHits), 1.5f)
                            GridCell(FMT_1F.format(row.figureHits), 1.5f)
                        } else {
                            GridCell(FMT_1F.format(row.avgScore), 1.5f)
                            GridCell(FMT_1F.format(row.avgHits), 1.5f)
                        }
                        GridCell(row.medalScore.toString(), 0.8f)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                GridRow {
                    GridCell("Alla", 1.5f, fontWeight = FontWeight.Bold)
                    if (isFalt) {
                        GridCell(FMT_1F.format(totalRow.avgHits), 1.5f, fontWeight = FontWeight.Bold)
                        GridCell(FMT_1F.format(totalRow.figureHits), 1.5f, fontWeight = FontWeight.Bold)
                    } else {
                        GridCell(FMT_1F.format(totalRow.avgScore), 1.5f, fontWeight = FontWeight.Bold)
                        GridCell(FMT_1F.format(totalRow.avgHits), 1.5f, fontWeight = FontWeight.Bold)
                    }
                    GridCell(totalRow.medalScore.toString(), 0.8f, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CompetitionSignupsItem(entries: List<SignupEntry>, resultStats: Map<Long, ResultStats>) {
    val competition = entries.first().competition
    val isFalt = competition.resultsTypeHuman == "Fält"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = competition.name, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${competition.date}  •  ${competition.statusHuman}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            GridRow {
                GridCell(stringResource(R.string.my_results_summary_class), 2f, fontWeight = FontWeight.Bold)
                if (isFalt) {
                    GridCell(stringResource(R.string.my_results_summary_avg_hits_falt), 1.5f, fontWeight = FontWeight.Bold)
                    GridCell(stringResource(R.string.my_results_summary_figures), 1.5f, fontWeight = FontWeight.Bold)
                } else {
                    GridCell(stringResource(R.string.my_results_summary_avg_score), 1.5f, fontWeight = FontWeight.Bold)
                    GridCell(stringResource(R.string.my_results_summary_avg_hits_pres), 1.5f, fontWeight = FontWeight.Bold)
                }
                GridCell(stringResource(R.string.medal), 1.2f, fontWeight = FontWeight.Bold)
                GridCell(stringResource(R.string.my_results_summary_medal_pts), 1.2f, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

            entries.sortedBy { it.weaponclass.classname }.forEach { entry ->
                val stats = resultStats[entry.id]
                val rp = entry.resultsPlacements
                val avgScore = if (rp != null) {
                    val stations = stats?.stationCount?.takeIf { it > 0 }
                    if (stations != null) rp.points.toDouble() / stations else rp.points.toDouble()
                } else null
                val avgHits = if (stats != null && stats.stationCount > 0) {
                    stats.hits.toDouble() / stats.stationCount
                } else null
                val figureHits = stats?.figureHits
                val medalCount = if (rp?.stdMedal != null) 1 else 0
                val medalScore = when (rp?.stdMedal) { "B" -> 1; "S" -> 2; else -> 0 }

                GridRow {
                    GridCell(entry.weaponclass.classname, 2f)
                    if (isFalt) {
                        GridCell(if (avgHits != null) FMT_1F.format(avgHits) else "-", 1.5f)
                        GridCell(if (figureHits != null) figureHits.toString() else "-", 1.5f)
                    } else {
                        GridCell(if (avgScore != null) FMT_1F.format(avgScore) else "-", 1.5f)
                        GridCell(if (avgHits != null) FMT_1F.format(avgHits) else "-", 1.5f)
                    }
                    GridCell(medalCount.toString(), 1.2f)
                    GridCell(medalScore.toString(), 1.2f)
                }
            }
        }
    }
}

@Composable
private fun GridRow(content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        content()
    }
}

@Composable
private fun RowScope.GridCell(
    text: String,
    weight: Float,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = fontWeight,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        modifier = Modifier.weight(weight)
    )
}
