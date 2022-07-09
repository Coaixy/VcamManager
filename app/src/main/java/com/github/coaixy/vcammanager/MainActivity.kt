package com.github.coaixy.vcammanager

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.coaixy.vcammanager.ui.theme.Purple500
import com.github.coaixy.vcammanager.ui.theme.VcamManagerTheme
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VcamManagerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    checkAndroid11FilePermission()
                    createFrame()
                    val list = getFileList()
                    var index = 0
                    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))
                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = { TopAppBar(title = {Text("VcamManager")},backgroundColor = Purple500)  },
//                        floatingActionButtonPosition = FabPosition.End,
//                        floatingActionButton = { FloatingActionButton(onClick = {}){
//                            Text("X")
//                        } },
//                        drawerContent = { Text(text = "drawerContent") },
                        content = {
                            LazyColumn(){
                                items(list.size){
                                    CardDemo(f = list[index])
                                    index++
                                }
                            }
                        },
//                        bottomBar = { BottomAppBar(backgroundColor = materialBlue700) { Text("BottomAppBar") } }
                    )

                }
            }
        }
    }
    @Composable
    fun CardDemo(f:File) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable {
                    if (f.name != "virtual.mp4") {
                        f.renameTo(File(path + "temp.mp4"))
                        File(path + "virtual.mp4").renameTo(f)
                        File(path + "temp.mp4").renameTo(File(path + "virtual.mp4"))
                        recreate()
                    } else {
                        Toast
                            .makeText(this, "你无法选择这个", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Row {
                    AsyncImage(
                        model = f.absolutePath.replace(f.name,"s/")+f.name.replace(".mp4",".png"),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        buildAnnotatedString {
                            append("FileName:")
                            withStyle(style = SpanStyle(fontWeight = FontWeight.W900)) {
                                append(f.name.replace(".mp4",""))
                            }
                        }
                    )

                }
            }
        }
    }

    /** 检查Android 11或更高版本的文件权限 */
    private fun checkAndroid11FilePermission() {
        // Android 11 (Api 30)或更高版本的写文件权限需要特殊申请，需要动态申请管理所有文件的权限
        if (!Environment.isExternalStorageManager()) {
            showDialog( """本应用需要获取"访问所有文件"权限，请给予此权限，否则无法使用本应用""") {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }

    private fun showDialog(message: String, okClick: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ -> okClick() }
            .setCancelable(false)
            .show()
    }

    private val path = "/storage/emulated/0/DCIM/Camera1/"

    /**
     * 创建缩略图
     */
    private fun createFrame(){
        //首先删除原来的缩略图
        for (i in getFrameList()){
            i.delete()
//            Toast.makeText(this, i.name, Toast.LENGTH_SHORT).show()
        }
        //创建缩略图
        for (i in getFileList()){
            val media = MediaMetadataRetriever()
            media.setDataSource(i.absolutePath)
            val file = File(i.absolutePath.replace(i.name,"s/")+i.name.replace(".mp4",".png"))
            //文件输出流
            val fileOutputStream = FileOutputStream(file)
            //压缩图片，如果要保存png，就用Bitmap.CompressFormat.PNG，要保存jpg就用Bitmap.CompressFormat.JPEG,质量是100%，表示不压缩
            media.frameAtTime?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            //写入，这里会卡顿，因为图片较大
            fileOutputStream.flush()
            //记得要关闭写入流
            fileOutputStream.close()
        }
    }
    /**
     * 返回视频文件列表
     */
    private fun getFileList():MutableList<File>{
        val result = mutableListOf<File>()
        val tree = File(path).walk()
        tree.maxDepth(1).filter { it.extension=="mp4" }.forEach {
            result.add(it)
        }
        return result
    }
    /**
     * 返回缩略图列表
     */
    private fun getFrameList():MutableList<File>{
        val result = mutableListOf<File>()
        val tree = File(path+"s/").walk()
        tree.maxDepth(1).filter { it.extension=="png" }.forEach {
            result.add(it)
        }
        return result
    }
}