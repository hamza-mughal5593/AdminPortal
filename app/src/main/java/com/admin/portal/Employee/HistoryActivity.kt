package com.admin.portal.Employee

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.portal.Model.Alluser
import com.admin.portal.Model.ItemsViewModel
import com.admin.portal.R
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivityHistoryBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import io.paperdb.Paper
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class HistoryActivity : AppCompatActivity() {
    lateinit var binding: ActivityHistoryBinding

    var em_id=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)



        var alluser = Paper.book().read<Alluser>("edit_user", null)
        if (alluser != null) {
            em_id =alluser.user_id
            Paper.book().delete("edit_user")
            binding.downloadcsv.visibility = View.VISIBLE
        } else {
            em_id = Paper.book().read<String>("id","").toString()
            binding.downloadcsv.visibility = View.GONE
        }



        binding.backBtn.setOnClickListener { onBackPressed() }



        binding.downloadcsv.setOnClickListener {
            splitdata()
        }



        allusers()
    }
    var datalist : ArrayList<ItemsViewModel>? = null
    lateinit var dialog: Dialog
    private fun allusers() {




        dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)


        dialog.show()


        val RegistrationRequest: StringRequest = @RequiresApi(Build.VERSION_CODES.N)
        object : StringRequest(
            Method.POST,
            "https://ayadimarble.com/checkin/",
            Response.Listener
            { response ->
                try {

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    val objects = JSONObject(response)
                    val response_code = objects.getInt("success")
                    if (response_code == 1) {


                         datalist = ArrayList<ItemsViewModel>()

//                        Toast.makeText(
//                            this,
//                            "Calculated Successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        val data = objects.getJSONArray("data")

                        for (i in 0 until data.length()) {
                            val actor: JSONObject = data.getJSONObject(i)
//                            val name = actor.getString("name")
                            datalist?.add(
                                ItemsViewModel(
                                    actor.getString("check_in"),
                                    actor.getString("check_out"),
                                    actor.getString("extra_time")
                                )
                            )
                        }

                        binding.recyclerview.layoutManager = LinearLayoutManager(this)

                        // ArrayList of class ItemsViewModel


                        // This loop will create 20 Views containing
                        // the image with the count of view
//                        for (i in 1..20) {
//                            data.add(ItemsViewModel("2022-07-26 02:50:00", "2022-07-26 02:50:00"))
//                        }

                        // This will pass the ArrayList to our Adapter
                        val adapter = CustomAdapter(datalist!!)

                        // Setting the Adapter with the recyclerview
                        binding.recyclerview.adapter = adapter


                    } else {
                        Toast.makeText(
                            this,
                            "No History Found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                dialog.dismiss()
            },
            Response.ErrorListener { volleyError ->
                dialog.dismiss()
                var message: String? = null
                when (volleyError) {
                    is NetworkError -> {
                        message = "Cannot connect to Internet...Please check your connection!"
                    }
                    is ServerError -> {
                        message =
                            "The server could not be found. Please try again after some time!!"
                    }
                    is AuthFailureError -> {
                        message = "Cannot connect to Internet...Please check your connection!"
                    }
                    is ParseError -> {
                        message = "Parsing error! Please try again after some time!!"
                    }
                    is NoConnectionError -> {
                        message = "Cannot connect to Internet...Please check your connection!"
                    }
                    is TimeoutError -> {
                        message = "Connection TimeOut! Please check your internet connection."
                    }
                }
                Toast.makeText(this@HistoryActivity, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val header: MutableMap<String, String> = HashMap()

                header["do"] = "employees_data"
                header["apikey"] = "dwamsoft12345"
                header["employ_id"] = em_id

                return header
            }

        }
        RegistrationRequest.retryPolicy = DefaultRetryPolicy(
            25000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        MySingleton.getInstance(this).addToRequestQueue(RegistrationRequest)
    }

    var data: java.lang.StringBuilder? = null
    private fun splitdata() {
//        println("GET DATA /**/IS $response")

        //StringBuilder  to store the data
        data = StringBuilder()

        //row heading to store in CSV file
        data?.append("id,firstname,lastname,phone")
        for (i in datalist!!.indices) {
            //then we split each user data using # symbol as we have in the response string
//            val each_user = res_data[i].split("#").toTypedArray()
            println("Splited # ID: " + datalist?.get(i)?.checkin)
//            println("Splited # Firstname? : " + each_user[1])
//            println("Splited # Lastname? : " + each_user[2])
//            println("Splited # Phone ? : " + each_user[3])
            // then add each user data in data string builder
            data?.append(
                """
                
                ${datalist!![i].checkin},${datalist!![i].checkout},${datalist!![i].extra_time},${datalist!![i].extra_time}
                """.trimIndent()
            )
        }
        CreateCSV(data!!)
    }

    private fun CreateCSV(data: StringBuilder) {
        val calendar: Calendar = Calendar.getInstance()
        val time: Long = calendar.timeInMillis
        try {
            //
            val out: FileOutputStream = openFileOutput("CSV_Data_$time.csv", Context.MODE_PRIVATE)

            //store the data in CSV file by passing String Builder data
            out.write(data.toString().toByteArray())
            out.close()
            val context: Context = applicationContext
            val newFile = File(Environment.getExternalStorageDirectory(), "SimpleCVS")
            if (!newFile.exists()) {
                newFile.mkdir()
            }
            val file = File(context.filesDir, "CSV_Data_$time.csv")
            val path: Uri = FileProvider.getUriForFile(context, "com.example.dataintocsvformat", file)
            //once the file is ready a share option will pop up using which you can share
            // the same CSV from via Gmail or store in Google Drive
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/csv"
            intent.putExtra(Intent.EXTRA_SUBJECT, "Data")
            intent.putExtra(Intent.EXTRA_STREAM, path)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(intent, "Excel Data"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }





}