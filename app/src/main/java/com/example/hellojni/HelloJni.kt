/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellojni

import android.content.Intent
import android.content.res.AssetManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hellojni.databinding.ActivityHelloJniBinding
import android.util.Log
import kotlinx.android.synthetic.main.activity_hello_jni.*
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Error

class HelloJni : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * Retrieve our TextView and set its content.
         * the text is retrieved by calling a native
         * function.
         */
        val binding = ActivityHelloJniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShare.setOnClickListener {
            //Get text from TextView and store in variable "s"
            val s = binding.helloTextview.text.toString()
            //Intent to share the text
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, s);
            startActivity(Intent.createChooser(shareIntent,"Share via"))
        }

        binding.btnRun.setOnClickListener {
            Thread {
                this@HelloJni.runOnUiThread(java.lang.Runnable {
                    this.hello_textview.text = "running ..."
                })
                val num_cpu_cores = Runtime.getRuntime().availableProcessors()
                val cpu_info = getCPUInfo()
                val stringFromJNI = stringFromJNI(this.applicationContext.assets, this.applicationContext.cacheDir.absolutePath)
                // val stringFromJNI = "stringFromJNI"
                this@HelloJni.runOnUiThread(java.lang.Runnable {
                    this.hello_textview.text = stringFromJNI +
                            "\nmodel: " + Build.MODEL + "\ncores: " + num_cpu_cores + "\ncpu_info: " + cpu_info
                })
            }.start()
        }
    }

    /*
     * A native method that is implemented by the
     * 'hello-jni' native library, which is packaged
     * with this application.
     */
    external fun stringFromJNI(assetManager: AssetManager, cacheDir: String): String?

    @Throws(IOException::class)
    fun getCPUInfo(): String? {
        val br = BufferedReader(FileReader("/proc/cpuinfo"))
        var str: String = ""
        val output: MutableMap<String, String> = HashMap()
        while (true) {
            val r = br.readLine() ?: break
            str = r
            val data = str.split(":").toTypedArray()
            if (data.size > 1) {
                var key = data[0].trim { it <= ' ' }.replace(" ", "_")
                if (key == "model_name") key = "cpu_model"
                output[key] = data[1].trim { it <= ' ' }
            }
        }

        br.close()
        return output.toString()
    }

    companion object {
    /*
     * this is used to load the 'hello-jni' library on application
     * startup. The library has already been unpacked into
     * /data/data/com.example.hellojni/lib/libhello-jni.so
     * at the installation time by the package manager.
     */
        init {
            try {
                System.loadLibrary("hello-jni")
            } catch (error: UnsatisfiedLinkError) {
                Log.e("Dummy", error.localizedMessage)
            } catch (error: Error) {
                Log.e("Dummy", error.localizedMessage)
            } catch (error: Exception) {
                Log.e("Dummy", error.localizedMessage)
            }
        }
    }
}

