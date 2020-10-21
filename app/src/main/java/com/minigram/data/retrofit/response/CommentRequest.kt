package com.minigram.data.retrofit.response


import com.google.gson.annotations.SerializedName

class CommentRequest : ArrayList<CommentRequest.CommentRequestItem>(){
    data class CommentRequestItem(
        @SerializedName("photo")
        val photo: String? = "",
        @SerializedName("username")
        val username: String? = "",
        @SerializedName("content")
        val content: String? = ""
    )
}