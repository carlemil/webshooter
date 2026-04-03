package se.kjellstrand.webshooter.ui.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.R

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.loggedOut) {
        if (uiState.loggedOut) onLoggedOut()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            Tab(
                selected = uiState.selectedTab == SettingsTab.PROFILE,
                onClick = { viewModel.setTab(SettingsTab.PROFILE) },
                text = { Text(stringResource(R.string.settings_profile)) }
            )
            Tab(
                selected = uiState.selectedTab == SettingsTab.PASSWORD,
                onClick = { viewModel.setTab(SettingsTab.PASSWORD) },
                text = { Text(stringResource(R.string.password)) }
            )
        }

        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            uiState.selectedTab == SettingsTab.PROFILE -> {
                ProfileTab(uiState, viewModel)
            }
            uiState.selectedTab == SettingsTab.PASSWORD -> {
                PasswordTab(uiState, viewModel)
            }
        }
    }
}
