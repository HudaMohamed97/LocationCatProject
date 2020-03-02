package com.example.myapplication.Models

import com.google.gson.annotations.SerializedName

data class SubmitModel(
    @SerializedName("message") val message: String
)