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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionPatrolsScreen(
    navController: NavController,
    viewModel: CompetitionPatrolsViewModel = hiltViewModel<CompetitionPatrolsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()

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

                uiState.patrols.isEmpty() -> {
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
                        uiState.patrols.forEach { patrol ->
                            item(key = "header_${patrol.id}") {
                                PatrolCard(patrol = patrol)
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
private fun PatrolCard(patrol: PatrolEntry) {
    val weaponGroups = patrol.signups
        .map { it.weaponclass.classnameGeneral }
        .distinct()
        .sorted()

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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (weaponGroups.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.weapon_group),
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
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SignupHeaderRow()

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
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = stringResource(R.string.competition_patrols_name_club),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(3f)
        )
        Text(
            text = stringResource(R.string.weapon_groups),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Normal,
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
            text = "#${signup.lane}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(0.5f)
        )
        Column(modifier = Modifier.weight(3f)) {
            Text(
                text = "${signup.user.name} ${signup.user.lastname}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = signup.club.name,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            WeaponClassBadge(
                weaponGroupName = signup.weaponclass.classname,
                isHighlighted = false,
                size = WeaponClassBadgeSize.Small
            )
        }
    }
}
