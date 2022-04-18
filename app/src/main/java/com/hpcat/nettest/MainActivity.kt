package com.hpcat.nettest

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import com.hpcat.permissionb.PermissionB

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.request_permission).apply {
            setOnClickListener {
                PermissionB(this@MainActivity)
                    .request(Manifest.permission.CAMERA)
            }
        }
    }
}