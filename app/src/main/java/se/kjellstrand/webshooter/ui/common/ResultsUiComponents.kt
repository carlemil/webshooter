package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

object ResultsUiComponents {


    @Composable
    fun HeaderText(
        stringRes: StringResource,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Center,
        style: TextStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
    ) {
        Text(
            text = stringResource(stringRes),
            style = style,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
            modifier = modifier
        )
    }

    @Composable
    fun ItemText(
        text: String,
        modifier: Modifier = Modifier,
        textAlign: TextAlign = TextAlign.Center,
        style: TextStyle = MaterialTheme.typography.bodySmall,
        overflow: TextOverflow = TextOverflow.Clip
    ) {
        Text(
            text = text,
            style = style,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = overflow,
            modifier = modifier
        )
    }

    @Composable
    fun WeaponGroupSeparator(groupName: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            WeaponClassBadge(
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 8.dp),
                weaponGroupName = groupName,
                isHighlighted = false,
                size = WeaponClassBadgeSize.Large
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }
    }
}
