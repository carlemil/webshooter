package se.kjellstrand.webshooter.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.ui.common.UiEvent
import se.kjellstrand.webshooter.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = hiltViewModel<LoginViewModelImpl>()
) {
    val uiState by loginViewModel.uiState.collectAsState()
    val eventFlow = loginViewModel.eventFlow

    var username by remember(loginViewModel.savedUsername) { mutableStateOf(loginViewModel.savedUsername) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loginViewModel.getCookies()
    }

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.NavigateToLandingPage -> {
                    navController.navigate(Screen.LandingScreen.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }

                is UiEvent.ShowErrorMessage -> {
                }
            }
        }
    }

    if (!uiState.autoLoginAttempted) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_login),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text(stringResource(R.string.login_username)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Default.Visibility
                    else
                        Icons.Default.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = stringResource(R.string.toggle_password_visibility))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    loginViewModel.login(username, password)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotEmpty() && password.isNotEmpty() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(stringResource(R.string.login_login))
                }
            }
            uiState.errorMessage?.let { errorMessage ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Login - Default")
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreen(
            navController = rememberNavController(),
            loginViewModel = se.kjellstrand.webshooter.ui.mock.LoginViewModelMock()
        )
    }
}

@Preview(showBackground = true, name = "Login - Loading")
@Composable
fun LoginScreenLoadingPreview() {
    MaterialTheme {
        LoginScreen(
            navController = rememberNavController(),
            loginViewModel = se.kjellstrand.webshooter.ui.mock.LoginViewModelMock(
                LoginUiState(isLoading = true, autoLoginAttempted = true)
            )
        )
    }
}

@Preview(showBackground = true, name = "Login - Error")
@Composable
fun LoginScreenErrorPreview() {
    MaterialTheme {
        LoginScreen(
            navController = rememberNavController(),
            loginViewModel = se.kjellstrand.webshooter.ui.mock.LoginViewModelMock(
                LoginUiState(errorMessage = "Invalid credentials", autoLoginAttempted = true)
            )
        )
    }
}
