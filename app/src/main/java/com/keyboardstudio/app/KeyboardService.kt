package com.keyboardstudio.app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.content.SharedPreferences
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

class KeyboardService : InputMethodService() {
    private lateinit var keyboardPrefs: SharedPreferences

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf("selected", "keyColor", "height", "rounding", "language") &&
                currentInputConnection != null
            ) {
                refreshKeyboard()
            }
        }

    override fun onCreate() {
        super.onCreate()
        keyboardPrefs = getSharedPreferences("keyboard_studio", MODE_PRIVATE)
        keyboardPrefs.registerOnSharedPreferenceChangeListener(preferenceListener)
    }

    override fun onDestroy() {
        keyboardPrefs.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        super.onDestroy()
    }
    private var shifted = false
    private var numbers = false
    private var root: LinearLayout? = null

    private val palettes = listOf(
        intArrayOf(0xFFFF8FC7.toInt(), 0xFF9B8CFF.toInt()),
        intArrayOf(0xFF35D6E8.toInt(), 0xFF4776FF.toInt()),
        intArrayOf(0xFFFFA34D.toInt(), 0xFFFF5D83.toInt()),
        intArrayOf(0xFF69E6B5.toInt(), 0xFF42B9D8.toInt()),
        intArrayOf(0xFFB66BFF.toInt(), 0xFF633BDB.toInt()),
        intArrayOf(0xFFFFE66D.toInt(), 0xFFFFA94D.toInt()),
        intArrayOf(0xFFFF758C.toInt(), 0xFFFF7EB3.toInt()),
        intArrayOf(0xFF43CEA2.toInt(), 0xFF185A9D.toInt()),
        intArrayOf(0xFFDA22FF.toInt(), 0xFF9733EE.toInt()),
        intArrayOf(0xFFFC4A1A.toInt(), 0xFFF7B733.toInt())
    )
    private val keyColors = intArrayOf(
        0xFFFFF1FA.toInt(), 0xFFEAFBFF.toInt(), 0xFFFFF3E9.toInt(),
        0xFFE9FFF7.toInt(), 0xFFF4EBFF.toInt(), 0xFF222033.toInt(),
        Color.WHITE, 0xFFFFE2B8.toInt(), 0xFFD8F4FF.toInt(), 0xFFFFDCEB.toInt()
    )

    override fun onCreateInputView(): View {
        return buildKeyboard()
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        shifted = info?.let { it.inputType and android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0 } ?: false
        refreshKeyboard()
    }

    private fun refreshKeyboard() { setInputView(buildKeyboard()) }

    private fun buildKeyboard(): LinearLayout {
        val prefs = getSharedPreferences("keyboard_studio", MODE_PRIVATE)
        val selected = prefs.getInt("selected", 0).coerceIn(0, 29)
        val palette = palettes[selected % palettes.size]
        val keyColor = keyColors[prefs.getInt("keyColor", 0).coerceIn(0, 9)]
        val height = prefs.getFloat("height", 39f).roundToInt().coerceIn(30, 54)
        val radius = prefs.getFloat("rounding", 9f).roundToInt().coerceIn(2, 18)
        val language = prefs.getString("language", "English") ?: "English"
        val rows = when (language) {
            "ქართული" -> listOf("ქწერტყუიოპ", "ასდფგჰჯკლ", "⇧ზხცვბნმ⌫")
            "Русский" -> listOf("йцукенгшщз", "фывапролдж", "⇧ячсмитьбю⌫")
            "Deutsch" -> listOf("qwertzuiop", "asdfghjkl", "⇧yxcvbnm⌫")
            "Español" -> listOf("qwertyuiop", "asdfghjklñ", "⇧zxcvbnm⌫")
            "Français" -> listOf("azertyuiop", "qsdfghjklm", "⇧wxcvbn⌫")
            "Türkçe" -> listOf("qwertyuıop", "asdfghjklş", "⇧zxcvbnmö⌫")
            "العربية" -> listOf("ضصثقفغعهخحج", "شسيبلاتنمك", "⇧ئءؤرلاى⌫")
            else -> listOf("qwertyuiop", "asdfghjkl", "⇧zxcvbnm⌫")
        }
        val spaceLabel = when (language) {
            "ქართული" -> "ქართული"; "Русский" -> "Русский"; "Deutsch" -> "Deutsch"
            "Español" -> "Español"; "Français" -> "Français"; "Türkçe" -> "Türkçe"
            "العربية" -> "العربية"; else -> "English"
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(5), dp(7), dp(5), dp(8))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(palette[0], palette[1])
            ).apply { cornerRadius = dp(12).toFloat() }
        }
        root = container

        if (numbers) {
            addRow(container, listOf("1","2","3","4","5","6","7","8","9","0"), keyColor, height, radius)
            addRow(container, listOf("@","#","\$","%","&","-","+","(",")","/"), keyColor, height, radius)
            addRow(container, listOf("=","*","\"","' ",":",";","!","?","⌫"), keyColor, height, radius) {
                if (it == "⌫") deleteOne() else commit(it)
            }
        } else {
            addRow(container, rows[0].map { it.toString() }, keyColor, height, radius)
            addRow(container, rows[1].map { it.toString() }, keyColor, height, radius)
            addRow(container, rows[2].map { it.toString() }, keyColor, height, radius) {
                when (it) {
                    "⇧" -> { shifted = !shifted; refreshKeyboard() }
                    "⌫" -> deleteOne()
                    else -> commit(if (shifted) it.uppercase() else it)
                }
            }
        }

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addSpecialKey(bottom, if (numbers) "ABC" else "?123", 1.25f, keyColor, height, radius) {
            numbers = !numbers
            refreshKeyboard()
        }
        addSpecialKey(bottom, ",", 0.75f, keyColor, height, radius) { commit(",") }
        addSpecialKey(bottom, spaceLabel, 4.4f, keyColor, height, radius) { commit(" ") }
        addSpecialKey(bottom, ".", 0.75f, keyColor, height, radius) { commit(".") }
        addSpecialKey(bottom, "↵", 1.25f, keyColor, height, radius) { enter() }
        container.addView(bottom)
        return container
    }

    private fun addRow(
        parent: LinearLayout,
        labels: List<String>,
        keyColor: Int,
        height: Int,
        radius: Int,
        special: ((String) -> Unit)? = null
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        labels.forEach { label ->
            val shown = if (label.length == 1 && label[0].isLetter() && shifted) label.uppercase() else label
            val weight = if (label == "⇧" || label == "⌫") 1.25f else 1f
            addKey(row, shown, weight, keyColor, height, radius) {
                if (special != null) special(label) else commit(shown)
            }
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(5)
        parent.addView(row, params)
    }

    private fun addSpecialKey(
        row: LinearLayout, label: String, weight: Float, keyColor: Int,
        height: Int, radius: Int, action: () -> Unit
    ) {
        addKey(row, label, weight, keyColor, height, radius, action)
    }

    private fun addKey(
        row: LinearLayout, label: String, weight: Float, keyColor: Int,
        height: Int, radius: Int, action: () -> Unit
    ) {
        val dark = keyColor == 0xFF222033.toInt()
        val view = TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = if (label.length > 2) 11f else 17f
            setTextColor(if (dark) Color.WHITE else 0xFF29243A.toInt())
            background = GradientDrawable().apply {
                setColor(keyColor)
                cornerRadius = dp(radius).toFloat()
                setStroke(dp(1), 0x66FFFFFF)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val params = LinearLayout.LayoutParams(0, dp(height), weight)
        params.setMargins(dp(2), dp(1), dp(2), dp(1))
        row.addView(view, params)
    }

    private fun commit(value: String) {
        currentInputConnection?.commitText(value, 1)
        if (shifted && value.length == 1 && value[0].isLetter()) {
            shifted = false
            refreshKeyboard()
        }
    }

    private fun deleteOne() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun enter() {
        val connection = currentInputConnection ?: return
        val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
            ?: EditorInfo.IME_ACTION_NONE
        if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
            connection.performEditorAction(action)
        } else {
            connection.commitText("\n", 1)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
