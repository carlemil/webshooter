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
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
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
                    modifier = Modifier.padding(2.dp),
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
    val textStyle: TextStyle
    val horizontalPadding: Dp
    val verticalPadding: Dp
    when (size) {
        WeaponClassBadgeSize.Small -> {
            textStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Normal)
            horizontalPadding = 6.dp
            verticalPadding = 2.dp
        }

        WeaponClassBadgeSize.Medium -> {
            textStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal)
            horizontalPadding = 8.dp
            verticalPadding = 3.dp
        }

        WeaponClassBadgeSize.Large -> {
            textStyle = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Normal)
            horizontalPadding = 12.dp
            verticalPadding = 4.dp
        }
    }

    val borderWidth = 1.dp
    val shape = RoundedCornerShape(integerResource(R.integer.rounded_corner_shape_percent))
    val outlineModifier = Modifier.border(
        width = borderWidth,
        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
        shape = shape
    )

    Surface(
        color = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        shape = shape,
        modifier = modifier
            .then(outlineModifier)
    ) {
        Text(
            text = weaponGroupName,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isHighlighted) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}
