package com.admin.portal.Employee

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.admin.portal.databinding.ActivityEomployeeMainBinding
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.*

class EomployeeMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityEomployeeMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEomployeeMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.checkin.setOnClickListener {
            if (binding.checkInData.text.toString().isEmpty()) {
                val intent = Intent(this, ScannerActivity::class.java)
                intent.putExtra("scantype", "checkin")
                startActivity(intent)
            } else {
                Toast.makeText(this, "Already Check In", Toast.LENGTH_LONG).show()
            }

        }
        binding.checkout.setOnClickListener {
            if (binding.checkOutData.text.toString().isEmpty()){
            val intent = Intent(this, ScannerActivity::class.java)
            intent.putExtra("scantype", "checkout")
            startActivity(intent)
            }else{
                Toast.makeText(this,"Already Check Out",Toast.LENGTH_LONG).show()
            }

        }
        binding.histry.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.putExtra("scantype", "checkout")
            startActivity(intent)
//            finish()
        }
        binding.name.text = Paper.book().read("name", "User Name")


    }

    override fun onResume() {
        super.onResume()


        var checkin: String? = Paper.book().read("check_in", "")
        var checkout: String? = Paper.book().read("check_in", "")

        binding.checkInData.text = checkin
        binding.checkOutData.text = checkout

        if (checkin!!.isNotEmpty()) {


            var currentDateandTime: String? = null

            if (checkout!!.isEmpty()) {
                val sdf = SimpleDateFormat("hh:mm:ss", Locale.getDefault())
                currentDateandTime = sdf.format(Date())
            } else {
                currentDateandTime = checkout.split(" ")[1]
            }


            val simpleDateFormat = SimpleDateFormat("hh:mm:ss")


            var data = checkin.split(" ")


            var date1 = simpleDateFormat.parse(data[1])
            var date2 = simpleDateFormat.parse(currentDateandTime)

            val difference: Long = date2.time - date1.time
            var days = (difference / (1000 * 60 * 60 * 24)).toInt()
            var hours = ((difference - 1000 * 60 * 60 * 24 * days) / (1000 * 60 * 60))
            var min =
                (difference - 1000 * 60 * 60 * 24 * days - 1000 * 60 * 60 * hours) / (1000 * 60)
            hours = if (hours < 0) -hours else hours
            min = if (min < 0) -min else min
            Log.i("======= Hours", " :: $hours  == $min")


                binding.totalHour.text = "$hours:$min"


        }

    }
}