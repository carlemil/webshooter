package se.kjellstrand.webshooter.ui.club

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.data.common.Club

@Composable
fun ClubScreen(viewModel: ClubViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            Tab(
                selected = uiState.selectedTab == ClubTab.INFORMATION,
                onClick = { viewModel.selectTab(ClubTab.INFORMATION) },
                text = { Text(stringResource(R.string.club_tab_information)) }
            )
            Tab(
                selected = uiState.selectedTab == ClubTab.ADMINS,
                onClick = { viewModel.selectTab(ClubTab.ADMINS) },
                text = { Text(stringResource(R.string.club_tab_admins)) }
            )
            Tab(
                selected = uiState.selectedTab == ClubTab.USERS,
                onClick = { viewModel.selectTab(ClubTab.USERS) },
                text = { Text(stringResource(R.string.club_tab_users)) }
            )
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        when (uiState.selectedTab) {
            ClubTab.INFORMATION -> ClubInformationTab(uiState.clubInfo)
            ClubTab.ADMINS -> ClubMemberListTab(uiState.admins)
            ClubTab.USERS -> ClubMemberListTab(uiState.users)
        }
    }
}

@Composable
private fun ClubInformationTab(club: Club?) {
    if (club == null) return

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item {
            Text(text = club.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            InfoCard {
                InfoRow(label = stringResource(R.string.club_number), value = club.clubsNr)
                InfoRow(label = stringResource(R.string.email), value = club.email)
                InfoRow(label = stringResource(R.string.phone), value = club.phone ?: "")
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard {
                InfoRow(label = stringResource(R.string.address), value = club.addressStreet)
                InfoRow(label = "", value = "${club.addressZipcode}  ${club.addressCity}")
                if (!club.addressCountry.isNullOrBlank()) {
                    InfoRow(label = "", value = club.addressCountry)
                }
            }
            if (!club.bankgiro.isNullOrBlank() || !club.postgiro.isNullOrBlank() || club.swish.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoCard {
                    if (!club.bankgiro.isNullOrBlank()) InfoRow(label = stringResource(R.string.bankgiro), value = club.bankgiro)
                    if (!club.postgiro.isNullOrBlank()) InfoRow(label = stringResource(R.string.postgiro), value = club.postgiro)
                    if (club.swish.isNotBlank()) InfoRow(label = stringResource(R.string.swish), value = club.swish)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.35f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = if (label.isNotBlank()) Modifier.weight(0.65f) else Modifier.fillMaxWidth()
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun ClubMemberListTab(members: List<ClubMember>) {
    if (members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
        items(members) { member ->
            MemberItem(member)
            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun MemberItem(member: ClubMember) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = member.fullname ?: "${member.name} ${member.lastname ?: ""}".trim(),
                style = MaterialTheme.typography.titleSmall
            )
            if (!member.userHasRole.isNullOrBlank()) {
                Text(
                    text = member.userHasRole,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!member.email.isNullOrBlank()) {
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!member.phone.isNullOrBlank() || !member.mobile.isNullOrBlank()) {
                Text(
                    text = (member.phone ?: member.mobile) ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
