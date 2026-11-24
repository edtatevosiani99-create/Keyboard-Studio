package com.keyboardstudio.app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.provider.Settings
import android.widget.Toast
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.roundToInt

class KeyboardService : InputMethodService() {
    private lateinit var keyboardPrefs: SharedPreferences

    private val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf("selected", "keyColor", "height", "rounding", "language", "keyboardLanguage") &&
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
    private val supportedLanguages = listOf("English", "ქართული", "Русский", "Deutsch", "Español", "Français", "Türkçe", "العربية")
    private var shifted = false
    private var numbers = false
    private var emojiPanel = false
    private var menuPanel = false
    private var textEditMode = false
    private var selectMode = false
    private var oneHandMode = false
    private var oneHandOnLeft = false
    private var root: LinearLayout? = null
    private val quickEmojis = listOf("😀", "😂", "🥰", "😍", "😘", "😊", "😭", "😉", "😎", "❤️", "👍", "🙏")

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
        return createKeyboardSafely()
    }

    private fun createKeyboardSafely(): View {
        return try {
            buildKeyboard()
        } catch (error: Throwable) {
            android.util.Log.e("KeyboardStudio", "Keyboard view creation failed", error)
            createFallbackKeyboard()
        }
    }

    private fun createFallbackKeyboard(): View {
        val fallback = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(0xFFEDEAF5.toInt())
        }
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p").forEach { letter ->
            val key = TextView(this).apply {
                text = letter
                textSize = 20f
                gravity = Gravity.CENTER
                setTextColor(Color.BLACK)
                background = GradientDrawable().apply {
                    setColor(Color.WHITE)
                    cornerRadius = dp(7).toFloat()
                }
                setOnClickListener { currentInputConnection?.commitText(letter, 1) }
            }
            fallback.addView(key, LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                setMargins(dp(2), 0, dp(2), 0)
            })
        }
        return fallback
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        shifted = info?.let { it.inputType and android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES != 0 } ?: false
        refreshKeyboard()
    }

    private fun refreshKeyboard() {
        runCatching { setInputView(createKeyboardSafely()) }
            .onFailure { android.util.Log.e("KeyboardStudio", "Keyboard refresh failed", it) }
    }

    private fun buildKeyboard(): LinearLayout {
        val prefs = getSharedPreferences("keyboard_studio", MODE_PRIVATE)
        val selected = prefs.getInt("selected", 0).coerceIn(0, 29)
        val palette = palettes[selected % palettes.size]
        val keyColor = keyColors[prefs.getInt("keyColor", 0).coerceIn(0, 9)]
        val height = prefs.getFloat("height", 39f).roundToInt().coerceIn(30, 54)
        val radius = prefs.getFloat("rounding", 9f).roundToInt().coerceIn(2, 18)
        val language = prefs.getString("keyboardLanguage", null)
            ?: (prefs.getString("language", "English") ?: "English")
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

        addToolbar(container, keyColor, height, radius)
        if (textEditMode) {
            addTextEditingPanel(container, keyColor, height, radius)
            return container
        }
        if (menuPanel) {
            addKeyboardMenuPanel(container, keyColor, height, radius)
            return container
        }
        if (emojiPanel) {
            addRow(container, quickEmojis, keyColor, (height - 4).coerceAtLeast(26), radius) { emoji ->
                commit(emoji)
                emojiPanel = false
                refreshKeyboard()
            }
        }

        if (numbers) {
            addRow(container, listOf("1","2","3","4","5","6","7","8","9","0"), keyColor, height, radius)
            addRow(container, listOf("@","#","\$","%","&","-","+","(",")","/"), keyColor, height, radius)
            addRow(container, listOf("=","*","\"","' ",":",";","!","?","⌫"), keyColor, height, radius) {
                if (it == "⌫") deleteOne() else commit(it)
            }
        } else {
            addRow(container, listOf("1","2","3","4","5","6","7","8","9","0"), keyColor, (height - 6).coerceAtLeast(26), radius)
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
        addSpecialKey(bottom, "☺", 0.8f, keyColor, height, radius) {
            emojiPanel = !emojiPanel
            refreshKeyboard()
        }
        addSpecialKey(bottom, "🌐", 0.9f, keyColor, height, radius) { switchKeyboardLanguage() }
        addSpecialKey(bottom, ",", 0.6f, keyColor, height, radius) { commit(",") }
        addSpecialKey(bottom, spaceLabel, 3.2f, keyColor, height, radius) { commit(" ") }
        addSpecialKey(bottom, ".", 0.6f, keyColor, height, radius) { commit(".") }
        addSpecialKey(bottom, "↵", 1.25f, keyColor, height, radius) { enter() }
        container.addView(bottom)
        return if (oneHandMode) {
            buildOneHandContainer(container, keyColor, height, radius, palette)
        } else {
            container
        }
    }

    private fun buildOneHandContainer(
        keyboard: LinearLayout, keyColor: Int, height: Int, radius: Int, palette: IntArray
    ): LinearLayout {
        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(palette[0], palette[1])
            )
        }
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        addOneHandControl(controls, "⛶", keyColor, height) {
            oneHandMode = false
            refreshKeyboard()
        }
        addOneHandControl(controls, if (oneHandOnLeft) "›" else "‹", keyColor, height) {
            oneHandOnLeft = !oneHandOnLeft
            refreshKeyboard()
        }
        addOneHandControl(controls, "↔", keyColor, height) {
            oneHandOnLeft = !oneHandOnLeft
            refreshKeyboard()
        }
        val keyboardParams = LinearLayout.LayoutParams(0, -2, 4f)
        val spacerParams = LinearLayout.LayoutParams(0, 1, 1f)
        val controlsParams = LinearLayout.LayoutParams(dp(48), -2)
        val spacer = View(this)
        if (oneHandOnLeft) {
            outer.addView(spacer, spacerParams)
            outer.addView(keyboard, keyboardParams)
            outer.addView(controls, controlsParams)
        } else {
            outer.addView(controls, controlsParams)
            outer.addView(keyboard, keyboardParams)
            outer.addView(spacer, spacerParams)
        }
        return outer
    }

    private fun addOneHandControl(
        parent: LinearLayout, label: String, keyColor: Int, height: Int, action: () -> Unit
    ) {
        val dark = keyColor == 0xFF222033.toInt()
        val control = TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 23f
            setTextColor(if (dark) Color.WHITE else 0xFF29243A.toInt())
            background = GradientDrawable().apply {
                setColor(keyColor)
                cornerRadius = dp(24).toFloat()
            }
            setOnClickListener { action() }
        }
        parent.addView(control, LinearLayout.LayoutParams(dp(42), dp(48)).apply {
            setMargins(dp(2), dp(5), dp(2), dp(5))
        })
    }

    private fun addToolbar(parent: LinearLayout, keyColor: Int, height: Int, radius: Int) {
        val toolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addSpecialKey(toolbar, "▦", 1f, keyColor, (height * 0.78f).roundToInt(), radius) {
            showKeyboardMenu()
        }
        addSpecialKey(toolbar, "▤", 1f, keyColor, (height * 0.78f).roundToInt(), radius) {
            pasteFromClipboard()
        }
        addSpecialKey(toolbar, "文A", 1f, keyColor, (height * 0.78f).roundToInt(), radius) {
            translateSelectedText()
        }
        addSpecialKey(toolbar, "⚙", 1f, keyColor, (height * 0.78f).roundToInt(), radius) {
            openKeyboardSettings()
        }
        addSpecialKey(toolbar, "🎙", 1f, keyColor, (height * 0.78f).roundToInt(), radius) {
            startVoiceInput()
        }
        val params = LinearLayout.LayoutParams(-1, -2)
        params.bottomMargin = dp(5)
        parent.addView(toolbar, params)
    }

    private fun showKeyboardMenu() {
        menuPanel = !menuPanel
        emojiPanel = false
        textEditMode = false
        refreshKeyboard()
    }

    private fun addKeyboardMenuPanel(parent: LinearLayout, keyColor: Int, height: Int, radius: Int) {
        val items = listOf(
            "Изменение текста", "GIF", "Режим одной руки",
            "Эмодзи", "Тема", "Стикеры",
            "Плавающий режим", "Изменить размер", "Автозаполнение"
        )
        items.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
            }
            pair.forEach { title ->
                addSpecialKey(row, title, 1f, keyColor, height, radius) {
                    when (title) {
                        "Изменение текста" -> {
                            menuPanel = false
                            textEditMode = true
                            selectMode = false
                            refreshKeyboard()
                        }
                        "Режим одной руки" -> {
                            menuPanel = false
                            oneHandMode = true
                            oneHandOnLeft = false
                            refreshKeyboard()
                        }
                        else -> Toast.makeText(this, "$title — скоро будет добавлено", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (pair.size == 1) {
                addSpecialKey(row, "", 1f, keyColor, height, radius) {}
            }
            val params = LinearLayout.LayoutParams(-1, -2)
            params.bottomMargin = dp(5)
            parent.addView(row, params)
        }
    }

    private fun addTextEditingPanel(parent: LinearLayout, keyColor: Int, height: Int, radius: Int) {
        val titleRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        addSpecialKey(titleRow, "←", 0.8f, keyColor, height, radius) {
            textEditMode = false
            selectMode = false
            refreshKeyboard()
        }
        val title = TextView(this).apply {
            text = "Редактирование текста"
            textSize = 17f
            gravity = Gravity.CENTER_VERTICAL
            setTextColor(if (keyColor == 0xFF222033.toInt()) Color.WHITE else 0xFF29243A.toInt())
        }
        titleRow.addView(title, LinearLayout.LayoutParams(0, dp(height), 3f))
        parent.addView(titleRow)

        val grid = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val leftColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val middleColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val rightColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val actionColumn = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        addEditKey(leftColumn, "‹", 1f, keyColor, height * 3, radius) { sendCursorKey(KeyEvent.KEYCODE_DPAD_LEFT) }
        addEditKey(middleColumn, "⌃", 1f, keyColor, height, radius) { sendCursorKey(KeyEvent.KEYCODE_DPAD_UP) }
        addEditKey(middleColumn, if (selectMode) "Отменить выделение" else "Выделить", 1f, keyColor, height) {
            selectMode = !selectMode
            refreshKeyboard()
        }
        addEditKey(middleColumn, "⌄", 1f, keyColor, height, radius) { sendCursorKey(KeyEvent.KEYCODE_DPAD_DOWN) }
        addEditKey(rightColumn, "›", 1f, keyColor, height * 3, radius) { sendCursorKey(KeyEvent.KEYCODE_DPAD_RIGHT) }
        addEditKey(actionColumn, "Выделить всё", 1f, keyColor, height) {
            currentInputConnection?.performContextMenuAction(android.R.id.selectAll)
        }
        addEditKey(actionColumn, "Копировать", 1f, keyColor, height) {
            currentInputConnection?.performContextMenuAction(android.R.id.copy)
        }
        addEditKey(actionColumn, "Вставить", 1f, keyColor, height) {
            pasteFromClipboard()
        }

        listOf(leftColumn, middleColumn, rightColumn, actionColumn).forEachIndexed { index, col ->
            val weight = if (index == 3) 1.15f else 1f
            grid.addView(col, LinearLayout.LayoutParams(0, -2, weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            })
        }
        parent.addView(grid)

        val bottom = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        addEditKey(bottom, "К началу", 1f, keyColor, height) { sendCursorKey(KeyEvent.KEYCODE_MOVE_HOME) }
        addEditKey(bottom, "К концу", 1f, keyColor, height) { sendCursorKey(KeyEvent.KEYCODE_MOVE_END) }
        addEditKey(bottom, "⌫", 0.7f, keyColor, height) { deleteOne() }
        parent.addView(bottom)
    }

    private fun addEditKey(
        row: LinearLayout, label: String, weight: Float, keyColor: Int,
        height: Int, radius: Int = 12, action: () -> Unit
    ) {
        val dark = keyColor == 0xFF222033.toInt()
        val key = TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = when {
                label.length > 12 -> 13f
                label.length > 5 -> 15f
                else -> 21f
            }
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
        val params = if (row.orientation == LinearLayout.VERTICAL) {
            LinearLayout.LayoutParams(-1, dp(height)).apply {
                setMargins(dp(2), dp(3), dp(2), dp(3))
            }
        } else {
            LinearLayout.LayoutParams(0, dp(height), weight).apply {
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
        }
        row.addView(key, params)
    }

    private fun sendCursorKey(keyCode: Int) {
        val connection = currentInputConnection ?: return
        val meta = if (selectMode) KeyEvent.META_SHIFT_ON else 0
        connection.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN, keyCode, 0, meta))
        connection.sendKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP, keyCode, 0, meta))
    }

    private fun translateSelectedText() {
        val selectedText = currentInputConnection?.getSelectedText(0)?.toString().orEmpty()
        if (selectedText.isBlank()) {
            Toast.makeText(this, "Select text first to translate", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "https://translate.google.com/?sl=auto&tl=en&text=" +
            android.net.Uri.encode(selectedText) + "&op=translate"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
            .onFailure { Toast.makeText(this, "Translation could not be opened", Toast.LENGTH_SHORT).show() }
    }

    private fun startVoiceInput() {
        val intent = Intent("android.speech.action.RECOGNIZE_SPEECH").apply {
            putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form")
            putExtra("android.speech.extra.PROMPT", "Speak now")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
            .onFailure {
                Toast.makeText(this, "Voice input is not available on this device", Toast.LENGTH_SHORT).show()
            }
    }

    private fun pasteFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip == null || clip.itemCount == 0) {
            Toast.makeText(this, "Clipboard is empty", Toast.LENGTH_SHORT).show()
            return
        }
        val value = clip.getItemAt(0).coerceToText(this)?.toString().orEmpty()
        if (value.isNotEmpty()) {
            currentInputConnection?.commitText(value, 1)
        }
    }

    private fun openKeyboardSettings() {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { startActivity(intent) }
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

    private fun switchKeyboardLanguage() {
        val current = keyboardPrefs.getString("keyboardLanguage", null)
            ?: (keyboardPrefs.getString("language", "English") ?: "English")
        val index = supportedLanguages.indexOf(current).let { if (it < 0) 0 else it }
        val next = supportedLanguages[(index + 1) % supportedLanguages.size]
        keyboardPrefs.edit().putString("keyboardLanguage", next).apply()
        shifted = false
        numbers = false
        refreshKeyboard()
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
