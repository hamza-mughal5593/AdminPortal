package com.admin.portal.Admin

import android.content.Intent
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.admin.portal.Model.Alluser
import com.admin.portal.R
import io.paperdb.Paper


class AllUserAdapter(
    private val mList: List<Alluser>,
    viewItemInterface: RecyclerViewItemInterface
) : RecyclerView.Adapter<AllUserAdapter.ViewHolder>() {

    private var viewItemInterface: RecyclerViewItemInterface? = null


    init {
        this.viewItemInterface = viewItemInterface;
    }


    interface RecyclerViewItemInterface {
        fun onItemClick(position: Int, path: Alluser, view: View)
        fun onMainClick(position: Int, path: Alluser, view: View)
    }


    // create new views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // inflates the card_view_design view
        // that is used to hold list item
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_user_item, parent, false)

        return ViewHolder(view)
    }

    // binds the list items to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val ItemsViewModel = mList[position]


        // sets the text to the textview from our itemHolder class
        holder.name.text = ItemsViewModel.name
        holder.userid.text = ItemsViewModel.id
        holder.optionbtn.setOnClickListener {
            viewItemInterface?.onItemClick(position, ItemsViewModel,it)
        }
        holder.mainitem.setOnClickListener {
            viewItemInterface?.onMainClick(position, ItemsViewModel,it)
        }


        if (!Paper.book().read("admin",true)!!){
            holder.optionbtn.visibility = View.GONE
        }

    }

    // return the number of the items in the list
    override fun getItemCount(): Int {
        return mList.size
    }

    // Holds the views for adding it to image and text
    class ViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val userid: TextView = itemView.findViewById(R.id.userid)
        val optionbtn: ImageView = itemView.findViewById(R.id.optionbtn)
        val mainitem: RelativeLayout = itemView.findViewById(R.id.mainitem)
    }


}