package com.minigram.data.retrofit.response


import com.google.gson.annotations.SerializedName

data class LikeRequest(
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
        @SerializedName("user_id")
        val userId: String? = "",
        @SerializedName("image_id")
        val imageId: String? = ""
    )
}