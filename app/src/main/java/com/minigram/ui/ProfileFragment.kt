package com.minigram.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.minigram.R
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.DeleteRequest
import com.minigram.data.retrofit.response.PostingRequest
import com.minigram.data.retrofit.response.UploadRequest
import com.minigram.ui.adapter.PostingGridAdapter
import com.minigram.ui.adapter.PostingVerticalAdapter
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.sub_header.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class ProfileFragment : Fragment() {

    private val verticalAdapter = PostingVerticalAdapter()
    private val gridAdapter = PostingGridAdapter()
    private var userId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateView()
        generateAdapter()
        adapterListener()
        generateTab()
        getProfile()
        buttonListener()
    }

    private fun generateView() {
        userId = requireArguments().getString("userId") ?: ""
    }

    private fun buttonListener() {
        swipe_lay.setOnRefreshListener {
            getProfile()
        }

        back_link.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun generateAdapter() {
        recycler_posting?.layoutManager = GridLayoutManager(context, 3)
        recycler_posting?.adapter = gridAdapter
    }

    private fun generateTab() {
        tab_lay.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab_lay.selectedTabPosition) {
                    0 -> adapterOrientation(false)
                    1 -> adapterOrientation(true)
                }
            }

        })
    }

    fun adapterOrientation(isVertical: Boolean) {
        if (isVertical) {
            recycler_posting.layoutManager = LinearLayoutManager(context,RecyclerView.VERTICAL,false)
            recycler_posting?.adapter = verticalAdapter
        } else {
            recycler_posting.layoutManager = GridLayoutManager(context, 3)
            recycler_posting?.adapter = gridAdapter
        }
    }

    private fun adapterListener() {
        verticalAdapter.setEventHandler(object: PostingVerticalAdapter.RecyclerClickListener {
            override fun isDelete(position: Int, postingId: String) {
                deleteDialog(postingId)
            }
        })
    }

    private fun deleteDialog(postingId: String) {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete this photo ?")
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                deletePosting(postingId)
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    @SuppressLint("SetTextI18n")
    private fun getProfile() {
        swipe_lay.isRefreshing = true
        verticalAdapter.clear()
        gridAdapter.clear()

        ApiClient.instance.getProfile(userId).enqueue(object : Callback<PostingRequest> {
            override fun onResponse(
                call: Call<PostingRequest>,
                response: Response<PostingRequest>?
            ) {
                swipe_lay.isRefreshing = false
                if (response != null && response.isSuccessful) {

                    if (!response.body().isNullOrEmpty()) {
                        val userPhoto = response.body()?.first()?.photo ?: ""
                        val userName = response.body()?.first()?.userName ?: ""
                        val postCount = response.body()?.size ?: 0

                        header_name.text = userName
                        posting_count.text = "$postCount Total Posts"

                        try {
                            Glide.with(requireContext()).load(userPhoto).into(photo)
                        } catch (e: Exception) {
                        }

                        response.body()?.forEach {
                            if (it.privilege != "1") {
                                if (it.userId == userId) {
                                    verticalAdapter.addList(it)
                                    gridAdapter.addList(it)
                                }
                            } else {
                                verticalAdapter.addList(it)
                                gridAdapter.addList(it)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PostingRequest>, t: Throwable) {
                swipe_lay.isRefreshing = false
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun deletePosting(postingId: String) {
        val params = HashMap<String, String>()
        params["id"] = postingId

        ApiClient.instance.deletePosting(params).enqueue(object : Callback<DeleteRequest> {
            override fun onResponse(call: Call<DeleteRequest>, response: Response<DeleteRequest>?) {
                val code = response?.body()?.code ?: ""
                val message = response?.body()?.message ?: ""

                if (code == "200") {
                    getProfile()
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(call: Call<DeleteRequest>, t: Throwable) {
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }

        })
    }
}