package com.minigram.data.retrofit.response


import com.google.gson.annotations.SerializedName

data class DeleteRequest(
    @SerializedName("code")
    val code: String? = "",
    @SerializedName("message")
    val message: String? = ""
)