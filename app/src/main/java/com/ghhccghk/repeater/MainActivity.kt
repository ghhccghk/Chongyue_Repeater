package com.ghhccghk.repeater

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ghhccghk.repeater.ui.main.MainScreen
import com.ghhccghk.repeater.ui.main.SettingsScreen
import com.ghhccghk.repeater.ui.theme.MyApplicationTheme
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MainActivity : ComponentActivity() {
    private lateinit var audioDir: String
    private var fileName: String = "明日方舟-重岳" // 用户输入的文件名

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
                Navigation()
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
    fun Navigation() {
        var selectedItem by remember { mutableStateOf(0) }

        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedItem == 0,
                        onClick = { selectedItem = 0 },
                        label = { Text("主页") },
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Home") }
                    )
                    NavigationBarItem(
                        selected = selectedItem == 1,
                        onClick = { selectedItem = 1 },
                        label = { Text("设置") },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
                    )
                }
            }
        ) { innerPadding ->
            when (selectedItem) {
                0 -> MainScreen(modifier = Modifier.padding(innerPadding) , audioDir = audioDir , fileNamea = fileName  )
                1 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
            }
        }
    }

    object GlobalState {
        var number : Int =  20
        var isFileLoaded : Boolean = false
        var isPlaying: Boolean = false
        var displayTexta: String = "明日方舟-重岳"
        var timerStarted: Boolean =false
        var displayTextb: String = "明日方舟-重岳"
    }

}
