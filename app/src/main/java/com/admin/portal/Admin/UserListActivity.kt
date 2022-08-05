package com.admin.portal.Admin


import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.admin.portal.Employee.HistoryActivity
import com.admin.portal.LoginMain.SignupActivity
import com.admin.portal.Model.Alluser
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivityUserListBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import com.admin.portal.R
import io.paperdb.Paper

class UserListActivity : AppCompatActivity(), AllUserAdapter.RecyclerViewItemInterface {
    lateinit var binding: ActivityUserListBinding
    var alluser: ArrayList<Alluser> = ArrayList()
    var user_type = ""
    var account_type = ""
    var isadmin = false


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


        binding.addNew.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            intent.putExtra("user_type", account_type)
            startActivity(intent)
        }

        isadmin = Paper.book().read("admin", true)!!

        if (!isadmin)
            binding.addNew.visibility = View.GONE


    }

    override fun onResume() {
        alluser.clear()
        allusers()
        super.onResume()
    }

    lateinit var adapter: AllUserAdapter
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
                                    actor.getString("email"),
                                    actor.getString("id")
                                )
                            )




                            binding.recyclerview.layoutManager = LinearLayoutManager(this)
//                            val data = ArrayList<ItemsViewModel>()
//                            for (i in 1..20) {
//                                data.add(ItemsViewModel("2022-07-26 02:50:00", "2022-07-26 02:50:00"))
//                            }
                            adapter = AllUserAdapter(alluser, this)
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

    override fun onItemClick(position: Int, path: Alluser, view: View) {

        val popup = PopupMenu(this, view)
        //Inflating the Popup using xml file
        //Inflating the Popup using xml file
        popup.menuInflater
            .inflate(R.menu.popup_menu, popup.menu)

        //registering popup with OnMenuItemClickListener

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener { item ->


            if (item.title.equals("Edit")) {

                Paper.book().write("edit_user", path)
                val intent = Intent(this, SignupActivity::class.java)
                intent.putExtra("user_type", account_type)
                startActivity(intent)

            } else if (item.title.equals("Delete")) {
                deleteuser(path.user_id, position)
            }
            true
        }

        popup.show() //showing popup menu


    }

    override fun onMainClick(position: Int, path: Alluser, view: View) {

        if (!isadmin){

           var dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.setCancelable(false)
            dialog.setContentView(R.layout.addextra_time)
            dialog.show()

            var cancel:TextView = dialog.findViewById(R.id.cancel)
            var yes:TextView = dialog.findViewById(R.id.yes)



            cancel.setOnClickListener { dialog.dismiss() }
            yes.setOnClickListener { dialog.dismiss()

                addetratime(path.user_id)
            }




        }else{
            val intent = Intent(this, HistoryActivity::class.java)
            Paper.book().write("edit_user", path)
            startActivity(intent)
        }

    }
    private fun addetratime(userId: String) {

        var dialog: Dialog = Dialog(this)
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

//                        alluser.removeAt(position)
//                        adapter.notifyItemRemoved(position)
//                        adapter.notifyItemRangeChanged(position, alluser.size)

                        Toast.makeText(
                            this,
                            "Time Added Successfully",
                            Toast.LENGTH_SHORT
                        ).show()


                    } else {
                        Toast.makeText(
                            this,
                            objects.getString("message"),
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

                header["do"] = "employee_extratime"
                header["apikey"] = "dwamsoft12345"
                header["employ_id"] = userId

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

    private fun deleteuser(userId: String, position: Int) {

        var dialog: Dialog = Dialog(this)
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

                        alluser.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, alluser.size)

                        Toast.makeText(
                            this,
                            "Calculated Successfully",
                            Toast.LENGTH_SHORT
                        ).show()


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

                header["do"] = "delete_user"
                header["apikey"] = "dwamsoft12345"
                header["account_type"] = "Admin"
                header["user_id"] = userId

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