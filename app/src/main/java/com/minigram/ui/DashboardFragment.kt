package com.minigram.ui

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.minigram.R
import com.minigram.data.SharedPreferenceData
import com.minigram.data.retrofit.ApiClient
import com.minigram.data.retrofit.response.UploadRequest
import kotlinx.android.synthetic.main.fragment_dashboard.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*


class DashboardFragment : Fragment() {

    private val pickImageRequest = 111
    private var privilege = 1
    private var imageName = ""
    private var imageData = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonListener()
        generateSpinner()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null) {
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
                    image.setImageBitmap(bitmap)
                    imageData = getStringImage(bitmap)!!
                    Log.e("aim","imageName : $imageName")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(requireContext(), "Choose .jpg or .jpeg format", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buttonListener() {
        image.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose Picture"), pickImageRequest)
        }

        btn_posting.setOnClickListener {
            if (desc_value.text.toString() != "") {
                postingData()
            } else {
                Toast.makeText(requireContext(), "Please insert description", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun generateSpinner(){
        val spinnerData = arrayOf("Public", "Private")

        val arrayAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            spinnerData
        )
        spinner_lay?.adapter = arrayAdapter

        spinner_lay?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) { }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position){
                    0 -> {
                        privilege = 1
                    }
                    1 -> {
                        privilege = 2
                    }
                }
            }
        }
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

    private fun postingData() {
        val userId = SharedPreferenceData.getString(requireContext(), 1, "")
        val params = HashMap<String, String>()
        params["userId"] = userId
        params["privilege"] = privilege.toString()
        params["imageName"] = imageName
        params["imageValue"] = "data:image/png;base64,$imageData"
        params["desc"] = desc_value.text.toString()
        Log.e("aim", "param : $params")

        ApiClient.instance.posting(params).enqueue(object : Callback<UploadRequest> {
            override fun onResponse(call: Call<UploadRequest>, response: Response<UploadRequest>?) {
                val code = response?.body()?.code ?: ""
                val message = response?.body()?.message ?: ""

                if (code == "200") {
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    Navigation.findNavController(requireView()).popBackStack()
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