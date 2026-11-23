package com.keyboardstudio.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class KeyboardTheme(
    val name: String,
    val subtitle: String,
    val colors: List<Color>,
    val keyColor: Color,
    val textColor: Color = Color(0xFF29243A)
)

private val starterThemes = listOf(
    KeyboardTheme("Cotton Candy", "Soft pink and violet", listOf(Color(0xFFFF8FC7), Color(0xFF9B8CFF)), Color(0xFFFFF1FA)),
    KeyboardTheme("Ocean Pop", "Fresh blue gradient", listOf(Color(0xFF35D6E8), Color(0xFF4776FF)), Color(0xFFEAFBFF)),
    KeyboardTheme("Sunset", "Warm orange and coral", listOf(Color(0xFFFFA34D), Color(0xFFFF5D83)), Color(0xFFFFF3E9)),
    KeyboardTheme("Mint Glow", "Cool mint colors", listOf(Color(0xFF69E6B5), Color(0xFF42B9D8)), Color(0xFFE9FFF7)),
    KeyboardTheme("Berry", "Bold purple tones", listOf(Color(0xFFB66BFF), Color(0xFF633BDB)), Color(0xFFF4EBFF)),
    KeyboardTheme("Lemonade", "Bright and playful", listOf(Color(0xFFFFE66D), Color(0xFFFFA94D)), Color(0xFFFFFBE5))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Color(0xFF7956E8),
                secondary = Color(0xFFFF6FAE),
                background = Color(0xFFFFF9FE),
                surface = Color.White
            )) {
                KeyboardStudioApp {
                    runCatching {
                        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyboardStudioApp(onOpenKeyboardSettings: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    var tab by remember { mutableStateOf("Themes") }
    val theme = starterThemes[selected]

    Scaffold(
        containerColor = Color(0xFFFFF9FE),
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = tab == "Themes",
                    onClick = { tab = "Themes" },
                    icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                    label = { Text("Themes") }
                )
                NavigationBarItem(
                    selected = tab == "Customize",
                    onClick = { tab = "Customize" },
                    icon = { Icon(Icons.Rounded.Keyboard, contentDescription = null) },
                    label = { Text("Customize") }
                )
                NavigationBarItem(
                    selected = tab == "Settings",
                    onClick = { tab = "Settings" },
                    icon = { Icon(Icons.Rounded.Settings, contentDescription = null) },
                    label = { Text("Settings") }
                )
            }
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(15.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFFFF79B7), Color(0xFF8067F5)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Keyboard, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Keyboard Studio", fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF28213D))
                    Text("Make your keyboard feel like you", fontSize = 12.sp, color = Color(0xFF8B829B))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                when (tab) {
                    "Themes" -> "Find your\ncolor mood."
                    "Customize" -> "Make it yours."
                    else -> "Your keyboard setup."
                },
                fontSize = 32.sp, lineHeight = 36.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF28213D)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                if (tab == "Settings") "Helpful shortcuts and compatibility information."
                else "Explore playful gradients and preview every detail.",
                color = Color(0xFF82798F), fontSize = 14.sp
            )
            Spacer(Modifier.height(20.dp))

            if (tab == "Settings") {
                SettingsPanel(onOpenKeyboardSettings)
            } else {
                KeyboardPreview(theme)
                Spacer(Modifier.height(22.dp))
                if (tab == "Themes") {
                    Text("POPULAR THEMES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92889F), letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(12.dp))
                    starterThemes.forEachIndexed { index, item ->
                        ThemeRow(item, selected == index) { selected = index }
                        Spacer(Modifier.height(10.dp))
                    }
                } else {
                    Text("COLOR PALETTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92889F), letterSpacing = 1.4.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        starterThemes.forEachIndexed { index, item ->
                            Column(
                                Modifier.width(86.dp).clip(RoundedCornerShape(16.dp))
                                    .clickable { selected = index }.padding(5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(Modifier.size(64.dp).clip(RoundedCornerShape(18.dp)).background(Brush.linearGradient(item.colors)))
                                Spacer(Modifier.height(6.dp))
                                Text(item.name, fontSize = 11.sp, color = Color(0xFF51485F))
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Text("Key style", fontWeight = FontWeight.Bold, color = Color(0xFF302840))
                    Text(
                        "Choose a palette above to update the keyboard preview. Detailed editing controls are coming next.",
                        fontSize = 13.sp, color = Color(0xFF82798F)
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KeyboardPreview(theme: KeyboardTheme) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp))
            .background(Brush.linearGradient(theme.colors)).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Preview", color = Color.White, fontWeight = FontWeight.Bold)
            Text("ABC  •  English", color = Color.White.copy(alpha = .85f), fontSize = 11.sp)
        }
        Spacer(Modifier.height(12.dp))
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
        rows.forEachIndexed { rowIndex, letters ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (rowIndex == 2) PreviewKey("⇧", Modifier.weight(1.1f), theme)
                letters.forEach { letter -> PreviewKey(letter.toString(), Modifier.weight(1f), theme) }
                if (rowIndex == 2) PreviewKey("⌫", Modifier.weight(1.1f), theme)
            }
            Spacer(Modifier.height(6.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PreviewKey("?123", Modifier.weight(1.2f), theme)
            PreviewKey("☺", Modifier.weight(.9f), theme)
            PreviewKey(",", Modifier.weight(.7f), theme)
            PreviewKey("English (US)", Modifier.weight(4f), theme)
            PreviewKey(".", Modifier.weight(.7f), theme)
            PreviewKey("↵", Modifier.weight(1.2f), theme)
        }
    }
}

@Composable
private fun PreviewKey(label: String, modifier: Modifier, theme: KeyboardTheme) {
    Box(
        modifier.height(39.dp).clip(RoundedCornerShape(9.dp))
            .background(theme.keyColor.copy(alpha = .94f))
            .border(1.dp, Color.White.copy(alpha = .35f), RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = theme.textColor,
            fontSize = if (label.length > 2) 9.sp else 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ThemeRow(theme: KeyboardTheme, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White)
            .clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(theme.colors)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(theme.name, fontWeight = FontWeight.Bold, color = Color(0xFF302840))
            Text(theme.subtitle, fontSize = 12.sp, color = Color(0xFF92889F))
        }
        if (isSelected) Icon(Icons.Rounded.Check, contentDescription = null, tint = Color(0xFF7956E8))
    }
}

@Composable
private fun SettingsPanel(onOpenKeyboardSettings: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(18.dp)
    ) {
        Text("Apply your design", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF302840))
        Spacer(Modifier.height(8.dp))
        Text(
            "Android does not provide a universal way for one app to change another keyboard's appearance. Gboard, Samsung Keyboard and SwiftKey manage themes separately.",
            fontSize = 13.sp, color = Color(0xFF746B80), lineHeight = 19.sp
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onOpenKeyboardSettings, modifier = Modifier.fillMaxWidth()) {
            Text("Open keyboard settings")
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Keyboard Studio previews designs inside this app. Importing a design into another keyboard depends on that keyboard's own supported features.",
            fontSize = 12.sp, color = Color(0xFF92889F)
        )
    }
}
