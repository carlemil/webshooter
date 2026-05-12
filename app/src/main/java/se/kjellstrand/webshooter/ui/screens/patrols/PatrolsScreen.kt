package se.kjellstrand.webshooter.ui.screens.patrols

import se.kjellstrand.webshooter.data.common.CompetitionType
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolEntry
import se.kjellstrand.webshooter.ui.mock.PatrolsViewModelMock
import se.kjellstrand.webshooter.data.competitionpatrols.remote.PatrolSignupEntry
import se.kjellstrand.webshooter.ui.common.GroupCardHeader
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.navigation.safePopBackStack
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.theme.appColors
import se.kjellstrand.webshooter.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionPatrolsScreen(
    navController: NavController,
    viewModel: PatrolsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val isFalt = uiState.competitionTypeId in CompetitionType.FALT_TYPE_IDS

    val sortedPatrols = remember(uiState.patrols, uiState.currentUserId, isFalt) {
        val userId = uiState.currentUserId
        if (isFalt) {
            uiState.patrols.sortedWith(
                compareByDescending<PatrolEntry> { patrol -> userId != null && patrol.signups.any { it.user.userId == userId } }
                    .thenBy { it.sortorder }
            )
        } else {
            uiState.patrols.sortedBy { it.sortorder }
        }
    }

    var filterQuery by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val filteredPatrols = remember(sortedPatrols, filterQuery) {
        val q = filterQuery.trim()
        if (q.isEmpty()) {
            sortedPatrols
        } else {
            sortedPatrols.mapNotNull { patrol ->
                val matching = patrol.signups.filter { signup ->
                    signup.user.name.contains(q, ignoreCase = true) ||
                        signup.user.lastname.contains(q, ignoreCase = true) ||
                        signup.club.name.contains(q, ignoreCase = true)
                }
                if (matching.isEmpty()) null else patrol.copy(signups = matching)
            }
        }
    }

    val currentUserSignupIndices = remember(filteredPatrols, uiState.currentUserId) {
        val userId = uiState.currentUserId ?: return@remember emptyList()
        val indices = mutableListOf<Int>()
        var base = 0
        filteredPatrols.forEach { patrol ->
            base++ // header item
            patrol.signups.forEachIndexed { i, signup ->
                if (signup.user.userId == userId) indices.add(base + i)
            }
            base += patrol.signups.size
        }
        indices
    }
    var occurrenceIdx by remember(currentUserSignupIndices) { mutableIntStateOf(-1) }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = stringResource(if (isFalt) Res.string.competitions_patrols_button else Res.string.competitions_relays_button),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val ffEnabled = currentUserSignupIndices.isNotEmpty()
            SmallFloatingActionButton(
                onClick = {
                    if (ffEnabled) {
                        val nextIdx = (occurrenceIdx + 1) % currentUserSignupIndices.size
                        occurrenceIdx = nextIdx
                        coroutineScope.launch {
                            val offset = -(listState.layoutInfo.viewportSize.height / 2)
                            listState.animateScrollToItem(
                                currentUserSignupIndices[nextIdx],
                                scrollOffset = offset
                            )
                        }
                    }
                },
                containerColor = if (ffEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (ffEnabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    painterResource(Res.drawable.fast_forward),
                    contentDescription = "Fast forward to current user"
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.patrols.isEmpty() -> {
                    Text(
                        text = stringResource(if (isFalt) Res.string.competition_patrols_no_patrols else Res.string.competition_patrols_no_relays),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 0.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.search),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        OutlinedTextField(
                            value = filterQuery,
                            onValueChange = { filterQuery = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 8.dp
                        )
                    ) {
                        filteredPatrols.forEach { patrol ->
                            // Header item: top-rounded card + patrol info + column header
                            item(key = "header_${patrol.id}") {
                                PatrolHeaderItem(patrol = patrol, isFalt = isFalt)
                            }
                            // Each signup row is a separate lazy item
                            itemsIndexed(
                                patrol.signups,
                                key = { _, signup -> "signup_${patrol.id}_${signup.lane}" }
                            ) { index, signup ->
                                val isLast = index == patrol.signups.size - 1
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isLast) Modifier.clip(
                                                RoundedCornerShape(
                                                    bottomStart = 12.dp,
                                                    bottomEnd = 12.dp
                                                )
                                            ) else Modifier
                                        )
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .padding(horizontal = 12.dp)
                                        .then(if (isLast) Modifier.padding(bottom = 12.dp) else Modifier)
                                ) {
                                    SignupRow(signup, isCurrentUser = signup.user.userId == uiState.currentUserId)
                                    if (!isLast) HorizontalDivider()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatrolHeaderItem(patrol: PatrolEntry, isFalt: Boolean) {
    val weaponGroups = patrol.signups
        .map { it.weaponclass.classnameGeneral }
        .distinct()
        .sorted()

    GroupCardHeader {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(
                    if (isFalt) Res.string.competition_patrols_patrol_number
                    else Res.string.competition_patrols_relay_number,
                    patrol.sortorder
                ),
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${patrol.startTimeHuman} – ${patrol.endTimeHuman}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = stringResource(
                    Res.string.competition_patrols_participant_count,
                    patrol.signups.size
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }

        if (weaponGroups.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.weapon_groups),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    weaponGroups.forEach { group ->
                        WeaponClassBadge(
                            weaponGroupName = group,
                            isHighlighted = false,
                            size = WeaponClassBadgeSize.Small
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        SignupHeaderRow()
        HorizontalDivider()
    }
}

@Composable
private fun SignupHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = "#",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = stringResource(Res.string.competition_patrols_name_club),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(10f)
        )
        Box(modifier = Modifier.weight(6f), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = stringResource(Res.string.weapon_group),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun SignupRow(signup: PatrolSignupEntry, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCurrentUser) Modifier.background(MaterialTheme.appColors.currentUserHighlight) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#${signup.lane}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(2f)
        )
        Column(modifier = Modifier.weight(12f)) {
            Text(
                text = "${signup.user.name} ${signup.user.lastname}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = signup.club.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Unspecified
            )
        }
        Box(modifier = Modifier.weight(4f), contentAlignment = Alignment.CenterEnd) {
            WeaponClassBadge(
                weaponGroupName = signup.weaponclass.classname,
                isHighlighted = false,
                size = WeaponClassBadgeSize.Small
            )
        }
    }
}

@Preview(showBackground = true, name = "Patrols - Loaded")
@Composable
fun PatrolsScreenPreview() {
    CompetitionPatrolsScreen(
        navController = rememberNavController(),
        viewModel = PatrolsViewModelMock()
    )
}

@Preview(showBackground = true, name = "Patrols - Loading")
@Composable
fun PatrolsScreenLoadingPreview() {
    CompetitionPatrolsScreen(
        navController = rememberNavController(),
        viewModel = PatrolsViewModelMock(CompetitionPatrolsUiState(isLoading = true))
    )
}

@Preview(showBackground = true, name = "Patrols - Empty")
@Composable
fun PatrolsScreenEmptyPreview() {
    CompetitionPatrolsScreen(
        navController = rememberNavController(),
        viewModel = PatrolsViewModelMock(CompetitionPatrolsUiState())
    )
}
