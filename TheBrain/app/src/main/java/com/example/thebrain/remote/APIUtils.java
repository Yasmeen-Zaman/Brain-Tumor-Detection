package com.example.thebrain.remote;

public class APIUtils {
    public  static final String API_URL = "http://bb51-139-5-117-236.ngrok.io/";
    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }
}
