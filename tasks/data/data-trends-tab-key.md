# LOW — Add trendsTabKeyFor utility

**Category:** Data
**Priority:** LOW
**Status:** TODO

## Files
- `ui/screens/charts/resulttrends/TrendsTabKey.kt` (new)

## Issue
The trends UI groups data points by `resultsType` to populate tabs. Magnumprecision competitions have `resultsType == "precision"` but a distinct `competitionType.name == "Magnumprecision"`, so they currently fall under the Precision tab instead of having their own. There is no shared place that derives the "tab key" from these two fields, so callers (ViewModel, tests) would each need to duplicate the rule.

## Fix
Create `ui/screens/charts/resulttrends/TrendsTabKey.kt` containing a single top-level function:

```kotlin
package se.kjellstrand.webshooter.ui.screens.charts.resulttrends

const val MAGNUMPRECISION_TAB_KEY = "magnumprecision"
const val MAGNUMPRECISION_COMPETITION_TYPE_NAME = "Magnumprecision"

fun trendsTabKeyFor(competitionTypeName: String?, resultsType: String): String =
    if (competitionTypeName == MAGNUMPRECISION_COMPETITION_TYPE_NAME) MAGNUMPRECISION_TAB_KEY
    else resultsType
```

Keep it stateless. Do not import any Android or repository types — this is pure logic so tests can call it without Robolectric.
