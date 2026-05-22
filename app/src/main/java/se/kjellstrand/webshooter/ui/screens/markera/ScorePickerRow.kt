package se.kjellstrand.webshooter.ui.screens.markera

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val PICKER_GAP = 4.dp
private val SLIDER_ITEM_WIDTH = 48.dp
private val SLIDER_ITEM_HEIGHT = 56.dp
private val SLIDER_VISIBLE_ITEMS = 5

/**
 * Horizontal sliding picker built on [HorizontalPager]. The selected
 * value is whichever item is centred in the viewport (highlighted with
 * a rounded border). User swipes left/right to change.
 */
@Composable
private fun HorizontalSlidingScorePicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = SCORE_PICKER_LABELS.size
    val pagerState = rememberPagerState(
        initialPage = value.coerceIn(0, SCORE_PICKER_INNER_TEN),
        pageCount = { pageCount },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            if (settled != value) onValueChange(settled)
        }
    }
    LaunchedEffect(value) {
        if (pagerState.currentPage != value) pagerState.scrollToPage(value)
    }

    val sideCount = SLIDER_VISIBLE_ITEMS / 2
    Box(
        modifier = modifier
            .width(SLIDER_ITEM_WIDTH * SLIDER_VISIBLE_ITEMS)
            .height(SLIDER_ITEM_HEIGHT),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(SLIDER_ITEM_WIDTH),
            contentPadding = PaddingValues(horizontal = SLIDER_ITEM_WIDTH * sideCount),
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = SCORE_PICKER_LABELS[page],
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Box(
            modifier = Modifier
                .width(SLIDER_ITEM_WIDTH)
                .fillMaxHeight()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp),
                ),
        )
    }
}

/**
 * Vertical mirror of [HorizontalSlidingScorePicker] — same look, but
 * the user swipes up/down and the centred item is highlighted.
 */
@Composable
private fun VerticalSlidingScorePicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pageCount = SCORE_PICKER_LABELS.size
    val pagerState = rememberPagerState(
        initialPage = value.coerceIn(0, SCORE_PICKER_INNER_TEN),
        pageCount = { pageCount },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { settled ->
            if (settled != value) onValueChange(settled)
        }
    }
    LaunchedEffect(value) {
        if (pagerState.currentPage != value) pagerState.scrollToPage(value)
    }

    val sideCount = SLIDER_VISIBLE_ITEMS / 2
    Box(
        modifier = modifier
            .width(SLIDER_ITEM_WIDTH)
            .height(SLIDER_ITEM_HEIGHT * SLIDER_VISIBLE_ITEMS),
        contentAlignment = Alignment.Center,
    ) {
        VerticalPager(
            state = pagerState,
            pageSize = PageSize.Fixed(SLIDER_ITEM_HEIGHT),
            contentPadding = PaddingValues(vertical = SLIDER_ITEM_HEIGHT * sideCount),
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = SCORE_PICKER_LABELS[page],
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SLIDER_ITEM_HEIGHT)
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(6.dp),
                ),
        )
    }
}

/** Portrait — five vertical sliding pickers in a row above the viewport. */
@Composable
fun ScorePickerHorizontalRow(
    values: List<Int>,
    onValueChange: (index: Int, value: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(PICKER_GAP),
    ) {
        values.forEachIndexed { i, v ->
            VerticalSlidingScorePicker(value = v, onValueChange = { onValueChange(i, it) })
        }
    }
}

/** Landscape — five horizontal sliding pickers in a column left of the viewport. */
@Composable
fun ScorePickerVerticalColumn(
    values: List<Int>,
    onValueChange: (index: Int, value: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(PICKER_GAP),
    ) {
        values.forEachIndexed { i, v ->
            HorizontalSlidingScorePicker(value = v, onValueChange = { onValueChange(i, it) })
        }
    }
}
