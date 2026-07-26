package fr.dutapp.tenky.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.action.actionStartActivity
import fr.dutapp.tenky.MainActivity
import fr.dutapp.tenky.R
import fr.dutapp.tenky.util.WeatherIcons
import kotlin.math.roundToInt

/**
 * Home screen widget showing current conditions for the followed city.
 *
 * Renders from the cache the app already keeps, so placing the widget or waking
 * the device never waits on a network round trip. Refreshing is left to
 * [WeatherWidgetWorker]; a stale render is scheduled to be replaced rather than
 * blocking on a fetch here.
 */
class TenkyWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = context.resolveWidgetData()

        // Nothing cached yet, or the data has aged out: ask for a refresh and
        // render whatever is available in the meantime.
        val age = data?.cached?.fetchedAtMillis?.let { System.currentTimeMillis() - it }
        if (data != null && (age == null || age > STALE_AFTER_MILLIS)) {
            WeatherWidgetWorker.refreshNow(context)
        }

        provideContent {
            GlanceTheme {
                WidgetContent(data)
            }
        }
    }

    @Composable
    private fun WidgetContent(data: WidgetData?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(16.dp)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val snapshot = data?.cached?.snapshot
            if (data == null || snapshot == null) {
                Text(
                    text = LocalContextText(R.string.widget_no_data),
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 14.sp,
                    ),
                )
                return@Column
            }

            Text(
                text = data.city.name,
                style = TextStyle(
                    color = GlanceTheme.colors.onSurface,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                ),
            )
            Spacer(GlanceModifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    provider = ImageProvider(WeatherIcons.forCode(snapshot.current.iconCode)),
                    contentDescription = null,
                    modifier = GlanceModifier.size(40.dp),
                )
                Spacer(GlanceModifier.height(8.dp))
                Text(
                    text = "${snapshot.current.temperature.roundToInt()}°",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
            Text(
                text = snapshot.current.description.replaceFirstChar { it.uppercase() },
                style = TextStyle(
                    color = GlanceTheme.colors.onSurfaceVariant,
                    fontSize = 12.sp,
                ),
            )
        }
    }

    private companion object {
        /** Matches the worker's cadence, so a render is at most one cycle old. */
        const val STALE_AFTER_MILLIS = 60 * 60 * 1000L
    }
}

/** Glance has no stringResource, so resources are read from the composition's context. */
@Composable
private fun LocalContextText(resId: Int): String =
    androidx.glance.LocalContext.current.getString(resId)
