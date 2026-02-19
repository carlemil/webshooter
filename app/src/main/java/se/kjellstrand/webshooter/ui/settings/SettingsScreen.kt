package se.kjellstrand.webshooter.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import se.kjellstrand.webshooter.data.settings.remote.UserProfile

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = uiState.selectedTab.ordinal) {
            Tab(
                selected = uiState.selectedTab == SettingsTab.PROFILE,
                onClick = { viewModel.setTab(SettingsTab.PROFILE) },
                text = { Text("Profile") }
            )
            Tab(
                selected = uiState.selectedTab == SettingsTab.PASSWORD,
                onClick = { viewModel.setTab(SettingsTab.PASSWORD) },
                text = { Text("Password") }
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

@Composable
private fun ProfileTab(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.errorMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (uiState.isEditMode) {
            EditProfileContent(uiState, viewModel)
        } else {
            ViewProfileContent(uiState.profile, onEditClick = { viewModel.setEditMode(true) })
        }
    }
}

@Composable
private fun ViewProfileContent(profile: UserProfile?, onEditClick: () -> Unit) {
    if (profile == null) {
        Text("No profile data available.")
        return
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Personal information", style = MaterialTheme.typography.titleMedium)
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit profile")
        }
    }

    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

    ProfileInfoRow("Name", "${profile.name} ${profile.lastname}")
    ProfileInfoRow("Email", profile.email)
    ProfileInfoRow("Mobile", profile.mobile ?: "-")
    ProfileInfoRow("Phone", profile.phone ?: "-")
    ProfileInfoRow("Gender", profile.gender?.replaceFirstChar { it.uppercase() } ?: "-")
    ProfileInfoRow("Birth year", profile.birthday?.substringBefore("-") ?: "-")
    ProfileInfoRow("Shooting card no.", profile.shootingCardNumber ?: "-")
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileContent(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Text("Edit profile", style = MaterialTheme.typography.titleMedium)
    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = uiState.editName,
        onValueChange = viewModel::onNameChange,
        label = { Text("First name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.editLastname,
        onValueChange = viewModel::onLastnameChange,
        label = { Text("Last name") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.editEmail,
        onValueChange = viewModel::onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.editMobile,
        onValueChange = viewModel::onMobileChange,
        label = { Text("Mobile phone") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.editPhone,
        onValueChange = viewModel::onPhoneChange,
        label = { Text("Home phone") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))

    GenderDropdown(
        selected = uiState.editGender,
        onSelect = viewModel::onGenderChange
    )
    Spacer(modifier = Modifier.height(8.dp))

    BirthYearDropdown(
        selected = uiState.editBirthday,
        onSelect = viewModel::onBirthdayChange
    )
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.editShootingCardNumber,
        onValueChange = viewModel::onShootingCardNumberChange,
        label = { Text("Shooting card number") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(16.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { viewModel.saveProfile() },
            modifier = Modifier.weight(1f)
        ) {
            Text("Save")
        }
        OutlinedButton(
            onClick = { viewModel.setEditMode(false) },
            modifier = Modifier.weight(1f)
        ) {
            Text("Cancel")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(selected: String, onSelect: (String) -> Unit) {
    val genders = listOf("" to "Select gender", "male" to "Male", "female" to "Female")
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = genders.find { it.first == selected }?.second ?: "Select gender"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Gender") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            genders.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthYearDropdown(selected: Int?, onSelect: (Int) -> Unit) {
    val currentYear = 2026
    val years = (currentYear downTo 1916).toList()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.toString() ?: "Select birth year",
            onValueChange = {},
            readOnly = true,
            label = { Text("Birth year") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = { Text(year.toString()) },
                    onClick = {
                        onSelect(year)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PasswordTab(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Change password", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.errorMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        PasswordField(
            value = uiState.currentPassword,
            onValueChange = viewModel::onCurrentPasswordChange,
            label = "Current password"
        )
        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = uiState.newPassword,
            onValueChange = viewModel::onNewPasswordChange,
            label = "New password"
        )
        Spacer(modifier = Modifier.height(8.dp))

        PasswordField(
            value = uiState.confirmPassword,
            onValueChange = viewModel::onConfirmPasswordChange,
            label = "Confirm new password"
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.updatePassword() },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.currentPassword.isNotEmpty() &&
                    uiState.newPassword.isNotEmpty() &&
                    uiState.confirmPassword.isNotEmpty() &&
                    !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Update password")
            }
        }
    }
}

@Composable
private fun PasswordField(value: String, onValueChange: (String) -> Unit, label: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}
