package se.kjellstrand.webshooter.ui.screens.signups

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import se.kjellstrand.webshooter.ui.navigation.safePopBackStack
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitionsignups.remote.CompetitionSignupEntry
import se.kjellstrand.webshooter.ui.mock.SignupsViewModelMock
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionSignupsScreen(
    navController: NavController,
    viewModel: SignupsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    var isFilterSheetOpen by rememberSaveable { mutableStateOf(false) }

    val displayed = uiState.filteredAndSorted
    val currentUserClub = uiState.currentUserClubName
    val grouped = remember(displayed, currentUserClub) {
        displayed.groupBy { it.club?.name ?: "" }.entries
            .sortedWith(compareByDescending { it.key == currentUserClub })
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = if (uiState.totalSignupsCount > 0)
                    stringResource(R.string.competition_signups_list_participants_count, uiState.uniquePersonCount, uiState.totalSignupsCount)
                else
                    stringResource(R.string.competition_signups_list_participants),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isFilterSheetOpen = true }) {
                Icon(painterResource(R.drawable.filter_list), contentDescription = "Filter")
            }
        }
    ) { paddingValues ->
        if (!uiState.isLoading && displayed.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.competition_signups_list_no_signups))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding()),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 8.dp
                )
            ) {
                grouped.forEach { (clubName, entries) ->
                    val byUser = entries.groupBy { "${it.user?.name} ${it.user?.lastname}" }.entries.toList()

                    // Header item: top-rounded card + club name + divider
                    item(key = "club_header_$clubName") {
                        ClubHeaderItem(clubName = clubName, entries = entries)
                    }
                    // Each user row is a separate lazy item
                    itemsIndexed(
                        byUser,
                        key = { _, entry -> "user_${clubName}_${entry.key}" }
                    ) { index, (userName, userEntries) ->
                        val isLast = index == byUser.size - 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isLast) Modifier.clip(
                                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                    ) else Modifier
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 12.dp)
                                .then(if (isLast) Modifier.padding(bottom = 12.dp) else Modifier)
                        ) {
                            SignupRow(userEntries, isCurrentUser = userName == uiState.currentUserFullName)
                            if (!isLast) HorizontalDivider()
                        }
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
            onToggleFilterClub = { viewModel.toggleFilterClub(it) },
            onClearFilterClubs = { viewModel.clearFilterClubs() },
            onToggleFilterWeaponGroup = { viewModel.toggleFilterWeaponGroup(it) },
            onClearFilterWeaponGroups = { viewModel.clearFilterWeaponGroups() },
            onDismiss = { isFilterSheetOpen = false }
        )
    }
}

@Composable
private fun ClubHeaderItem(clubName: String, entries: List<CompetitionSignupEntry>) {
    val uniquePersons = entries.map { "${it.user?.name} ${it.user?.lastname}" }.distinct().size
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = clubName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "$uniquePersons / ${entries.size}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun SignupsFilterSheet(
    uiState: CompetitionSignupsUiState,
    onToggleFilterClub: (String) -> Unit,
    onClearFilterClubs: () -> Unit,
    onToggleFilterWeaponGroup: (String) -> Unit,
    onClearFilterWeaponGroups: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp)
        ) {

            // Club filter section
            if (uiState.availableClubs.isNotEmpty()) {
                Text(
                    stringResource(R.string.competition_signups_list_filter_club_label),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = uiState.filterClubs.isEmpty(),
                        onClick = { onClearFilterClubs() },
                        label = { Text(stringResource(R.string.competition_signups_list_filter_all)) }
                    )
                    uiState.availableClubs.forEach { club ->
                        FilterChip(
                            selected = club in uiState.filterClubs,
                            onClick = { onToggleFilterClub(club) },
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
                        selected = uiState.filterWeaponGroups.isEmpty(),
                        onClick = { onClearFilterWeaponGroups() },
                        label = { Text(stringResource(R.string.competition_signups_list_filter_all)) }
                    )
                    uiState.availableWeaponGroups.forEach { group ->
                        FilterChip(
                            selected = group in uiState.filterWeaponGroups,
                            onClick = { onToggleFilterWeaponGroup(group) },
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
                    onClearFilterClubs()
                    onClearFilterWeaponGroups()
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
private fun SignupRow(entries: List<CompetitionSignupEntry>, isCurrentUser: Boolean = false) {
    val user = entries.first().user
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCurrentUser) Modifier.background(MaterialTheme.appColors.currentUserHighlight) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${user?.name ?: ""} ${user?.lastname ?: ""}".trim(),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            entries.forEach { entry ->
                WeaponClassBadge(
                    weaponGroupName = entry.weaponclass?.classname ?: "",
                    isHighlighted = false,
                    size = WeaponClassBadgeSize.Small
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Signups - Loaded")
@Composable
fun SignupsScreenPreview() {
    CompetitionSignupsScreen(
        navController = rememberNavController(),
        viewModel = SignupsViewModelMock()
    )
}

@Preview(showBackground = true, name = "Signups - Loading")
@Composable
fun SignupsScreenLoadingPreview() {
    CompetitionSignupsScreen(
        navController = rememberNavController(),
        viewModel = SignupsViewModelMock(CompetitionSignupsUiState(isLoading = true))
    )
}

@Preview(showBackground = true, name = "Signups - Empty")
@Composable
fun SignupsScreenEmptyPreview() {
    CompetitionSignupsScreen(
        navController = rememberNavController(),
        viewModel = SignupsViewModelMock(CompetitionSignupsUiState())
    )
}
