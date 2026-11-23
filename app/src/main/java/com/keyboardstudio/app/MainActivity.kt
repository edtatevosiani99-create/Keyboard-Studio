package com.keyboardstudio.app

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class KTheme(val name: String, val colors: List<Color>, val key: Color)
private val palettes = listOf(
 listOf(Color(0xFFFF8FC7),Color(0xFF9B8CFF)), listOf(Color(0xFF35D6E8),Color(0xFF4776FF)),
 listOf(Color(0xFFFFA34D),Color(0xFFFF5D83)), listOf(Color(0xFF69E6B5),Color(0xFF42B9D8)),
 listOf(Color(0xFFB66BFF),Color(0xFF633BDB)), listOf(Color(0xFFFFE66D),Color(0xFFFFA94D)),
 listOf(Color(0xFFFF758C),Color(0xFFFF7EB3)), listOf(Color(0xFF43CEA2),Color(0xFF185A9D)),
 listOf(Color(0xFFDA22FF),Color(0xFF9733EE)), listOf(Color(0xFFFC4A1A),Color(0xFFF7B733))
)
private val languages = listOf("English","ქართული","Русский","Deutsch","Español","Français","Türkçe","العربية")
private val uiText = mapOf("ქართული" to mapOf("Themes" to "თემები","Customize" to "მორგება","My Designs" to "ჩემი დიზაინები","Settings" to "პარამეტრები","Interface language" to "ინტერფეისის ენა","Restore original design" to "საწყისი დიზაინის აღდგენა","Open keyboard settings" to "კლავიატურის პარამეტრები"),"Русский" to mapOf("Themes" to "Темы","Customize" to "Настройка","My Designs" to "Мои дизайны","Settings" to "Настройки","Interface language" to "Язык интерфейса","Restore original design" to "Вернуть исходный дизайн","Open keyboard settings" to "Настройки клавиатуры"),"Deutsch" to mapOf("Themes" to "Designs","Customize" to "Anpassen","My Designs" to "Meine Designs","Settings" to "Einstellungen","Interface language" to "Sprache der Oberfläche","Restore original design" to "Originaldesign wiederherstellen","Open keyboard settings" to "Tastatureinstellungen öffnen"),"Español" to mapOf("Themes" to "Temas","Customize" to "Personalizar","My Designs" to "Mis diseños","Settings" to "Ajustes","Interface language" to "Idioma de la interfaz"),"Français" to mapOf("Themes" to "Thèmes","Customize" to "Personnaliser","My Designs" to "Mes créations","Settings" to "Paramètres","Interface language" to "Langue de l’interface"),"Türkçe" to mapOf("Themes" to "Temalar","Customize" to "Özelleştir","My Designs" to "Tasarımlarım","Settings" to "Ayarlar","Interface language" to "Arayüz dili"),"العربية" to mapOf("Themes" to "السمات","Customize" to "تخصيص","My Designs" to "تصميماتي","Settings" to "الإعدادات","Interface language" to "لغة الواجهة"))
private fun tx(language:String,key:String):String = uiText[language]?.get(key) ?: key

private val names = listOf(
 "Cotton Candy","Ocean Pop","Sunset","Mint Glow","Berry","Lemonade","Rose Quartz","Aqua Dream","Violet Sky","Tangerine",
 "Peach Bloom","Arctic Wave","Electric Lilac","Coral Crush","Golden Hour","Bubblegum","Blue Lagoon","Grape Soda","Mango Splash","Emerald",
 "Candy Floss","Midnight Pop","Lavender Mist","Tropical","Cherry Ice","Lime Light","Aurora","Cosmic Pink","Citrus Mix","Dreamy Purple"
)
private val themes = names.mapIndexed { i, n ->
 val key = listOf(Color(0xFFFFF1FA),Color(0xFFEAFBFF),Color(0xFFFFF3E9),Color(0xFFE9FFF7),Color(0xFFF4EBFF))[i % 5]
 KTheme(n, palettes[i % palettes.size], key)
}

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme = lightColorScheme(primary=Color(0xFF7956E8), secondary=Color(0xFFFF6FAE), background=Color(0xFFFFF9FE))) {
    Studio { runCatching { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) } }
   }
  }
 }
}

@Composable
private fun Studio(openSettings: () -> Unit) {
 val context = LocalContext.current
 val prefs = remember { context.getSharedPreferences("keyboard_studio", 0) }
 var selected by remember { mutableIntStateOf(prefs.getInt("selected",0).coerceIn(0,29)) }
 var tab by remember { mutableStateOf("Themes") }
 var language by remember { mutableStateOf(prefs.getString("language","English") ?: "English") }
 var keyHeight by remember { mutableFloatStateOf(prefs.getFloat("height",39f)) }
 var rounding by remember { mutableFloatStateOf(prefs.getFloat("rounding",9f)) }
 var keyColorIndex by remember { mutableIntStateOf(prefs.getInt("keyColor",0)) }
 var photoUri by remember { mutableStateOf(prefs.getString("photo",null)?.let(Uri::parse)) }
 var saved by remember { mutableStateOf(prefs.getStringSet("saved", emptySet())?.toSet() ?: emptySet()) }
 val keyColors = listOf(Color(0xFFFFF1FA),Color(0xFFEAFBFF),Color(0xFFFFF3E9),Color(0xFFE9FFF7),Color(0xFFF4EBFF),Color(0xFF222033),Color.White,Color(0xFFFFE2B8),Color(0xFFD8F4FF),Color(0xFFFFDCEB))
 val current = themes[selected].copy(key=keyColors[keyColorIndex.coerceIn(0,9)])
 val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
  if (uri != null) {
   runCatching { context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION) }
   photoUri=uri; prefs.edit().putString("photo",uri.toString()).apply()
  }
 }
 fun saveDesign() {
  saved = saved + selected.toString()
  prefs.edit().putStringSet("saved",saved).putInt("selected",selected).putFloat("height",keyHeight).putFloat("rounding",rounding).putInt("keyColor",keyColorIndex).apply()
 }
 Scaffold(containerColor=Color(0xFFFFF9FE), bottomBar={
  NavigationBar(containerColor=Color.White) {
   listOf("Themes","Customize","My Designs","Settings").forEach { item ->
    NavigationBarItem(selected=tab==item,onClick={tab=item},icon={
     Icon(when(item){"Themes"->Icons.Rounded.Palette;"Customize"->Icons.Rounded.Keyboard;"My Designs"->Icons.Rounded.Save;else->Icons.Rounded.Settings},null)
    },label={Text(tx(language,item),fontSize=10.sp)})
   }
  }
 }) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal=20.dp)) {
   Spacer(Modifier.height(20.dp))
   Row(verticalAlignment=Alignment.CenterVertically) {
    Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Brush.linearGradient(listOf(Color(0xFFFF79B7),Color(0xFF8067F5)))),contentAlignment=Alignment.Center) { Icon(Icons.Rounded.Keyboard,null,tint=Color.White) }
    Spacer(Modifier.width(12.dp))
    Column { Text("Keyboard Studio",fontSize=21.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF28213D)); Text("Make your keyboard feel like you",fontSize=12.sp,color=Color(0xFF8B829B)) }
   }
   Spacer(Modifier.height(22.dp))
   Text(when(tab){"Themes"->"Find your\ncolor mood.";"Customize"->"Make it yours.";"My Designs"->"Your creations.";else->"Your keyboard setup."},fontSize=31.sp,lineHeight=35.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF28213D))
   Spacer(Modifier.height(7.dp))
   Text(when(tab){"Themes"->"Explore 30 colorful gradient themes.";"Customize"->"Tune the keys and background of your preview.";"My Designs"->"Your saved designs stay on this device.";else->"Shortcuts and compatibility information."},color=Color(0xFF82798F),fontSize=14.sp)
   Spacer(Modifier.height(18.dp))
   when(tab) {
    "Settings" -> SettingsCard(openSettings,language,{ language=it; prefs.edit().putString("language",it).apply() },{ selected=0; keyHeight=39f; rounding=9f; keyColorIndex=0; photoUri=null; prefs.edit().putInt("selected",0).putFloat("height",39f).putFloat("rounding",9f).putInt("keyColor",0).remove("photo").apply() })
    "My Designs" -> {
     if(saved.isEmpty()) InfoCard("No saved designs yet","Open Customize, choose your colors and tap Save design.")
     else saved.sorted().forEach { id ->
      val i=id.toIntOrNull()?.coerceIn(0,29) ?: 0
      ThemeRow(themes[i],selected==i) { selected=i; prefs.edit().putInt("selected",i).apply(); tab="Customize" }
      Spacer(Modifier.height(9.dp))
     }
    }
    else -> {
     KeyboardPreview(current,keyHeight.dp,rounding.dp,photoUri)
     Spacer(Modifier.height(20.dp))
     if(tab=="Themes") {
      Text("ALL THEMES  •  30",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFF92889F),letterSpacing=1.3.sp)
      Spacer(Modifier.height(12.dp))
      themes.forEachIndexed { i,t ->
       ThemeRow(t,selected==i) { selected=i; prefs.edit().putInt("selected",i).apply() }
       Spacer(Modifier.height(9.dp))
      }
     } else {
      Editor(keyColorIndex,keyColors,keyHeight,rounding,
       { keyColorIndex=it; prefs.edit().putInt("keyColor",it).apply() },
       { keyHeight=it; prefs.edit().putFloat("height",it).apply() },
       { rounding=it; prefs.edit().putFloat("rounding",it).apply() },
       { picker.launch(arrayOf("image/*")) },
       { photoUri=null; prefs.edit().remove("photo").apply() },
       { saveDesign() }, photoUri!=null)
     }
    }
   }
   Spacer(Modifier.height(24.dp))
  }
 }
}

@Composable
private fun KeyboardPreview(theme:KTheme,height:Dp,rounding:Dp,uri:Uri?) {
 val context=LocalContext.current
 val bitmap by produceState<Bitmap?>(null,uri) {
  value=if(uri==null) null else withContext(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull() }
 }
 Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(theme.colors)).padding(13.dp)) {
  if(bitmap!=null) Image(bitmap!!.asImageBitmap(),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
  Column {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text("LIVE PREVIEW",color=Color.White,fontWeight=FontWeight.Bold); Text("ABC • English",color=Color.White,fontSize=11.sp) }
   Spacer(Modifier.height(12.dp))
   listOf("QWERTYUIOP","ASDFGHJKL","ZXCVBNM").forEachIndexed { row,letters ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
     if(row==2) Key("⇧",Modifier.weight(1.1f),theme,height,rounding)
     letters.forEach { Key(it.toString(),Modifier.weight(1f),theme,height,rounding) }
     if(row==2) Key("⌫",Modifier.weight(1.1f),theme,height,rounding)
    }
    Spacer(Modifier.height(5.dp))
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
    Key("?123",Modifier.weight(1.2f),theme,height,rounding); Key("☺",Modifier.weight(.9f),theme,height,rounding)
    Key(",",Modifier.weight(.7f),theme,height,rounding); Key("English",Modifier.weight(4f),theme,height,rounding)
    Key(".",Modifier.weight(.7f),theme,height,rounding); Key("↵",Modifier.weight(1.2f),theme,height,rounding)
   }
  }
 }
}

@Composable
private fun Key(label:String,modifier:Modifier,theme:KTheme,height:Dp,rounding:Dp) {
 Box(modifier.height(height).clip(RoundedCornerShape(rounding)).background(theme.key.copy(alpha=.94f)).border(1.dp,Color.White.copy(alpha=.45f),RoundedCornerShape(rounding)),contentAlignment=Alignment.Center) {
  Text(label,color=if(theme.key==Color(0xFF222033)) Color.White else Color(0xFF29243A),fontSize=if(label.length>2)9.sp else 13.sp,fontWeight=FontWeight.SemiBold)
 }
}

@Composable
private fun Editor(colorIndex:Int,colors:List<Color>,height:Float,rounding:Float,onColor:(Int)->Unit,onHeight:(Float)->Unit,onRounding:(Float)->Unit,onPhoto:()->Unit,onRemovePhoto:()->Unit,onSave:()->Unit,hasPhoto:Boolean) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(17.dp)) {
  Text("KEY COLOR",fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFF92889F),letterSpacing=1.2.sp)
  Spacer(Modifier.height(11.dp))
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   colors.forEachIndexed { i,c ->
    Box(Modifier.size(37.dp).clip(RoundedCornerShape(12.dp)).background(c).border(if(colorIndex==i)2.dp else 1.dp,if(colorIndex==i)Color(0xFF7956E8) else Color(0xFFE8E1EF),RoundedCornerShape(12.dp)).clickable{onColor(i)},contentAlignment=Alignment.Center) {
     if(colorIndex==i) Icon(Icons.Rounded.Check,null,tint=if(i==5)Color.White else Color(0xFF7956E8),modifier=Modifier.size(18.dp))
    }
   }
  }
  Spacer(Modifier.height(17.dp))
  Text("KEY HEIGHT • ${height.toInt()} dp",fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Slider(value=height,onValueChange=onHeight,valueRange=30f..54f)
  Text("CORNER ROUNDING • ${rounding.toInt()} dp",fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Slider(value=rounding,onValueChange=onRounding,valueRange=2f..18f)
  Spacer(Modifier.height(8.dp))
  OutlinedButton(onClick=onPhoto,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.PhotoLibrary,null); Spacer(Modifier.width(8.dp)); Text(if(hasPhoto)"Change background photo" else "Choose background photo") }
  if(hasPhoto) TextButton(onClick=onRemovePhoto,modifier=Modifier.align(Alignment.End)){Text("Remove photo")}
  Spacer(Modifier.height(8.dp))
  Button(onClick=onSave,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Save,null); Spacer(Modifier.width(8.dp)); Text("Save design") }
  Spacer(Modifier.height(8.dp))
  Text("Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them.",fontSize=12.sp,color=Color(0xFF92889F))
 }
}

@Composable
private fun ThemeRow(theme:KTheme,selected:Boolean,onClick:()->Unit) {
 Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(onClick=onClick).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
  Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(theme.colors)))
  Spacer(Modifier.width(12.dp))
  Column(Modifier.weight(1f)) { Text(theme.name,fontWeight=FontWeight.Bold,color=Color(0xFF302840)); Text("Colorful gradient theme",fontSize=12.sp,color=Color(0xFF92889F)) }
  if(selected) Icon(Icons.Rounded.Check,null,tint=Color(0xFF7956E8))
 }
}
@Composable
private fun InfoCard(title:String,body:String) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(18.dp)) { Text(title,fontWeight=FontWeight.Bold,color=Color(0xFF302840)); Spacer(Modifier.height(6.dp)); Text(body,color=Color(0xFF82798F),fontSize=13.sp) }
}
@Composable
private fun SettingsCard(open:()->Unit,language:String,onLanguage:(String)->Unit,onReset:()->Unit) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(18.dp)) {
  Text(tx(language,"Apply your design"),fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Spacer(Modifier.height(8.dp))
  Text("Android does not provide a universal way for one app to change another keyboard's appearance. Gboard, Samsung Keyboard and SwiftKey manage themes separately.",fontSize=13.sp,color=Color(0xFF746B80),lineHeight=19.sp)
  Spacer(Modifier.height(16.dp))
  var expanded by remember { mutableStateOf(false) }
  Text(tx(language,"Interface language"),fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Spacer(Modifier.height(8.dp))
  Box {
   OutlinedButton(onClick={expanded=true},modifier=Modifier.fillMaxWidth()) { Text(language,modifier=Modifier.weight(1f)); Text("⌄") }
   DropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {
    languages.forEach { option -> DropdownMenuItem(text={Text(option)},onClick={onLanguage(option);expanded=false}) }
   }
  }
  Spacer(Modifier.height(14.dp))
  Button(onClick=open,modifier=Modifier.fillMaxWidth()){Text(tx(language,"Open keyboard settings"))}
  Spacer(Modifier.height(8.dp))
  OutlinedButton(onClick=onReset,modifier=Modifier.fillMaxWidth()){Text(tx(language,"Restore original design"))}
  Spacer(Modifier.height(8.dp))
  Text(if(language=="Русский") "Сбросит предпросмотр в приложении. Для возврата заводской темы Gboard или другой клавиатуры открой ее настройки и выбери тему по умолчанию." else if(language=="ქართული") "აღდგება აპის საწყისი წინასწარი ხედი. კლავიატურის ქარხნული თემის დასაბრუნებლად გახსენით თავად კლავიატურის პარამეტრები." else "This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme.",fontSize=12.sp,color=Color(0xFF92889F),lineHeight=18.sp)
  Spacer(Modifier.height(12.dp))
  Text("Keyboard Studio previews designs inside this app. Importing a design into another keyboard depends on that keyboard's own supported features.",fontSize=12.sp,color=Color(0xFF92889F))
 }
}
