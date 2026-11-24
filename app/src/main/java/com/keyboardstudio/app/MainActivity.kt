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
 listOf(Color(0xFFDA22FF),Color(0xFF9733EE)), listOf(Color(0xFFFC4A1A),Color(0xFFF7B733)),
 listOf(Color(0xFF7A0718),Color(0xFF12040A)),
 listOf(Color(0xFFFFD21F),Color(0xFF0755A5)),
 listOf(Color(0xFF69D9FF),Color(0xFF071B43)),
 listOf(Color(0xFFFF6A00),Color(0xFF260707)),
 listOf(Color(0xFFFF00C8),Color(0xFF062A63))
)
private val languages = listOf("English","ქართული","Русский","Deutsch","Español","Français","Türkçe","العربية")
private val uiText = mapOf(
"ქართული" to mapOf(
"Themes" to "თემები","Customize" to "მორგება","My Designs" to "ჩემი დიზაინები","Settings" to "პარამეტრები",
"Interface language" to "ინტერფეისის ენა","Restore original design" to "საწყისი დიზაინის აღდგენა","Open keyboard settings" to "კლავიატურის პარამეტრების გახსნა",
"Make your keyboard feel like you" to "შექმენი შენი უნიკალური კლავიატურა","Find your\ncolor mood." to "იპოვე შენი\nფერების განწყობა.",
"Make it yours." to "მოირგე შენზე.","Your creations." to "შენი ნამუშევრები.","Your keyboard setup." to "შენი კლავიატურის პარამეტრები.",
"Explore 35 colorful gradient themes." to "დაათვალიერე 30 ფერადი გრადიენტული თემა.",
"Tune the keys and background of your preview." to "მოარგე კლავიშები და ფონი.",
"Your saved designs stay on this device." to "შენახული დიზაინები ამ მოწყობილობაზე რჩება.",
"Shortcuts and compatibility information." to "სწრაფი ბმულები და თავსებადობის ინფორმაცია.",
"No saved designs yet" to "ჯერ დიზაინები შენახული არ არის","Open Customize, choose your colors and tap Save design." to "გახსენით მორგება, აირჩიეთ ფერები და დააჭირეთ შენახვას.",
"ALL THEMES  •  35" to "ყველა თემა  •  30","LIVE PREVIEW" to "პირდაპირი წინასწარი ნახვა","KEY COLOR" to "კლავიშის ფერი",
"KEY HEIGHT • %s dp" to "კლავიშის სიმაღლე • %s dp","CORNER ROUNDING • %s dp" to "კუთხეების მომრგვალება • %s dp",
"Change background photo" to "ფონის ფოტოს შეცვლა","Choose background photo" to "ფონის ფოტოს არჩევა","Remove photo" to "ფოტოს წაშლა","Save design" to "დიზაინის შენახვა",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "წინასწარი ნახვა და პარამეტრები ინახება ამ მოწყობილობაზე. ფოტო მხოლოდ წინასწარ ხედში ჩანს.",
"Colorful gradient theme" to "ფერადი გრადიენტული თემა","Apply your design" to "გამოიყენე შენი დიზაინი",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "Keyboard Studio-ს აქვს საკუთარი Android კლავიატურა. ჩართე ის სისტემურ პარამეტრებში და აირჩიე აქტიურ კლავიატურად. არჩეული დიზაინი გამოყენებული იქნება აკრეფისას.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "ეს აღადგენს აპის წინასწარ ხედს. სხვა კლავიატურის საწყისი თემის დასაბრუნებლად გახსენი მისი პარამეტრები.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "არჩეული დიზაინი გამოიყენება Keyboard Studio-ს კლავიატურაზე. Gboard და Samsung Keyboard საკუთარ თემებს ინარჩუნებენ."
),
"Русский" to mapOf(
"Themes" to "Темы","Customize" to "Настройка","My Designs" to "Мои дизайны","Settings" to "Настройки",
"Interface language" to "Язык интерфейса","Restore original design" to "Вернуть исходный дизайн","Open keyboard settings" to "Открыть настройки клавиатуры",
"Make your keyboard feel like you" to "Создай клавиатуру под себя","Find your\ncolor mood." to "Найди своё\nцветовое настроение.",
"Make it yours." to "Настрой под себя.","Your creations." to "Твои дизайны.","Your keyboard setup." to "Настройки клавиатуры.",
"Explore 35 colorful gradient themes." to "Выбирай из 35 ярких градиентных тем.",
"Tune the keys and background of your preview." to "Настрой клавиши и фон клавиатуры.",
"Your saved designs stay on this device." to "Сохранённые дизайны хранятся на этом устройстве.",
"Shortcuts and compatibility information." to "Быстрые настройки и информация о совместимости.",
"No saved designs yet" to "Пока нет сохранённых дизайнов","Open Customize, choose your colors and tap Save design." to "Открой «Настройка», выбери цвета и нажми «Сохранить дизайн».",
"ALL THEMES  •  35" to "ВСЕ ТЕМЫ  •  35","LIVE PREVIEW" to "ПРЕДПРОСМОТР","KEY COLOR" to "ЦВЕТ КЛАВИШ",
"KEY HEIGHT • %s dp" to "ВЫСОТА КЛАВИШ • %s dp","CORNER ROUNDING • %s dp" to "СКРУГЛЕНИЕ УГЛОВ • %s dp",
"Change background photo" to "Изменить фото фона","Choose background photo" to "Выбрать фото фона","Remove photo" to "Удалить фото","Save design" to "Сохранить дизайн",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "Предпросмотр и настройки сохраняются на этом устройстве. Фото используется только в предпросмотре.",
"Colorful gradient theme" to "Яркая градиентная тема","Apply your design" to "Применение дизайна",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "В Keyboard Studio есть собственная клавиатура Android. Включи её в системных настройках клавиатуры и выбери активной. Выбранный дизайн будет применяться при наборе текста.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "Это сбрасывает предпросмотр приложения. Чтобы вернуть стандартную тему другой клавиатуры, открой её настройки и выбери тему по умолчанию.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "Выбранный дизайн применяется к клавиатуре Keyboard Studio. Gboard и Samsung Keyboard сохраняют собственные темы."
),
"Deutsch" to mapOf(
"Themes" to "Designs","Customize" to "Anpassen","My Designs" to "Meine Designs","Settings" to "Einstellungen",
"Interface language" to "Sprache der Oberfläche","Restore original design" to "Originaldesign wiederherstellen","Open keyboard settings" to "Tastatureinstellungen öffnen",
"Make your keyboard feel like you" to "Gestalte deine persönliche Tastatur","Find your\ncolor mood." to "Finde deine\nFarbwelt.",
"Make it yours." to "Mach sie zu deiner.","Your creations." to "Deine Designs.","Your keyboard setup." to "Deine Tastatureinstellungen.",
"Explore 35 colorful gradient themes." to "Entdecke 35 farbenfrohe Farbverläufe.",
"Tune the keys and background of your preview." to "Passe Tasten und Hintergrund an.",
"Your saved designs stay on this device." to "Deine Designs bleiben auf diesem Gerät gespeichert.",
"Shortcuts and compatibility information." to "Verknüpfungen und Kompatibilitätsinformationen.",
"No saved designs yet" to "Noch keine Designs gespeichert","Open Customize, choose your colors and tap Save design." to "Öffne „Anpassen“, wähle Farben und tippe auf „Design speichern“. ",
"ALL THEMES  •  35" to "ALLE DESIGNS  •  35","LIVE PREVIEW" to "LIVE-VORSCHAU","KEY COLOR" to "TASTENFARBE",
"KEY HEIGHT • %s dp" to "TASTENHÖHE • %s dp","CORNER ROUNDING • %s dp" to "ECKENRUNDUNG • %s dp",
"Change background photo" to "Hintergrundfoto ändern","Choose background photo" to "Hintergrundfoto auswählen","Remove photo" to "Foto entfernen","Save design" to "Design speichern",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "Vorschau und Einstellungen werden auf diesem Gerät gespeichert. Fotos sind nur in der Vorschau sichtbar.",
"Colorful gradient theme" to "Farbenfrohes Verlaufdesign","Apply your design" to "Design anwenden",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "Keyboard Studio enthält eine eigene Android-Tastatur. Aktiviere sie in den Systemeinstellungen und wähle sie als aktive Tastatur. Dein Design wird dann beim Tippen verwendet.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "Dadurch wird die Vorschau zurückgesetzt. Für das Standarddesign einer anderen Tastatur wähle dort das Standardthema.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "Dein Design gilt für Keyboard Studio. Gboard und Samsung Keyboard behalten ihre eigenen Designs."
),
"Español" to mapOf(
"Themes" to "Temas","Customize" to "Personalizar","My Designs" to "Mis diseños","Settings" to "Ajustes",
"Interface language" to "Idioma de la interfaz","Restore original design" to "Restaurar diseño original","Open keyboard settings" to "Abrir ajustes del teclado",
"Make your keyboard feel like you" to "Personaliza tu teclado a tu estilo","Find your\ncolor mood." to "Encuentra tu\nestilo de color.",
"Make it yours." to "Hazlo tuyo.","Your creations." to "Tus diseños.","Your keyboard setup." to "Configuración del teclado.",
"Explore 35 colorful gradient themes." to "Explora 35 temas con degradados coloridos.",
"Tune the keys and background of your preview." to "Ajusta las teclas y el fondo de la vista previa.",
"Your saved designs stay on this device." to "Tus diseños se guardan en este dispositivo.",
"Shortcuts and compatibility information." to "Accesos directos e información de compatibilidad.",
"No saved designs yet" to "Todavía no hay diseños guardados","Open Customize, choose your colors and tap Save design." to "Abre Personalizar, elige los colores y pulsa Guardar diseño.",
"ALL THEMES  •  35" to "TODOS LOS TEMAS  •  35","LIVE PREVIEW" to "VISTA PREVIA","KEY COLOR" to "COLOR DE TECLAS",
"KEY HEIGHT • %s dp" to "ALTURA DE TECLAS • %s dp","CORNER ROUNDING • %s dp" to "REDONDEO DE ESQUINAS • %s dp",
"Change background photo" to "Cambiar foto de fondo","Choose background photo" to "Elegir foto de fondo","Remove photo" to "Eliminar foto","Save design" to "Guardar diseño",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "La vista previa y los ajustes se guardan en este dispositivo. Las fotos solo aparecen en la vista previa.",
"Colorful gradient theme" to "Tema con degradado colorido","Apply your design" to "Aplicar tu diseño",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "Keyboard Studio incluye su propio teclado Android. Actívalo en los ajustes del sistema y selecciónalo como teclado activo. Tu diseño se aplicará al escribir.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "Esto restablece la vista previa. Para restaurar el tema original de otro teclado, selecciónalo en sus ajustes.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "Tu diseño se aplica al teclado Keyboard Studio. Gboard y Samsung Keyboard conservan sus propios temas."
),
"Français" to mapOf(
"Themes" to "Thèmes","Customize" to "Personnaliser","My Designs" to "Mes créations","Settings" to "Paramètres",
"Interface language" to "Langue de l’interface","Restore original design" to "Restaurer le design d’origine","Open keyboard settings" to "Ouvrir les paramètres du clavier",
"Make your keyboard feel like you" to "Créez un clavier à votre image","Find your\ncolor mood." to "Trouvez votre\nunivers de couleurs.",
"Make it yours." to "Personnalisez-le.","Your creations." to "Vos créations.","Your keyboard setup." to "Configuration du clavier.",
"Explore 35 colorful gradient themes." to "Découvrez 35 thèmes dégradés colorés.",
"Tune the keys and background of your preview." to "Personnalisez les touches et l’arrière-plan.",
"Your saved designs stay on this device." to "Vos créations restent sur cet appareil.",
"Shortcuts and compatibility information." to "Raccourcis et informations de compatibilité.",
"No saved designs yet" to "Aucun design enregistré","Open Customize, choose your colors and tap Save design." to "Ouvrez Personnaliser, choisissez vos couleurs et appuyez sur Enregistrer.",
"ALL THEMES  •  35" to "TOUS LES THÈMES  •  35","LIVE PREVIEW" to "APERÇU EN DIRECT","KEY COLOR" to "COULEUR DES TOUCHES",
"KEY HEIGHT • %s dp" to "HAUTEUR DES TOUCHES • %s dp","CORNER ROUNDING • %s dp" to "ARRONDI DES ANGLES • %s dp",
"Change background photo" to "Changer la photo d’arrière-plan","Choose background photo" to "Choisir une photo d’arrière-plan","Remove photo" to "Supprimer la photo","Save design" to "Enregistrer le design",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "L’aperçu et les réglages sont enregistrés sur cet appareil. Les photos ne sont visibles que dans l’aperçu.",
"Colorful gradient theme" to "Thème dégradé coloré","Apply your design" to "Appliquer votre design",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "Keyboard Studio possède son propre clavier Android. Activez-le dans les paramètres système, puis choisissez-le comme clavier actif. Votre design sera utilisé pendant la saisie.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "Cela réinitialise l’aperçu. Pour restaurer le thème d’origine d’un autre clavier, choisissez son thème par défaut dans ses paramètres.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "Votre design s’applique au clavier Keyboard Studio. Gboard et Samsung Keyboard conservent leurs propres thèmes."
),
"Türkçe" to mapOf(
"Themes" to "Temalar","Customize" to "Özelleştir","My Designs" to "Tasarımlarım","Settings" to "Ayarlar",
"Interface language" to "Arayüz dili","Restore original design" to "Orijinal tasarımı geri yükle","Open keyboard settings" to "Klavye ayarlarını aç",
"Make your keyboard feel like you" to "Klavyeni kendi tarzına göre tasarla","Find your\ncolor mood." to "Renk tarzını\nkeşfet.",
"Make it yours." to "Kendine göre düzenle.","Your creations." to "Tasarımların.","Your keyboard setup." to "Klavye ayarların.",
"Explore 35 colorful gradient themes." to "35 renkli geçişli temayı keşfet.",
"Tune the keys and background of your preview." to "Tuşları ve arka planı düzenle.",
"Your saved designs stay on this device." to "Kaydettiğin tasarımlar bu cihazda kalır.",
"Shortcuts and compatibility information." to "Kısayollar ve uyumluluk bilgileri.",
"No saved designs yet" to "Henüz kaydedilmiş tasarım yok","Open Customize, choose your colors and tap Save design." to "Özelleştir'i aç, renklerini seç ve Tasarımı kaydet'e dokun.",
"ALL THEMES  •  35" to "TÜM TEMALAR  •  35","LIVE PREVIEW" to "CANLI ÖNİZLEME","KEY COLOR" to "TUŞ RENGİ",
"KEY HEIGHT • %s dp" to "TUŞ YÜKSEKLİĞİ • %s dp","CORNER ROUNDING • %s dp" to "KÖŞE YUVARLAKLIĞI • %s dp",
"Change background photo" to "Arka plan fotoğrafını değiştir","Choose background photo" to "Arka plan fotoğrafı seç","Remove photo" to "Fotoğrafı kaldır","Save design" to "Tasarımı kaydet",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "Önizleme ve ayarlar bu cihazda saklanır. Fotoğraflar yalnızca önizlemede görünür.",
"Colorful gradient theme" to "Renkli geçiş teması","Apply your design" to "Tasarımını uygula",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "Keyboard Studio kendi Android klavyesini içerir. Sistem klavye ayarlarından etkinleştir ve aktif klavye olarak seç. Tasarımın yazarken uygulanır.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "Bu işlem uygulama önizlemesini sıfırlar. Diğer klavyenin varsayılan temasını kendi ayarlarından seç.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "Seçtiğin tasarım Keyboard Studio klavyesine uygulanır. Gboard ve Samsung Keyboard kendi temalarını kullanır."
),
"العربية" to mapOf(
"Themes" to "السمات","Customize" to "تخصيص","My Designs" to "تصميماتي","Settings" to "الإعدادات",
"Interface language" to "لغة الواجهة","Restore original design" to "استعادة التصميم الأصلي","Open keyboard settings" to "فتح إعدادات لوحة المفاتيح",
"Make your keyboard feel like you" to "اجعل لوحة المفاتيح تعبر عنك","Find your\ncolor mood." to "اكتشف\nألوانك المفضلة.",
"Make it yours." to "خصصها كما تحب.","Your creations." to "تصميماتك.","Your keyboard setup." to "إعدادات لوحة المفاتيح.",
"Explore 35 colorful gradient themes." to "اكتشف 35 سمة بتدرجات لونية جميلة.",
"Tune the keys and background of your preview." to "خصص الأزرار والخلفية في المعاينة.",
"Your saved designs stay on this device." to "تبقى تصميماتك المحفوظة على هذا الجهاز.",
"Shortcuts and compatibility information." to "اختصارات ومعلومات التوافق.",
"No saved designs yet" to "لا توجد تصميمات محفوظة بعد","Open Customize, choose your colors and tap Save design." to "افتح التخصيص واختر الألوان واضغط حفظ التصميم.",
"ALL THEMES  •  35" to "كل السمات  •  35","LIVE PREVIEW" to "معاينة مباشرة","KEY COLOR" to "لون الأزرار",
"KEY HEIGHT • %s dp" to "ارتفاع الأزرار • %s dp","CORNER ROUNDING • %s dp" to "استدارة الزوايا • %s dp",
"Change background photo" to "تغيير صورة الخلفية","Choose background photo" to "اختيار صورة للخلفية","Remove photo" to "إزالة الصورة","Save design" to "حفظ التصميم",
"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them." to "يتم حفظ المعاينة والإعدادات على هذا الجهاز. تظهر الصور في المعاينة فقط.",
"Colorful gradient theme" to "سمة بتدرج لوني","Apply your design" to "تطبيق تصميمك",
"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing." to "يتضمن Keyboard Studio لوحة مفاتيح خاصة به. فعّلها من إعدادات لوحة المفاتيح في النظام ثم اخترها كلوحة نشطة. سيُطبق تصميمك أثناء الكتابة.",
"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme." to "يعيد هذا ضبط المعاينة داخل التطبيق. لاستعادة السمة الأصلية للوحة أخرى، اختر السمة الافتراضية من إعداداتها.",
"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes." to "يُطبق تصميمك على لوحة Keyboard Studio. تحتفظ لوحات Gboard وSamsung Keyboard بسماتها الخاصة."
)
)
private fun tx(language:String,key:String):String = uiText[language]?.get(key) ?: key
private fun metricText(language:String,key:String,value:Int):String = tx(language,key).replace("%s",value.toString())


private val names = listOf(
 "Cotton Candy","Ocean Pop","Sunset","Mint Glow","Berry","Lemonade","Rose Quartz","Aqua Dream","Violet Sky","Tangerine",
 "Peach Bloom","Arctic Wave","Electric Lilac","Coral Crush","Golden Hour","Bubblegum","Blue Lagoon","Grape Soda","Mango Splash","Emerald",
 "Candy Floss","Midnight Pop","Lavender Mist","Tropical","Cherry Ice","Lime Light","Aurora","Cosmic Pink","Citrus Mix","Dreamy Purple",
 "Dracula","Minions","Wolf","Dragon","Cyberpunk"
)
private val translatedThemeNames = mapOf(
 "ქართული" to listOf("ბამბის კანფეტი","ოკეანის ტალღა","მზის ჩასვლა","პიტნის ნათება","კენკრა","ლიმონათი","ვარდის კვარცი","აკვა ოცნება","იისფერი ცა","მანდარინი","ატმის ყვავილი","არქტიკული ტალღა","ელექტრო იასამნისფერი","მარჯნის აფეთქება","ოქროს საათი","საღეჭი რეზინი","ლურჯი ლაგუნა","ყურძნის სოდა","მანგოს შხეფი","ზურმუხტი","ბამბის ნაზი","შუაღამის ნათება","ლავანდის ნისლი","ტროპიკული","ალუბლის ყინული","ლაიმის ნათება","აურორა","კოსმოსური ვარდისფერი","ციტრუსის მიქსი","ოცნების იისფერი"),
 "Русский" to listOf("Сахарная вата","Океанская волна","Закат","Мятное сияние","Ягода","Лимонад","Розовый кварц","Аква-мечта","Фиолетовое небо","Мандарин","Персиковый цвет","Арктическая волна","Электрическая сирень","Коралловый всплеск","Золотой час","Жвачка","Голубая лагуна","Виноградная газировка","Манговый всплеск","Изумруд","Сахарная нить","Полуночное сияние","Лавандовый туман","Тропики","Вишнёвый лёд","Лаймовый свет","Аврора","Космический розовый","Цитрусовый микс","Мечтательный фиолетовый","Дракула","Миньоны","Волк","Дракон","Киберпанк"),
 "Deutsch" to listOf("Zuckerwatte","Ozean-Pop","Sonnenuntergang","Minzleuchten","Beere","Limonade","Rosenquarz","Aqua-Traum","Violetter Himmel","Mandarine","Pfirsichblüte","Arktische Welle","Elektrisches Flieder","Korallenrausch","Goldene Stunde","Kaugummi","Blaue Lagune","Traubenlimonade","Mango-Spritzer","Smaragd","Zuckerflocke","Mitternachtsglanz","Lavendelnebel","Tropisch","Kirsch-Eis","Limettenlicht","Aurora","Kosmisches Rosa","Zitrusmix","Traumhaftes Violett"),
 "Español" to listOf("Algodón de azúcar","Ola del océano","Atardecer","Brillo de menta","Frutos rojos","Limonada","Cuarzo rosa","Sueño aqua","Cielo violeta","Mandarina","Flor de melocotón","Ola ártica","Lila eléctrico","Explosión coral","Hora dorada","Chicle","Laguna azul","Refresco de uva","Toque de mango","Esmeralda","Nube de azúcar","Brillo de medianoche","Niebla lavanda","Tropical","Hielo de cereza","Luz lima","Aurora","Rosa cósmico","Mezcla cítrica","Violeta de ensueño"),
 "Français" to listOf("Barbe à papa","Vague océanique","Coucher de soleil","Éclat menthe","Baies","Limonade","Quartz rose","Rêve aqua","Ciel violet","Mandarine","Fleur de pêche","Vague arctique","Lilas électrique","Éclat corail","Heure dorée","Chewing-gum","Lagon bleu","Soda raisin","Éclaboussure mangue","Émeraude","Nuage sucré","Éclat de minuit","Brume lavande","Tropical","Glace cerise","Lumière citron vert","Aurore","Rose cosmique","Mélange d’agrumes","Violet rêveur"),
 "Türkçe" to listOf("Pamuk şeker","Okyanus dalgası","Gün batımı","Nane parıltısı","Meyve","Limonata","Pembe kuvars","Aqua rüyası","Mor gökyüzü","Mandalina","Şeftali çiçeği","Kutup dalgası","Elektrik lila","Mercan patlaması","Altın saat","Sakız","Mavi lagün","Üzüm sodası","Mango esintisi","Zümrüt","Şeker bulutu","Gece yarısı parıltısı","Lavanta sisi","Tropik","Kiraz buzu","Lime ışığı","Aurora","Kozmik pembe","Narenciye karışımı","Rüya moru"),
 "العربية" to listOf("غزل البنات","موج المحيط","غروب الشمس","توهج النعناع","التوت","ليمونادة","كوارتز وردي","حلم مائي","سماء بنفسجية","يوسفي","زهرة الخوخ","موجة قطبية","ليلكي كهربائي","مرجان متوهج","الساعة الذهبية","علكة","البحيرة الزرقاء","مشروب العنب","لمسة مانجو","زمرد","سحابة سكر","وهج منتصف الليل","ضباب اللافندر","استوائي","كرز مثلج","ضوء الليمون","الشفق","وردي كوني","مزيج الحمضيات","بنفسجي حالم")
)
private fun localizedThemeName(language:String,index:Int):String =
 translatedThemeNames[language]?.getOrNull(index) ?: names.getOrNull(index) ?: "Theme"

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
 var selected by remember { mutableIntStateOf(prefs.getInt("selected",0).coerceIn(0,34)) }
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
  val id = System.currentTimeMillis().toString()
  val snapshot = listOf(selected,keyColorIndex,keyHeight,rounding).joinToString(",")
  prefs.edit().putString("design_$id",snapshot).apply()
  saved = saved + id
  prefs.edit().putStringSet("saved",saved).apply()
 }
 fun loadDesign(id:String) {
  val parts = prefs.getString("design_$id",null)?.split(",")
  selected = (parts?.getOrNull(0)?.toIntOrNull() ?: id.toIntOrNull() ?: selected).coerceIn(0,34)
  if(parts != null && parts.size >= 4) {
   keyColorIndex = (parts[1].toIntOrNull() ?: keyColorIndex).coerceIn(0,9)
   keyHeight = (parts[2].toFloatOrNull() ?: keyHeight).coerceIn(30f,54f)
   rounding = (parts[3].toFloatOrNull() ?: rounding).coerceIn(2f,18f)
  }
  prefs.edit().putInt("selected",selected).putInt("keyColor",keyColorIndex)
   .putFloat("height",keyHeight).putFloat("rounding",rounding).apply()
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
    Column { Text("Keyboard Studio",fontSize=21.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF28213D)); Text(tx(language,"Make your keyboard feel like you"),fontSize=12.sp,color=Color(0xFF8B829B)) }
   }
   Spacer(Modifier.height(22.dp))
   Text(tx(language,when(tab){"Themes"->"Find your\ncolor mood.";"Customize"->"Make it yours.";"My Designs"->"Your creations.";else->"Your keyboard setup."}),fontSize=31.sp,lineHeight=35.sp,fontWeight=FontWeight.ExtraBold,color=Color(0xFF28213D))
   Spacer(Modifier.height(7.dp))
   Text(tx(language,when(tab){"Themes"->"Explore 35 colorful gradient themes.";"Customize"->"Tune the keys and background of your preview.";"My Designs"->"Your saved designs stay on this device.";else->"Shortcuts and compatibility information."}),color=Color(0xFF82798F),fontSize=14.sp)
   Spacer(Modifier.height(18.dp))
   when(tab) {
    "Settings" -> SettingsCard(openSettings,language,{ language=it; prefs.edit().putString("language",it).apply() },{ selected=0; keyHeight=39f; rounding=9f; keyColorIndex=0; photoUri=null; prefs.edit().putInt("selected",0).putFloat("height",39f).putFloat("rounding",9f).putInt("keyColor",0).remove("photo").apply() })
    "My Designs" -> {
     if(saved.isEmpty()) InfoCard(language,"No saved designs yet","Open Customize, choose your colors and tap Save design.")
     else saved.sortedDescending().forEach { id ->
      val stored = prefs.getString("design_$id",null)?.split(",")
      val i=(stored?.getOrNull(0)?.toIntOrNull() ?: id.toIntOrNull() ?: 0).coerceIn(0,34)
      ThemeRow(themes[i],selected==i,language) { loadDesign(id); tab="Customize" }
      Spacer(Modifier.height(9.dp))
     }
    }
    else -> {
     KeyboardPreview(current,keyHeight.dp,rounding.dp,photoUri,language)
     Spacer(Modifier.height(20.dp))
     if(tab=="Themes") {
      Text(tx(language,"ALL THEMES  •  35"),fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFF92889F),letterSpacing=1.3.sp)
      Spacer(Modifier.height(12.dp))
      themes.forEachIndexed { i,t ->
       ThemeRow(t,selected==i,language) { selected=i; prefs.edit().putInt("selected",i).apply() }
       Spacer(Modifier.height(9.dp))
      }
     } else {
      Editor(language,keyColorIndex,keyColors,keyHeight,rounding,
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
private fun KeyboardPreview(theme:KTheme,height:Dp,rounding:Dp,uri:Uri?,language:String) {
 val context=LocalContext.current
 val bitmap by produceState<Bitmap?>(null,uri) {
  value=if(uri==null) null else withContext(Dispatchers.IO) { runCatching { context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) } }.getOrNull() }
 }
 val previewRows = when(language) {
  "ქართული" -> listOf("ქწერტყუიოპ","ასდფგჰჯკლ","ზხცვბნმ")
  "Русский" -> listOf("ЙЦУКЕНГШЩЗ","ФЫВАПРОЛДЖ","ЯЧСМИТЬБЮ")
  "Deutsch" -> listOf("QWERTZUIOP","ASDFGHJKL","YXCVBNM")
  "Español" -> listOf("QWERTYUIOP","ASDFGHJKLÑ","ZXCVBNM")
  "Français" -> listOf("AZERTYUIOP","QSDFGHJKLM","WXCVBN")
  "Türkçe" -> listOf("QWERTYUIOP","ASDFGHJKLŞ","ZXCVBNMÖ")
  "العربية" -> listOf("ضصثقفغعهخحج","شسيبلاتنمك","ئءؤرلاى")
  else -> listOf("QWERTYUIOP","ASDFGHJKL","ZXCVBNM")
 }
 Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(Brush.linearGradient(theme.colors)).padding(13.dp)) {
  if(bitmap!=null) Image(bitmap!!.asImageBitmap(),null,Modifier.matchParentSize(),contentScale=ContentScale.Crop)
  Column {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { Text(tx(language,"LIVE PREVIEW"),color=Color.White,fontWeight=FontWeight.Bold); Text("ABC • $language",color=Color.White,fontSize=11.sp) }
   Spacer(Modifier.height(12.dp))
   previewRows.forEachIndexed { row,letters ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
     if(row==2) Key("⇧",Modifier.weight(1.1f),theme,height,rounding)
     letters.forEach { Key(it.toString(),Modifier.weight(1f),theme,height,rounding) }
     if(row==2) Key("⌫",Modifier.weight(1.1f),theme,height,rounding)
    }
    Spacer(Modifier.height(5.dp))
   }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(4.dp)) {
    Key("?123",Modifier.weight(1.2f),theme,height,rounding); Key("☺",Modifier.weight(.9f),theme,height,rounding)
    Key(",",Modifier.weight(.7f),theme,height,rounding); Key(language,Modifier.weight(4f),theme,height,rounding)
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
private fun Editor(language:String,colorIndex:Int,colors:List<Color>,height:Float,rounding:Float,onColor:(Int)->Unit,onHeight:(Float)->Unit,onRounding:(Float)->Unit,onPhoto:()->Unit,onRemovePhoto:()->Unit,onSave:()->Unit,hasPhoto:Boolean) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(17.dp)) {
  Text(tx(language,"KEY COLOR"),fontSize=11.sp,fontWeight=FontWeight.Bold,color=Color(0xFF92889F),letterSpacing=1.2.sp)
  Spacer(Modifier.height(11.dp))
  Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   colors.forEachIndexed { i,c ->
    Box(Modifier.size(37.dp).clip(RoundedCornerShape(12.dp)).background(c).border(if(colorIndex==i)2.dp else 1.dp,if(colorIndex==i)Color(0xFF7956E8) else Color(0xFFE8E1EF),RoundedCornerShape(12.dp)).clickable{onColor(i)},contentAlignment=Alignment.Center) {
     if(colorIndex==i) Icon(Icons.Rounded.Check,null,tint=if(i==5)Color.White else Color(0xFF7956E8),modifier=Modifier.size(18.dp))
    }
   }
  }
  Spacer(Modifier.height(17.dp))
  Text(metricText(language,"KEY HEIGHT • %s dp",height.toInt()),fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Slider(value=height,onValueChange=onHeight,valueRange=30f..54f)
  Text(metricText(language,"CORNER ROUNDING • %s dp",rounding.toInt()),fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Slider(value=rounding,onValueChange=onRounding,valueRange=2f..18f)
  Spacer(Modifier.height(8.dp))
  OutlinedButton(onClick=onPhoto,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.PhotoLibrary,null); Spacer(Modifier.width(8.dp)); Text(tx(language,if(hasPhoto)"Change background photo" else "Choose background photo")) }
  if(hasPhoto) TextButton(onClick=onRemovePhoto,modifier=Modifier.align(Alignment.End)){Text(tx(language,"Remove photo"))}
  Spacer(Modifier.height(8.dp))
  Button(onClick=onSave,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Save,null); Spacer(Modifier.width(8.dp)); Text(tx(language,"Save design")) }
  Spacer(Modifier.height(8.dp))
  Text(tx(language,"Your preview and saved settings are stored on this device. Photo backgrounds are preview-only unless a target keyboard supports importing them."),fontSize=12.sp,color=Color(0xFF92889F))
 }
}

@Composable
private fun ThemeRow(theme:KTheme,selected:Boolean,language:String,onClick:()->Unit) {
 Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).clickable(onClick=onClick).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
  Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(Brush.linearGradient(theme.colors)))
  Spacer(Modifier.width(12.dp))
  Column(Modifier.weight(1f)) { Text(localizedThemeName(language,names.indexOf(theme.name)),fontWeight=FontWeight.Bold,color=Color(0xFF302840)); Text(tx(language,"Colorful gradient theme"),fontSize=12.sp,color=Color(0xFF92889F)) }
  if(selected) Icon(Icons.Rounded.Check,null,tint=Color(0xFF7956E8))
 }
}
@Composable
private fun InfoCard(language:String,title:String,body:String) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color.White).padding(18.dp)) { Text(tx(language,title),fontWeight=FontWeight.Bold,color=Color(0xFF302840)); Spacer(Modifier.height(6.dp)); Text(tx(language,body),color=Color(0xFF82798F),fontSize=13.sp) }
}
@Composable
private fun SettingsCard(open:()->Unit,language:String,onLanguage:(String)->Unit,onReset:()->Unit) {
 Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(18.dp)) {
  Text(tx(language,"Apply your design"),fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF302840))
  Spacer(Modifier.height(8.dp))
  Text(tx(language,"Keyboard Studio includes its own Android keyboard. Enable it in system keyboard settings, then choose Keyboard Studio as your active keyboard. Your selected theme and key settings will be used while typing."),fontSize=13.sp,color=Color(0xFF746B80),lineHeight=19.sp)
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
  Text(tx(language,"This resets the in-app preview. To restore the factory theme of another keyboard, open that keyboard settings and choose its default theme."),fontSize=12.sp,color=Color(0xFF92889F),lineHeight=18.sp)
  Spacer(Modifier.height(12.dp))
  Text(tx(language,"Your selected design is applied to the Keyboard Studio keyboard. Other keyboards such as Gboard and Samsung Keyboard keep their own themes."),fontSize=12.sp,color=Color(0xFF92889F))
 }
}
