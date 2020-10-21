package com.minigram.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.minigram.R
import com.minigram.data.SharedPreferenceData
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.CommentRequest
import com.minigram.data.retrofit.response.DeleteRequest
import com.minigram.data.retrofit.response.DetailRequest
import com.minigram.data.retrofit.response.LikeRequest
import com.minigram.ui.adapter.CommentAdapter
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_detail.image
import kotlinx.android.synthetic.main.fragment_detail.photo
import kotlinx.android.synthetic.main.fragment_detail.swipe_lay
import kotlinx.android.synthetic.main.fragment_detail.username
import kotlinx.android.synthetic.main.sub_header.*
import kotlinx.android.synthetic.main.sub_header.header_name
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class DetailFragment : Fragment() {

    private val commentAdapter = CommentAdapter()
    private var postingId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateView()
        buttonListener()
        generateAdapter()

        getPosting()
        getComment()
        getLike("0")

    }

    @SuppressLint("SetTextI18n")
    private fun generateView() {
        postingId = requireArguments().getString("postingId") ?: ""
        header_name.text = "Photo"
    }

    private fun buttonListener() {
        swipe_lay.setOnRefreshListener {
            getPosting()
            getComment()
            getLike("0")
        }

        back_link.setOnClickListener {
            requireActivity().onBackPressed()
        }

        btn_like.setOnClickListener {
            getLike("1")
        }

        btn_send.setOnClickListener {
            if (comment_value.text.toString() != "") {
                sendComment()
            } else {
                Toast.makeText(requireContext(),"Please type a comment",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generateAdapter() {
        recycler_comment?.layoutManager = LinearLayoutManager(
            activity,
            RecyclerView.VERTICAL,
            false
        )
        recycler_comment?.adapter = commentAdapter
    }

    private fun getPosting() {
        swipe_lay.isRefreshing = true
        ApiClient.instance.getDetail(postingId).enqueue(object : Callback<DetailRequest> {
            override fun onResponse(
                call: Call<DetailRequest>,
                response: Response<DetailRequest>?
            ) {
                swipe_lay.isRefreshing = false
                if (response != null && response.isSuccessful) {

                    if (!response.body().isNullOrEmpty()) {
                        val userPhoto = response.body()?.first()?.photo ?: ""
                        val userImage = response.body()?.first()?.image ?: ""

                        username.text = response.body()?.first()?.userName ?: ""
                        like_count.text = response.body()?.first()?.likes ?: ""
                        comment_count.text = response.body()?.first()?.comments ?: ""
                        desc.text = response.body()?.first()?.description ?: ""

                        try {
                            Glide.with(requireContext()).load(userPhoto).into(photo)
                            Glide.with(requireContext()).load(userImage).into(image)
                        } catch (e: Exception) {}
                    }
                }
            }

            override fun onFailure(call: Call<DetailRequest>, t: Throwable) {
                swipe_lay.isRefreshing = false
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getComment() {
        swipe_lay.isRefreshing = true
        commentAdapter.clear()
        ApiClient.instance.getComment(postingId).enqueue(object : Callback<CommentRequest> {
            override fun onResponse(
                call: Call<CommentRequest>,
                response: Response<CommentRequest>?
            ) {
                swipe_lay.isRefreshing = false
                if (response != null && response.isSuccessful) {
                    if (!response.body().isNullOrEmpty()) {
                        response.body()?.forEach {
                            commentAdapter.addList(it)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CommentRequest>, t: Throwable) {
                swipe_lay.isRefreshing = false
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun getLike(state: String) {
        swipe_lay.isRefreshing = true

        // 0 = check like, 1 = send like / unlike
        val userId = SharedPreferenceData.getString(requireContext(), 1, "")
        val params = HashMap<String, String>()
        params["userId"] = userId
        params["imageId"] = postingId
        params["state"] = state
        Log.e("aim", "param : $params")

        ApiClient.instance.getLike(params).enqueue(object : Callback<LikeRequest> {
            override fun onResponse(
                call: Call<LikeRequest>,
                response: Response<LikeRequest>?
            ) {
                swipe_lay.isRefreshing = false
                if (response != null && response.isSuccessful) {

                    when (response.body()?.code ?: "") {
                        "201" -> {
                            // Like
                            comment_desc.visibility = View.VISIBLE
                            getPosting()
                        }
                        "202" -> {
                            // Unlike
                            comment_desc.visibility = View.GONE
                            getPosting()
                        }
                        "203" -> {
                            // Check
                            if (!response.body()?.data.isNullOrEmpty()) {
                                comment_desc.visibility = View.VISIBLE
                            } else {
                                comment_desc.visibility = View.GONE
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<LikeRequest>, t: Throwable) {
                swipe_lay.isRefreshing = false
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun sendComment() {
        swipe_lay.isRefreshing = true
        val userId = SharedPreferenceData.getString(requireContext(),1,"")
        val params = HashMap<String, String>()
        params["userId"] = userId
        params["imageId"] = postingId
        params["content"] = comment_value.text.toString()

        ApiClient.instance.addComment(params).enqueue(object : Callback<DeleteRequest> {
            override fun onResponse(call: Call<DeleteRequest>, response: Response<DeleteRequest>?) {
                swipe_lay.isRefreshing = false
                val code = response?.body()?.code ?: ""
                val message = response?.body()?.message ?: ""

                if (code == "200") {
                    getPosting()
                    getComment()
                    comment_value.setText("")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(call: Call<DeleteRequest>, t: Throwable) {
                swipe_lay.isRefreshing = false
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }

        })
    }

}