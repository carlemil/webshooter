package se.kjellstrand.webshooter.ui.competitions

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.CompetitionStatus
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponClassBadges
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(
    navController: NavController,
    competitionsViewModel: CompetitionsViewModel = hiltViewModel<CompetitionsViewModelImpl>()
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    var expanded by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf(competitionsState.competitionStatus) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = {
                    expanded = !expanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    value = selectedStatus.status,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    CompetitionStatus.values().forEach { status ->
                        DropdownMenuItem(
                            text = { Text(text = status.status) },
                            onClick = {
                                selectedStatus = status
                                competitionsViewModel.setCompetitionStatus(status)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        competitionsState.competitions?.let { competitions ->
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(
                    top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp
                )
            ) {
                items(competitions.data) { competition ->
                    CompetitionItem(competition = competition,
                        onDetailsClick = {
                            navController.navigate("competition_detail/${competition.id}")
                        },
                        onResultsClick = {
                            navController.navigate("competition_results/${competition.id.toInt()}")
                        })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Load more items when reaching the end of the list
            LaunchedEffect(listState) {
                val lastVisibleItemIndex =
                    listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
                if (lastVisibleItemIndex != null && lastVisibleItemIndex >= competitions.data.size - 5) {
                    competitionsViewModel.loadNextPage()
                }
            }

        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun CompetitionItem(
    competition: Datum,
    onDetailsClick: () -> Unit,
    onResultsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main content on the left
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = competition.name,
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.date, competition.date),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.status, competition.statusHuman),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(
                    R.string.competition_type,
                    competition.competitionType.name
                ),
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            WeaponClassBadges(
                weaponClasses = competition.weaponClasses,
                userSignups = competition.userSignups
            )
        }

        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Button(onClick = { onDetailsClick() }) {
                Text(stringResource(R.string.details))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(enabled = competition.status == "completed",
                onClick = { onResultsClick() }) {
                Text(stringResource(R.string.result))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompetitionsScreenPreview() {
    val navController = rememberNavController()
    CompetitionsScreen(
        navController = navController, competitionsViewModel = CompetitionsViewModelMock()
    )
}
