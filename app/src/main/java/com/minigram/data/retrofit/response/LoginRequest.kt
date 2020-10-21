package com.minigram.data.retrofit.response


import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("code")
    val code: String? = "",
    @SerializedName("message")
    val message: String? = "",
    @SerializedName("data")
    val `data`: List<Data?>? = listOf()
) {
    data class Data(
        @SerializedName("id")
        val id: String? = "",
        @SerializedName("username")
        val username: String? = ""
    )
}