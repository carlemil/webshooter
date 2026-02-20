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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.competitions.remote.Usersignup

enum class WeaponClassBadgeSize { Small, Medium, Large }

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
                    isHighlighted = isHighlighted,
                    size = WeaponClassBadgeSize.Small
                )
            }
        }
    }
}

@Composable
fun WeaponClassBadge(
    modifier: Modifier = Modifier,
    weaponGroupName: String,
    isHighlighted: Boolean,
    size: WeaponClassBadgeSize = WeaponClassBadgeSize.Small
) {
    val shape = RoundedCornerShape(50)
    val fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal

    val textStyle: TextStyle
    val horizontalPadding: Dp
    val verticalPadding: Dp
    when (size) {
        WeaponClassBadgeSize.Small -> {
            textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = fontWeight)
            horizontalPadding = 6.dp
            verticalPadding = 2.dp
        }
        WeaponClassBadgeSize.Medium -> {
            textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = fontWeight)
            horizontalPadding = 8.dp
            verticalPadding = 3.dp
        }
        WeaponClassBadgeSize.Large -> {
            textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = fontWeight)
            horizontalPadding = 12.dp
            verticalPadding = 4.dp
        }
    }

    val outlineModifier = if (isHighlighted) {
        Modifier.border(width = 2.dp, color = MaterialTheme.colorScheme.onPrimaryContainer, shape = shape)
    } else {
        Modifier
    }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = shape,
        modifier = modifier
            .padding(end = 4.dp, bottom = 4.dp)
            .then(outlineModifier)
    ) {
        Text(
            text = weaponGroupName,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}
