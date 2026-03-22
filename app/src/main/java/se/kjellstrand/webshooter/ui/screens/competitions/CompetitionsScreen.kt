package se.kjellstrand.webshooter.ui.screens.competitions

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.CompetitionType
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponClassBadges
import se.kjellstrand.webshooter.ui.mock.CompetitionsViewModelMock
import se.kjellstrand.webshooter.ui.navigation.Screen
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

    Box(modifier = Modifier.fillMaxSize()) {
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
                        top = 8.dp, start = 16.dp, end = 16.dp, bottom = 80.dp
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
                                navController.navigate(
                                    Screen.CompetitionResults.createRoute(
                                        competition.id.toInt(),
                                        (competition.resultsType).name,
                                        competition.name,
                                        competition.date
                                    )
                                )
                            },
                            onSignupClick = {
                                navController.navigate(
                                    Screen.CompetitionSignup.createRoute(competition.id)
                                )
                            },
                            onSignupsListClick = {
                                navController.navigate(
                                    Screen.CompetitionSignupsList.createRoute(competition.id)
                                )
                            },
                            onPatrolsOrRelayClick = {
                                navController.navigate(
                                    Screen.CompetitionPatrols.createRoute(
                                        competition.id,
                                        competition.competitionType.id
                                    )
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

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
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
                Icon(imageVector = Icons.Default.FastForward, contentDescription = "Scroll to next signed-up competition")
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
                Icon(imageVector = Icons.Default.Today, contentDescription = "Scroll to next upcoming competition")
            }
            FloatingActionButton(onClick = { isFilterBottomSheetOpen = true }) {
                Icon(imageVector = Icons.Default.FilterList, contentDescription = "Open Filters")
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CompetitionsFilterBottomSheet(
    allCompetitionTypes: List<CompetitionType>,
    allStatuses: Map<String, String>,
    selectedCompetitionTypeIds: Set<Int>,
    selectedStatuses: Set<String>,
    onCompetitionTypesChange: (Set<Int>) -> Unit,
    onStatusesChange: (Set<String>) -> Unit,
    onDismissRequest: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = bottomSheetState
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                stringResource(R.string.competitions_filter_status),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allStatuses.forEach { status ->
                    val isSelected = selectedStatuses.contains(status.key)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated = if (isSelected) selectedStatuses - status.key
                            else selectedStatuses + status.key
                            onStatusesChange(updated)
                        },
                        label = { Text(status.value) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.competitions_filter_competition_type),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                allCompetitionTypes.forEach { competitionType ->
                    val isSelected = selectedCompetitionTypeIds.contains(competitionType.id)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            val updated =
                                if (isSelected) selectedCompetitionTypeIds - competitionType.id
                                else selectedCompetitionTypeIds + competitionType.id
                            onCompetitionTypesChange(updated)
                        },
                        label = { Text(competitionType.name) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = onDismissRequest) {
                    Text(stringResource(R.string.results_done_button))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CompetitionItem(
    competition: Datum,
    patrolOrRelayButtonText: Int,
    onResultsClick: () -> Unit,
    onSignupClick: () -> Unit = {},
    onSignupsListClick: () -> Unit = {},
    onPatrolsOrRelayClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hasLocation =
        competition.lat != 0.0 || competition.lng != 0.0 || !competition.googleMaps.isNullOrBlank()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = competition.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${competition.date}  •  ${competition.statusHuman}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                R.string.competitions_competition_type,
                                competition.competitionType.name
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            val uri = when {
                                competition.lat != 0.0 || competition.lng != 0.0 ->
                                    "geo:${competition.lat},${competition.lng}?q=${competition.lat},${competition.lng}".toUri()

                                !competition.googleMaps.isNullOrBlank() ->
                                    competition.googleMaps!!.replace("/maps/embed", "/maps").toUri()

                                else -> "geo:0,0".toUri()
                            }
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        enabled = hasLocation
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = stringResource(R.string.competitions_open_map)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                WeaponClassBadges(
                    weaponClasses = competition.weaponClasses,
                    userSignups = competition.userSignups
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val shape =
                        RoundedCornerShape(integerResource(R.integer.rounded_corner_shape_percent))

                    val buttonContentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    val resultsEnabled = competition.status == "completed" ||
                            runCatching {
                                !LocalDate.parse(competition.date).isAfter(LocalDate.now())
                            }.getOrDefault(false)
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = resultsEnabled,
                        onClick = onResultsClick,
                        shape = shape,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(R.string.competitions_result),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onSignupsListClick,
                        shape = shape,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(R.string.competition_signups_list_participants),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onPatrolsOrRelayClick,
                        shape = shape,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(patrolOrRelayButtonText),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        enabled = competition.status == "open",
                        shape = shape,
                        onClick = onSignupClick,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(R.string.sign_up),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            HorizontalDivider()
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.competitions_collapse)
                    else stringResource(R.string.competitions_expand)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CompetitionDetail(
                        competition = competition,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompetitionDetail(competition: Datum, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_information),
                    style = MaterialTheme.typography.titleSmall
                )
                DetailRow(
                    label = stringResource(R.string.competitions_contact_name, ""),
                    value = competition.contactName ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_date, ""),
                    value = competition.date
                )
                DetailRow(
                    label = stringResource(R.string.competitions_status, ""),
                    value = competition.statusHuman
                )
                DetailRow(
                    label = stringResource(R.string.competitions_open_for_team_signup, ""),
                    value = competition.signupsOpeningDate
                )
                DetailRow(
                    label = stringResource(R.string.competitions_last_signup_date, ""),
                    value = competition.signupsClosingDate
                )
                DetailRow(
                    label = stringResource(R.string.competitions_late_signup, ""),
                    value = competition.allowSignupsAfterClosingDateHuman
                )
                DetailRow(
                    label = stringResource(R.string.competitions_team_signup, ""),
                    value = if (competition.allowTeams == 1L) stringResource(R.string.competitions_yes) else stringResource(R.string.competitions_no)
                )
                DetailRow(
                    label = stringResource(R.string.competitions_competition_type, ""),
                    value = competition.competitionType.name
                )
                DetailRow(
                    label = stringResource(R.string.competitions_result_calculation, ""),
                    value = competition.resultsTypeHuman
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_contact_information),
                    style = MaterialTheme.typography.titleSmall
                )
                DetailRow(
                    label = stringResource(R.string.competitions_arranger),
                    value = competition.club.name
                )
                DetailRow(
                    label = stringResource(R.string.competitions_venue),
                    value = competition.contactVenue ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_city),
                    value = competition.contactCity ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_contact_person),
                    value = competition.contactName ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_phone),
                    value = competition.contactTelephone ?: "",
                    onClick = competition.contactTelephone?.takeIf { it.isNotEmpty() }?.let {
                        { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$it".toUri())) }
                    }
                )
                DetailRow(
                    label = stringResource(R.string.competitions_email),
                    value = competition.contactEmail ?: "",
                    onClick = competition.contactEmail?.takeIf { it.isNotEmpty() }?.let {
                        { context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$it".toUri())) }
                    }
                )
                DetailRow(
                    label = stringResource(R.string.competitions_website),
                    value = competition.website ?: "",
                    onClick = competition.website?.takeIf { it.isNotEmpty() }?.let {
                        { context.startActivity(Intent(Intent.ACTION_VIEW, it.toUri())) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_description),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = competition.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(3f)
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
