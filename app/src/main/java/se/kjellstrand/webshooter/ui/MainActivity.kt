package se.kjellstrand.webshooter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import se.kjellstrand.webshooter.ui.navigation.AppNavHost
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            WebShooterTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}