package se.kjellstrand.webshooter.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock

@Composable
fun CompetitionResultsScreen(
    resultsViewModel: ResultsViewModel
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                PaddingValues(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp
                )
            )
    ) {
        // Chips på toppen för å filtrera samt switch för å välja filter/standard grouping
        FilterAndSwitch(resultsViewModel)

        when (resultsUiState.layoutType) {
            LayoutType.GROUP -> StandardGroupingLayout(resultsUiState)
            LayoutType.FILTER -> FilterLayout(resultsUiState)
        }
    }
}

@Composable
fun FilterAndSwitch(
    resultsViewModel: ResultsViewModel
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "FILTER")
        Switch(
            checked = resultsUiState.layoutType == LayoutType.GROUP,
            onCheckedChange = { isChecked ->
                resultsViewModel.setLayoutType(if (isChecked) LayoutType.GROUP else LayoutType.FILTER)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Green,
                uncheckedThumbColor = Color.Red
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(text = "GROUP")
    }
}

@Composable
fun StandardGroupingLayout(
    resultsUiState: ResultsUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (resultsUiState.groupedResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                resultsUiState.groupedResults.forEach { group ->
                    item {
                        Text(
                            text = "Vapengrupp: " + group.header,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                        )
                    }
                    // Display the results for each group
                    items(group.items) { result ->
                        ResultItem(resultsUiState, result = result)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    resultsUiState: ResultsUiState,
    result: Result
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = result.placement.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(2f)
            )
            Column(
                modifier = Modifier.weight(16f)
            ) {
                Text(
                    text = "${result.signup.user.name} ${result.signup.user.lastname}",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.signup.club?.name ?: "Unknown club",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (resultsUiState.layoutType == LayoutType.FILTER) {
                Text(
                    text = result.weaponClass.classname,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(2f)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .weight(6f)
            ) {
                Text(
                    text = "T:${result.hits} F:${result.figureHits} P:${result.points}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun FilterLayout(
    resultsUiState: ResultsUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 16.dp
            )
        ) {
            if (resultsUiState.groupedResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(resultsUiState.results) { result ->
                    ResultItem(resultsUiState, result = result)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

data class GroupedItem(
    val header: String,
    val items: List<Result>
)

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock()
    )
}
