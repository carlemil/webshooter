package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.ui.screens.charts.seriespoints.WeaponClassGroup

@Composable
fun WeaponClassGroupFilter(
    availableGroups: Set<WeaponClassGroup>,
    selectedGroup: WeaponClassGroup?,
    onSelectGroup: (WeaponClassGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        availableGroups.sortedBy { it.ordinal }.forEach { group ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(
                    selected = group == selectedGroup,
                    onClick = { onSelectGroup(group) },
                    role = Role.RadioButton
                )
            ) {
                RadioButton(
                    selected = group == selectedGroup,
                    onClick = null
                )
                Text(
                    text = "${group.prefix}*",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
