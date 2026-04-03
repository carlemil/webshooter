package se.kjellstrand.webshooter.ui.screens.competitions

import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import se.kjellstrand.webshooter.R
import se.kjellstrand.webshooter.data.competitions.remote.Datum
import se.kjellstrand.webshooter.ui.common.WeaponClassBadges
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun CompetitionItem(
    competition: Datum,
    patrolOrRelayButtonText: Int,
    onResultsClick: () -> Unit,
    onSignupClick: () -> Unit = {},
    onSignupsListClick: () -> Unit = {},
    onPatrolsOrRelayClick: () -> Unit = {},
    onTeamsClick: () -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showCalendarDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hasLocation =
        competition.lat != 0.0 || competition.lng != 0.0 || !competition.googleMaps.isNullOrBlank()

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = competition.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${competition.date}  •  ${competition.statusHuman}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(
                                R.string.competitions_competition_type,
                                competition.competitionType.name
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val isFutureCompetition = remember(competition.date) {
                        runCatching { LocalDate.parse(competition.date) >= LocalDate.now() }.getOrDefault(false)
                    }
                    if (isFutureCompetition) {
                        IconButton(onClick = { showCalendarDialog = true }) {
                            Icon(
                                painter = painterResource(R.drawable.calendar_add_on),
                                contentDescription = stringResource(R.string.competitions_add_to_calendar)
                            )
                        }
                    }
                    if (hasLocation) {
                        IconButton(
                            onClick = {
                                val uri = when {
                                    competition.lat != 0.0 || competition.lng != 0.0 ->
                                        "geo:${competition.lat},${competition.lng}?q=${competition.lat},${competition.lng}".toUri()

                                    else ->
                                        competition.googleMaps!!.replace("/maps/embed", "/maps").toUri()
                                }
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.map_search),
                                contentDescription = stringResource(R.string.competitions_open_map)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                WeaponClassBadges(
                    weaponClasses = competition.weaponClasses,
                    userSignups = competition.userSignups
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val shape =
                        RoundedCornerShape(integerResource(R.integer.rounded_corner_shape_percent))

                    val buttonContentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    val resultsEnabled = competition.status == "completed" ||
                            runCatching {
                                !LocalDate.parse(competition.date).isAfter(LocalDate.now())
                            }.getOrDefault(false)
                    if (resultsEnabled) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onResultsClick,
                            shape = shape,
                            contentPadding = buttonContentPadding
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = stringResource(R.string.competitions_result),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onSignupsListClick,
                        shape = shape,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(R.string.competition_signups_list_participants),
                            textAlign = TextAlign.Center
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onPatrolsOrRelayClick,
                        shape = shape,
                        contentPadding = buttonContentPadding
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            text = stringResource(patrolOrRelayButtonText),
                            textAlign = TextAlign.Center
                        )
                    }
                    if (competition.allowTeams > 0) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = onTeamsClick,
                            shape = shape,
                            contentPadding = buttonContentPadding
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = stringResource(R.string.competitions_teams_button),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    if (competition.status == "open") {
                        Button(
                            modifier = Modifier.weight(1f),
                            shape = shape,
                            onClick = onSignupClick,
                            contentPadding = buttonContentPadding
                        ) {
                            Text(
                                style = MaterialTheme.typography.bodySmall,
                                text = stringResource(R.string.sign_up),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.competitions_collapse)
                    else stringResource(R.string.competitions_expand)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    CompetitionDetail(
                        competition = competition,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    if (showCalendarDialog) {
        val weaponClassMap = competition.weaponClasses.associate { it.id to it.classname }
        val signedUpClasses = competition.userSignups
            .mapNotNull { weaponClassMap[it.weaponClassesID] }
            .distinct()
        val classesStr = if (signedUpClasses.isNotEmpty()) " - ${signedUpClasses.joinToString(", ")}" else ""
        val eventTitle = competition.name + classesStr

        val startTimeStr = competition.userSignups
            .mapNotNull { it.startTimeHuman.takeIf { t -> t.isNotBlank() && t != "01:00" } }
            .minOrNull()
        val endTimeStr = competition.userSignups
            .mapNotNull { it.endTimeHuman.takeIf { t -> t.isNotBlank() } }
            .maxOrNull()

        val startMillis = runCatching {
            LocalDateTime.of(LocalDate.parse(competition.date), LocalTime.parse(startTimeStr ?: "00:00"))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()
        val endMillis = runCatching {
            LocalDateTime.of(LocalDate.parse(competition.date), LocalTime.parse(endTimeStr ?: "23:59"))
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()

        AlertDialog(
            onDismissRequest = { showCalendarDialog = false },
            title = { Text(eventTitle) },
            text = {
                Column {
                    Text(competition.date)
                    if (startTimeStr != null) Text("$startTimeStr – ${endTimeStr ?: ""}")
                }
            },
            confirmButton = {
                Button(onClick = {
                    showCalendarDialog = false
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, eventTitle)
                        if (startMillis != null) putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                        if (endMillis != null) putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                        if (startMillis == null) putExtra(CalendarContract.Events.ALL_DAY, true)
                    }
                    context.startActivity(intent)
                }) {
                    Text(stringResource(R.string.competitions_add_to_calendar))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCalendarDialog = false }) {
                    Text(stringResource(R.string.settings_cancel))
                }
            }
        )
    }
}

@Composable
private fun CompetitionDetail(competition: Datum, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_information),
                    style = MaterialTheme.typography.titleSmall
                )
                DetailRow(
                    label = stringResource(R.string.competitions_contact_name, ""),
                    value = competition.contactName ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_date, ""),
                    value = competition.date
                )
                DetailRow(
                    label = stringResource(R.string.competitions_status, ""),
                    value = competition.statusHuman
                )
                DetailRow(
                    label = stringResource(R.string.competitions_open_for_team_signup, ""),
                    value = competition.signupsOpeningDate
                )
                DetailRow(
                    label = stringResource(R.string.competitions_last_signup_date, ""),
                    value = competition.signupsClosingDate
                )
                DetailRow(
                    label = stringResource(R.string.competitions_late_signup, ""),
                    value = competition.allowSignupsAfterClosingDateHuman
                )
                DetailRow(
                    label = stringResource(R.string.competitions_team_signup, ""),
                    value = if (competition.allowTeams == 1L) stringResource(R.string.competitions_yes) else stringResource(R.string.competitions_no)
                )
                DetailRow(
                    label = stringResource(R.string.competitions_competition_type, ""),
                    value = competition.competitionType.name
                )
                DetailRow(
                    label = stringResource(R.string.competitions_result_calculation, ""),
                    value = competition.resultsTypeHuman
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_contact_information),
                    style = MaterialTheme.typography.titleSmall
                )
                DetailRow(
                    label = stringResource(R.string.competitions_arranger),
                    value = competition.club.name
                )
                DetailRow(
                    label = stringResource(R.string.competitions_venue),
                    value = competition.contactVenue ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_city),
                    value = competition.contactCity ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_contact_person),
                    value = competition.contactName ?: ""
                )
                DetailRow(
                    label = stringResource(R.string.competitions_phone),
                    value = competition.contactTelephone ?: "",
                    onClick = competition.contactTelephone?.takeIf { it.isNotEmpty() }?.let {
                        { context.startActivity(Intent(Intent.ACTION_DIAL, "tel:$it".toUri())) }
                    }
                )
                DetailRow(
                    label = stringResource(R.string.competitions_email),
                    value = competition.contactEmail ?: "",
                    onClick = competition.contactEmail?.takeIf { it.isNotEmpty() }?.let {
                        { context.startActivity(Intent(Intent.ACTION_SENDTO, "mailto:$it".toUri())) }
                    }
                )
                DetailRow(
                    label = stringResource(R.string.competitions_website),
                    value = competition.website ?: "",
                    onClick = competition.website?.takeIf { it.isNotEmpty() }?.let {
                        val url = if (it.startsWith("http://") || it.startsWith("https://")) it else "https://$it"
                        { context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri())) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(R.string.competitions_description),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = competition.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(3f)
        )
    }
}
