package com.example.captionapp;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @Multipart
    @POST("caption")
    Call<CaptionResponse> uploadImage(@Part MultipartBody.Part image);
}
