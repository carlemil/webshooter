package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
    onSelectGroup: (WeaponClassGroup?) -> Unit,
    modifier: Modifier = Modifier,
    showAllOption: Boolean = false
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAllOption) {
            GroupRadio(
                label = "Alla",
                selected = selectedGroup == null,
                onClick = { onSelectGroup(null) }
            )
        }
        availableGroups.sortedBy { it.ordinal }.forEach { group ->
            // Single-char prefixes (A, B, C, R) get a trailing "*" because they
            // match any class starting with that letter; multi-char prefixes
            // (M1..M9) name a single class and read better without the star.
            val label = if (group.prefix.length == 1) "${group.prefix}*" else group.prefix
            GroupRadio(
                label = label,
                selected = group == selectedGroup,
                onClick = { onSelectGroup(group) }
            )
        }
    }
}

@Composable
private fun GroupRadio(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onClick,
            role = Role.RadioButton
        )
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
