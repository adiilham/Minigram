package com.minigram.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.minigram.R
import com.minigram.data.SharedPreferenceData
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.DeleteRequest
import com.minigram.data.retrofit.response.PostingRequest
import com.minigram.ui.adapter.PostingVerticalAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap

class HomeFragment : Fragment() {

    private val postingAdapter = PostingVerticalAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        generateAdapter()
        adapterListener()
        getPosting()
    }

    private fun generateAdapter() {
        recycler_posting?.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        recycler_posting?.adapter = postingAdapter
    }

    private fun adapterListener() {
        postingAdapter.setEventHandler(object: PostingVerticalAdapter.RecyclerClickListener {
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

    private fun getPosting() {
        postingAdapter.clear()
        val userId = SharedPreferenceData.getString(requireContext(),1,"")

        ApiClient.instance.getPosting().enqueue(object: Callback<PostingRequest> {
            override fun onResponse(call: Call<PostingRequest>, response: Response<PostingRequest>?) {
                if (response != null && response.isSuccessful) {
                    response.body()?.forEach {
                        if (it.privilege != "1") {
                            if (it.userId == userId) {
                                postingAdapter.addList(it)
                            }
                        } else {
                            postingAdapter.addList(it)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<PostingRequest>, t: Throwable) {
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
                    getPosting()
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