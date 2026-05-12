package se.kjellstrand.webshooter.ui.screens.shooterresult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.mock.MockResults
import se.kjellstrand.webshooter.ui.mock.ShooterResultViewModelMock
import se.kjellstrand.webshooter.ui.screens.results.ResultsViewModelImpl
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.HeaderText
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.ItemText
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.WeaponGroupSeparator
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.resources.*
import se.kjellstrand.webshooter.ui.common.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShooterResultScreen(
    onBack: () -> Unit,
    viewModel: ShooterResultViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            ScreenTopBar(
                title = uiState.shooterName,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .padding(top = Dimens.ScreenContentTopPadding)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                Text(text = "Error: ${uiState.error}")
            } else {
                LazyColumn {
                    uiState.groupedResults.forEach { group ->
                        item(key = "separator-${group.header}") {
                            WeaponGroupSeparator(group.header)
                        }
                        items(group.items, key = { it.id }) { result ->
                            StationResultsGrid(
                                stationResults = result.results,
                                resultsType = uiState.resultsType
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StationResultsGrid(stationResults: List<StationResult>, resultsType: ResultsType) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (resultsType) {
            ResultsType.FIELD,
            ResultsType.POINTS_FIELD -> {
                HeaderText(
                    Res.string.shooter_result_station,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                HeaderText(Res.string.hits, modifier = Modifier.weight(1f))
                HeaderText(Res.string.figures, modifier = Modifier.weight(1f))
                HeaderText(Res.string.points, modifier = Modifier.weight(1f))
            }

            ResultsType.PRECISION,
            ResultsType.MILITARY -> {
                HeaderText(
                    Res.string.shooter_result_serie,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                HeaderText(Res.string.points, modifier = Modifier.weight(1f))
                HeaderText(Res.string.x, modifier = Modifier.weight(1f))
            }
        }
    }
    Column(modifier = Modifier.padding(top = 8.dp)) {
        stationResults.forEachIndexed { index, stationResult ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                ItemText(
                    text = (index + 1).toString(),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                when (resultsType) {
                    ResultsType.FIELD,
                    ResultsType.POINTS_FIELD -> {
                        ItemText(
                            text = stationResult.hits.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        ItemText(
                            text = stationResult.figureHits.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        ItemText(
                            text = stationResult.points.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }

                    ResultsType.PRECISION,
                    ResultsType.MILITARY -> {
                        ItemText(
                            text = stationResult.points.toString(),
                            modifier = Modifier.weight(1f),
                        )
                        ItemText(
                            text = stationResult.hits.toString(),
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            HorizontalDivider()
        }
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(8.dp)
        ) {
            ItemText(
                text = "",
                modifier = Modifier.weight(1f),
            )
            when (resultsType) {
                ResultsType.FIELD,
                ResultsType.POINTS_FIELD -> {
                    ItemText(
                        text = stationResults.sumOf { it.hits }.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                    ItemText(
                        text = stationResults.sumOf { it.figureHits }.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                    ItemText(
                        text = stationResults.sumOf { it.points }.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }

                ResultsType.PRECISION,
                ResultsType.MILITARY -> {
                    ItemText(
                        text = stationResults.sumOf { it.points }.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                    ItemText(
                        text = stationResults.sumOf { it.hits }.toString(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

}

@Preview
@Composable
fun ShooterResultScreenPreview() {
    val mockResults = MockResults().results
    ShooterResultScreen(
        onBack = {},
        viewModel = ShooterResultViewModelMock(ShooterResultUiState(
            isLoading = false,
            shooterName = "Erik Svensson",
            results = mockResults,
            groupedResults = ResultsViewModelImpl.groupResults(mockResults, ResultsType.FIELD)
        ))
    )
}

@Preview
@Composable
fun ShooterResultScreenLoadingPreview() {
    ShooterResultScreen(
        onBack = {},
        viewModel = ShooterResultViewModelMock(ShooterResultUiState(isLoading = true))
    )
}

@Preview
@Composable
fun ShooterResultScreenErrorPreview() {
    ShooterResultScreen(
        onBack = {},
        viewModel = ShooterResultViewModelMock(ShooterResultUiState(isLoading = false, error = "NetworkError"))
    )
}
