package com.ifs21034.lostandfound.data.remote.retrofit

import com.ifs21034.lostandfound.data.remote.response.DelcomAddLostFoundResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomLoginResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomLostFoundResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomLostFoundsResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomResponse
import com.ifs21034.lostandfound.data.remote.response.DelcomUserResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface IApiService {
    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomResponse

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomLoginResponse
    @GET("users/me")
    suspend fun getMe(): DelcomUserResponse

    @FormUrlEncoded
    @POST("lost-founds")
    suspend fun postLostFound(
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String,
    ): DelcomAddLostFoundResponse

    @POST("lost-founds/{id}/cover")
    suspend fun postCoverLostFound(
        @Path("id") lostFoundId: Int,
    ): DelcomResponse

    @FormUrlEncoded
    @PUT("lost-founds/{id}")
    suspend fun putLostFound(
        @Path("id") lostFoundId: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("status") status: String,
        @Field("is_completed") isCompleted: Int,
    ): DelcomResponse

    @GET("lost-founds")
    suspend fun getLostFounds(
        @Query("is_completed") isCompleted: Int?,
        @Query("is_me") isMe: Int?,
        @Query("status") status: String?
    ): DelcomLostFoundsResponse

    @GET("lost-founds/{id}")
    suspend fun getLostFound(
        @Path("id") lostFoundId: Int,
    ): DelcomLostFoundResponse

    @DELETE("lost-founds/{id}")
    suspend fun deleteLostFound(
        @Path("id") lostFoundId: Int,
    ): DelcomResponse

    @Multipart
    @POST("lost-founds/{id}/cover")
    suspend fun addCoverLostFound(
        @Path("id") lostFoundId: Int,
        @Part cover: MultipartBody.Part,
    ): DelcomResponse

    @Multipart
    @POST("upload_profile_image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    )

    @Multipart
    @POST("users/photo")
    suspend fun  addphoto(
        @Part photo :MultipartBody.Part,
    ):DelcomResponse
}