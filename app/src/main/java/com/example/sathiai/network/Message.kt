package com.example.sathiai.network

import com.google.gson.annotations.SerializedName

data class Message(
    val role: String,
    val content: Any
)

data class ContentItem(
    val type: String,
    val text: String? = null,
    @SerializedName("image_url")
    val imageUrl: ImageUrl? = null
)

data class ImageUrl(
    val url: String
)