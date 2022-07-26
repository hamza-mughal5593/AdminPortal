package com.admin.portal.Employee

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.admin.portal.MainActivity
import com.admin.portal.databinding.ActivityEomployeeMainBinding

class EomployeeMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityEomployeeMainBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEomployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkin.setOnClickListener {
            val intent = Intent(this, ScannerActivity::class.java)
            startActivity(intent)
//            finish()
        }








    }


}