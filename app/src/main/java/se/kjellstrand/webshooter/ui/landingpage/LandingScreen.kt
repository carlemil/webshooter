package se.kjellstrand.webshooter.ui.landingpage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import se.kjellstrand.webshooter.ui.Screen

@Composable
fun LandingScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to the Landing Screen!",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = {
                    navController.navigate(Screen.CompetitionsList.route)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Competitions")
            }
        }
    }
}
