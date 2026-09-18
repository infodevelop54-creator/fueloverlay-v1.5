package com.infodevelop54.fueloverlay

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.widget.RemoteViews

abstract class BaseFuelWidget : AppWidgetProvider() {

    abstract val layoutId: Int

    abstract fun bindValue(
        views: RemoteViews,
        context: Context,
        repo: FuelStateRepository,
        app: WidgetAppearance
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            appWidgetManager.updateAppWidget(id, buildViews(context))
        }
    }

    fun buildViews(context: Context): RemoteViews {
        val repo = FuelStateRepository(context)
        val app = AppearanceRepository.load(context)
        val views = RemoteViews(context.packageName, layoutId)

        val bgColor = if (app.cwgShowBackground)
            safeColor(app.cwgBackgroundColor, 0xCC000000.toInt())
        else
            Color.TRANSPARENT
        views.setInt(R.id.cwgRoot, "setBackgroundColor", bgColor)

        bindValue(views, context, repo, app)
        return views
    }

    protected fun applyTextStyle(views: RemoteViews, app: WidgetAppearance, vararg ids: Int) {
        val tc = safeColor(app.cwgTextColor, Color.WHITE)
        ids.forEach { id ->
            views.setTextColor(id, tc)
            views.setTextViewTextSize(id, TypedValue.COMPLEX_UNIT_SP, app.cwgTextSizeSp)
        }
    }

    protected fun applyIconTint(views: RemoteViews, app: WidgetAppearance) {
        try {
            views.setInt(
                R.id.cwgIcon, "setColorFilter",
                safeColor(app.cwgTextColor, Color.WHITE)
            )
        } catch (_: Exception) { }
    }

    protected fun safeColor(hex: String, fallback: Int): Int =
        try { Color.parseColor(hex) } catch (_: Exception) { fallback }
}

object CwgRefresher {

    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val providers = listOf(
            FuelSpentWidget(),
            FuelRemainingWidget(),
            FuelMileageWidget(),
            FuelRangeWidget()
        )
        for (p in providers) {
            val ids = manager.getAppWidgetIds(ComponentName(context, p.javaClass))
            if (ids.isEmpty()) continue
            for (id in ids) manager.updateAppWidget(id, p.buildViews(context))
        }
    }
}