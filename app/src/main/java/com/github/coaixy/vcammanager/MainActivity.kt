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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.github.coaixy.vcammanager.ui.theme.Purple500
import com.github.coaixy.vcammanager.ui.theme.VcamManagerTheme
import java.io.File
import java.io.FileOutputStream
import java.util.*

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
                    ComposeNavigation()
                }
            }
        }
    }
    @Composable
    fun ComposeNavigation() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "first"
        ) {
            composable("first") {
                first(navController = navController)
            }
            composable("second"){
                second(navController = navController)
            }
        }
    }
    @Composable
    fun second(navController: NavController){
        createTrueFrame()
        val list = getVideoFileList()
        var index = 0
        val number = if (list.size>10){ 10 }else{ list.size }
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = {Text("TrueManager")},backgroundColor = Purple500
                    , navigationIcon = {
                        IconButton(onClick = { navController.navigate("first") }) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                ) },
            content = {
                LazyColumn(){
                    items(number){
                        Card2(f = list[index],navController)
                        index++
                    }
                }
            },
        )
    }
    lateinit var firstFile: File
    @Composable
    fun first(navController: NavController){
        createVirtualFrame()
        val list = getVirtualVideoList()
        var index = 0
        val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Open))
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(title = {Text("VcamManager")},backgroundColor = Purple500
                    , navigationIcon = {
                        IconButton(onClick = { navController.navigate("second") }) {
                            Icon(imageVector = Icons.Filled.ArrowForward, contentDescription = null)
                        }
                    }, actions = {
                        IconButton(onClick = {
                            if (firstFile!=null){
                                val path = path + "Camera1/"
                                firstFile.renameTo(File(path + "temp.mp4"))
                                File(path + "virtual.mp4").renameTo(firstFile)
                                File(path + "temp.mp4").renameTo(File(path + "virtual.mp4"))
                                recreate()
                            }
                        }) {
                            Icon(Icons.Filled.Send,null)
                        }
                        IconButton(onClick = {
                            if (firstFile!=null){
                                val path = path + "Camera/"
                                val name = Date().time.toString()+".mp4"
                                firstFile.renameTo(File(path+name))
                                recreate()
                            }
                        }) {
                            Icon(Icons.Filled.Delete,null)
                        }
                        IconButton(onClick = {
                            Toast.makeText(this@MainActivity, "?????????????????????", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.Settings,null)
                        }
                    }
                ) },
            content = {
                LazyColumn(){
                    items(list.size){
                        Card1(f = list[index])
                        index++
                    }
                }
            },
        )
    }
    @Composable
    fun Card2(f:File,navController: NavController) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable {
                    if (f.name != "virtual.mp4") {
                        val path = path + "Camera1/"
                        f.renameTo(File(path + f.name))
                        navController.navigate("first")
                    } else {
                        Toast
                            .makeText(this, "?????????????????????", Toast.LENGTH_SHORT)
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
    @Composable
    fun Card1(f:File) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable {
                    if (f.name != "virtual.mp4") {
                        firstFile = f
                        Toast
                            .makeText(this, "????????????" + f.name, Toast.LENGTH_SHORT)
                            .show()
//                        val path = path + "Camera1/"
//                        f.renameTo(File(path + "temp.mp4"))
//                        File(path + "virtual.mp4").renameTo(f)
//                        File(path + "temp.mp4").renameTo(File(path + "virtual.mp4"))
//                        recreate()
                    } else {
                        Toast
                            .makeText(this, "?????????????????????", Toast.LENGTH_SHORT)
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

    /** ??????Android 11?????????????????????????????? */
    private fun checkAndroid11FilePermission() {
        // Android 11 (Api 30)???????????????????????????????????????????????????????????????????????????????????????????????????
        if (!Environment.isExternalStorageManager()) {
            showDialog( """?????????????????????"??????????????????"?????????????????????????????????????????????????????????""") {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }
    }

    private fun showDialog(message: String, okClick: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("??????")
            .setMessage(message)
            .setPositiveButton("??????") { _, _ -> okClick() }
            .setCancelable(false)
            .show()
    }

    private val path = "/storage/emulated/0/DCIM/"

    /**
     * ??????True?????????
     */
    private fun createTrueFrame(){
        //??????????????????????????????
        for (i in getFrameList(0)){
            i.delete()
//            Toast.makeText(this, i.name, Toast.LENGTH_SHORT).show()
        }
        //???????????????
        var count = 1
        for (i in getVideoFileList()){
            if (count==10)break
            val media = MediaMetadataRetriever()
            media.setDataSource(i.absolutePath)
            val file = File(i.absolutePath.replace(i.name,"s/")+i.name.replace(".mp4",".png"))
            if (!file.exists())file.createNewFile()
            //???????????????
            val fileOutputStream = FileOutputStream(file)
            //??????????????????????????????png?????????Bitmap.CompressFormat.PNG????????????jpg??????Bitmap.CompressFormat.JPEG,?????????100%??????????????????
            media.frameAtTime?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            //?????????????????????????????????????????????
            fileOutputStream.flush()
            //????????????????????????
            fileOutputStream.close()
            count++
        }
    }
    /**
     * ??????Virtual?????????
     */
    private fun createVirtualFrame(){
        //??????????????????????????????
        for (i in getFrameList(1)){
            i.delete()
//            Toast.makeText(this, i.name, Toast.LENGTH_SHORT).show()
        }
        //???????????????
        for (i in getVirtualVideoList()){
            val media = MediaMetadataRetriever()
            media.setDataSource(i.absolutePath)
            val file = File(i.absolutePath.replace(i.name,"s/")+i.name.replace(".mp4",".png"))
            if (!file.exists())file.createNewFile()
            //???????????????
            val fileOutputStream = FileOutputStream(file)
            //??????????????????????????????png?????????Bitmap.CompressFormat.PNG????????????jpg??????Bitmap.CompressFormat.JPEG,?????????100%??????????????????
            media.frameAtTime?.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            //?????????????????????????????????????????????
            fileOutputStream.flush()
            //????????????????????????
            fileOutputStream.close()
        }
    }
    /**
     * ??????Camera????????????
     */
    private fun getVideoFileList():MutableList<File>{
        val path = path+"Camera/"
        val result = mutableListOf<File>()
        val tree = File(path).walk()
        tree.maxDepth(1).filter { it.extension=="mp4" }.forEach {
            result.add(it)
        }
        return result
    }
    /**
     * ??????Camera1??????????????????
     */
    private fun getVirtualVideoList():MutableList<File>{
        val path = path+"Camera1/"
        val result = mutableListOf<File>()
        val tree = File(path).walk()
        tree.maxDepth(1).filter { it.extension=="mp4" }.forEach {
            result.add(it)
        }
        return result
    }
    /**
     * ?????????????????????
     */
    private fun getFrameList(flag:Int):MutableList<File>{
        val path = if (flag == 1){path+"Camera1/s/"}else{path+"Camera/s/"}
        if (!File(path).exists())File(path).mkdir()
        val result = mutableListOf<File>()
        val tree = File(path+"s/").walk()
        tree.maxDepth(1).filter { it.extension=="png" }.forEach {
            result.add(it)
        }
        return result
    }
}