package se.kjellstrand.webshooter.ui.screens.results

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.ResultItem
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.ResultsListHeader
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.WeaponGroupSeparator
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen

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
        runCatching { java.time.LocalDate.parse(resultsViewModel.competitionDate) == java.time.LocalDate.now() }.getOrDefault(
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
            resultsUiState.filterResults.mapIndexedNotNull { i, result ->
                if (result.signup.user.userID == userId) i + 1 else null
            }
        } else {
            val indices = mutableListOf<Int>()
            var base = 0
            resultsUiState.groupedResults.forEach { group ->
                group.items.forEachIndexed { i, result ->
                    if (result.signup.user.userID == userId) indices.add(base + 2 + i)
                }
                base += 2 + group.items.size
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
                    navController.popBackStack()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = resultsUiState.competitionName,
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                            coroutineScope.launch { listState.animateScrollToItem(currentUserIndices[nextIdx]) }
                        }
                    },
                    containerColor = if (ffEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (ffEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = "Fast forward to current user"
                    )
                }
                FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
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
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = dimensionResource(R.dimen.screen_content_top_padding), bottom = 16.dp)
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
                    listState
                )
            }
        }
    }

    if (isFilterBottomSheetOpen) {
        FilterBottomSheet(
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

@Composable
fun ResultsList(
    resultsUiState: ResultsUiState,
    competitionId: Int,
    navController: NavController,
    resultsType: ResultsType = ResultsType.FIELD,
    listState: LazyListState = rememberLazyListState()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 0.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 16.dp
            )
        ) {
            val noneSelected = resultsUiState.selectedWeaponGroups.isEmpty()
                    && resultsUiState.allWeaponGroups.isNotEmpty()

            if (noneSelected) {
                // empty — nothing to show
            } else if (resultsUiState.isLoading) {
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
            } else if (resultsUiState.groupingMode == GroupingMode.NONE) {
                // FLAT VIEW
                item {
                    ResultsListHeader(isGrouped = false, resultsType = resultsType)
                }
                itemsIndexed(
                    resultsUiState.filterResults,
                    key = { _, it -> it.id }) { index, result ->
                    ResultItem(
                        result = result,
                        index = index,
                        isGrouped = false,
                        resultsType = resultsType,
                        loggedInUserId = resultsUiState.loggedInUserId,
                        onItemClick = {
                            navController.navigate(
                                Screen.ShooterResult.createRoute(
                                    competitionId,
                                    result.signup.user.userID.toInt(),
                                    resultsType.name
                                )
                            )
                        })
                    Spacer(modifier = Modifier.height(2.dp))
                }
            } else {
                // GROUPED VIEW with separators
                resultsUiState.groupedResults.forEach { group ->
                    item(key = "separator-${group.header}") {
                        WeaponGroupSeparator(group.header)
                    }
                    item {
                        ResultsListHeader(isGrouped = true, resultsType = resultsType)
                    }
                    itemsIndexed(group.items, key = { _, item -> item.id }) { index, result ->
                        ResultItem(
                            result = result,
                            index = index,
                            isGrouped = true,
                            resultsType = resultsType,
                            loggedInUserId = resultsUiState.loggedInUserId,
                            onItemClick = {
                                navController.navigate(
                                    Screen.ShooterResult.createRoute(
                                        competitionId,
                                        result.signup.user.userID.toInt(),
                                        resultsType.name
                                    )
                                )
                            })
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    allWeaponGroups: List<String>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit,
    onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState,
        content = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Gruppering", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                GroupingMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterChange(filterState.copy(groupingMode = mode)) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = filterState.groupingMode == mode,
                            onClick = { onFilterChange(filterState.copy(groupingMode = mode)) }
                        )
                        Text(
                            text = when (mode) {
                                GroupingMode.WEAPON_CLASS -> "Vapenklass"
                                GroupingMode.CLUB -> "Klubb"
                                GroupingMode.MEDL -> "Medl"
                                GroupingMode.NONE -> "Ingen"
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.results_select_weapon_groups),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterOptionsContent(
                    weaponGroups = allWeaponGroups,
                    filterState = filterState,
                    onFilterChange = onFilterChange
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        onFilterChange(filterState.copy(selectedWeaponGroups = allWeaponGroups.toSet()))
                    }) {
                        Text(stringResource(R.string.results_filter_all))
                    }
                    TextButton(onClick = {
                        onFilterChange(filterState.copy(selectedWeaponGroups = emptySet()))
                    }) {
                        Text(stringResource(R.string.results_filter_none))
                    }
                    Button(onClick = onDismissRequest) {
                        Text(stringResource(R.string.results_done_button))
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterOptionsContent(
    weaponGroups: List<String>,
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit
) {
    FlowRow {
        weaponGroups.forEach { weaponGroup ->
            val isSelected = filterState.selectedWeaponGroups.contains(weaponGroup)
            WeaponClassBadge(
                weaponGroupName = weaponGroup,
                isHighlighted = isSelected,
                size = WeaponClassBadgeSize.Large,
                modifier = Modifier.clickable {
                    val newSelectedGroups = if (isSelected) {
                        filterState.selectedWeaponGroups - weaponGroup
                    } else {
                        filterState.selectedWeaponGroups + weaponGroup
                    }
                    onFilterChange(filterState.copy(selectedWeaponGroups = newSelectedGroups))
                }
            )
        }
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
