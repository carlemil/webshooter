package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.charts.Participant
import se.kjellstrand.webshooter.data.club.remote.ClubMember

@Composable
fun ShooterPickerDialog(
    title: String,
    searchQuery: String,
    clubMembers: List<ClubMember>,
    allParticipants: List<Participant>,
    selectedShooters: Map<Long, String>,
    onSearchQueryChanged: (String) -> Unit,
    onPickShooter: (Long) -> Unit,
    onRemoveShooter: (Long) -> Unit,
    onDismiss: () -> Unit,
    showSelectedSection: Boolean = true
) {
    val filtered = remember(searchQuery, clubMembers, allParticipants, selectedShooters) {
        val base = if (searchQuery.isBlank()) {
            clubMembers
        } else {
            val q = searchQuery.lowercase()
            allParticipants
                .filter { it.fullname.lowercase().contains(q) }
                .map { ClubMember(userId = it.userId, name = it.fullname, fullname = it.fullname) }
        }
        base.filter { !selectedShooters.containsKey(it.userId) }
            .sortedBy { (it.fullname ?: it.name).lowercase() }
    }

    val halfDialog = LocalConfiguration.current.screenHeightDp.dp / 2

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(modifier = Modifier.fillMaxHeight()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChanged,
                    label = { Text(stringResource(R.string.charts_search_shooter)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showSelectedSection && selectedShooters.isNotEmpty()) {
                    Text(
                        text = "${selectedShooters.size} valda",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = halfDialog)
                    ) {
                        items(selectedShooters.toList()) { (userId, name) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.charts_remove),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { onRemoveShooter(userId) }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = halfDialog)
                ) {
                    items(filtered) { member ->
                        Text(
                            text = member.fullname ?: "${member.name} ${member.lastname ?: ""}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPickShooter(member.userId) }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.charts_close))
            }
        }
    )
}
