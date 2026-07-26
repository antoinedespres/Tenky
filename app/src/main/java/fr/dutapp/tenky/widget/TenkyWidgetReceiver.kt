package fr.dutapp.tenky.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Starts and stops the refresh schedule with the widget's own lifetime, so no
 * periodic work runs while no widget is placed.
 */
class TenkyWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = TenkyWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WeatherWidgetWorker.schedule(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WeatherWidgetWorker.cancel(context)
    }
}
