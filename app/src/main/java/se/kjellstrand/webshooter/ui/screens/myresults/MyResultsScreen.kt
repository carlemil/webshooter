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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.mysignups.remote.SignupEntry
import se.kjellstrand.webshooter.ui.mock.MyResultsViewModelMock

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
                if (uiState.isLoadingStats) {
                    item(key = "loading_stats") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.loading),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                item(key = "header_all_time") {
                    Text(
                        text = stringResource(R.string.my_results_all_time),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                item(key = "summary_all_time") {
                    YearlySummaryCard(uiState.allTimeSummaryRows)
                }
                uiState.groupedEntries.keys.forEach { year ->
                    item(key = "header_$year") {
                        Text(
                            text = year,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    item(key = "summary_$year") {
                        YearlySummaryCard(uiState.yearlySummaryRows[year] ?: emptyList())
                    }
                }
                item(key = "header_competitions") {
                    Text(
                        text = stringResource(R.string.my_results_competitions),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                items(
                    uiState.allCompetitions.size,
                    key = { uiState.allCompetitions[it].first().competition.id }
                ) { index ->
                    CompetitionSignupsItem(uiState.allCompetitions[index], uiState.resultStats)
                }
            }
        }
    }
}

private const val FMT_1F = "%.2f"

@Composable
private fun SymbolInfoDialog(onDismiss: () -> Unit) {
    val symbols = listOf(
        stringResource(R.string.my_results_symbol_xbar) to stringResource(R.string.my_results_symbol_xbar_desc),
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
private fun YearlySummaryCard(rowsByType: List<Triple<String, List<SummaryRow>, SummaryRow>>) {
    var showSymbolInfo by remember { mutableStateOf(false) }
    if (showSymbolInfo) SymbolInfoDialog(onDismiss = { showSymbolInfo = false })
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
                    modifier = Modifier.size(48.dp)
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
                    GridCell(stringResource(R.string.my_results_summary_class), 0.7f, fontWeight = FontWeight.Bold)
                    GridCell(stringResource(R.string.my_results_summary_starter), 0.9f, fontWeight = FontWeight.Bold)
                    if (isFalt) {
                        GridCell(stringResource(R.string.my_results_summary_avg_hits_falt), 1.4f, fontWeight = FontWeight.Bold)
                        GridCell(stringResource(R.string.my_results_summary_figures), 1.4f, fontWeight = FontWeight.Bold)
                    } else {
                        GridCell(stringResource(R.string.my_results_summary_avg_score), 1.4f, fontWeight = FontWeight.Bold)
                        GridCell(stringResource(R.string.my_results_summary_avg_hits_pres), 1.4f, fontWeight = FontWeight.Bold)
                    }
                    GridCell(stringResource(R.string.my_results_symbol_medal_pts), 0.8f, fontWeight = FontWeight.Bold)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                rows.forEach { row ->
                    GridRow {
                        GridCell(row.weaponClass, 0.7f)
                        GridCell(row.count.toString(), 0.9f)
                        if (isFalt) {
                            GridCell(FMT_1F.format(row.avgHits), 1.4f)
                            GridCell(FMT_1F.format(row.figureHits), 1.4f)
                        } else {
                            GridCell(FMT_1F.format(row.avgScore), 1.4f)
                            GridCell(FMT_1F.format(row.avgHits), 1.4f)
                        }
                        GridCell(row.medalScore.toString(), 0.8f)
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                GridRow {
                    GridCell("Alla", 0.7f, fontWeight = FontWeight.Bold)
                    GridCell(totalRow.count.toString(), 0.9f, fontWeight = FontWeight.Bold)
                    if (isFalt) {
                        GridCell(FMT_1F.format(totalRow.avgHits), 1.4f, fontWeight = FontWeight.Bold)
                        GridCell(FMT_1F.format(totalRow.figureHits), 1.4f, fontWeight = FontWeight.Bold)
                    } else {
                        GridCell(FMT_1F.format(totalRow.avgScore), 1.4f, fontWeight = FontWeight.Bold)
                        GridCell(FMT_1F.format(totalRow.avgHits), 1.4f, fontWeight = FontWeight.Bold)
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
                GridCell(stringResource(R.string.my_results_symbol_medal_pts), 1.2f, fontWeight = FontWeight.Bold)
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
                    if (isFalt) stats.hits.toDouble() / stats.stationCount
                    else stats.hits.toDouble() / stats.stationCount * 7
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

@Preview(showBackground = true, name = "MyResults - Loaded")
@Composable
fun MyEntriesScreenPreview() {
    MyEntriesScreen(viewModel = MyResultsViewModelMock())
}

@Preview(showBackground = true, name = "MyResults - Loading")
@Composable
fun MyEntriesScreenLoadingPreview() {
    MyEntriesScreen(viewModel = MyResultsViewModelMock(MyResultsUiState(isLoading = true)))
}

@Preview(showBackground = true, name = "MyResults - Empty")
@Composable
fun MyEntriesScreenEmptyPreview() {
    MyEntriesScreen(viewModel = MyResultsViewModelMock(MyResultsUiState()))
}
