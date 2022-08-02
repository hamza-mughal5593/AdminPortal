package com.admin.portal.LoginMain

import com.admin.portal.R
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.admin.portal.Model.Alluser
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivitySignupBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import io.paperdb.Paper
import org.json.JSONException
import org.json.JSONObject

class SignupActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignupBinding
    var user_type = ""
    var what = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)


        user_type = intent.getStringExtra("user_type").toString()


        var alluser = Paper.book().read<Alluser>("edit_user", null)
        if (alluser != null) {
            binding.etname.setText(alluser.name)
            binding.etEmail.setText(alluser.id)
            what = "edit_user"
            Paper.book().delete("edit_user")
        } else {
            what = "add_user"
        }



        binding.signupBtn.setOnClickListener {


            if (binding.etname.text.toString().isNotEmpty() && binding.etEmail.text.toString()
                    .isNotEmpty() && binding.etPassword.text.toString()
                    .isNotEmpty()
            ) {


                signup()


            } else {
                Toast.makeText(
                    this,
                    "Please fill all the fields",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        binding.backBtn.setOnClickListener {
            finish()
        }
    }


    private fun signup() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)



        dialog.show()

        val RegistrationRequest: StringRequest = object : StringRequest(Method.POST,
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
                        if (what.equals("add_user")) {
                            Toast.makeText(
                                this,
                                "User created Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Updated Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

//                        val intent = Intent(this, LoginActivity::class.java)
//                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this,
                            "Email Address already exists...!",
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
                Toast.makeText(this@SignupActivity, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val header: MutableMap<String, String> = HashMap()

                header["do"] = what
                header["apikey"] = "dwamsoft12345"
                header["name"] = binding.etname.text.toString()
                header["email"] = binding.etEmail.text.toString()
                header["password"] = binding.etPassword.text.toString()
                header["account_type"] = "Admin"

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