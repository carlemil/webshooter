package se.kjellstrand.webshooter.ui.results

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.common.WeaponGroupBadge
import se.kjellstrand.webshooter.ui.navigation.Screen

@Composable
fun ResultsScreen(
    navController: NavController, resultsState: ResultsUiState
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
                ResultItem(result = result, onItemClick = {
                    navController.navigate(Screen.CompetitionDetail.createRoute(result.id))
                })
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
    result: Result, onItemClick: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .clickable { onItemClick() }
    ) {
        Text(
            text = result.signup.user.name + " " + result.signup.user.lastname,
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Status: ${result.signup.club.name}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "T/F: ${result.hits}/${result.figureHits}/${result.points}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(6.dp))
        WeaponGroupBadge(
            weaponGroupName = result.weaponClass.classname,
            isHighlighted = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    val navController = rememberNavController()

    ResultsScreen(
        navController = navController,
        resultsState = MockResultsUiState().uiState
    )
}