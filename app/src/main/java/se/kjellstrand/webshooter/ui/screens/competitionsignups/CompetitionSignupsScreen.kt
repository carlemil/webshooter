package se.kjellstrand.webshooter.ui.screens.competitionsignups

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
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionSignupsScreen(
    navController: NavController,
    viewModel: CompetitionSignupsViewModel = hiltViewModel<CompetitionSignupsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = if (uiState.totalSignupsCount > 0)
                    "${stringResource(R.string.competition_signups_list_participants)}  ${uiState.uniquePersonCount} / ${uiState.totalSignupsCount}"
                else
                    stringResource(R.string.competition_signups_list_participants),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isFilterSheetOpen = true }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter och sortering")
            }
        }
    ) { paddingValues ->
        val displayed = uiState.filteredAndSorted
        val grouped = displayed.groupBy { it.club.name }.entries.sortedBy { it.key }

        if (!uiState.isLoading && displayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.competition_signups_list_no_signups))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                grouped.forEach { (clubName, entries) ->
                    item(key = "club_$clubName") {
                        ClubCard(clubName = clubName, entries = entries)
                    }
                }
                if (uiState.isLoading) {
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
                }
            }
        }
    }

    if (isFilterSheetOpen) {
        SignupsFilterSheet(
            uiState = uiState,
            onSetSortField = { viewModel.setSortField(it) },
            onSetFilterClub = { viewModel.setFilterClub(it) },
            onSetFilterWeaponGroup = { viewModel.setFilterWeaponGroup(it) },
            onDismiss = { isFilterSheetOpen = false }
        )
    }
}

@Composable
private fun ClubCard(clubName: String, entries: List<CompetitionSignupEntry>) {
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
                text = clubName,
                style = MaterialTheme.typography.titleSmall
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            val byUser = entries
                .groupBy { "${it.user.name} ${it.user.lastname}" }
                .entries.sortedBy { it.key }
            byUser.forEach { (_, userEntries) ->
                SignupRow(userEntries)
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SignupsFilterSheet(
    uiState: CompetitionSignupsUiState,
    onSetSortField: (SignupsListSortField) -> Unit,
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
                stringResource(R.string.competition_signups_list_sort_label),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            SignupsListSortField.entries.forEach { field ->
                val label = when (field) {
                    SignupsListSortField.Name -> stringResource(R.string.competition_signups_list_sort_name)
                    SignupsListSortField.Club -> stringResource(R.string.competition_signups_list_sort_club)
                    SignupsListSortField.WeaponGroup -> stringResource(R.string.competition_signups_list_sort_group)
                }
                val directionLabel = if (uiState.sortField == field) {
                    if (uiState.sortDirection == SortDirection.Ascending) " ↑" else " ↓"
                } else ""
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = uiState.sortField == field,
                        onClick = { onSetSortField(field) }
                    )
                    Text(label + directionLabel)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            // Club filter section
            if (uiState.availableClubs.isNotEmpty()) {
                Text(
                    stringResource(R.string.competition_signups_list_filter_club_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterClub == null,
                        onClick = { onSetFilterClub(null) },
                        label = { Text(stringResource(R.string.competition_signups_list_filter_all)) }
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

            // Weapon group filter section
            if (uiState.availableWeaponGroups.isNotEmpty()) {
                Text(
                    stringResource(R.string.competition_signups_list_filter_group_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterWeaponGroup == null,
                        onClick = { onSetFilterWeaponGroup(null) },
                        label = { Text(stringResource(R.string.competition_signups_list_filter_all)) }
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
                    Text(stringResource(R.string.competition_signups_list_clear_filters))
                }
                Button(onClick = onDismiss) {
                    Text(stringResource(R.string.results_done_button))
                }
            }
        }
    }
}

@Composable
private fun SignupRow(entries: List<CompetitionSignupEntry>) {
    val user = entries.first().user
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${user.name} ${user.lastname}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.forEach { entry ->
                WeaponClassBadge(
                    weaponGroupName = entry.weaponclass.classname,
                    isHighlighted = false,
                    size = WeaponClassBadgeSize.Small
                )
            }
        }
    }
}
