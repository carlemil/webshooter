package se.kjellstrand.webshooter.ui.screens.results

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.safePopBackStack
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionResultsScreen(
    resultsViewModel: ResultsViewModel,
    navController: NavController
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()
    var isFilterBottomSheetOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val isCompetitionToday = remember(resultsViewModel.competitionDate) {
        runCatching { LocalDate.parse(resultsViewModel.competitionDate) == LocalDate.now() }.getOrDefault(
            false
        )
    }

    val refreshIntervalSeconds = 5 * 60
    var secondsLeft by remember { mutableIntStateOf(refreshIntervalSeconds) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshVersion = resultsUiState.refreshVersion
    var lastSeenRefreshVersion by remember { mutableIntStateOf(refreshVersion) }

    LaunchedEffect(refreshVersion) {
        if (refreshVersion > lastSeenRefreshVersion) {
            isRefreshing = false
        }
        lastSeenRefreshVersion = refreshVersion
    }

    LaunchedEffect(lifecycleOwner, isCompetitionToday, refreshTrigger) {
        if (!isCompetitionToday) return@LaunchedEffect
        secondsLeft = refreshIntervalSeconds
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            while (true) {
                delay(1000L)
                secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
                if (secondsLeft <= 0) {
                    resultsViewModel.refresh()
                    secondsLeft = refreshIntervalSeconds
                }
            }
        }
    }

    val currentUserIndices = remember(
        resultsUiState.filterResults,
        resultsUiState.groupedResults,
        resultsUiState.groupingMode,
        resultsUiState.loggedInUserId
    ) {
        val userId = resultsUiState.loggedInUserId
        if (userId == -1L) return@remember emptyList()
        if (resultsUiState.groupingMode == GroupingMode.NONE) {
            // Item 0 is the header; result rows are items 1..N
            resultsUiState.filterResults.mapIndexedNotNull { i, result ->
                if (result.signup.user.userID == userId) i + 1 else null
            }
        } else {
            // Each group occupies 1 header item + group.items.size result items
            val indices = mutableListOf<Int>()
            var base = 0
            resultsUiState.groupedResults.forEach { group ->
                base++ // header item
                group.items.forEachIndexed { i, result ->
                    if (result.signup.user.userID == userId) indices.add(base + i)
                }
                base += group.items.size
            }
            indices
        }
    }
    var occurrenceIdx by remember(currentUserIndices) { mutableIntStateOf(-1) }

    LaunchedEffect(Unit) {
        resultsViewModel.resultsEvent.collect { event ->
            when (event) {
                is ResultsEvent.Empty -> {
                    Toast.makeText(
                        context,
                        context.getString(R.string.results_no_results_found),
                        Toast.LENGTH_LONG
                    ).show()
                    navController.safePopBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = resultsUiState.competitionName,
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                val ffEnabled = currentUserIndices.isNotEmpty()
                SmallFloatingActionButton(
                    onClick = {
                        if (ffEnabled) {
                            val nextIdx = (occurrenceIdx + 1) % currentUserIndices.size
                            occurrenceIdx = nextIdx
                            coroutineScope.launch {
                                val offset = -(listState.layoutInfo.viewportSize.height / 2)
                                listState.animateScrollToItem(
                                    currentUserIndices[nextIdx],
                                    scrollOffset = offset
                                )
                            }
                        }
                    },
                    containerColor = if (ffEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (ffEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        painterResource(R.drawable.fast_forward),
                        contentDescription = "Fast forward to current user"
                    )
                }
                FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                    Icon(
                        painterResource(R.drawable.filter_list),
                        contentDescription = "Open Filters"
                    )
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                resultsViewModel.refresh()
                refreshTrigger++
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = dimensionResource(R.dimen.screen_content_top_padding))
            ) {
                if (isCompetitionToday) {
                    Text(
                        text = stringResource(R.string.updates_in, secondsLeft),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
                ResultsList(
                    resultsUiState,
                    resultsViewModel.competitionId,
                    navController,
                    resultsUiState.resultsType,
                    listState,
                    bottomContentPadding = paddingValues.calculateBottomPadding()
                )
            }
        }
    }

    if (isFilterBottomSheetOpen) {
        GroupingAndFilterBottomSheet(
            allWeaponGroups = resultsUiState.allWeaponGroups,
            filterState = FilterState(
                selectedWeaponGroups = resultsUiState.selectedWeaponGroups,
                groupingMode = resultsUiState.groupingMode
            ),
            onFilterChange = { newFilterState ->
                resultsViewModel.setSelectedWeaponGroups(newFilterState.selectedWeaponGroups)
                resultsViewModel.setGroupingMode(newFilterState.groupingMode)
            },
            onDismissRequest = { isFilterBottomSheetOpen = false }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock(),
        navController = NavController(LocalContext.current)
    )
}
