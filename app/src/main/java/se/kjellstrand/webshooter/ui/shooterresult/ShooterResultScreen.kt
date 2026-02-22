package se.kjellstrand.webshooter.ui.shooterresult

import android.util.Log
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.StationResult
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.HeaderText
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.ItemText
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.WeaponGroupSeparator

@Composable
fun ShooterResultScreen(
    viewModel: ShooterResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Log.d("ShooterResultScreen", "uiState.isLoading: ${uiState.isLoading}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
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
            Text(text = uiState.shooterName, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
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
            ResultsType.FIELD -> {
                HeaderText(
                    R.string.station,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                HeaderText(R.string.hits, modifier = Modifier.weight(1f))
                HeaderText(R.string.figures, modifier = Modifier.weight(1f))
                HeaderText(R.string.points, modifier = Modifier.weight(1f))
            }

            ResultsType.PRECISION,
            ResultsType.MILITARY -> {
                HeaderText(
                    R.string.serie,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Start
                )
                HeaderText(R.string.points, modifier = Modifier.weight(1f))
                HeaderText(R.string.x, modifier = Modifier.weight(1f))
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
                    ResultsType.FIELD -> {
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
    }
}
