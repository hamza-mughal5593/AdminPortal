package com.admin.portal.Admin

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.portal.Model.Alluser
import com.admin.portal.Model.ItemsViewModel
import com.admin.portal.R
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivityUserListBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class UserListActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserListBinding
    var alluser: java.util.ArrayList<Alluser> = java.util.ArrayList()
    var user_type = ""
    var account_type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val intent = intent
        user_type = intent.getStringExtra("user_type").toString()

        if (user_type.equals("emp")) {
            binding.name.text = "All Employees"
            account_type = "Employee"
        } else {
            account_type = "Supervisor"
            binding.name.text = "All Supervisor"
        }


        binding.backBtn.setOnClickListener { onBackPressed() }




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

//                        Toast.makeText(
//                            this,
//                            "Calculated Successfully",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        val data = objects.getJSONArray("data")

                        for (i in 0 until data.length()) {
                            val actor: JSONObject = data.getJSONObject(i)
//                            val name = actor.getString("name")
                            alluser.add(
                                Alluser(
                                    actor.getString("name"),
                                    actor.getString("account_type"),
                                    actor.getString("email")
                                )
                            )




                            binding.recyclerview.layoutManager = LinearLayoutManager(this)
//                            val data = ArrayList<ItemsViewModel>()
//                            for (i in 1..20) {
//                                data.add(ItemsViewModel("2022-07-26 02:50:00", "2022-07-26 02:50:00"))
//                            }
                            val adapter = AllUserAdapter(alluser)
                            binding.recyclerview.adapter = adapter


                        }

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
                Toast.makeText(this@UserListActivity, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val header: MutableMap<String, String> = HashMap()

                header["do"] = "all_users"
                header["apikey"] = "dwamsoft12345"
                header["account_type"] = account_type

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