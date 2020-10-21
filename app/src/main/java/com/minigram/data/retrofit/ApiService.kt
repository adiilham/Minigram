package com.minigram.data.retrofit

import com.minigram.data.retrofit.response.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/login.php")
    fun loginRequest(
        @FieldMap body: HashMap<String, String>
    ): Call<LoginRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/register.php")
    fun registerRequest(
        @FieldMap body: HashMap<String, String>
    ): Call<LoginRequest>

    @Headers("Accept: application/json")
    @GET("Minigram/get_posting.php")
    fun getPosting(): Call<PostingRequest>

    @Headers("Accept: application/json")
    @GET("Minigram/get_profile.php")
    fun getProfile(
        @Query("userId") userId: String?
    ): Call<PostingRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/update_profile.php")
    fun updateProfile(
        @FieldMap body: HashMap<String,String>
    ): Call<UploadRequest>

    @Headers("Accept: application/json")
    @GET("Minigram/get_detail.php")
    fun getDetail(
        @Query("postingId") postingId: String?
    ): Call<DetailRequest>

    @Headers("Accept: application/json")
    @GET("Minigram/get_comment.php")
    fun getComment(
        @Query("postingId") postingId: String?
    ): Call<CommentRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/add_comment.php")
    fun addComment(
        @FieldMap body: HashMap<String,String>
    ): Call<DeleteRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/posting.php")
    fun posting(
        @FieldMap body: HashMap<String,String>
    ): Call<UploadRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/delete_posting.php")
    fun deletePosting(
        @FieldMap body: HashMap<String,String>
    ): Call<DeleteRequest>

    @Headers("Accept: application/json")
    @FormUrlEncoded
    @POST("Minigram/get_like.php")
    fun getLike(
        @FieldMap body: HashMap<String,String>
    ): Call<LikeRequest>

}