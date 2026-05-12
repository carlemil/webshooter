package se.kjellstrand.webshooter.ui.screens.club

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.club.remote.ClubData
import se.kjellstrand.webshooter.data.club.remote.ClubMember
import se.kjellstrand.webshooter.ui.mock.ClubViewModelMock
import se.kjellstrand.webshooter.resources.*

@Composable
fun ClubScreen(viewModel: ClubViewModel = koinViewModel<ClubViewModelImpl>()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            Tab(
                selected = uiState.selectedTab == ClubTab.INFORMATION,
                onClick = { viewModel.selectTab(ClubTab.INFORMATION) },
                text = { Text(stringResource(Res.string.club_club_tab_information)) }
            )
            Tab(
                selected = uiState.selectedTab == ClubTab.ADMINS,
                onClick = { viewModel.selectTab(ClubTab.ADMINS) },
                text = { Text(stringResource(Res.string.club_club_tab_admins)) }
            )
            Tab(
                selected = uiState.selectedTab == ClubTab.USERS,
                onClick = { viewModel.selectTab(ClubTab.USERS) },
                text = { Text(stringResource(Res.string.club_club_tab_users)) }
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
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(text = club.name, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            InfoCard {
                club.clubsNr?.takeIf { it.isNotBlank() }?.let { clubsNr ->
                    InfoRow(stringResource(Res.string.club_club_number), clubsNr)
                }
                club.email?.takeIf { it.isNotBlank() }?.let { email ->
                    InfoRow(
                        label = stringResource(Res.string.email),
                        value = email,
                        onClick = { clipboardManager.setText(AnnotatedString(email)) }
                    )
                }
                club.phone?.takeIf { it.isNotBlank() }?.let { phone ->
                    InfoRow(stringResource(Res.string.phone), phone)
                }

                val street = club.addressStreet ?: ""
                val zip = club.addressZipcode ?: ""
                val city = club.addressCity ?: ""
                val country = club.addressCountry ?: ""
                if (street.isNotBlank()) InfoRow(
                    stringResource(Res.string.club_address),
                    "$street, $zip, $city".trim()
                )
                if (country.isNotBlank()) InfoRow("", country)

                val hasBankgiro = !club.bankgiro.isNullOrBlank()
                val hasPostgiro = !club.postgiro.isNullOrBlank()
                val hasSwish = !club.swish.isNullOrBlank()
                if (hasBankgiro || hasPostgiro || hasSwish) {
                    if (hasBankgiro) InfoRow(
                        stringResource(Res.string.club_bankgiro),
                        club.bankgiro!!
                    )
                    if (hasPostgiro) InfoRow(
                        stringResource(Res.string.club_postgiro),
                        club.postgiro!!
                    )
                    if (hasSwish) InfoRow(stringResource(Res.string.club_swish), club.swish!!)

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
private fun InfoRow(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
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
    }
}

@Composable
private fun ClubMemberListTab(members: List<ClubMember>) {
    if (members.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.club_no_members))
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

@Preview(showBackground = true, name = "Club - Information")
@Composable
fun ClubScreenPreview() {
    ClubScreen(viewModel = ClubViewModelMock())
}

@Preview(showBackground = true, name = "Club - Loading")
@Composable
fun ClubScreenLoadingPreview() {
    ClubScreen(viewModel = ClubViewModelMock(ClubUiState(isLoading = true)))
}

@Preview(showBackground = true, name = "Club - Admins")
@Composable
fun ClubScreenAdminsPreview() {
    ClubScreen(viewModel = ClubViewModelMock(
        ClubUiState(clubData = se.kjellstrand.webshooter.ui.mock.MockClub().clubData, selectedTab = ClubTab.ADMINS)
    ))
}

@Preview(showBackground = true, name = "Club - Users")
@Composable
fun ClubScreenUsersPreview() {
    ClubScreen(viewModel = ClubViewModelMock(
        ClubUiState(clubData = se.kjellstrand.webshooter.ui.mock.MockClub().clubData, selectedTab = ClubTab.USERS)
    ))
}

@Preview(showBackground = true, name = "Club - Error")
@Composable
fun ClubScreenErrorPreview() {
    ClubScreen(viewModel = ClubViewModelMock(ClubUiState(error = "NetworkError")))
}

@Composable
private fun MemberItem(member: ClubMember) {
    val clipboardManager = LocalClipboardManager.current
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
            member.email?.takeIf { it.isNotBlank() }?.let { email ->
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        clipboardManager.setText(AnnotatedString(email))
                    }
                )
            }
            member.shootingCardNumber?.takeIf { it.isNotBlank() }?.let { number ->
                Text(
                    text = stringResource(Res.string.shooting_card_number, number),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            member.status?.takeIf { it.isNotBlank() }?.let { status ->
                Text(
                    text = stringResource(Res.string.club_member_status, status),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
