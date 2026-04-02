package se.kjellstrand.webshooter.ui.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamEntry
import se.kjellstrand.webshooter.data.competitionteams.remote.TeamSignupEntry
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.navigation.safePopBackStack
import se.kjellstrand.webshooter.ui.theme.appColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompetitionTeamsScreen(
    navController: NavController,
    viewModel: TeamsViewModel = hiltViewModel<TeamsViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()

    val totalShooters = uiState.teams.sumOf { team ->
        team.signups.map { it.user.userId }.distinct().size
    }

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = if (uiState.teams.isNotEmpty())
                    stringResource(R.string.competition_teams_title_count, uiState.teams.size, totalShooters)
                else
                    stringResource(R.string.competitions_teams_button),
                navigationIcon = {
                    IconButton(onClick = { navController.safePopBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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

                uiState.teams.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.competition_teams_no_teams),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp,
                            top = 8.dp,
                            bottom = paddingValues.calculateBottomPadding() + 8.dp
                        )
                    ) {
                        uiState.teams.forEach { team ->
                            item(key = "header_${team.id}") {
                                TeamHeaderItem(team = team)
                            }
                            val sortedSignups = team.signups.sortedBy { it.pivot.position }
                            itemsIndexed(
                                sortedSignups,
                                key = { _, signup -> "signup_${team.id}_${signup.id}" }
                            ) { index, signup ->
                                val isLast = index == sortedSignups.size - 1
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
                                    TeamSignupRow(signup, isCurrentUser = signup.user.userId == uiState.currentUserId)
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

@Composable
private fun TeamHeaderItem(team: TeamEntry) {
    val uniqueShooters = team.signups.map { it.user.userId }.distinct().size
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
                text = team.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (team.weapongroup != null) {
                    WeaponClassBadge(
                        weaponGroupName = team.weapongroup.name,
                        isHighlighted = false,
                        size = WeaponClassBadgeSize.Small
                    )
                }
                Text(
                    text = "$uniqueShooters / ${team.signups.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Composable
private fun TeamSignupRow(signup: TeamSignupEntry, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isCurrentUser) Modifier.background(MaterialTheme.appColors.currentUserHighlight) else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${signup.user.name} ${signup.user.lastname}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}
