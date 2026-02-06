package com.siliconsage.miner.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Serializable
data class UpdateInfo(
    val version: String,
    val build: Int = 0,
    val date: String = "",
    val changes: String = "",
    val url: String = "", // Release page URL
    val downloadUrl: String = "" // Direct APK download URL
)

object UpdateManager {
    private const val UPDATE_URL = "https://raw.githubusercontent.com/Vatteck/SiliconSageAIMiner/refs/heads/master/version.json"
    private val client = OkHttpClient()
    private val jsonConfig = Json { ignoreUnknownKeys = true }

    fun checkUpdate(currentVersion: String, currentBuild: Int, onResult: (UpdateInfo?, Boolean) -> Unit) {
        val request = Request.Builder().url(UPDATE_URL).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UpdateManager", "Failed to check update", e)
                onResult(null, false)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onResult(null, false)
                        return
                    }
                    try {
                        val jsonString = response.body?.string()
                        if (jsonString != null) {
                            val info = jsonConfig.decodeFromString<UpdateInfo>(jsonString)
                            if (isNewer(currentVersion, currentBuild, info.version, info.build)) {
                                onResult(info, true)
                            } else {
                                onResult(null, true)
                            }
                        } else {
                            onResult(null, false)
                        }
                    } catch (e: Exception) {
                        Log.e("UpdateManager", "Error parsing update info", e)
                        onResult(null, false)
                    }
                }
            }
        })
    }

    private fun isNewer(currentVer: String, currentBuild: Int, remoteVer: String, remoteBuild: Int): Boolean {
        try {
            // Strip "v" prefix if present and pre-release suffix
            val cClean = currentVer.removePrefix("v").split("-")[0]
            val rClean = remoteVer.removePrefix("v").split("-")[0]
            
            val cParts = cClean.split(".").map { it.toIntOrNull() ?: 0 }
            val rParts = rClean.split(".").map { it.toIntOrNull() ?: 0 }
            val length = maxOf(cParts.size, rParts.size)

            for (i in 0 until length) {
                val c = cParts.getOrElse(i) { 0 }
                val r = rParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            
            // If core versions match, check build number
            return remoteBuild > currentBuild
        } catch (e: Exception) {
            return remoteVer > currentVer // Fallback
        }
    }

    fun openReleasePage(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Failed to open browser", e)
        }
    }

    fun downloadUpdate(url: String, context: Context, onProgress: (Float) -> Unit, onComplete: (File?) -> Unit) {
        val request = Request.Builder().url(url).build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onComplete(null)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    onComplete(null)
                    return
                }

                val body = response.body ?: return
                val file = File(context.externalCacheDir, "update.apk")
                
                try {
                    val input = body.byteStream()
                    val output = FileOutputStream(file)
                    val buffer = ByteArray(4 * 1024)
                    val total = body.contentLength()
                    var downloaded = 0L
                    var read: Int
                    
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) {
                            onProgress(downloaded.toFloat() / total.toFloat())
                        }
                    }
                    output.flush()
                    output.close()
                    onComplete(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onComplete(null)
                } finally {
                    body.close()
                }
            }
        })
    }
    
    fun installUpdate(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpdateManager", "Install failed", e)
        }
    }
}
