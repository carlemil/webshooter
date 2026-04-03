package se.kjellstrand.webshooter.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.ResultsType
import se.kjellstrand.webshooter.data.results.remote.Result
import se.kjellstrand.webshooter.data.results.remote.StdMedal
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.HeaderText
import se.kjellstrand.webshooter.ui.common.ResultsUiComponents.ItemText
import se.kjellstrand.webshooter.ui.common.WeaponClassBadge
import se.kjellstrand.webshooter.ui.common.WeaponClassBadgeSize
import se.kjellstrand.webshooter.ui.navigation.Screen
import se.kjellstrand.webshooter.ui.navigation.safeNavigate
import se.kjellstrand.webshooter.ui.theme.appColors

@Composable
fun ResultsList(
    resultsUiState: ResultsUiState,
    competitionId: Long,
    navController: NavController,
    resultsType: ResultsType = ResultsType.FIELD,
    listState: LazyListState = rememberLazyListState(),
    bottomContentPadding: Dp = 0.dp
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = bottomContentPadding + 8.dp)
        ) {
            val noneSelected = resultsUiState.selectedWeaponGroups.isEmpty()
                    && resultsUiState.allWeaponGroups.isNotEmpty()

            if (noneSelected) {
                // empty — nothing to show
            } else if (resultsUiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else if (resultsUiState.groupingMode == GroupingMode.NONE) {
                // FLAT VIEW — header is item 0, each result row is a separate lazy item
                item(key = "flat-header") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                    ) {
                        ResultsListHeader(
                            resultsType = resultsType,
                            inCard = true
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
                itemsIndexed(
                    resultsUiState.filterResults,
                    key = { _, r -> "flat-${r.id}" }
                ) { index, result ->
                    val isLast = index == resultsUiState.filterResults.size - 1
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (isLast) Modifier.clip(
                                    RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                ) else Modifier
                            )
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 12.dp)
                            .then(if (isLast) Modifier.padding(bottom = 12.dp) else Modifier)
                    ) {
                        ResultItem(
                            result = result,
                            resultsType = resultsType,
                            loggedInUserId = resultsUiState.loggedInUserId,
                            inCard = true,
                            onItemClick = {
                                navController.safeNavigate(
                                    Screen.ShooterResult.createRoute(
                                        competitionId,
                                        result.signup.user.userID,
                                        resultsType.name
                                    )
                                )
                            }
                        )
                        if (!isLast) HorizontalDivider()
                    }
                }
            } else {
                // GROUPED VIEW — each group: 1 header item + N result items
                resultsUiState.groupedResults.forEach { group ->
                    val isWeaponClassGrouping =
                        resultsUiState.groupingMode == GroupingMode.WEAPON_CLASS
                    val isMedlGrouping = resultsUiState.groupingMode == GroupingMode.MEDL
                    val isClubGrouping = resultsUiState.groupingMode == GroupingMode.CLUB

                    item(key = "group-header-${group.header}") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(start = 12.dp, end = 12.dp, top = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val countText =
                                    "${group.items.size}/${resultsUiState.results.size}"
                                Text(
                                    text = countText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Transparent,
                                    modifier = Modifier.weight(1f)
                                )
                                Box(
                                    modifier = Modifier.weight(3f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isWeaponClassGrouping) {
                                        WeaponClassBadge(
                                            weaponGroupName = group.header,
                                            isHighlighted = false,
                                            size = WeaponClassBadgeSize.Large
                                        )
                                    } else {
                                        val headerText =
                                            if (isMedlGrouping) {
                                                when (group.header) {
                                                    StdMedal.S.value -> stringResource(R.string.silver)
                                                    StdMedal.B.value -> stringResource(R.string.bronze)
                                                    else -> group.header
                                                }
                                            } else {
                                                group.header
                                            }
                                        Text(
                                            text = headerText,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                Text(
                                    text = countText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.End
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ResultsListHeader(
                                resultsType = resultsType,
                                inCard = true,
                                showMedal = !isMedlGrouping,
                                showWeaponClass = !isWeaponClassGrouping
                            )
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                    itemsIndexed(
                        group.items,
                        key = { _, r -> "group-${group.header}-${r.id}" }
                    ) { index, result ->
                        val isLast = index == group.items.size - 1
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .then(
                                    if (isLast) Modifier.clip(
                                        RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
                                    ) else Modifier
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                .padding(horizontal = 12.dp)
                                .then(if (isLast) Modifier.padding(bottom = 12.dp) else Modifier)
                        ) {
                            ResultItem(
                                result = result,
                                resultsType = resultsType,
                                loggedInUserId = resultsUiState.loggedInUserId,
                                inCard = true,
                                showMedal = !isMedlGrouping,
                                showClub = !isClubGrouping,
                                showWeaponClass = !isWeaponClassGrouping,
                                onItemClick = {
                                    navController.safeNavigate(
                                        Screen.ShooterResult.createRoute(
                                            competitionId,
                                            result.signup.user.userID,
                                            resultsType.name
                                        )
                                    )
                                }
                            )
                            if (!isLast) HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultsListHeader(
    resultsType: ResultsType,
    inCard: Boolean = false,
    showMedal: Boolean = true,
    showWeaponClass: Boolean = true
) {
    val isFieldType = resultsType == ResultsType.FIELD || resultsType == ResultsType.POINTS_FIELD
    val scoreWeight = if (isFieldType) 4f else 3f
    val nameWeight = if (showWeaponClass) 8f else 10f
    val rightWeight = (if (showWeaponClass) 2f else 0f) + (if (showMedal) 1f else 0f) + scoreWeight
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!inCard) Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh) else Modifier)
            .padding(horizontal = if (inCard) 0.dp else 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderText(
            R.string.placement, modifier = Modifier.weight(2f)
        )
        HeaderText(R.string.name, modifier = Modifier.weight(nameWeight))

        Row(
            modifier = Modifier.weight(rightWeight),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showWeaponClass) {
                HeaderText(
                    R.string.weapon_class_short,
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 4.dp)
                )
            }
            if (showMedal) HeaderText(R.string.medal, modifier = Modifier.weight(1f))
            when (resultsType) {
                ResultsType.FIELD,
                ResultsType.POINTS_FIELD -> {
                    HeaderText(R.string.hfp, modifier = Modifier.weight(scoreWeight))
                }

                ResultsType.PRECISION,
                ResultsType.MILITARY -> {
                    HeaderText(R.string.px, modifier = Modifier.weight(scoreWeight))
                }
            }
        }
    }
}

@Composable
fun ResultItem(
    result: Result,
    resultsType: ResultsType = ResultsType.FIELD,
    loggedInUserId: Long = -1L,
    inCard: Boolean = false,
    showMedal: Boolean = true,
    showClub: Boolean = true,
    showWeaponClass: Boolean = true,
    onItemClick: () -> Unit
) {
    val isCurrentUser = result.signup.user.userID == loggedInUserId
    val itemStyle =
        if (isCurrentUser) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
        else MaterialTheme.typography.bodySmall
    val backgroundColor = if (isCurrentUser) {
        MaterialTheme.appColors.currentUserHighlight
    } else {
        Color.Transparent
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onItemClick)
            .padding(horizontal = if (inCard) 0.dp else 8.dp, vertical = 6.dp),
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
            modifier = Modifier.weight(if (showWeaponClass) 8f else 10f)
        ) {
            ItemText(
                text = "${result.signup.user.name} ${result.signup.user.lastname}",
                style = itemStyle,
                overflow = TextOverflow.Ellipsis
            )
            if (showClub) ItemText(
                text = result.signup.club?.name ?: stringResource(R.string.unknown_club),
                overflow = TextOverflow.Ellipsis
            )
        }

        val isFieldType = resultsType == ResultsType.FIELD || resultsType == ResultsType.POINTS_FIELD
        val scoreWeight = if (isFieldType) 4f else 3f
        val weights = (if (showWeaponClass) 2f else 0f) + (if (showMedal) 1f else 0f) + scoreWeight

        Row(
            modifier = Modifier.weight(weights),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showWeaponClass) {
                ItemText(
                    text = result.weaponClass.classname,
                    style = itemStyle,
                    modifier = Modifier
                        .weight(2f)
                        .padding(start = 4.dp)
                )
            }
            if (showMedal) ItemText(
                text = when (result.stdMedal) {
                    StdMedal.S -> stringResource(R.string.silver)
                    StdMedal.B -> stringResource(R.string.bronze)
                    null -> stringResource(R.string.dash)
                },
                style = itemStyle,
                modifier = Modifier.weight(1f)
            )
            when (resultsType) {
                ResultsType.FIELD,
                ResultsType.POINTS_FIELD -> {
                    ItemText(
                        text = result.hits.toString() + "/" + result.figureHits.toString() + "/" + result.points.toString(),
                        style = itemStyle,
                        modifier = Modifier.weight(scoreWeight)
                    )
                }

                ResultsType.PRECISION,
                ResultsType.MILITARY -> {
                    ItemText(
                        text = result.points.toString() + "/" + result.hits.toString(),
                        style = itemStyle,
                        modifier = Modifier.weight(scoreWeight)
                    )
                }
            }
        }
    }
}
