package se.kjellstrand.webshooter.ui.screens.settings

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import se.kjellstrand.webshooter.ui.mock.MockSettings
import se.kjellstrand.webshooter.ui.mock.SettingsViewModelMock
import se.kjellstrand.webshooter.data.settings.remote.UserProfile
import se.kjellstrand.webshooter.resources.*

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel<SettingsViewModelImpl>()
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
                text = { Text(stringResource(Res.string.settings_profile)) }
            )
            Tab(
                selected = uiState.selectedTab == SettingsTab.PASSWORD,
                onClick = { viewModel.setTab(SettingsTab.PASSWORD) },
                text = { Text(stringResource(Res.string.password)) }
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
            ViewProfileContent(
                uiState.profile,
                onEditClick = { viewModel.setEditMode(true) },
                onLogoutClick = { viewModel.logout() }
            )
        }
    }
}

@Composable
private fun ViewProfileContent(profile: UserProfile?, onEditClick: () -> Unit, onLogoutClick: () -> Unit) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            text = { Text(stringResource(Res.string.logga_out_confirm)) },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogoutClick() },
                ) { Text(stringResource(Res.string.yes)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text(stringResource(Res.string.no)) }
            }
        )
    }

    if (profile == null) {
        Text(stringResource(Res.string.settings_no_profile_data))
        return
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.settings_personal_information), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(Res.string.settings_edit_profile))
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            ProfileInfoRow(stringResource(Res.string.name), "${profile.name} ${profile.lastname}")
            ProfileInfoRow(stringResource(Res.string.email), profile.email)
            ProfileInfoRow(stringResource(Res.string.settings_mobile), profile.mobile ?: stringResource(Res.string.dash))
            ProfileInfoRow(stringResource(Res.string.phone), profile.phone ?: stringResource(Res.string.dash))
            val genderEnum = Gender.fromApiValue(profile.gender)
            ProfileInfoRow(stringResource(Res.string.settings_gender), if (genderEnum == Gender.UNSET) stringResource(Res.string.dash) else stringResource(genderLabelRes(genderEnum)))
            ProfileInfoRow(stringResource(Res.string.settings_birth_year), profile.birthday?.substringBefore("-") ?: stringResource(Res.string.dash))
            ProfileInfoRow(stringResource(Res.string.settings_shooting_card_no), profile.shootingCardNumber ?: stringResource(Res.string.dash))
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    Button(
        onClick = { showLogoutDialog = true },
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Text(stringResource(Res.string.settings_logout))
    }
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
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(stringResource(Res.string.settings_edit_profile), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.editName,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(Res.string.settings_first_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.editLastname,
                onValueChange = viewModel::onLastnameChange,
                label = { Text(stringResource(Res.string.settings_last_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.editEmail,
                onValueChange = viewModel::onEmailChange,
                label = { Text(stringResource(Res.string.email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.editMobile,
                onValueChange = viewModel::onMobileChange,
                label = { Text(stringResource(Res.string.settings_mobile_phone)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.editPhone,
                onValueChange = viewModel::onPhoneChange,
                label = { Text(stringResource(Res.string.settings_home_phone)) },
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
                label = { Text(stringResource(Res.string.settings_shooting_card_number_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.settings_save))
                }
                OutlinedButton(
                    onClick = { viewModel.setEditMode(false) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.settings_cancel))
                }
            }
        }
    }
}

private fun genderLabelRes(gender: Gender): org.jetbrains.compose.resources.StringResource = when (gender) {
    Gender.UNSET -> Res.string.select_gender
    Gender.MALE -> Res.string.male
    Gender.FEMALE -> Res.string.female
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(selected: Gender, onSelect: (Gender) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = stringResource(genderLabelRes(selected)),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.settings_gender)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Gender.entries.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(stringResource(genderLabelRes(gender))) },
                    onClick = {
                        onSelect(gender)
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
    val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    val years = (currentYear downTo 1916).toList()
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.toString() ?: stringResource(Res.string.settings_select_birth_year),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.settings_birth_year)) },
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
        uiState.successMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
        }
        uiState.errorMessage?.let { msg ->
            Text(text = msg, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(Res.string.settings_change_password), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                PasswordField(
                    value = uiState.currentPassword,
                    onValueChange = viewModel::onCurrentPasswordChange,
                    label = stringResource(Res.string.settings_current_password)
                )
                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = uiState.newPassword,
                    onValueChange = viewModel::onNewPasswordChange,
                    label = stringResource(Res.string.settings_new_password)
                )
                Spacer(modifier = Modifier.height(8.dp))

                PasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    label = stringResource(Res.string.settings_confirm_new_password)
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
                        Text(stringResource(Res.string.settings_update_password))
                    }
                }
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
                    contentDescription = stringResource(Res.string.toggle_password_visibility)
                )
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(viewModel = SettingsViewModelMock())
}

@Preview
@Composable
fun SettingsScreenLoadingPreview() {
    SettingsScreen(viewModel = SettingsViewModelMock(SettingsUiState(isLoading = true)))
}

@Preview
@Composable
fun SettingsScreenEditPreview() {
    SettingsScreen(viewModel = SettingsViewModelMock(SettingsUiState(
        profile = MockSettings().userProfile,
        isEditMode = true,
        editName = "Erik",
        editLastname = "Svensson",
        editEmail = "erik@example.se"
    )))
}

@Preview
@Composable
fun SettingsScreenPasswordPreview() {
    SettingsScreen(viewModel = SettingsViewModelMock(SettingsUiState(
        profile = MockSettings().userProfile,
        selectedTab = SettingsTab.PASSWORD
    )))
}

@Preview
@Composable
fun SettingsScreenErrorPreview() {
    SettingsScreen(viewModel = SettingsViewModelMock(SettingsUiState(
        profile = MockSettings().userProfile,
        errorMessage = "Failed to save profile"
    )))
}
