package com.example.comments;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface JsonPlaceHolderApi {
    @GET("messages")
    Call<List<Post>> getPosts();

    @POST("message")
    Call<ResponseBody> createPost(@Body PostSend postSend);

    @PUT("message/{id}")
    Call<ResponseBody> updatePost(@Path("id") String postId, @Body PostSend postSend);
    @DELETE("message/{id}")
    Call<ResponseBody> deletePost(@Path("id") String postId);



}





