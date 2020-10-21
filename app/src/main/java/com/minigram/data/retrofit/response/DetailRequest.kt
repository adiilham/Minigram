package com.minigram.data.retrofit.response


import com.google.gson.annotations.SerializedName

class DetailRequest : ArrayList<DetailRequest.DetailRequestItem>(){
    data class DetailRequestItem(
        @SerializedName("posting_id")
        val postingId: String? = "",
        @SerializedName("user_id")
        val userId: String? = "",
        @SerializedName("user_name")
        val userName: String? = "",
        @SerializedName("photo")
        val photo: String? = "",
        @SerializedName("image")
        val image: String? = "",
        @SerializedName("description")
        val description: String? = "",
        @SerializedName("privilege")
        val privilege: String? = "",
        @SerializedName("comments")
        val comments: String? = "",
        @SerializedName("likes")
        val likes: String? = ""
    )
}