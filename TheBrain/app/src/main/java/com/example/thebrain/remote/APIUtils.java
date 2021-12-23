package com.example.thebrain.remote;

public class APIUtils {
    public  static final String API_URL = "http://1167-121-52-154-74.ngrok.io/";
    public static FileService getFileService(){
        return RetrofitClient.getClient(API_URL).create(FileService.class);
    }
}
