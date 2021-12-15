package com.example.thebrain.remote;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileService {
    @Multipart
    @POST("predict")
    @Headers({"accept:application/json", "content:application/json"})
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part file);
}
