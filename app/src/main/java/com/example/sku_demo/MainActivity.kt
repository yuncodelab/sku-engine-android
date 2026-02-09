package com.example.sku_demo

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.sku_demo.ui.SkuBottomSheetDialogFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnOpenSku: Button = findViewById(R.id.btnShowSku)
        btnOpenSku.setOnClickListener {
            val skuDialog = SkuBottomSheetDialogFragment()
            skuDialog.show(supportFragmentManager, "SkuDialog")
        }
    }
}