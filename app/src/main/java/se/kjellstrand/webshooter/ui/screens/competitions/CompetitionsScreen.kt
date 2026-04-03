package se.kjellstrand.webshooter.ui.screens.competitions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen
import se.kjellstrand.webshooter.ui.navigation.safeNavigate
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionsScreen(
    navController: NavController,
    competitionsViewModel: CompetitionsViewModel = hiltViewModel<CompetitionsViewModelImpl>()
) {
    val competitionsState by competitionsViewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }

    val upcomingIndex = remember(competitionsState.filteredData) {
        val today = LocalDate.now()
        competitionsState.filteredData.indexOfLast { competition ->
            runCatching { !LocalDate.parse(competition.date).isBefore(today) }.getOrDefault(false)
        }
    }

    val signedUpIndices = remember(competitionsState.filteredData) {
        competitionsState.filteredData.mapIndexedNotNull { i, datum ->
            if (datum.userSignups.isNotEmpty()) i else null
        }
    }

    // Scroll-based trigger: compare against filtered list size, not raw size
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleItemIndex ->
            val comps = competitionsState.competitions ?: return@collect
            if (lastVisibleItemIndex != null &&
                lastVisibleItemIndex >= competitionsState.filteredData.size - 5 &&
                comps.data.size.toLong() < comps.total
            ) {
                competitionsViewModel.loadNextPage()
            }
        }
    }

    // State-based trigger: auto-load more pages when filtered results are sparse,
    // so filters don't stall pagination even when the user can't scroll
    LaunchedEffect(
        competitionsState.filteredData.size,
        competitionsState.competitions?.data?.size
    ) {
        val comps = competitionsState.competitions ?: return@LaunchedEffect
        if (!competitionsState.isLoading &&
            competitionsState.filteredData.size < 5 &&
            comps.data.size.toLong() < comps.total
        ) {
            competitionsViewModel.loadNextPage()
        }
    }

    Scaffold(
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                val ffEnabled = signedUpIndices.isNotEmpty()
                SmallFloatingActionButton(
                    onClick = {
                        if (ffEnabled) {
                            val firstVisible = listState.firstVisibleItemIndex
                            val nextIndex = signedUpIndices.firstOrNull { it > firstVisible }
                                ?: signedUpIndices.first()
                            coroutineScope.launch { listState.animateScrollToItem(nextIndex) }
                        }
                    },
                    containerColor = if (ffEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (ffEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(painter = painterResource(R.drawable.fast_forward), contentDescription = "Scroll to next signed-up competition")
                }
                SmallFloatingActionButton(
                    onClick = {
                        if (upcomingIndex >= 0) {
                            coroutineScope.launch { listState.animateScrollToItem(upcomingIndex) }
                        }
                    },
                    containerColor = if (upcomingIndex >= 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (upcomingIndex >= 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(painter = painterResource(R.drawable.event_upcoming), contentDescription = "Scroll to next upcoming competition")
                }
                FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                    Icon(painter = painterResource(R.drawable.filter_list), contentDescription = "Open Filters")
                }
            }
        }
    ) { paddingValues ->
        competitionsState.competitions?.let {
            val filteredData = competitionsState.filteredData
            if (filteredData.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.competitions_no_competitions_match_filter))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = paddingValues.calculateTopPadding() + 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = paddingValues.calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredData) { competition ->
                        CompetitionItem(
                            competition = competition,
                            patrolOrRelayButtonText = when (competition.competitionType.id) {
                                2, 3, 9, 10 -> R.string.competitions_patrols_button
                                else -> R.string.competitions_relays_button
                            },
                            onResultsClick = {
                                navController.safeNavigate(
                                    Screen.CompetitionResults.createRoute(
                                        competition.id,
                                        (competition.resultsType).name,
                                        competition.name,
                                        competition.date
                                    )
                                )
                            },
                            onSignupClick = {
                                navController.safeNavigate(
                                    Screen.CompetitionSignup.createRoute(competition.id)
                                )
                            },
                            onSignupsListClick = {
                                navController.safeNavigate(
                                    Screen.CompetitionSignupsList.createRoute(competition.id)
                                )
                            },
                            onPatrolsOrRelayClick = {
                                navController.safeNavigate(
                                    Screen.CompetitionPatrols.createRoute(
                                        competition.id,
                                        competition.competitionType.id
                                    )
                                )
                            },
                            onTeamsClick = {
                                navController.safeNavigate(
                                    Screen.CompetitionTeams.createRoute(competition.id)
                                )
                            }
                        )
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (competitionsState.hasError) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.competitions_load_error))
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { competitionsViewModel.reload() }) {
                            Text(stringResource(R.string.competitions_retry))
                        }
                    }
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (isFilterBottomSheetOpen) {
        CompetitionsFilterBottomSheet(
            allCompetitionTypes = competitionsState.allCompetitionTypes,
            allStatuses = competitionsState.allStatuses,
            selectedCompetitionTypeIds = competitionsState.selectedCompetitionTypeIds,
            selectedStatuses = competitionsState.selectedStatuses,
            onCompetitionTypesChange = { competitionsViewModel.setSelectedCompetitionTypeIds(it) },
            onStatusesChange = { competitionsViewModel.setSelectedStatuses(it) },
            onDismissRequest = { isFilterBottomSheetOpen = false }
        )
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
