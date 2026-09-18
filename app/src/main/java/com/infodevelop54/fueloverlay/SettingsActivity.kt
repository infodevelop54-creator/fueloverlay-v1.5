package com.infodevelop54.fueloverlay

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var fuelRepo: FuelStateRepository

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@registerForActivityResult
        try {
            contentResolver.openOutputStream(uri)?.use {
                it.write(FuelBackup.export(this).toByteArray())
            }
            toast("БД экспортирована")
        } catch (e: Exception) { toast("Ошибка экспорта: ${e.message}") }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        try {
            val text = contentResolver.openInputStream(uri)?.use {
                it.readBytes().decodeToString()
            } ?: return@registerForActivityResult
            FuelBackup.import(this, text)
            toast("БД импортирована")
            recreate()
        } catch (e: Exception) { toast("Ошибка импорта: ${e.message}") }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fuelRepo = FuelStateRepository(this)

        val scroll = ScrollView(this).apply { setBackgroundColor(Color.parseColor("#1A1A1A")) }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(32))
        }
        scroll.addView(content)

        content.addView(sectionTitle("Данные поездки"))
        val etOdo = addLabeledInput(content, "Текущий одометр, км",
            fuelRepo.currentOdometerKm.toString())
        val etTank = addLabeledInput(content, "Объём бака, л",
            fuelRepo.tankCapacityLiters.toString())
        val etCons = addLabeledInput(content, "Ручной средний расход, л/100 км",
            fuelRepo.manualAverageConsumptionL100.toString())
        val etRefuelOdo = addLabeledInput(content, "Одометр на последней заправке, км",
            fuelRepo.refuelOdometerKm.toString())
        content.addView(Button(this).apply {
            text = "Сохранить данные"
            setOnClickListener {
                parseFloat(etOdo)?.let { fuelRepo.syncOdometer(it) }
                parseFloat(etTank)?.let { fuelRepo.tankCapacityLiters = it }
                parseFloat(etCons)?.let {
                    fuelRepo.manualAverageConsumptionL100 = it
                    if (AdaptiveConsumption.loadCycles(this@SettingsActivity).isEmpty()) {
                        fuelRepo.averageConsumptionL100 = it
                    }
                }
                parseFloat(etRefuelOdo)?.let { fuelRepo.refuelOdometerKm = it }
                toast("Сохранено")
            }
        })

        content.addView(sectionTitle("Разделы"))

        val toggleBtn = Button(this).apply {
            text = if (fuelRepo.widgetVisible) "Скрыть виджет" else "Показать виджет"
            setOnClickListener {
                val svc = OverlayService.instance
                if (svc != null) {
                    svc.toggleWidget()
                } else {
                    fuelRepo.widgetVisible = true
                    startService(Intent(this@SettingsActivity, OverlayService::class.java))
                }
                text = if (fuelRepo.widgetVisible) "Скрыть виджет" else "Показать виджет"
            }
        }
        content.addView(toggleBtn)

        content.addView(Button(this).apply {
            text = getString(R.string.menu_journal)
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, RefuelJournalActivity::class.java))
            }
        })

        content.addView(Button(this).apply {
            text = getString(R.string.btn_visual_settings)
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, VisualSettingsActivity::class.java))
            }
        })

        content.addView(Button(this).apply {
            text = getString(R.string.menu_adaptive)
            setOnClickListener {
                startActivity(Intent(this@SettingsActivity, AdaptiveConsumptionActivity::class.java))
            }
        })

        content.addView(sectionTitle("Импорт\\Экспорт"))
        content.addView(TextView(this).apply {
            text = "Путь: ${FuelDatabase.baseDir(this@SettingsActivity).absolutePath}"
            textSize = 12f
            setTextColor(Color.WHITE)
        })
        content.addView(Button(this).apply {
            text = getString(R.string.menu_export)
            setOnClickListener { exportLauncher.launch("fuel_overlay_backup.json") }
        })
        content.addView(Button(this).apply {
            text = getString(R.string.menu_import)
            setOnClickListener { importLauncher.launch(arrayOf("application/json", "*/*")) }
        })

        setContentView(scroll)
    }

    private fun sectionTitle(text: String): TextView = TextView(this).apply {
        this.text = text
        textSize = 14f
        setTextColor(Color.WHITE)
        setPadding(0, dp(12), 0, dp(4))
    }

    private fun addLabeledInput(parent: LinearLayout, label: String, initial: String): EditText {
        parent.addView(TextView(this).apply {
            text = label
            textSize = 13f
            setTextColor(Color.WHITE)
            setPadding(0, dp(8), 0, dp(4))
        })
        val et = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(initial)
            setTextColor(Color.WHITE)
        }
        parent.addView(et)
        return et
    }

    private fun parseFloat(et: EditText): Float? =
        et.text.toString().replace(',', '.').trim().toFloatOrNull()

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
}