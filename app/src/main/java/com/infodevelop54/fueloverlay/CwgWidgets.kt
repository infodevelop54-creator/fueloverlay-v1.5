package com.infodevelop54.fueloverlay

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.util.Locale

// ---------- Потрачено ----------
class FuelSpentWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_spent
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        val mileage = (repo.currentOdometerKm - repo.refuelOdometerKm).coerceAtLeast(0f)
        val spent = mileage * repo.averageConsumptionL100 / 100f
        views.setTextViewText(R.id.cwgValue, String.format(Locale.US, "%.1f л", spent))
        applyTextStyle(views, app, R.id.cwgLabel, R.id.cwgValue)
        applyIconTint(views, app)
    }
}

// ---------- Остаток ----------
class FuelRemainingWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_remaining
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        val mileage = (repo.currentOdometerKm - repo.refuelOdometerKm).coerceAtLeast(0f)
        val spent = mileage * repo.averageConsumptionL100 / 100f
        val remaining = (repo.tankCapacityLiters - spent + repo.extraFuelAddedL)
            .coerceIn(0f, repo.tankCapacityLiters)
        views.setTextViewText(R.id.cwgValue, String.format(Locale.US, "%.1f л", remaining))
        applyTextStyle(views, app, R.id.cwgLabel, R.id.cwgValue)
        applyIconTint(views, app)
    }
}

// ---------- Пробег ----------
class FuelMileageWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_mileage
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        val mileage = (repo.currentOdometerKm - repo.refuelOdometerKm).coerceAtLeast(0f)
        views.setTextViewText(R.id.cwgValue, String.format(Locale.US, "%.1f км", mileage))
        applyTextStyle(views, app, R.id.cwgLabel, R.id.cwgValue)
        applyIconTint(views, app)
    }
}

// ---------- Остаток пробега ----------
class FuelRangeWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_range
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        val mileage = (repo.currentOdometerKm - repo.refuelOdometerKm).coerceAtLeast(0f)
        val spent = mileage * repo.averageConsumptionL100 / 100f
        val remaining = (repo.tankCapacityLiters - spent + repo.extraFuelAddedL)
            .coerceIn(0f, repo.tankCapacityLiters)
        val rangeKm = if (repo.averageConsumptionL100 > 0f)
            remaining / repo.averageConsumptionL100 * 100f else 0f
        views.setTextViewText(R.id.cwgValue, String.format(Locale.US, "%.1f км", rangeKm))
        applyTextStyle(views, app, R.id.cwgLabel, R.id.cwgValue)
        applyIconTint(views, app)
    }
}

// ---------- Кнопка «Запр.100%» ----------
class RefuelFullWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_refuel_full
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        applyTextStyle(views, app, R.id.cwgLabel)

        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_REFUEL_FULL
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getForegroundService(context, 101, intent, flags)
        views.setOnClickPendingIntent(R.id.cwgRoot, pi)
    }
}

// ---------- Кнопка «Заправка +» ----------
class RefuelPlusWidget : BaseFuelWidget() {
    override val layoutId = R.layout.fuel_widget_refuel_plus
    override fun bindValue(views: RemoteViews, context: Context, repo: FuelStateRepository, app: WidgetAppearance) {
        applyTextStyle(views, app, R.id.cwgLabel)

        val intent = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_REFUEL_PLUS
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pi = PendingIntent.getForegroundService(context, 102, intent, flags)
        views.setOnClickPendingIntent(R.id.cwgRoot, pi)
    }
}