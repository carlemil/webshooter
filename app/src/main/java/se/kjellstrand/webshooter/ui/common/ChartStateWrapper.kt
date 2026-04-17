package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import se.kjellstrand.webshooter.R

@Composable
fun ColumnScope.ChartStateWrapper(
    isLoading: Boolean,
    hasError: Boolean,
    isEmpty: Boolean = false,
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable ColumnScope.() -> Unit,
) {
    when {
        isLoading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        hasError -> Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.competitions_load_error),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        isEmpty -> Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.charts_no_data),
                style = MaterialTheme.typography.bodyLarge
            )
        }

        else -> content()
    }
}
