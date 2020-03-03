package com.example.myapplication.LoginFragment

import androidx.lifecycle.MutableLiveData
import com.example.locationcatproject.NetworkLayer.Webservice
import com.example.myapplication.Models.Account
import com.example.myapplication.Models.ResponseModelData
import com.example.myapplication.Models.RegisterRequestModel
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RegisterRepository {
    fun register(registerRequestModel: RegisterRequestModel): MutableLiveData<ResponseModelData> {
        val email =
            RequestBody.create(MediaType.parse("multipart/form-data"), registerRequestModel.email)
        val name =
            RequestBody.create(MediaType.parse("multipart/form-data"), registerRequestModel.name)
        val password = RequestBody.create(
            MediaType.parse("multipart/form-data"),
            registerRequestModel.password
        )
        var fileToUpload: MultipartBody.Part? = null
        try {
            val file = File(registerRequestModel.photo)
            if (file.exists()) {
                val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
                fileToUpload = MultipartBody.Part.createFormData("image", file.name, requestFile)
            } else {
                val attachmentEmpty = RequestBody.create(MediaType.parse("text/plain"), "")
                fileToUpload = MultipartBody.Part.createFormData("image", "", attachmentEmpty)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        val userData = MutableLiveData<ResponseModelData>()
        Webservice.getInstance().api.register(email, password, name, fileToUpload!!)
            .enqueue(object : Callback<ResponseModelData> {
                override fun onResponse(
                    call: Call<ResponseModelData>,
                    response: Response<ResponseModelData>
                ) {
                    if (response.isSuccessful) {
                        userData.value = response.body()
                    } else {
                        if (response.code() == 422) {
                            val dummyResponse =
                                ResponseModelData(
                                    "",
                                    "this email is already token please enter valid Email",
                                    "",
                                    Account()
                                )
                            userData.value = dummyResponse
                        } else {
                            userData.value = response.body()
                        }

                    }
                }

                override fun onFailure(call: Call<ResponseModelData>, t: Throwable) {
                    userData.value = null
                }
            })

        return userData

    }


}



