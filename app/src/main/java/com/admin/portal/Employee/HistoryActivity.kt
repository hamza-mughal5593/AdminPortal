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
import com.admin.portal.Model.ItemsViewModel
import com.admin.portal.R
import com.admin.portal.Utils.MySingleton
import com.admin.portal.databinding.ActivityHistoryBinding
import com.android.volley.*
import com.android.volley.toolbox.StringRequest
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class HistoryActivity : AppCompatActivity() {
    lateinit var binding: ActivityHistoryBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)




        binding.backBtn.setOnClickListener { onBackPressed() }


        binding.recyclerview.layoutManager = LinearLayoutManager(this)

        // ArrayList of class ItemsViewModel
        val data = ArrayList<ItemsViewModel>()

        // This loop will create 20 Views containing
        // the image with the count of view
        for (i in 1..20) {
            data.add(ItemsViewModel("2022-07-26 02:50:00", "2022-07-26 02:50:00"))
        }

        // This will pass the ArrayList to our Adapter
        val adapter = CustomAdapter(data)

        // Setting the Adapter with the recyclerview
        binding.recyclerview.adapter = adapter


    }

//    lateinit var dialog: Dialog
//    private fun allusers() {
//
//
//
//
//        dialog = Dialog(this)
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//        dialog.setCancelable(false)
//        dialog.setContentView(R.layout.loading_dialog)
//
//
//        dialog.show()
//
//
//        val RegistrationRequest: StringRequest = @RequiresApi(Build.VERSION_CODES.N)
//        object : StringRequest(
//            Method.POST,
//            "http://www.greensave.co/",
//            Response.Listener
//            { response ->
//                try {
//
//                    if (dialog.isShowing) {
//                        dialog.dismiss()
//                    }
//                    val objects = JSONObject(response)
//                    val response_code = objects.getInt("success")
//                    if (response_code == 1) {
//
////                        Toast.makeText(
////                            this,
////                            "Calculated Successfully",
////                            Toast.LENGTH_SHORT
////                        ).show()
//                        val data = objects.getJSONArray("data")
//
//                        for (i in 0 until data.length()) {
//                            val actor: JSONObject = data.getJSONObject(i)
////                            val name = actor.getString("name")
//                            allname.add(actor.getString("name"))
//                            alluser.add(
//                                Alluser(
//                                    actor.getString("image"),
//                                    actor.getString("lat"),
//                                    actor.getString("lng"),
//                                    actor.getString("lifestyle"),
//                                    actor.getString("name"),
//                                    actor.getString("account_type")
//                                )
//                            )
//                        }
//
//                        ArrayAdapter<String>(
//                            this,
//                            android.R.layout.simple_list_item_1,
//                            allname
//                        ).also { adapter ->
//                            binding.autoCompleteTextView1.setAdapter(adapter)
//                        }
//
//                        binding.autoCompleteTextView1.onItemClickListener =
//                            OnItemClickListener { parent, view, position, id ->
//                                Log.d("TAG", binding.autoCompleteTextView1.text.toString())
//
//
//                                for (item in alluser) {
//                                    if (binding.autoCompleteTextView1.text.toString()
//                                            .equals(item.name)
//                                    ) {
//
//                                        hideKeyboard(this)
//
//                                        setdata(
//                                            item.lifestyle,
//                                            item.name,
//                                            item.account_type,
//                                            item.img,
//                                            item.lat.toDouble(),
//                                            item.lng.toDouble()
//                                        )
//
//                                        val sydney =
//                                            LatLng(item.lat.toDouble(), item.lng.toDouble())
//                                        mMap?.moveCamera(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                sydney,
//                                                8f
//                                            )
//                                        )
//
//
//                                    }
//                                }
//
//
////                                val i = Intent(applicationContext, Dashboard::class.java)
////                                i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
////                                applicationContext.startActivity(i)
//                            }
//
//                        val marker =
//                            arrayOfNulls<Marker>(alluser.size) //change length of array according to you
//
//                        mMap!!.setOnMarkerClickListener(this)
//                        for (i in 0 until alluser.size) {
//
//                            // below line is use to add marker to each location of our array list.
//                            Log.e(
//                                "121212",
//                                "allusers: ${alluser.get(i).lat.toDouble()}      ${alluser.get(i).lng.toDouble()}"
//                            )
//                            marker[i] = mMap?.addMarker(
////                            mMap?.addMarker(
//                                MarkerOptions()
//                                    .position(
//                                        LatLng(
//                                            alluser.get(i).lat.toDouble(),
//                                            alluser.get(i).lng.toDouble()
//                                        )
//                                    )
////                                    .position(LatLng(31.5204 + i, 74.3587 + i))
//                                    .icon(
//                                        getMarkerBitmapFromView(
//                                            alluser.get(i).img,
//                                            alluser.get(i).lifestyle
//                                        )?.let {
//                                            BitmapDescriptorFactory.fromBitmap(
//                                                it
//                                            )
//                                        })
//                            )
//
//
//                        }
//
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "Error..!",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                } catch (e: JSONException) {
//                    e.printStackTrace()
//                }
//                dialog.dismiss()
//            },
//            Response.ErrorListener { volleyError ->
//                dialog.dismiss()
//                var message: String? = null
//                when (volleyError) {
//                    is NetworkError -> {
//                        message = "Cannot connect to Internet...Please check your connection!"
//                    }
//                    is ServerError -> {
//                        message =
//                            "The server could not be found. Please try again after some time!!"
//                    }
//                    is AuthFailureError -> {
//                        message = "Cannot connect to Internet...Please check your connection!"
//                    }
//                    is ParseError -> {
//                        message = "Parsing error! Please try again after some time!!"
//                    }
//                    is NoConnectionError -> {
//                        message = "Cannot connect to Internet...Please check your connection!"
//                    }
//                    is TimeoutError -> {
//                        message = "Connection TimeOut! Please check your internet connection."
//                    }
//                }
//                Toast.makeText(this@HistoryActivity, message, Toast.LENGTH_LONG).show()
//            }) {
//            override fun getParams(): MutableMap<String, String> {
//                val header: MutableMap<String, String> = HashMap()
//
//                header["do"] = "all_users"
//                header["apikey"] = "dwamsoft12345"
//                header["account_type"] = "dwamsoft12345"
//
//                return header
//            }
//
//        }
//        RegistrationRequest.retryPolicy = DefaultRetryPolicy(
//            25000,
//            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        )
//        MySingleton.getInstance(this).addToRequestQueue(RegistrationRequest)
//    }


}