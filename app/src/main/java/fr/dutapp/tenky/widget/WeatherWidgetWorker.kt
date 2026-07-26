package fr.dutapp.tenky.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import fr.dutapp.tenky.domain.model.DataResult
import java.util.concurrent.TimeUnit

/**
 * Refreshes the widget's city and redraws it.
 *
 * The fetch goes through the normal repository, so the response also lands in
 * the shared cache — opening the app after a widget refresh shows fresh data
 * without another request.
 */
class WeatherWidgetWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val data = context.resolveWidgetData() ?: return Result.success()

        val result = context.container.weatherRepository
            .getWeather(data.city.coordinates, data.unit)

        TenkyWidget().updateAll(context)

        // A failed fetch is worth another go later; the widget meanwhile keeps
        // showing the cached reading rather than going blank.
        return if (result is DataResult.Failure) Result.retry() else Result.success()
    }

    companion object {
        private const val PERIODIC_WORK = "tenky-widget-refresh"
        private const val ONE_SHOT_WORK = "tenky-widget-refresh-now"
        private const val REFRESH_INTERVAL_MINUTES = 60L

        /** Keeps the widget current while it is on a home screen. */
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<WeatherWidgetWorker>(
                    REFRESH_INTERVAL_MINUTES,
                    TimeUnit.MINUTES,
                ).build(),
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK)
        }

        /** Used when the widget renders with missing or aged-out data. */
        fun refreshNow(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_SHOT_WORK,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<WeatherWidgetWorker>().build(),
            )
        }
    }
}
