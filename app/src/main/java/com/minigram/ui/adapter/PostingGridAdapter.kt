package com.minigram.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.minigram.R
import com.minigram.data.retrofit.response.PostingRequest
import kotlinx.android.synthetic.main.item_posting_grid.view.*

class PostingGridAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<PostingRequest.PostingRequestItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VendorListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_posting_grid, parent, false))
    }
    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as VendorListViewHolder
        holder.bindView(data[position])

        holder.itemView.image.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("postingId",data[position].postingId ?: "")
            Navigation.findNavController(holder.itemView).navigate(R.id.navigation_detail,bundle)
        }

    }

    fun setList(listOfVendor: List<PostingRequest.PostingRequestItem>) {
        this.data = listOfVendor.toMutableList()
        notifyDataSetChanged()
    }

    fun addList(data: PostingRequest.PostingRequestItem) {
        this.data.add(data)
        notifyDataSetChanged()
    }

    fun clear() {
        this.data.clear()
    }

    class VendorListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(data: PostingRequest.PostingRequestItem) {
            try {
                Glide.with(itemView.context).load(data.image).into(itemView.image)
            } catch (e: Exception) { }
        }
    }
}