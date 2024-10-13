package com.ghhccghk.repeater

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.ghhccghk.repeater.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Random

class MainActivity : ComponentActivity() {
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var jsonObject: JSONObject
    private lateinit var audioDir: String
    private var fileName: String = "明日方舟-重岳" // 用户输入的文件名
    private var displayText: String = "明日方舟-重岳"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查是否为首次启动，并进行解压
        val sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("is_first_run", true)

        if (isFirstRun) {
            // 执行解压操作
            unpackResources()

            // 设置为非首次启动
            with(sharedPreferences.edit()) {
                putBoolean("is_first_run", false)
                apply()
            }
        }

        // 设置音频目录
        val outputDir = getExternalFilesDir(null)?.absolutePath ?: return
        audioDir = "$outputDir/audio/"

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    private fun unpackResources() {
        // 从 res/raw 文件夹中获取资源包 (res.zip)
        val resourceZip: InputStream = resources.openRawResource(R.raw.res)

        // 获取应用的外部存储目录，例如 /storage/emulated/0/Android/data/<package_name>/files/
        val outputDir = getExternalFilesDir(null)?.absolutePath ?: return

        // 使用 Apache Commons Compress 的 ZipArchiveInputStream 读取 ZIP 文件，并指定 UTF-8 编码
        ZipArchiveInputStream(resourceZip, "UTF-8").use { zipInputStream ->
            var entry: ZipArchiveEntry? = zipInputStream.nextZipEntry
            while (entry != null) {
                val outputFile = File(outputDir, entry.name)

                if (entry.isDirectory) {
                    // 如果是目录，创建文件夹
                    outputFile.mkdirs()
                } else {
                    // 如果是文件，解压文件
                    FileOutputStream(outputFile).use { outputStream ->
                        zipInputStream.copyTo(outputStream)
                    }
                }

                entry = zipInputStream.nextZipEntry
            }
        }
    }

    @Composable
    fun MainScreen(modifier: Modifier = Modifier) {
        //var switchState by remember { mutableStateOf(false) }
        var number by remember { mutableStateOf(20) }
        var isFileLoaded by remember { mutableStateOf(false) }
        var isPlaying by remember { mutableStateOf(false) }
        var displayTexta by remember { mutableStateOf("明日方舟-重岳") }
        var timerStarted by remember { mutableStateOf(false) }
        var displayTextb by remember { mutableStateOf("明日方舟-重岳") }
        //var switchState by remember { mutableStateOf(false) }

            LaunchedEffect(timerStarted, number) {
            if (timerStarted) {
                while (true) {
                    delay(number.toLong() * 100) // 设置循环时间
                    isPlaying =!isPlaying
                    onSwitchStateChanged(isPlaying)

                }
            }
        }

        Column(
            modifier = modifier
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Greeting
            Greeting(name = "重岳")

            // 用户输入文件名的 TextField 和 加载文件按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween // 可选: 设置组件之间的间距
            ) {
                // 用户输入文件名的 TextField
                TextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("输入文件名") },
                    modifier = Modifier
                        .weight(1f) // 占据 Row 的大部分空间
                        .padding(end = 8.dp) // 可选: 添加右侧间距
                )

                // 加载文件按钮
                Button(
                    onClick = {
                        val jsonFilePath = "${getExternalFilesDir(null)?.absolutePath}/read/$fileName.json"
                        val jsonFile = File(jsonFilePath)
                        if (jsonFile.exists()) {
                            jsonObject = parseJsonFromFile(jsonFilePath)
                            displayTexta = "文件加载成功"
                            isFileLoaded = true
                        } else {
                            displayTexta = "文件不存在"
                            isFileLoaded = false
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterVertically) // 垂直居中对齐
                ) {
                    Text("加载文件")
                }
            }
            // 显示文件加载状态
            Text(text = displayTexta)

            if (isFileLoaded) {
                // 显示开关按钮用于播放音频
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(displayTextb,
                        modifier = Modifier.offset(y = 9.dp))
                    Switch(
                        checked = isPlaying,
                        onCheckedChange = { checked ->
                            isPlaying = checked
                            if (checked) {
                                // 播放音频
                                displayText = playAudio()
                                displayTextb = "音频已播放"

                            } else {
                                stopAudio()
                                displayText = ""
                                displayTextb = "音频已停止"
                            }

                        },
                        modifier = Modifier.padding(start = 15.dp , end = 15.dp) // 为开关添加一些间距
                    )
                    Button(onClick = { timerStarted = !timerStarted }) {
                        Text(if (timerStarted) "停止循环" else "开始循环")
                    }
                }

                // Number Control with Button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "循环时间: $number 毫秒" ,modifier = Modifier.offset(y = 9.dp) )
                    Button(onClick = { if (number > 2) number-- }) {
                        Text("减少")
                    }
                    Button(onClick = { number++ }) {
                        Text("增加")
                    }
                }

                // Number Control with Slider
                Text(text = "设置循环时间")
                Slider(
                    value = number.toFloat(),
                    onValueChange = { number = it.toInt() },
                    valueRange = 2f..100f,
                    modifier = Modifier.fillMaxWidth()
                )

                // 显示对应的文本内容
            Text(text = displayText)
            }


            // Show GIF if switch is on
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .padding(20.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    AsyncImage(
                        model = R.drawable.czdq,  // Load GIF from resources
                        contentDescription = "Animated GIF",
                        modifier = Modifier.size(200.dp)
                    )
                }
            }

        }
    }

    @Composable
    fun Greeting(name: String, modifier: Modifier = Modifier) {
        Text(
            text = "$name 复读机",
            modifier = modifier
        )
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        MyApplicationTheme {
            MainScreen()
        }
    }

    // 解析 JSON 文件
    private fun parseJsonFromFile(jsonFilePath: String): JSONObject {
        val jsonFile = File(jsonFilePath)
        val jsonString = jsonFile.readText(Charsets.UTF_8)
        return JSONObject(jsonString)
    }

    // 播放音频并显示文本
    private fun playAudio(): String {
        val keys = jsonObject.keys().asSequence().filter { it.matches(Regex("\\d+")) }.toList() // 获取所有数字键
        // 随机生成一个可用的键
        val randomKey = keys[Random().nextInt(keys.size)]
        val textKey = "$randomKey"
        val textKeya = "$randomKey-txt"
        val audioFileName = jsonObject.optString(textKey, ".mp3")  // 获取 $key-txt 对应的音频文件名
        val audioFilePath = "$audioDir$fileName/$audioFileName.wav"

        val audioFile = File(audioFilePath)
        if (!audioFile.exists()) {
            Log.e("AudioPlay", "音频文件不存在: $audioFilePath")
        }
        if (audioFile.exists()) {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
        }

        return jsonObject.optString(textKeya, "无对应文本")
    }

    // 停止音频播放
    private fun stopAudio() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    // This function will be called when the switch state changes
    fun onSwitchStateChanged(isChecked: Boolean) {
        if (isChecked) {
            // Execute the code when the switch is turned on
            displayText = playAudio()
        } else {
            stopAudio()
            displayText = "音频已停止"
        }
    }
}
