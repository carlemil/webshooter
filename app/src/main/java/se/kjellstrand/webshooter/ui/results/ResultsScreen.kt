package se.kjellstrand.webshooter.ui.results

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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.ui.mock.ResultsViewModelMock

@Composable
fun CompetitionResultsScreen(
    resultsViewModel: ResultsViewModel
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()

    var currentMode by remember { mutableStateOf(Mode.GROUP) }
    var filterState by remember { mutableStateOf(FilterState()) }

    Scaffold(
        modifier = Modifier.padding(16.dp),
        floatingActionButton = {
            ModeSwitcher(
                currentMode = currentMode,
                onModeChange = { mode -> currentMode = mode }
            )
        }
    ) { paddingValues ->
        // Main content goes here
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Conditionally show filter options
            if (currentMode == Mode.FILTER) {
                FilterOptions(
                    filterState = filterState,
                    onFilterChange = { newState -> filterState = newState }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (currentMode == Mode.GROUP) {
                StandardGroupingLayout(resultsUiState)
            } else {
                FilterLayout(resultsUiState)
            }
        }
    }
}

@Composable
fun FilterAndSwitch(
    resultsViewModel: ResultsViewModel
) {
    val resultsUiState by resultsViewModel.uiState.collectAsState()

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "FILTER")
        Switch(
            checked = resultsUiState.mode == Mode.GROUP,
            onCheckedChange = { isChecked ->
                resultsViewModel.setLayoutType(if (isChecked) Mode.GROUP else Mode.FILTER)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Green,
                uncheckedThumbColor = Color.Red
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Text(text = "GROUP")
    }
}

@Composable
fun StandardGroupingLayout(
    resultsUiState: ResultsUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            if (resultsUiState.groupedResults.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                resultsUiState.groupedResults.forEach { group ->
                    item {
                        Text(
                            text = "Vapengrupp: " + group.header,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
                        )
                    }
                    // Display the results for each group
                    items(group.items) { result ->
                        ResultItem(resultsUiState, result = result)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    resultsUiState: ResultsUiState,
    result: Result
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = result.placement.toString(),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(2f)
            )
            Column(
                modifier = Modifier.weight(16f)
            ) {
                Text(
                    text = "${result.signup.user.name} ${result.signup.user.lastname}",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = result.signup.club?.name ?: "Unknown club",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (resultsUiState.mode == Mode.FILTER) {
                Text(
                    text = result.weaponClass.classname,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(2f)
                )
            }
            Box(
                modifier = Modifier
                    .wrapContentWidth(Alignment.End)
                    .weight(6f)
            ) {
                Text(
                    text = "T:${result.hits} F:${result.figureHits} P:${result.points}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun FilterLayout(
    resultsUiState: ResultsUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp,
                start = 0.dp,
                end = 0.dp,
                bottom = 16.dp
            )
        ) {
            if (resultsUiState.groupedResults.isEmpty()) {
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
            } else {
                items(resultsUiState.results) { result ->
                    ResultItem(resultsUiState, result = result)
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }
        }
    }
}

@Composable
fun ModeSwitcher(
    currentMode: Mode,
    onModeChange: (Mode) -> Unit
) {
    // Determine the icon and label based on the current mode
    val icon = if (currentMode == Mode.GROUP) Icons.Default.FilterList else Icons.Default.Group
    val contentDescription = if (currentMode == Mode.GROUP) "Switch to Filter Mode" else "Switch to Group Mode"

    // Floating Action Button
    FloatingActionButton(
        onClick = {
            // Toggle the mode when FAB is clicked
            val newMode = if (currentMode == Mode.GROUP) Mode.FILTER else Mode.GROUP
            onModeChange(newMode)
        },
        content = {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    )
}

@Composable
fun FilterOptions(
    filterState: FilterState,
    onFilterChange: (FilterState) -> Unit
) {
    Column {
        Text("Filter Options:")
        Checkbox(
            checked = filterState.option1,
            onCheckedChange = { onFilterChange(filterState.copy(option1 = it)) }
        )
        Text("Option 1")
        Checkbox(
            checked = filterState.option2,
            onCheckedChange = { onFilterChange(filterState.copy(option2 = it)) }
        )
        Text("Option 2")
        // Repeat for other options...
    }
}

@Preview(showBackground = true)
@Composable
fun ResultsScreenPreview() {
    CompetitionResultsScreen(
        resultsViewModel = ResultsViewModelMock()
    )
}
