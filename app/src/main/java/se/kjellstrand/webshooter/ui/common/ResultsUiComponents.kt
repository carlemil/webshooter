package se.kjellstrand.webshooter.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.Result

object ResultsUiComponents {

    @Composable
    fun ResultsListHeader(isGrouped: Boolean, resultsType: ResultsType) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderText(
                R.string.placement, modifier = Modifier.weight(2f)
            )
            HeaderText(R.string.name, modifier = Modifier.weight(10f))

            Row(
                modifier = Modifier.weight(if (isGrouped) 8f else 9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isGrouped) {
                    HeaderText(
                        R.string.weapon_class_short,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    )
                }
                HeaderText(R.string.medal_short, modifier = Modifier.weight(1f))
                when (resultsType) {
                    ResultsType.FIELD -> {
                        HeaderText(R.string.hits_short, modifier = Modifier.weight(1f))
                        HeaderText(R.string.figures_short, modifier = Modifier.weight(1f))
                        HeaderText(R.string.points_short, modifier = Modifier.weight(1f))
                    }

                    ResultsType.PRECISION,
                    ResultsType.MILITARY -> {
                        HeaderText(R.string.points_short, modifier = Modifier.weight(1f))
                        HeaderText(R.string.x, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    @Composable
    fun HeaderText(
        stringRes: Int,
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
    fun ResultItem(
        result: Result,
        index: Int,
        isGrouped: Boolean,
        resultsType: ResultsType = ResultsType.FIELD,
        loggedInUserId: Long = -1L,
        onItemClick: () -> Unit
    ) {
        val isCurrentUser = result.signup.user.userID == loggedInUserId
        val itemStyle = if (isCurrentUser) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        else MaterialTheme.typography.bodySmall
        val backgroundColor = if (index % 2 == 0) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .clickable(onClick = onItemClick)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemText(
                text = result.placement.toString(),
                style = MaterialTheme.typography.labelLarge.let {
                    if (isCurrentUser) it.copy(fontWeight = FontWeight.Bold) else it
                },
                modifier = Modifier.weight(2f)
            )

            Column(
                modifier = Modifier.weight(10f)
            ) {
                ItemText(
                    text = "${result.signup.user.name} ${result.signup.user.lastname}",
                    style = itemStyle,
                    overflow = TextOverflow.Ellipsis
                )
                ItemText(
                    text = result.signup.club?.name ?: stringResource(R.string.unknown_club),
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.weight(if (isGrouped) 8f else 9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isGrouped) {
                    ItemText(
                        text = result.weaponClass.classname,
                        style = itemStyle,
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    )
                }
                ItemText(text = result.stdMedal?.value ?: "-", style = itemStyle, modifier = Modifier.weight(1f))
                when (resultsType) {
                    ResultsType.FIELD -> {
                        ItemText(text = result.hits.toString(), style = itemStyle, modifier = Modifier.weight(1f))
                        ItemText(text = result.figureHits.toString(), style = itemStyle, modifier = Modifier.weight(1f))
                        ItemText(text = result.points.toString(), style = itemStyle, modifier = Modifier.weight(1f))
                    }

                    ResultsType.PRECISION,
                    ResultsType.MILITARY -> {
                        ItemText(text = result.points.toString(), style = itemStyle, modifier = Modifier.weight(1f))
                        ItemText(text = result.hits.toString(), style = itemStyle, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
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
}