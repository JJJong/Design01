package com.example.design01

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST


interface JsonPlaceHOlderApiKt {


//    @GET("/tests/{pk}/")
//    fun get_pk(@Path("pk") pk: Int): Call<PostItemKt?>?

//    @Multipart
//    @POST("/tests/")
//    fun post_posts(
//        @Part ("filename") fileName : String)
//        @Part ("type") mimeType : String,
//        @Part ("bitmap") bitmap : Bitmap)           // 결과 값을 넣은 변수 지정
//            : Call<PostItemKt>              // PostItemKt 형식으로 받는다.

    @GET("/tests/")
    fun get_result(): Call<List<GetItemKt?>?>?

    @POST("/tests/")
    fun post_posts(
        @Body post: PostItemKt
    ) : Call<PostItemKt>
}