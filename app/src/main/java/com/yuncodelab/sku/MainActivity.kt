package com.yuncodelab.sku

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.yuncodelab.sku.databinding.ActivityMainBinding
import com.yuncodelab.sku.ui.SkuBottomSheetDialogFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnShowSku.setOnClickListener {
            val skuDialog = SkuBottomSheetDialogFragment()
            skuDialog.show(supportFragmentManager, "SkuDialog")
        }
    }
}