package com.admin.portal.Employee

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.admin.portal.databinding.ActivityEomployeeMainBinding

class EomployeeMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityEomployeeMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEomployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}