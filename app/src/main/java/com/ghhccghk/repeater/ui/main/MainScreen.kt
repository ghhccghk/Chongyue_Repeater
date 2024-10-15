package com.ghhccghk.repeater.ui.main

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.ghhccghk.repeater.R
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.File
import java.util.Random


lateinit var mediaPlayer: MediaPlayer
lateinit var jsonObject: JSONObject
var displayText: String = "明日方舟-重岳"
var fileName: String = "明日方舟-重岳"
lateinit var audioaDir: String

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    audioDir: String,
    fileNamea: String
) {
    var number by rememberSaveable  { mutableStateOf(20) }
    var isFileLoaded by rememberSaveable  { mutableStateOf(false) }
    var isPlaying by rememberSaveable  { mutableStateOf(false) }
    var displayTexta by rememberSaveable  { mutableStateOf("明日方舟-重岳") }
    var timerStarted by rememberSaveable  { mutableStateOf(false) }
    var displayTextb by rememberSaveable  { mutableStateOf("音频未播放") }
    val context = LocalContext.current // 获取 Context
    var fileName = fileNamea
    audioaDir = audioDir
    val scrollState = rememberScrollState() // 创建滚动状态

    LaunchedEffect(timerStarted, number) {
        if (timerStarted) {
            while (true) {
                delay(number.toLong() * 100) // 设置循环时间
                isPlaying = !isPlaying
                onSwitchStateChanged(isPlaying)
            }
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp) // 增加一些外边距
            .verticalScroll(scrollState) // 添加垂直滚动功能
            .fillMaxSize(), // 使列填满可用空间
        verticalArrangement = Arrangement.spacedBy(16.dp) // 组件之间的间隔
    ) {
        // Greeting
        Greeting(name = "重岳")

        // 用户输入文件名的 TextField 和 加载文件按钮
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                    val jsonFilePath = "${context.getExternalFilesDir(null)?.absolutePath}/read/$fileName.json"
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
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(displayTextb, modifier = Modifier.align(Alignment.CenterVertically))
                Switch(
                    checked = isPlaying,
                    onCheckedChange = { checked ->
                        isPlaying = checked
                        if (checked) {
                            displayText = playAudio()
                            displayTextb = "音频已播放"
                        } else {
                            stopAudio()
                            displayText = "音频已停止"
                            displayTextb = "音频已停止"
                        }
                    },
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp) // 为开关添加一些间距
                )
                Button(onClick = { timerStarted = !timerStarted }) {
                    Text(if (timerStarted) "停止循环" else "开始循环")
                }
            }

            // Number Control with Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "循环时间: $number 毫秒", modifier = Modifier.align(Alignment.CenterVertically))
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
                    .padding(20.dp)
                    .fillMaxWidth(), // 使 Box 填满宽度
                contentAlignment = Alignment.TopCenter
            ) {
                AsyncImage(
                    model = R.drawable.czdq, // Load GIF from resources
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
        modifier = modifier,
        fontSize = 24.sp
    )
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
    val audioFilePath = "$audioaDir$fileName/$audioFileName.wav"

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