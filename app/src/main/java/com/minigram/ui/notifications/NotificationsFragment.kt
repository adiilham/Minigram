package com.minigram.ui.notifications

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.minigram.MainActivity
import com.minigram.R
import com.minigram.data.SharedPreferenceData
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.DeleteRequest
import com.minigram.data.retrofit.response.PostingRequest
import com.minigram.data.retrofit.response.UploadRequest
import com.minigram.ui.adapter.PostingGridAdapter
import com.minigram.ui.adapter.PostingVerticalAdapter
import kotlinx.android.synthetic.main.fragment_notifications.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class NotificationsFragment : Fragment() {

    private val pickImageRequest = 111
    private val verticalAdapter = PostingVerticalAdapter()
    private val gridAdapter = PostingGridAdapter()

    private var imageName = ""
    private var imageData = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequest && resultCode == Activity.RESULT_OK && data != null) {
            val filePath: Uri = data.data!!
            imageName = getFileName(filePath) ?: ""
            val fileType = imageName.split(".")
            val lastIndex = fileType.size - 1
            val extension = fileType[lastIndex].toLowerCase(Locale.getDefault())
            if (extension == "jpg" || extension == "jpeg") {
                try {
                    val imageUri: Uri = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireContext().contentResolver,
                        imageUri
                    )
                    photo.setImageBitmap(bitmap)
                    imageData = getStringImage(bitmap)!!
                    upDateProfile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(requireContext(), "Choose .jpg or .jpeg format", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generateView() {
        header_name.text = SharedPreferenceData.getString(requireContext(),2,"")
    }

    private fun buttonListener() {
        swipe_lay.setOnRefreshListener {
            getProfile()
        }

        photo.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose Picture"), pickImageRequest)
        }

        btn_setting.setOnClickListener {
            dialogLogout()
        }
    }

    private fun generateAdapter() {
        recycler_posting?.layoutManager = GridLayoutManager(context, 3)
        recycler_posting?.adapter = gridAdapter
    }

    private fun adapterListener() {
        verticalAdapter.setEventHandler(object: PostingVerticalAdapter.RecyclerClickListener {
            override fun isDelete(position: Int, postingId: String) {
                deleteDialog(postingId)
            }
        })
    }

    @SuppressLint("Recycle")
    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor =
                requireContext().contentResolver.query(uri, null, null, null, null)!!
            cursor.use {
                if (it.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result
    }

    private fun getStringImage(bmp: Bitmap): String? {
        val baos = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageBytes: ByteArray = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun deleteDialog(postingId: String) {
        AlertDialog.Builder(requireContext())
            .setMessage("Delete this photo ?")
            .setPositiveButton(android.R.string.yes) { dialog, whichButton ->
                deletePosting(postingId)
            }
            .setNegativeButton(android.R.string.no, null).show()
    }

    private fun dialogLogout() {
        val view = layoutInflater.inflate(R.layout.dialog_profile,null)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(view)

        val btnLogout = dialog.findViewById<TextView>(R.id.btn_logout)
        btnLogout?.setOnClickListener {

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            requireActivity().finish()
        }

        dialog.show()
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
            recycler_posting.layoutManager = LinearLayoutManager(context,
                RecyclerView.VERTICAL,false)
            recycler_posting?.adapter = verticalAdapter
        } else {
            recycler_posting.layoutManager = GridLayoutManager(context, 3)
            recycler_posting?.adapter = gridAdapter
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getProfile() {
        swipe_lay.isRefreshing = true
        verticalAdapter.clear()
        gridAdapter.clear()

        val userId = SharedPreferenceData.getString(requireContext(), 1, "")

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

    private fun upDateProfile() {
        val userId = SharedPreferenceData.getString(requireContext(), 1, "")
        val params = HashMap<String, String>()
        params["userId"] = userId
        params["imageName"] = imageName
        params["imageValue"] = "data:image/png;base64,$imageData"
        Log.e("aim", "param : $params")

        ApiClient.instance.updateProfile(params).enqueue(object : Callback<UploadRequest> {
            override fun onResponse(call: Call<UploadRequest>, response: Response<UploadRequest>?) {
                val code = response?.body()?.code ?: ""
                val message = response?.body()?.message ?: ""

                if (code == "200") {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(call: Call<UploadRequest>, t: Throwable) {
                Toast.makeText(requireContext(), "Connection failed", Toast.LENGTH_LONG).show()
            }

        })
    }

}