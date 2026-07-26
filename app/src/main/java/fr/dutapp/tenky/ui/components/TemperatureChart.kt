package fr.dutapp.tenky.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.dutapp.tenky.domain.model.TemperaturePoint
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Temperature across the five-day forecast, one point per three-hour step.
 *
 * A single series, so there is no legend — the section title names it. Only the
 * warmest and coldest points are labelled by default; touching the chart reads
 * out any other step. The tooltip only ever enhances: every value is also
 * readable as text in the hourly strip and daily list, which are this chart's
 * table-view equivalent.
 *
 * The line colour is validated against both surfaces for lightness, chroma and
 * contrast rather than picked by eye.
 */
@Composable
fun TemperatureChart(
    points: List<TemperaturePoint>,
    modifier: Modifier = Modifier,
    plotHeight: Dp = 132.dp,
) {
    if (points.size < MINIMUM_POINTS) return

    val lineColor = if (isSystemInDarkTheme()) DarkLine else LightLine
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = AXIS_ALPHA)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val labelColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val locale = Locale.getDefault()

    val warmest = remember(points) { points.maxBy { it.temperature } }
    val coldest = remember(points) { points.minBy { it.temperature } }
    val description = remember(points, locale) {
        "Temperature trend from ${coldest.temperature.roundToInt()} to " +
            "${warmest.temperature.roundToInt()} degrees over ${points.dayCount()} days"
    }
    val touchLabels = remember(points, locale) { points.map { it.touchLabel(locale) } }

    /** Which step the finger is currently over; null when not touching. */
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    val labelStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
    val valueStyle = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(plotHeight)
                .padding(horizontal = 16.dp)
                .semantics { contentDescription = description }
                .pointerInput(points) {
                    // Nothing is consumed, so the parent list still scrolls
                    // vertically while a horizontal drag reads the series.
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        selectedIndex = indexAt(down.position.x, size.width, points.size)
                        do {
                            val event = awaitPointerEvent()
                            event.changes.lastOrNull()?.let { change ->
                                selectedIndex = indexAt(change.position.x, size.width, points.size)
                            }
                        } while (event.changes.any { it.pressed })
                        selectedIndex = null
                    }
                },
        ) {
            val temperatures = points.map { it.temperature }
            val low = temperatures.min()
            val high = temperatures.max()
            // A flat series would divide by zero; give it a nominal band so the
            // line sits in the middle rather than collapsing onto an edge.
            val span = (high - low).takeIf { it > 0.0 } ?: FLAT_SERIES_SPAN

            val topInset = with(density) { LABEL_INSET.toPx() }
            // The weekday labels get their own band at the bottom, so a value
            // label sitting on the minimum can never land on top of them.
            val plotBottom = size.height - with(density) { AXIS_BAND.toPx() }
            val usableHeight = plotBottom - topInset
            val stepX = size.width / (points.size - 1)

            fun offsetFor(index: Int): Offset {
                val ratio = (points[index].temperature - low) / span
                return Offset(
                    x = index * stepX,
                    y = topInset + (usableHeight - (ratio * usableHeight)).toFloat(),
                )
            }

            val linePath = Path().apply {
                points.indices.forEach { index ->
                    val point = offsetFor(index)
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }

            // Area under the line, fading out so it reads as support for the
            // line rather than a filled block competing with it.
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(size.width, plotBottom)
                lineTo(0f, plotBottom)
                close()
            }
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = AREA_TOP_ALPHA), Color.Transparent),
                    startY = 0f,
                    endY = plotBottom,
                ),
            )

            drawDayBoundaries(
                points = points,
                stepX = stepX,
                plotBottom = plotBottom,
                axisColor = axisColor,
                textMeasurer = textMeasurer,
                style = labelStyle,
                labelColor = mutedColor,
                locale = locale,
            )

            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = with(density) { LINE_WIDTH.toPx() }),
            )

            val selected = selectedIndex
            if (selected != null) {
                drawTouchReadout(
                    centre = offsetFor(selected),
                    label = touchLabels[selected],
                    temperature = points[selected].temperature,
                    plotBottom = plotBottom,
                    lineColor = lineColor,
                    surfaceColor = surfaceColor,
                    axisColor = axisColor,
                    labelColor = labelColor,
                    textMeasurer = textMeasurer,
                    style = valueStyle,
                    density = density,
                )
            } else {
                // Direct labels for the two extremes, hidden while reading the
                // series by touch so the two never sit on top of each other.
                listOf(
                    points.indexOf(warmest) to warmest,
                    points.indexOf(coldest) to coldest,
                ).forEach { (index, point) ->
                    val centre = offsetFor(index)
                    drawMarker(centre, lineColor, surfaceColor, density)
                    drawValueLabel(
                        value = point.temperature,
                        centre = centre,
                        isMaximum = point === warmest,
                        plotBottom = plotBottom,
                        textMeasurer = textMeasurer,
                        style = valueStyle,
                        color = labelColor,
                        density = density,
                    )
                }
            }
        }
    }
}

/** Nearest step to an x position; every x maps to one, so the target is generous. */
private fun indexAt(x: Float, width: Int, count: Int): Int {
    if (count < 2 || width <= 0) return 0
    val step = width.toFloat() / (count - 1)
    return (x / step).roundToInt().coerceIn(0, count - 1)
}

/** Crosshair, marker and a small readout for the step under the finger. */
private fun DrawScope.drawTouchReadout(
    centre: Offset,
    label: String,
    temperature: Double,
    plotBottom: Float,
    lineColor: Color,
    surfaceColor: Color,
    axisColor: Color,
    labelColor: Color,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    density: Density,
) {
    drawLine(
        color = axisColor,
        start = Offset(centre.x, 0f),
        end = Offset(centre.x, plotBottom),
        strokeWidth = with(density) { LINE_WIDTH.toPx() },
    )
    drawMarker(centre, lineColor, surfaceColor, density)

    val text = textMeasurer.measure(
        text = "$label  ${temperature.roundToInt()}°",
        style = style.copy(color = labelColor),
    )
    val padding = with(density) { READOUT_PADDING.toPx() }
    val boxWidth = text.size.width + padding * 2
    val boxHeight = text.size.height + padding

    // Pinned to the top of the plot: it never covers the point being read, and
    // cannot collide with the weekday band.
    val boxX = (centre.x - boxWidth / 2f).coerceIn(0f, size.width - boxWidth)
    drawRoundRect(
        color = surfaceColor.copy(alpha = READOUT_ALPHA),
        topLeft = Offset(boxX, 0f),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(with(density) { READOUT_RADIUS.toPx() }),
    )
    drawText(
        textLayoutResult = text,
        topLeft = Offset(boxX + padding, padding / 2f),
    )
}

/** Solid hairline where the day changes, with the weekday beneath it. */
private fun DrawScope.drawDayBoundaries(
    points: List<TemperaturePoint>,
    stepX: Float,
    plotBottom: Float,
    axisColor: Color,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    labelColor: Color,
    locale: Locale,
) {
    val formatter = DateTimeFormatter.ofPattern("EEE", locale)
    var previousDate: LocalDate? = null

    points.forEachIndexed { index, point ->
        val date = point.dateTime.toLocalDate()
        if (previousDate != null && date != previousDate) {
            val x = index * stepX
            drawLine(
                color = axisColor,
                start = Offset(x, 0f),
                end = Offset(x, plotBottom),
                strokeWidth = 1f,
            )
            val label = textMeasurer.measure(
                text = date.format(formatter).replaceFirstChar { it.titlecase(locale) },
                style = style.copy(color = labelColor),
            )
            // Nudged clear of the rule, and kept inside the canvas so the last
            // day's label is not cropped at the right edge.
            val labelX = (x + DAY_LABEL_GAP).coerceAtMost(size.width - label.size.width)
            drawText(textLayoutResult = label, topLeft = Offset(labelX, plotBottom + DAY_LABEL_GAP))
        }
        previousDate = date
    }
}

/** An 8px marker with a 2px surface ring, rather than a border. */
private fun DrawScope.drawMarker(
    centre: Offset,
    color: Color,
    surfaceColor: Color,
    density: Density,
) {
    val radius = with(density) { MARKER_RADIUS.toPx() }
    val ring = with(density) { MARKER_RING.toPx() }
    drawCircle(color = surfaceColor, radius = radius + ring, center = centre)
    drawCircle(color = color, radius = radius, center = centre)
}

private fun DrawScope.drawValueLabel(
    value: Double,
    centre: Offset,
    isMaximum: Boolean,
    plotBottom: Float,
    textMeasurer: TextMeasurer,
    style: TextStyle,
    color: Color,
    density: Density,
) {
    val label = textMeasurer.measure(text = "${value.roundToInt()}°", style = style.copy(color = color))
    val gap = with(density) { LABEL_GAP.toPx() }
    val markerClearance = with(density) { (MARKER_RADIUS + MARKER_RING).toPx() } + gap

    // Above the warmest point, below the coldest, so neither sits on the line.
    val preferred = if (isMaximum) {
        centre.y - label.size.height - markerClearance
    } else {
        centre.y + markerClearance
    }
    // Flip to the other side rather than spilling out of the plot area, which
    // is what put the minimum's label on top of the weekday row.
    val y = when {
        preferred < 0f -> centre.y + markerClearance
        preferred + label.size.height > plotBottom -> centre.y - label.size.height - markerClearance
        else -> preferred
    }.coerceIn(0f, plotBottom - label.size.height)

    // Kept inside the plot so the text is never clipped at either edge.
    val x = (centre.x - label.size.width / 2f).coerceIn(0f, size.width - label.size.width)
    drawText(textLayoutResult = label, topLeft = Offset(x, y))
}

/** "Mon 17:00", in the device's own time convention. */
private fun TemperaturePoint.touchLabel(locale: Locale): String {
    val day = dateTime.format(DateTimeFormatter.ofPattern("EEE", locale))
        .replaceFirstChar { it.titlecase(locale) }
    val time = dateTime.toLocalTime()
        .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale))
    return "$day $time"
}

private fun List<TemperaturePoint>.dayCount(): Int =
    map { it.dateTime.toLocalDate() }.distinct().size

/**
 * Line colours, both validated with the palette checker against their own
 * surface: lightness band, chroma floor and >= 3:1 contrast.
 */
private val LightLine = Color(0xFF1B99E0)
private val DarkLine = Color(0xFF22A0DC)

private const val MINIMUM_POINTS = 2
private const val FLAT_SERIES_SPAN = 1.0
private const val AXIS_ALPHA = 0.25f
private const val AREA_TOP_ALPHA = 0.22f
private const val READOUT_ALPHA = 0.95f
private const val DAY_LABEL_GAP = 6f
private val LINE_WIDTH = 2.dp
private val MARKER_RADIUS = 4.dp
private val MARKER_RING = 2.dp
private val LABEL_GAP = 4.dp
private val LABEL_INSET = 20.dp
private val AXIS_BAND = 18.dp
private val READOUT_PADDING = 6.dp
private val READOUT_RADIUS = 6.dp
