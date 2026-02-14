package se.kjellstrand.webshooter.ui.shooterresult

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.ui.results.ResultItem

@Composable
fun ShooterResultScreen(
    viewModel: ShooterResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Log.d("ShooterResultScreen", "uiState.isLoading: ${uiState.isLoading}")
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Text(text = "Error: ${uiState.error}")
        } else {
            Text(text = uiState.shooterName, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(uiState.results) { result ->
                    ResultItem(result = result, index = 0, isGrouped = false, onItemClick = {})
                }
            }
        }
    }
}