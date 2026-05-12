package se.kjellstrand.webshooter.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import se.kjellstrand.webshooter.ui.common.ScreenTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.mock.MockCompetitions
import se.kjellstrand.webshooter.ui.mock.SignupViewModelMock
import se.kjellstrand.webshooter.resources.*
import se.kjellstrand.webshooter.ui.common.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    competition: Datum,
    viewModel: SignupViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            ScreenTopBar(
                title = competition.name,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.navigate_back))
                    }
                }
            )
        }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp)
            .padding(top = Dimens.ScreenContentTopPadding)
    ) {
        Text(
            text = "${competition.date}  •  ${competition.statusHuman}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (competition.userSignups.isNotEmpty()) {
            Text(
                text = stringResource(Res.string.signup_current_signups),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            competition.userSignups.forEach { signup ->
                val weaponClassName = competition.weaponClasses
                    .find { it.id == signup.weaponClassesID }?.classname ?: signup.weaponClassesID.toString()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = weaponClassName, style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = { viewModel.removeSignup(signup.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        Text(stringResource(Res.string.signup_remove_signup))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
        }

        WeaponClassDropdown(
            weaponClasses = competition.weaponClasses,
            selectedId = uiState.selectedWeaponClassId,
            onSelected = { viewModel.selectWeaponClass(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.updateNote(it) },
            label = { Text(stringResource(Res.string.signup_note)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        uiState.error?.let { errorText ->
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Button(
                onClick = { viewModel.submit() },
                enabled = uiState.selectedWeaponClassId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.sign_up))
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeaponClassDropdown(
    weaponClasses: List<WeaponClass>,
    selectedId: Long?,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedClass = weaponClasses.find { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedClass?.classname ?: stringResource(Res.string.signup_select_weapon_class),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.weapon_class)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            weaponClasses.forEach { weaponClass ->
                DropdownMenuItem(
                    text = { Text(weaponClass.classname) },
                    onClick = {
                        onSelected(weaponClass.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Signup - Default")
@Composable
fun SignupScreenPreview() {
    SignupScreen(
        competition = MockCompetitions().competitions.data.first(),
        viewModel = SignupViewModelMock(),
        onBack = {}
    )
}

@Preview(showBackground = true, name = "Signup - Loading")
@Composable
fun SignupScreenLoadingPreview() {
    SignupScreen(
        competition = MockCompetitions().competitions.data.first(),
        viewModel = SignupViewModelMock(SignupUiState(isLoading = true)),
        onBack = {}
    )
}

@Preview(showBackground = true, name = "Signup - Error")
@Composable
fun SignupScreenErrorPreview() {
    SignupScreen(
        competition = MockCompetitions().competitions.data.first(),
        viewModel = SignupViewModelMock(SignupUiState(error = "Registration failed")),
        onBack = {}
    )
}
