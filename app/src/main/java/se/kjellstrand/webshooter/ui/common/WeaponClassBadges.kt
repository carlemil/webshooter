package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeaponClassBadges(
    weaponClasses: List<WeaponClass>,
    userSignups: List<Usersignup>
) {
    val userSignedUpForWeaponClassesIDs = userSignups.map { it.weaponClassesID }.toSet()
    FlowRow {
        weaponClasses.forEach { weaponClass ->
            val isHighlighted = weaponClass.id in userSignedUpForWeaponClassesIDs
            @Suppress("UNNECESSARY_SAFE_CALL")
            weaponClass.classname?.let {
                WeaponClassBadge(
                    weaponGroupName = it,
                    isHighlighted = isHighlighted
                )
            }
        }
    }
}

@Composable
fun WeaponClassBadge(
    modifier: Modifier = Modifier,
    weaponGroupName: String,
    isHighlighted: Boolean
) {
    val borderModifier = if (isHighlighted) {
        Modifier.border(
            width = 0.6.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(4.dp)
        )
    } else {
        Modifier
    }
    val fontWeight = if (isHighlighted) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .then(borderModifier)
    ) {
        Text(
            text = weaponGroupName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 10.sp,
                fontWeight = fontWeight
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}