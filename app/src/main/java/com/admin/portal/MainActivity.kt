package com.admin.portal

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.admin.portal.Admin.UserListActivity
import com.admin.portal.Employee.EomployeeMainActivity
import com.admin.portal.databinding.ActivityMainBinding
import io.paperdb.Paper

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        if (!Paper.book().read("admin",true)!!){
            val intent = Intent(this, UserListActivity::class.java)
            intent.putExtra("user_type","emp")
            startActivity(intent)
            finish()
        }else{
            binding.empBtn.setOnClickListener{
                val intent = Intent(this, UserListActivity::class.java)
                intent.putExtra("user_type","emp")
                startActivity(intent)
            }
            binding.supBtn.setOnClickListener{
                val intent = Intent(this, UserListActivity::class.java)
                intent.putExtra("user_type","sup")
                startActivity(intent)
            }
        }



    }
}