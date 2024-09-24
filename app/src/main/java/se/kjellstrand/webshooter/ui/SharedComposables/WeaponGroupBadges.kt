package se.kjellstrand.webshooter.ui.SharedComposables

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup
import se.kjellstrand.webshooter.data.competitions.remote.Weapongroup

@Composable
fun WeaponGroupBadges(
    weaponGroups: List<Weapongroup>,
    userSignups: List<Usersignup>
) {
    val userSignedUpForWeaponclassesIDs = userSignups.map { it.weaponclassesID }.toSet()
    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        weaponGroups.forEach { weaponGroup ->
            val isHighlighted = weaponGroup.id in userSignedUpForWeaponclassesIDs
            WeaponGroupBadge(
                weaponGroup = weaponGroup,
                isHighlighted = isHighlighted
            )
        }
    }
}

@Composable
fun WeaponGroupBadge(weaponGroup: Weapongroup, isHighlighted: Boolean) {
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
        modifier = Modifier.padding(end = 4.dp).then(borderModifier)
    ) {
        // Do not remove ? below, it will crash the app with a null pointer exception.
        // java.lang.NullPointerException: Attempt to invoke virtual method 'java.lang.String se.kjellstrand.webshooter.data.competitions.remote.ClassnameGeneral.getValue()' on a null object reference
        // 	at se.kjellstrand.webshooter.ui.SharedComposables.WeaponGroupBadgesKt$WeaponGroupBadge$1.invoke(WeaponGroupBadges.kt:61)
        weaponGroup.name?.let { weaponGroupName ->
            Text(
                text = weaponGroupName.value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = fontWeight
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}