package com.example.myapplication.NetworkLayer

import com.example.myapplication.Models.*
import com.example.myapplication.Models.EventModels.SingleEventResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ApiServices {

    @POST("auth/login")
    fun login(@Body loginRequestModel: LoginRequestModel): Call<ResponseModelData>


    @POST("check/{user}/add")
    fun submitlocation(
        @Path("user") user: Int, @Body body: Map<String, String>
    ): Call<ResponseBody>


    @Multipart
    @POST("auth/register")
    fun register(
        @Part("email") email: RequestBody, @Part("password") password: RequestBody, @Part("name") name: RequestBody,
        @Part image: MultipartBody.Part
    ): Call<ResponseModelData>


   /* @POST("auth/register")
    fun register(@Body registerRequestModel: RegisterRequestModel): Call<ResponseModelData>*/
}