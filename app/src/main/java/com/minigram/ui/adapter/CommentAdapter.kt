package com.minigram.ui.adapter

import android.annotation.SuppressLint
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.minigram.R
import com.minigram.data.retrofit.response.CommentRequest
import kotlinx.android.synthetic.main.item_comment.view.*

class CommentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = mutableListOf<CommentRequest.CommentRequestItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return VendorListViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false))
    }
    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        val holder = viewHolder as VendorListViewHolder
        holder.bindView(data[position])

        holder.itemView.item_header.setOnClickListener {
        }

        holder.itemView.setOnClickListener {
        }

    }

    fun setList(listOfVendor: List<CommentRequest.CommentRequestItem>) {
        this.data = listOfVendor.toMutableList()
        notifyDataSetChanged()
    }

    fun addList(data: CommentRequest.CommentRequestItem) {
        this.data.add(data)
        notifyDataSetChanged()
    }

    fun clear() {
        this.data.clear()
    }

    class VendorListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindView(data: CommentRequest.CommentRequestItem) {
            try {
                Glide.with(itemView.context).load(data.photo).into(itemView.photo)
            } catch (e: Exception) { }

            val userName = data.username ?: ""
            val comment = data.content ?: ""
            itemView.comment.text = Html.fromHtml("<b>$userName</b> - $comment")
        }
    }
}