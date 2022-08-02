package com.admin.portal.Employee

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.util.HashMap

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
        } else {
            em_id = Paper.book().read<String>("id","").toString()
        }



        binding.backBtn.setOnClickListener { onBackPressed() }




        allusers()
    }

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


                        val datalist = ArrayList<ItemsViewModel>()

//                        Toast.makeText(
//                            this,
//                            "Calculated Successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        val data = objects.getJSONArray("data")

                        for (i in 0 until data.length()) {
                            val actor: JSONObject = data.getJSONObject(i)
//                            val name = actor.getString("name")
                            datalist.add(
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
                        val adapter = CustomAdapter(datalist)

                        // Setting the Adapter with the recyclerview
                        binding.recyclerview.adapter = adapter


                    } else {
                        Toast.makeText(
                            this,
                            "Error..!",
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


}