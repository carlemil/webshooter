package se.kjellstrand.webshooter.ui.competitionpatrols

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry
import se.kjellstrand.webshooter.ui.common.ScreenTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionPatrolsScreen(
    navController: NavController,
    viewModel: CompetitionPatrolsViewModel = hiltViewModel<CompetitionPatrolsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(R.string.competition_patrols_title),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (!uiState.isLoading) {
                FloatingActionButton(onClick = { isFilterSheetOpen = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter och sortering")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.filteredPatrols.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.competition_patrols_no_patrols),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.filteredPatrols.forEach { patrol ->
                            item(key = "header_${patrol.id}") {
                                PatrolCard(patrol = patrol, uiState = uiState)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isFilterSheetOpen) {
        PatrolsFilterSheet(
            uiState = uiState,
            onSetSortField = { viewModel.setSortField(it) },
            onSetFilterClub = { viewModel.setFilterClub(it) },
            onSetFilterWeaponGroup = { viewModel.setFilterWeaponGroup(it) },
            onDismiss = { isFilterSheetOpen = false }
        )
    }
}

@Composable
private fun PatrolCard(patrol: PatrolEntry, uiState: CompetitionPatrolsUiState) {
    val weaponGroups = patrol.signups
        .map { it.weaponclass.classnameGeneral }
        .distinct()
        .sorted()
        .joinToString(" ")

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Patrol header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            R.string.competition_patrols_patrol_number,
                            patrol.sortorder
                        ),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${patrol.startTimeHuman} – ${patrol.endTimeHuman}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(
                            R.string.competition_patrols_participant_count,
                            patrol.signups.size
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (weaponGroups.isNotEmpty()) {
                        Text(
                            text = weaponGroups,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Column header
            SignupHeaderRow(uiState.sortField)

            HorizontalDivider(modifier = Modifier.padding(bottom = 4.dp))

            // Signup rows
            patrol.signups.forEach { signup ->
                SignupRow(signup)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SignupHeaderRow(sortField: PatrolsSortField) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.competition_patrols_sort_name),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (sortField == PatrolsSortField.Name) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = stringResource(R.string.competition_patrols_sort_club),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (sortField == PatrolsSortField.Club) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = stringResource(R.string.competition_patrols_sort_group),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (sortField == PatrolsSortField.WeaponGroup) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SignupRow(signup: PatrolSignupEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${signup.user.name} ${signup.user.lastname}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = signup.club.name,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = signup.weaponclass.classnameGeneral,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PatrolsFilterSheet(
    uiState: CompetitionPatrolsUiState,
    onSetSortField: (PatrolsSortField) -> Unit,
    onSetFilterClub: (String?) -> Unit,
    onSetFilterWeaponGroup: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 32.dp)) {

            // Sort section
            Text(
                stringResource(R.string.competition_patrols_sort_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            PatrolsSortField.entries.forEach { field ->
                val label = when (field) {
                    PatrolsSortField.Name -> stringResource(R.string.competition_patrols_sort_name)
                    PatrolsSortField.Club -> stringResource(R.string.competition_patrols_sort_club)
                    PatrolsSortField.WeaponGroup -> stringResource(R.string.competition_patrols_sort_group)
                }
                val directionSuffix = if (uiState.sortField == field) {
                    if (uiState.sortDirection == PatrolsSortDirection.Ascending) " ↑" else " ↓"
                } else ""
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.sortField == field,
                        onClick = { onSetSortField(field) }
                    )
                    Text(label + directionSuffix)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Club filter
            if (uiState.availableClubs.isNotEmpty()) {
                Text(
                    stringResource(R.string.competition_patrols_filter_club_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterClub == null,
                        onClick = { onSetFilterClub(null) },
                        label = { Text(stringResource(R.string.competition_patrols_filter_all)) }
                    )
                    uiState.availableClubs.forEach { club ->
                        FilterChip(
                            selected = uiState.filterClub == club,
                            onClick = {
                                onSetFilterClub(if (uiState.filterClub == club) null else club)
                            },
                            label = { Text(club) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Weapon group filter
            if (uiState.availableWeaponGroups.isNotEmpty()) {
                Text(
                    stringResource(R.string.competition_patrols_filter_group_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterWeaponGroup == null,
                        onClick = { onSetFilterWeaponGroup(null) },
                        label = { Text(stringResource(R.string.competition_patrols_filter_all)) }
                    )
                    uiState.availableWeaponGroups.forEach { group ->
                        FilterChip(
                            selected = uiState.filterWeaponGroup == group,
                            onClick = {
                                onSetFilterWeaponGroup(
                                    if (uiState.filterWeaponGroup == group) null else group
                                )
                            },
                            label = { Text(group) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {
                    onSetFilterClub(null)
                    onSetFilterWeaponGroup(null)
                }) {
                    Text(stringResource(R.string.competition_patrols_clear_filters))
                }
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.results_done_button))
                }
            }
        }
    }
}
