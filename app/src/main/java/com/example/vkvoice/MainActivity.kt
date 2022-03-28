package com.example.vkvoice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.vkvoice.db.RecordDatabase
import com.example.vkvoice.repository.RecordRepository
import com.example.vkvoice.ui.RecordViewModel
import com.example.vkvoice.ui.RecordViewModelProviderFactory
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.exceptions.VKAuthException
import kotlinx.android.synthetic.main.login_fragment.*

class MainActivity : AppCompatActivity() {
    lateinit var recordViewModel: RecordViewModel

    private val permList: Array<String> = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        val recordRepository = RecordRepository(RecordDatabase(this))
        val recordViewModelProviderFactory = RecordViewModelProviderFactory(recordRepository)
        recordViewModel = ViewModelProvider(this, recordViewModelProviderFactory).get(RecordViewModel::class.java)

        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        if (!checkForPermission(Manifest.permission.RECORD_AUDIO) && !checkForPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            askPermission(permList)
        }
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object: VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                Log.d("qwert", "res")
                next.text = "Перейти к аудио заметкам"
                recordViewModel.loginState = true
                vk.visibility = View.INVISIBLE
            }

            override fun onLoginFailed(authException: VKAuthException) {
                Toast.makeText(applicationContext, "Вы не дали согласия на доступ к документам ВК", Toast.LENGTH_LONG).show()
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun checkForPermission(permission: String): Boolean {
        ActivityCompat.checkSelfPermission(this, permission)
        return ContextCompat.checkSelfPermission(
            applicationContext,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            askPermission(permList)
        }
    }

    private fun askPermission(permissions: Array<String>) {
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty()) {
            val bool = grantResults[0] == PackageManager.PERMISSION_GRANTED
            onPermissionResult(bool)
            val bool1 = grantResults[1] == PackageManager.PERMISSION_GRANTED
            onPermissionResult(bool1)
        }
    }
}