package se.kjellstrand.webshooter.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.mock.MockResultsUiState

@Composable
fun CompetitionResultsScreen(
    resultsState: ResultsUiState
) {
    resultsState.results?.let { results ->
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
            items(results) { result ->
                ResultItem(result = result)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
            Text(
                text = result.weaponClass.classname,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(2f)
            )
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
        resultsState = MockResultsUiState().uiState
    )
}
