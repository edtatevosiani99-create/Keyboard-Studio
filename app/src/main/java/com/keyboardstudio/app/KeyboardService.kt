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
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
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
    private var oneHandAdjustMode = false
    // Width limits match the supplied minimum and maximum Gboard examples.
    private var oneHandWidth = 0.78f
    private var oneHandHeightScale = 1.0f
    private var oneHandPositionX = Float.NaN
    private var oneHandPositionY = 0f
    private var root: LinearLayout? = null
    private var activeThemeIndex: Int = 0
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
        intArrayOf(0xFFFC4A1A.toInt(), 0xFFF7B733.toInt()),
        // Dracula
        intArrayOf(0xFF7A0718.toInt(), 0xFF12040A.toInt()),
        // Minions
        intArrayOf(0xFFFFD21F.toInt(), 0xFF0755A5.toInt()),
        // Wolf
        intArrayOf(0xFF69D9FF.toInt(), 0xFF071B43.toInt()),
        // Dragon
        intArrayOf(0xFFFF6A00.toInt(), 0xFF260707.toInt()),
        // Cyberpunk
        intArrayOf(0xFFFF00C8.toInt(), 0xFF062A63.toInt()),
        // Sakura
        intArrayOf(0xFFFFB7D5.toInt(), 0xFFB83280.toInt()),
        // Galaxy
        intArrayOf(0xFF6426C7.toInt(), 0xFF071B68.toInt()),
        // Ocean
        intArrayOf(0xFF007F9E.toInt(), 0xFF00E0C6.toInt()),
        // Autumn
        intArrayOf(0xFFFFB02E.toInt(), 0xFF9D3D16.toInt()),
        // Ice
        intArrayOf(0xFF9BE7FF.toInt(), 0xFF2478D4.toInt()),
        // Rainbow
        intArrayOf(0xFFFF4FA3.toInt(), 0xFF633BFF.toInt()),
        // Gothic Rose
        intArrayOf(0xFF170407.toInt(), 0xFF030303.toInt()),
        // Raven
        intArrayOf(0xFF202A38.toInt(), 0xFF030507.toInt()),
        // Cross
        intArrayOf(0xFF300509.toInt(), 0xFF030303.toInt()),
        // Purple Gothic
        intArrayOf(0xFF26083D.toInt(), 0xFF030207.toInt()),
        // Matte Black
        intArrayOf(0xFF181818.toInt(), 0xFF050505.toInt()),
        // Shadow Gold
        intArrayOf(0xFF33230B.toInt(), 0xFF030303.toInt())
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

    private fun buildKeyboard(): View {
        val prefs = getSharedPreferences("keyboard_studio", MODE_PRIVATE)
        val selected = prefs.getInt("selected", 0).coerceIn(0, 46)
        activeThemeIndex = selected
        val paletteIndex = if (selected >= 30) selected - 20 else selected % 10
        val palette = palettes[paletteIndex.coerceIn(0, palettes.lastIndex)]
        val keyColor = if (selected >= 41) 0xFF080808.toInt()
            else keyColors[prefs.getInt("keyColor", 0).coerceIn(0, 9)]
        val baseHeight = prefs.getFloat("height", 39f).roundToInt().coerceIn(30, 54)
        val height = if (oneHandMode) {
            (baseHeight * oneHandHeightScale).roundToInt().coerceIn(24, 64)
        } else baseHeight
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
    ): FrameLayout {
        val screenWidth = resources.displayMetrics.widthPixels
        val keyboardWidth = (screenWidth * oneHandWidth).roundToInt()
            .coerceIn((screenWidth * 0.75f).roundToInt(), (screenWidth * 0.90f).roundToInt())
        keyboard.measure(
            View.MeasureSpec.makeMeasureSpec(keyboardWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val keyboardHeight = keyboard.measuredHeight.coerceAtLeast(dp(180))
        val frame = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(-1, -1)
            setBackground(
                GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(palette[0], palette[1])
                )
            )
        }

        if (oneHandAdjustMode) {
            buildOneHandAdjustView(frame, keyboard, keyboardWidth, keyboardHeight, keyColor, palette)
            return frame
        }

        val group = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(4), dp(4), dp(4))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(palette[0], palette[1])
            ).apply { cornerRadius = dp(14).toFloat() }
        }
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        addOneHandControl(controls, "⛶", keyColor, height) {
            oneHandMode = false
            oneHandAdjustMode = false
            oneHandPositionX = Float.NaN
            oneHandPositionY = 0f
            refreshKeyboard()
        }
        addOneHandControl(controls, if (oneHandOnLeft) "›" else "‹", keyColor, height) {
            oneHandOnLeft = !oneHandOnLeft
            oneHandPositionX = Float.NaN
            refreshKeyboard()
        }
        addOneHandControl(controls, "✥", keyColor, height) {
            oneHandAdjustMode = true
            refreshKeyboard()
        }

        val groupWidth = keyboardWidth + dp(48) + dp(8)
        if (oneHandOnLeft) {
            group.addView(keyboard, LinearLayout.LayoutParams(keyboardWidth, -2))
            group.addView(controls, LinearLayout.LayoutParams(dp(48), -2))
        } else {
            group.addView(controls, LinearLayout.LayoutParams(dp(48), -2))
            group.addView(keyboard, LinearLayout.LayoutParams(keyboardWidth, -2))
        }

        val maxX = (screenWidth - groupWidth).coerceAtLeast(0)
        val defaultX = if (oneHandOnLeft) 0 else maxX
        val x = if (oneHandPositionX.isNaN()) defaultX else
            oneHandPositionX.roundToInt().coerceIn(0, maxX)
        val maxY = dp(76)
        val y = oneHandPositionY.roundToInt().coerceIn(0, maxY)
        val params = FrameLayout.LayoutParams(groupWidth, keyboardHeight + dp(8)).apply {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            leftMargin = x
            bottomMargin = y
        }
        frame.addView(group, params)
        return frame
    }

    private fun buildOneHandAdjustView(
        frame: FrameLayout,
        keyboard: LinearLayout,
        keyboardWidth: Int,
        keyboardHeight: Int,
        keyColor: Int,
        palette: IntArray
    ) {
        val screenWidth = resources.displayMetrics.widthPixels
        val panelHeight = keyboardHeight + dp(76)
        val maxX = (screenWidth - keyboardWidth).coerceAtLeast(0)
        val defaultX = if (oneHandOnLeft) 0 else maxX
        val initialX = if (oneHandPositionX.isNaN()) defaultX else
            oneHandPositionX.roundToInt().coerceIn(0, maxX)
        val initialY = oneHandPositionY.roundToInt().coerceIn(0, dp(76))

        val panel = FrameLayout(this).apply {
            background = GradientDrawable().apply {
                setColor(0xBDF7F6FF.toInt())
                cornerRadius = dp(28).toFloat()
                setStroke(dp(2), 0xFF7180A7.toInt())
            }
        }
        keyboard.alpha = 0.30f
        val keyboardParams = FrameLayout.LayoutParams(keyboardWidth - dp(16), keyboardHeight - dp(16)).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = dp(8)
        }
        panel.addView(keyboard, keyboardParams)

        val reset = makeAdjustPill("⟲  Reset")
        reset.setOnClickListener {
            oneHandWidth = 0.90f
            oneHandHeightScale = 1.0f
            oneHandPositionX = Float.NaN
            oneHandPositionY = 0f
            refreshKeyboard()
        }
        panel.addView(reset, FrameLayout.LayoutParams(dp(142), dp(48)).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            topMargin = (keyboardHeight * 0.27f).roundToInt()
        })

        val move = makeAdjustPill("✥")
        panel.addView(move, FrameLayout.LayoutParams(dp(54), dp(54)).apply {
            gravity = Gravity.CENTER
        })
        var downX = 0f
        var downY = 0f
        var startX = 0f
        var startY = 0f
        move.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startX = panel.translationX
                    startY = panel.translationY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    panel.translationX = (startX + event.rawX - downX)
                        .coerceIn(-initialX.toFloat(), (maxX - initialX).toFloat())
                    panel.translationY = (startY + event.rawY - downY)
                        .coerceIn(-initialY.toFloat(), (dp(76) - initialY).toFloat())
                    true
                }
                MotionEvent.ACTION_UP -> {
                    oneHandPositionX = (initialX + panel.translationX).coerceIn(0f, maxX.toFloat())
                    oneHandPositionY = (initialY + panel.translationY).coerceIn(0f, dp(76).toFloat())
                    true
                }
                MotionEvent.ACTION_CANCEL -> true
                else -> false
            }
        }

        val done = makeAdjustPill("✓  Done")
        done.setOnClickListener {
            oneHandPositionX = (initialX + panel.translationX).coerceIn(0f, maxX.toFloat())
            oneHandPositionY = (initialY + panel.translationY).coerceIn(0f, dp(76).toFloat())
            oneHandAdjustMode = false
            refreshKeyboard()
        }
        panel.addView(done, FrameLayout.LayoutParams(dp(142), dp(48)).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            bottomMargin = dp(14)
        })

        addResizeHandle(panel, "◜", Gravity.TOP or Gravity.LEFT, true, true)
        addResizeHandle(panel, "◝", Gravity.TOP or Gravity.RIGHT, false, true)
        addResizeHandle(panel, "◟", Gravity.BOTTOM or Gravity.LEFT, true, false)
        addResizeHandle(panel, "◞", Gravity.BOTTOM or Gravity.RIGHT, false, false)

        frame.addView(panel, FrameLayout.LayoutParams(keyboardWidth, panelHeight).apply {
            gravity = Gravity.BOTTOM or Gravity.LEFT
            leftMargin = initialX
            bottomMargin = initialY
        })
    }

    private fun makeAdjustPill(label: String): TextView {
        return TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 16f
            setTextColor(0xFF35415D.toInt())
            background = GradientDrawable().apply {
                setColor(0xFFE0E6FF.toInt())
                cornerRadius = dp(28).toFloat()
            }
            isClickable = true
            isFocusable = true
        }
    }

    private fun addResizeHandle(
        panel: FrameLayout, label: String, gravity: Int, leftCorner: Boolean, topCorner: Boolean
    ) {
        val handle = TextView(this).apply {
            text = label
            textSize = 28f
            this.gravity = Gravity.CENTER
            setTextColor(0xFF61739F.toInt())
        }
        var downX = 0f
        var downY = 0f
        var startWidth = oneHandWidth
        var startHeight = oneHandHeightScale
        var previewWidth = startWidth
        var previewHeight = startHeight
        handle.setOnTouchListener { _, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    startWidth = oneHandWidth
                    startHeight = oneHandHeightScale
                    previewWidth = startWidth
                    previewHeight = startHeight
                    // Keep the opposite corner fixed while the dragged corner moves.
                    panel.pivotX = if (leftCorner) panel.width.toFloat() else 0f
                    panel.pivotY = if (topCorner) panel.height.toFloat() else 0f
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downX
                    val dy = event.rawY - downY
                    val widthDelta = (if (leftCorner) -dx else dx) /
                        resources.displayMetrics.widthPixels.toFloat()
                    val heightDelta = (if (topCorner) -dy else dy) / dp(520).toFloat()
                    previewWidth = (startWidth + widthDelta).coerceIn(0.75f, 0.90f)
                    previewHeight = (startHeight + heightDelta).coerceIn(0.75f, 1.25f)

                    // Live preview: scale the whole keyboard frame on every movement event.
                    panel.scaleX = previewWidth / startWidth
                    panel.scaleY = previewHeight / startHeight
                    true
                }
                MotionEvent.ACTION_UP -> {
                    oneHandWidth = previewWidth
                    oneHandHeightScale = previewHeight
                    oneHandPositionX = Float.NaN
                    panel.scaleX = 1f
                    panel.scaleY = 1f
                    refreshKeyboard()
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    panel.scaleX = 1f
                    panel.scaleY = 1f
                    true
                }
                else -> false
            }
        }
        val isRight = gravity and Gravity.RIGHT == Gravity.RIGHT
        val isBottom = gravity and Gravity.BOTTOM == Gravity.BOTTOM
        panel.addView(handle, FrameLayout.LayoutParams(dp(38), dp(38)).apply {
            this.gravity = gravity
            if (isRight) rightMargin = dp(5) else leftMargin = dp(5)
            if (isBottom) bottomMargin = dp(5) else topMargin = dp(5)
        })
    }

    private fun addOneHandControl(
        parent: LinearLayout, label: String, keyColor: Int, height: Int, action: () -> Unit
    ): TextView {
        val dark = keyColor == 0xFF222033.toInt()
        val control = TextView(this).apply {
            text = label
            gravity = Gravity.CENTER
            textSize = 23f
            setTextColor(if (dark) Color.WHITE else 0xFF29243A.toInt())
            background = GradientDrawable().apply {
                setColor(keyColor)
                cornerRadius = dp(24).toFloat()
                setStroke(dp(1), 0x66FFFFFF)
            }
            setOnClickListener { action() }
        }
        parent.addView(control, LinearLayout.LayoutParams(dp(42), dp(48)).apply {
            setMargins(dp(2), dp(5), dp(2), dp(5))
        })
        return control
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
            val gothic = activeThemeIndex >= 41
            val accent = when (activeThemeIndex) {
                41 -> 0xFF74202B.toInt()
                42 -> 0xFF718196.toInt()
                43 -> 0xFF8B202A.toInt()
                44 -> 0xFF713A91.toInt()
                45 -> 0xFF171717.toInt()
                46 -> 0xFF9A7734.toInt()
                else -> 0x66FFFFFF.toInt()
            }
            val keyTop = when (activeThemeIndex) {
                41 -> 0xFF292020.toInt()
                42 -> 0xFF293440.toInt()
                43 -> 0xFF2A1518.toInt()
                44 -> 0xFF321B40.toInt()
                45 -> 0xFF111111.toInt()
                46 -> 0xFF302512.toInt()
                else -> keyColor
            }
            val keyBottom = when (activeThemeIndex) {
                41 -> 0xFF080505.toInt()
                42 -> 0xFF070A0E.toInt()
                43 -> 0xFF080303.toInt()
                44 -> 0xFF09040D.toInt()
                45 -> 0xFF090909.toInt()
                46 -> 0xFF090704.toInt()
                else -> keyColor
            }
            setTextColor(if (gothic && activeThemeIndex == 46) 0xFFFFD98A.toInt()
                else if (gothic || dark) Color.WHITE else 0xFF29243A.toInt())
            background = if (gothic) {
                GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(keyTop, keyBottom)).apply {
                    cornerRadius = dp(radius).toFloat()
                    if (activeThemeIndex == 45) {
                        setStroke(0, 0xFF111111.toInt())
                    } else {
                        setStroke(dp(1), accent)
                    }
                }
            } else GradientDrawable().apply {
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
