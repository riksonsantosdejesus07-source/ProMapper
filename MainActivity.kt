package com.promapper.app

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

private val Bg = Color(0xFF070A10)
private val Card = Color(0xFF111722)
private val Cyan = Color(0xFF19D8FF)

private const val PREFS = "promapper"
private const val KEY_PROFILES = "profiles"
private const val KEY_ACTIVE = "active_profile"

data class Command(val id: Long, val name: String, val key: String, val action: String, val x: Int = 50, val y: Int = 50)
data class Profile(val name: String, val game: String, val horizontal: Int, val vertical: Int, val aim: Int, val commands: List<Command>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ProMapperApp(this) }
    }
}

private fun loadProfiles(context: Context): MutableList<Profile> {
    val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PROFILES, "[]") ?: "[]"
    val arr = JSONArray(raw)
    val result = mutableListOf<Profile>()
    for (i in 0 until arr.length()) {
        val p = arr.getJSONObject(i)
        val ca = p.optJSONArray("commands") ?: JSONArray()
        val commands = mutableListOf<Command>()
        for (j in 0 until ca.length()) {
            val c = ca.getJSONObject(j)
            commands += Command(c.optLong("id"), c.optString("name"), c.optString("key"), c.optString("action"), c.optInt("x", 50), c.optInt("y", 50))
        }
        result += Profile(p.optString("name", "Meu perfil"), p.optString("game", "Outro"), p.optInt("horizontal", 50), p.optInt("vertical", 50), p.optInt("aim", 50), commands)
    }
    return result
}

private fun saveProfiles(context: Context, profiles: List<Profile>) {
    val arr = JSONArray()
    profiles.forEach { p ->
        val po = JSONObject().apply {
            put("name", p.name); put("game", p.game); put("horizontal", p.horizontal); put("vertical", p.vertical); put("aim", p.aim)
            put("commands", JSONArray().apply { p.commands.forEach { c -> put(JSONObject().apply { put("id", c.id); put("name", c.name); put("key", c.key); put("action", c.action); put("x", c.x); put("y", c.y) }) } })
        }
        arr.put(po)
    }
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_PROFILES, arr.toString()).apply()
}

@Composable
fun ProMapperApp(context: Context) {
    var screen by remember { mutableStateOf("home") }
    var profiles by remember { mutableStateOf(loadProfiles(context)) }
    var active by remember { mutableStateOf(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_ACTIVE, "Free Fire") ?: "Free Fire") }
    MaterialTheme(colorScheme = darkColorScheme(background = Bg, surface = Card, primary = Cyan)) {
        Surface(Modifier.fillMaxSize(), color = Bg) {
            when (screen) {
                "home" -> Home(active, { screen = it })
                "mapper" -> Mapper(active, profiles.find { it.name == active }, { updated ->
                    profiles = (profiles.filterNot { it.name == updated.name } + updated).toMutableList(); saveProfiles(context, profiles); active = updated.name; context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE, active).apply()
                }, { screen = "home" })
                "profiles" -> Profiles(profiles, active, { p -> active = p.name; context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_ACTIVE, active).apply(); screen = "mapper" }, { profiles = it.toMutableList(); saveProfiles(context, profiles) }, { screen = "home" })
                "premium" -> Premium { screen = "home" }
                "permissions" -> Permissions(context) { screen = "home" }
                else -> Home(active, { screen = it })
            }
        }
    }
}

@Composable
fun Header(title: String, back: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
        if (back != null) TextButton(onClick = back) { Text("← Voltar", color = Cyan) }
        Spacer(Modifier.width(8.dp)); Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun Home(active: String, go: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(45.dp)); Text("PRO", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold); Text("MAPPER", color = Cyan, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        Text("Seu controle. Seu jeito.", color = Color.LightGray); Spacer(Modifier.height(30.dp))
        Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("Perfil ativo", color = Color.Gray); Text(active, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold) } }
        Spacer(Modifier.height(18.dp)); Button(onClick = { go("mapper") }, Modifier.fillMaxWidth().height(58.dp)) { Text("🎯 MAPEADOR") }
        Spacer(Modifier.height(12.dp)); Button(onClick = { go("profiles") }, Modifier.fillMaxWidth().height(52.dp)) { Text("🎮 MEUS PERFIS") }
        Spacer(Modifier.height(12.dp)); Button(onClick = { go("permissions") }, Modifier.fillMaxWidth().height(52.dp)) { Text("⚙ PERMISSÕES") }
        Spacer(Modifier.height(12.dp)); Button(onClick = { go("premium") }, Modifier.fillMaxWidth().height(52.dp)) { Text("⭐ PREMIUM") }
    }
}

@Composable
fun Mapper(activeName: String, existing: Profile?, onSave: (Profile) -> Unit, back: () -> Unit) {
    var profileName by remember(existing) { mutableStateOf(existing?.name ?: activeName) }
    var game by remember(existing) { mutableStateOf(existing?.game ?: "Free Fire") }
    var horizontal by remember(existing) { mutableFloatStateOf((existing?.horizontal ?: 50).toFloat()) }
    var vertical by remember(existing) { mutableFloatStateOf((existing?.vertical ?: 50).toFloat()) }
    var aim by remember(existing) { mutableFloatStateOf((existing?.aim ?: 50).toFloat()) }
    var commands by remember(existing) { mutableStateOf(existing?.commands ?: emptyList()) }
    var showDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Header("Mapeador", back)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
            item {
                OutlinedTextField(profileName, { profileName = it }, label = { Text("Nome do perfil") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp)); OutlinedTextField(game, { game = it }, label = { Text("Jogo") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(15.dp)); Text("Sensibilidade", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Sens("Horizontal", horizontal) { horizontal = it }; Sens("Vertical", vertical) { vertical = it }; Sens("Mira", aim) { aim = it }
                Button(onClick = { showDialog = true }, Modifier.fillMaxWidth().height(55.dp)) { Text("＋ ADICIONAR COMANDO") }
                Spacer(Modifier.height(12.dp)); Text("Comandos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(commands, key = { it.id }) { command ->
                Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) { Text(command.name, color = Color.White, fontWeight = FontWeight.Bold); Text("${command.key} • ${command.action} • X${command.x} Y${command.y}", color = Color.Gray, fontSize = 12.sp) }
                    TextButton(onClick = { commands = commands.filterNot { it.id == command.id } }) { Text("Excluir", color = Color(0xFFFF6B6B)) }
                } }
            }
            item { Spacer(Modifier.height(10.dp)); Button(onClick = { onSave(Profile(profileName.ifBlank { "Meu perfil" }, game.ifBlank { "Outro" }, horizontal.toInt(), vertical.toInt(), aim.toInt(), commands)); }, Modifier.fillMaxWidth().height(54.dp)) { Text("💾 SALVAR PERFIL") }; Spacer(Modifier.height(25.dp)) }
        }
    }
    if (showDialog) AddCommandDialog({ showDialog = false }) { commands = commands + it; showDialog = false }
}

@Composable
fun AddCommandDialog(onDismiss: () -> Unit, onAdd: (Command) -> Unit) {
    var name by remember { mutableStateOf("") }; var key by remember { mutableStateOf("") }; var action by remember { mutableStateOf("Toque") }; var x by remember { mutableStateOf("50") }; var y by remember { mutableStateOf("50") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Novo comando") }, text = { Column {
        OutlinedTextField(name, { name = it }, label = { Text("Nome") }, singleLine = true); Spacer(Modifier.height(7.dp)); OutlinedTextField(key, { key = it }, label = { Text("Tecla / botão") }, singleLine = true); Spacer(Modifier.height(7.dp)); OutlinedTextField(action, { action = it }, label = { Text("Ação") }, singleLine = true); Spacer(Modifier.height(7.dp)); Row { OutlinedTextField(x, { x = it.filter(Char::isDigit) }, label = { Text("X") }, Modifier.weight(1f), singleLine = true); Spacer(Modifier.width(7.dp)); OutlinedTextField(y, { y = it.filter(Char::isDigit) }, label = { Text("Y") }, Modifier.weight(1f), singleLine = true) }
    } }, confirmButton = { TextButton(onClick = { if (name.isNotBlank() && key.isNotBlank()) onAdd(Command(System.currentTimeMillis(), name, key, action, x.toIntOrNull() ?: 50, y.toIntOrNull() ?: 50)) }) { Text("Adicionar", color = Cyan) } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } })
}

@Composable fun Sens(label: String, value: Float, onChange: (Float) -> Unit) { Column(Modifier.padding(vertical = 5.dp)) { Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) { Text(label, color = Color.White); Text(value.toInt().toString(), color = Cyan) }; Slider(value, onChange, valueRange = 0f..100f) } }

@Composable
fun Profiles(profiles: List<Profile>, active: String, select: (Profile) -> Unit, update: (List<Profile>) -> Unit, back: () -> Unit) {
    var showNew by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) { Header("Meus perfis", back); LazyColumn(Modifier.padding(horizontal = 20.dp)) {
        item { Button(onClick = { showNew = true }, Modifier.fillMaxWidth()) { Text("＋ NOVO PERFIL") }; Spacer(Modifier.height(10.dp)) }
        items(profiles) { p -> Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) { Row(Modifier.padding(15.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(p.name, color = Color.White, fontWeight = FontWeight.Bold); Text(p.game + if (p.name == active) " • ATIVO" else "", color = if (p.name == active) Cyan else Color.Gray) }; TextButton({ select(p) }) { Text("Abrir", color = Cyan) }; TextButton({ update(profiles.filterNot { it.name == p.name }) }) { Text("Excluir", color = Color(0xFFFF6B6B)) } } } }
    } }
    if (showNew) AddProfileDialog({ showNew = false }) { p -> update(profiles + p); showNew = false }
}

@Composable fun AddProfileDialog(dismiss: () -> Unit, add: (Profile) -> Unit) { var name by remember { mutableStateOf("") }; var game by remember { mutableStateOf("Free Fire") }; AlertDialog(onDismissRequest = dismiss, title = { Text("Novo perfil") }, text = { Column { OutlinedTextField(name, { name = it }, label = { Text("Nome") }, singleLine = true); Spacer(Modifier.height(8.dp)); OutlinedTextField(game, { game = it }, label = { Text("Jogo") }, singleLine = true) } }, confirmButton = { TextButton({ if (name.isNotBlank()) add(Profile(name, game, 50, 50, 50, emptyList())) }) { Text("Criar", color = Cyan) } }, dismissButton = { TextButton(dismiss) { Text("Cancelar") } }) }

@Composable
fun Permissions(context: Context, back: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp)) { Header("Permissões", back); Text("Para recursos de mapeamento/overlay, o Android exige permissões especiais.", color = Color.LightGray); Spacer(Modifier.height(18.dp)); Button({ context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))) }, Modifier.fillMaxWidth()) { Text("Ativar sobreposição") }; Spacer(Modifier.height(10.dp)); Button({ context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }, Modifier.fillMaxWidth()) { Text("Abrir Acessibilidade") }; Spacer(Modifier.height(18.dp)); Text("A compatibilidade com jogos depende das APIs do Android e das regras de cada jogo. O ProMapper não tenta burlar anti-cheat.", color = Color.Gray, fontSize = 13.sp) } }

@Composable fun Premium(back: () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp)) { Header("Premium", back); Text("Planos", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.height(12.dp)); Plan("Semanal", "promapper_weekly"); Plan("Mensal", "promapper_monthly"); Plan("Anual", "promapper_yearly"); Spacer(Modifier.height(15.dp)); Text("Os IDs acima são os produtos que devem ser cadastrados no Google Play Console antes da cobrança real funcionar.", color = Color.Gray, fontSize = 13.sp) } }
@Composable fun Plan(name: String, id: String) { Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) { Column { Text(name, color = Color.White, fontWeight = FontWeight.Bold); Text(id, color = Color.Gray, fontSize = 12.sp) }; Button({}) { Text("Assinar") } } } }
