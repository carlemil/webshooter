package se.kjellstrand.webshooter.ui.signup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.competitions.remote.Datum

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    competition: Datum,
    viewModel: SignupViewModel,
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isSuccess) {
        navController.popBackStack()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = competition.name,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${competition.date}  •  ${competition.statusHuman}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))

        WeaponClassDropdown(
            weaponClasses = competition.weaponClasses,
            selectedId = uiState.selectedWeaponClassId,
            onSelected = { viewModel.selectWeaponClass(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.note,
            onValueChange = { viewModel.updateNote(it) },
            label = { Text(stringResource(R.string.note)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.submit() },
                enabled = uiState.selectedWeaponClassId != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sign_up))
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
            value = selectedClass?.classname ?: stringResource(R.string.select_weapon_class),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.weapon_class)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
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
