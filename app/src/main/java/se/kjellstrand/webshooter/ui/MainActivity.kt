package se.kjellstrand.webshooter.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import se.kjellstrand.webshooter.data.competitions.remote.CompetitionsResponse
import se.kjellstrand.webshooter.data.login.LoginRepository
import se.kjellstrand.webshooter.ui.theme.WebShooterTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var loginRepository: LoginRepository

    @Inject
    lateinit var competitionsRepository: CompetitionsRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WebShooterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row {

                        Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                        )

                        LoginButton(onClick = {
                            executeLogin(
                                "erbsman@gmail.com",
                                "erbsman@gmail.com",
                                "6nRgbXK3urbFsyi"
                            )
                        })
                    }
                }
            }
        }
    }

    // Sample usage in a non-UI coroutine, such as in a ViewModel or repository
    fun executeLogin(email: String, username: String, password: String) {
        runBlocking {
            launch {
                loginRepository.login(email, username, password).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            // Handle successful login
                            println("Login Success: ${resource.data}")
                        }

                        is Resource.Error -> {
                            // Handle error
                            println("Login Failed: ${resource.error}")
                        }
                        else -> {
                            // Handle other states
                            println("Login other state")
                        }
                    }
                }
                competitionsRepository.get().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            // Handle successful login
                            println("getCompetitions Success: ${resource.data}")
                        }

                        is Resource.Error -> {
                            // Handle error
                            println("getCompetitions Failed: ${resource.error}")
                        }

                        else -> {
                            // Handle other states
                            println("getCompetitions other state")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoginButton(onClick: () -> Unit) {
    Button(
        onClick = { onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = "Login")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WebShooterTheme {
        Greeting("Android")
    }
}