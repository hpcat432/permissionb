package com.hpcat.nettest

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.PermissionB
import com.hpcat.permissionb.request.Deny
import com.hpcat.permissionb.request.RequestCallback

class MainActivity : FragmentActivity() {

    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initPermissionB()

        tvResult = findViewById(R.id.tv_result)

        findViewById<Button>(R.id.request_permission).apply {
            setOnClickListener {
                PermissionB(this@MainActivity)
                    .callback(object : RequestCallback {
                        override fun onResult(allGrated: Boolean, grantList: List<String>, denyList: List<Deny>
                        ) {
                            tvResult.text = "onResult allGranted : $allGrated, grantList : $grantList, denyList : $denyList"
                        }
                    })
                    .hideNeverAskDialog(true)
                    .request(Manifest.permission.CAMERA)
            }
        }

        findViewById<Button>(R.id.request_permission_multi).apply {
            setOnClickListener {
                PermissionB(this@MainActivity)
                    .callback(object : RequestCallback {
                        override fun onResult(allGrated: Boolean, grantList: List<String>, denyList: List<Deny>
                        ) {
                            tvResult.text = "onResult allGranted : $allGrated, grantList : $grantList, denyList : $denyList"
                        }
                    })
                    .request(Manifest.permission.CAMERA, Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_STATE)
            }
        }

        findViewById<Button>(R.id.request_permission_top_hint).apply {
            setOnClickListener {
                PermissionB(this@MainActivity)
                    .callback(object : RequestCallback {
                        override fun onResult(allGrated: Boolean, grantList: List<String>, denyList: List<Deny>
                        ) {
                            tvResult.text = "onResult allGranted : $allGrated, grantList : $grantList, denyList : $denyList"
                        }
                    })
                    .showTopHint(true)
                    .request(Manifest.permission.CAMERA)
            }
        }

    }

    private fun initPermissionB() {
        PermissionB.sTopHintTextMap = mutableMapOf<String, Pair<String, String>>().apply {
            put(Manifest.permission.CAMERA, Pair("相机权限", "正在请求相机权限"))
        }
    }
}