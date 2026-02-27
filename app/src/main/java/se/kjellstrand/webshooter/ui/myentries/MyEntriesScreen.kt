package se.kjellstrand.webshooter.ui.myentries

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

@Composable
fun MyEntriesScreen(
    viewModel: MyEntriesViewModel = hiltViewModel<MyEntriesViewModelImpl>()
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading && uiState.entries.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        uiState.isFinished && uiState.entries.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.my_entries_no_entries))
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.entries) { entry ->
                    MyEntryItem(entry)
                }
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MyEntryItem(entry: Datum) {
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
                text = entry.name,
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${entry.date}  •  ${entry.statusHuman}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (entry.userSignups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                entry.userSignups.forEach { signup ->
                    SignupRow(signup, entry)
                    if (signup != entry.userSignups.last()) {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SignupRow(signup: Usersignup, entry: Datum) {
    val weaponClass = entry.weaponClasses.find { it.id == signup.weaponClassesID }
    val className = weaponClass?.classname ?: signup.weaponClassesID.toString()

    Column {
        Text(
            text = stringResource(R.string.my_entries_weapon_class, className),
            style = MaterialTheme.typography.bodySmall
        )
        if (signup.startTimeHuman.isNotBlank() && signup.startTimeHuman != "01:00") {
            Text(
                text = stringResource(R.string.my_entries_start_time, signup.startTimeHuman),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (signup.lane > 0) {
            Text(
                text = stringResource(R.string.my_entries_lane, signup.lane),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!signup.note.isNullOrBlank()) {
            Text(
                text = stringResource(R.string.my_entries_note, signup.note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
