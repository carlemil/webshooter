package se.kjellstrand.webshooter.ui.competitions

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
                OutlinedTextField(
                    value = stringResource(selectedStatus.labelRes),
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
                    CompetitionStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(status.labelRes),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                selectedStatus = status
                                competitionsViewModel.setCompetitionStatus(status)
                                expanded = false
                            },
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        competitionsState.competitions?.let { competitions ->
            val displayedCompetitions = if (selectedStatus == CompetitionStatus.MY_ENTRIES) {
                competitions.data.filter { it.userSignups.isNotEmpty() }
            } else {
                competitions.data
            }

            if (displayedCompetitions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_competitions_match_filter))
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedCompetitions) { competition ->
                        CompetitionItem(
                            competition = competition,
                            onResultsClick = {
                                navController.navigate(
                                    Screen.CompetitionResults.createRoute(
                                        competition.id.toInt(),
                                        competition.resultsType.name
                                    )
                                )
                            },
                            onSignupClick = {
                                navController.navigate(
                                    Screen.CompetitionSignup.createRoute(competition.id)
                                )
                            }
                        )
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
    onResultsClick: () -> Unit,
    onSignupClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main content on the left
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
                            R.string.competition_type,
                            competition.competitionType.name
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    Button(
                        enabled = competition.status == "completed",
                        onClick = onResultsClick
                    ) {
                        Text(stringResource(R.string.result))
                    }
                    if (competition.status == "open") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = onSignupClick) {
                            Text(stringResource(R.string.sign_up))
                        }
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
                    contentDescription = if (isExpanded) stringResource(R.string.collapse)
                    else stringResource(R.string.expand)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider()
                    CompetitionDetail(
                        competition = competition,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CompetitionDetail(competition: Datum, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                DetailRow(
                    label = stringResource(R.string.contact_name, ""),
                    value = competition.contactName
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.date, ""),
                    value = competition.date
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.status, ""),
                    value = competition.statusHuman
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.open_for_team_signup, ""),
                    value = competition.signupsOpeningDate
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.last_signup_date, ""),
                    value = competition.signupsClosingDate
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.late_signup, ""),
                    value = competition.allowSignupsAfterClosingDateHuman
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.team_signup, ""),
                    value = if (competition.allowTeams == 1L) stringResource(R.string.yes) else stringResource(
                        R.string.no
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.competition_type, ""),
                    value = competition.competitionType.name
                )
                Spacer(modifier = Modifier.height(8.dp))
                DetailRow(
                    label = stringResource(R.string.result_calculation, ""),
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
            Spacer(modifier = Modifier.height(4.dp))
            Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 4.dp)) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.bodyMedium

                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = competition.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
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
