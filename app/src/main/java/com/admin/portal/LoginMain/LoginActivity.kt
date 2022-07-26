package com.admin.portal.LoginMain

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.widget.Toast
import com.admin.portal.Employee.EomployeeMainActivity
import com.admin.portal.MainActivity
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivityLoginBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import io.paperdb.Paper
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import com.admin.portal.R

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, EomployeeMainActivity::class.java)
            startActivity(intent)
            finish()

            if (binding.etEmail.text.toString().isNotEmpty()
                && binding.etPassword.text.toString().isNotEmpty()){
                login()
            }else{
                Toast.makeText(
                    this,
                    "Enter valid Email and Password",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        binding.backBtn.setOnClickListener {
            finish()
        }


        binding.registrBtn.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }


    }


    private fun login() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)



        dialog.show()

        val RegistrationRequest: StringRequest = object : StringRequest(
            Method.POST,
            "http://www.greensave.co/",
            Response.Listener
            { response ->
                try {

                    if (dialog.isShowing) {
                        dialog.dismiss()
                    }
                    val objects = JSONObject(response)
                    val response_code = objects.getInt("success")
                    if (response_code == 1) {

                        Toast.makeText(
                            this,
                            "Login Successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val data = objects.getJSONObject("data")
                        val lifestyle = data.getString("lifestyle")
                        Paper.book().write("email",data.getString("email"))
                        Paper.book().write("name",data.getString("name"))
                        Paper.book().write("account_type",data.getString("account_type"))
                        Paper.book().write("profile_image",data.getString("image"))
                        if (lifestyle.isNotEmpty())
                        {
//                            Paper.book().write("lifestyle",lifestyle)
//                            val intent = Intent(this, CalculateResultActivity::class.java)
//                            startActivity(intent)
//                            finish()
                        }else{
//                            val intent = Intent(this, MainActivity::class.java)
//                            startActivity(intent)
//                            finish()
                        }


                    } else {
                        Toast.makeText(
                            this,
                            "Enter valid Email and Password",
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
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val header: MutableMap<String, String> = HashMap()

                header["do"]= "login"
                header["apikey"]= "dwamsoft12345"
                header["email"]= binding.etEmail.text.toString()
                header["password"]=binding.etPassword.text.toString()
                header["lat"]= Paper.book().read("latitude", 0.0).toString()
                header["lng"]=Paper.book().read("longitude", 0.0).toString()

                return header
            }

//            override fun getParams(): MutableMap<String, String> {
//                val header: MutableMap<String, String> = HashMap()
//
//                header[""] = ""
//
//                header["do"]="add_user"
//                header["apikey"]="dwamsoft12345"
//                header["name"]=binding.etname.text.toString()
//                header["email"]=binding.etEmail.text.toString()
//                header["password"]=binding.etPassword.text.toString()
//                header["account_type"]=courses[accounttype].toString()
//                header["lat"]= Paper.book().read("latitude", 0.0).toString()
//                header["lng"]=Paper.book().read("longitude", 0.0).toString()
//                return header
//            }
        }
        RegistrationRequest.retryPolicy = DefaultRetryPolicy(
            25000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        MySingleton.getInstance(this).addToRequestQueue(RegistrationRequest)
    }


}