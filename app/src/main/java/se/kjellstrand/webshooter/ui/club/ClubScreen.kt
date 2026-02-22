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
import se.kjellstrand.webshooter.data.club.remote.ClubData
import se.kjellstrand.webshooter.data.club.remote.ClubMember

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
            ClubTab.INFORMATION -> ClubInformationTab(uiState.clubData)
            ClubTab.ADMINS -> ClubMemberListTab(uiState.admins)
            ClubTab.USERS -> ClubMemberListTab(uiState.users)
        }
    }
}

@Composable
private fun ClubInformationTab(club: ClubData?) {
    if (club == null) return

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(text = club.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            InfoCard {
                if (!club.clubsNr.isNullOrBlank()) InfoRow(stringResource(R.string.club_number), club.clubsNr)
                if (!club.email.isNullOrBlank()) InfoRow(stringResource(R.string.email), club.email)
                if (!club.phone.isNullOrBlank() && club.phone != "null") InfoRow(stringResource(R.string.phone), club.phone)
            }
            Spacer(modifier = Modifier.height(8.dp))
            InfoCard {
                val street = club.addressStreet?.takeIf { it != "null" } ?: ""
                val zip = club.addressZipcode?.takeIf { it != "null" } ?: ""
                val city = club.addressCity?.takeIf { it != "null" } ?: ""
                val country = club.addressCountry?.takeIf { it != "null" } ?: ""
                if (street.isNotBlank()) InfoRow(stringResource(R.string.address), street)
                if (zip.isNotBlank() || city.isNotBlank()) InfoRow("", "$zip  $city".trim())
                if (country.isNotBlank()) InfoRow("", country)
            }
            val hasBankgiro = !club.bankgiro.isNullOrBlank() && club.bankgiro != "null"
            val hasPostgiro = !club.postgiro.isNullOrBlank() && club.postgiro != "null"
            val hasSwish = !club.swish.isNullOrBlank()
            if (hasBankgiro || hasPostgiro || hasSwish) {
                Spacer(modifier = Modifier.height(8.dp))
                InfoCard {
                    if (hasBankgiro) InfoRow(stringResource(R.string.bankgiro), club.bankgiro!!)
                    if (hasPostgiro) InfoRow(stringResource(R.string.postgiro), club.postgiro!!)
                    if (hasSwish) InfoRow(stringResource(R.string.swish), club.swish!!)
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (label.isNotBlank()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.35f)
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(0.65f)
                )
            } else {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
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
            if (!member.email.isNullOrBlank()) {
                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!member.shootingCardNumber.isNullOrBlank()) {
                Text(
                    text = member.shootingCardNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
