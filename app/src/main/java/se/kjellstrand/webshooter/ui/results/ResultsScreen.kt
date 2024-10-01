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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp
        )
    ) {
        resultsUiState.groupedResults?.forEach { group ->
            item {
                Text(
                    text = "Vapengrupp: " + group.header,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
            group.items.forEach { result ->
                item {
                    ResultItem(resultsViewModel, result = result)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    } ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ResultItem(
    resultsViewModel: ResultsViewModel,
    result: Result
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()
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
            if (resultsUiState.showClassname) {
                Text(
                    text = result.weaponClass.classname,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(2f)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .weight(4f)
            ) {
                Text(
                    text = "${result.hits}/${result.figureHits}/${result.points}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock()
    )
}
