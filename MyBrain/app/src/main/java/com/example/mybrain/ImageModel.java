package com.example.mybrain;

public class ImageModel {

    String imageUri;
//    String userId;

    public ImageModel(){

    }

    public ImageModel(String imageUri){
        this.imageUri = imageUri;
//        this.userId = userId;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

//    public String getUserId() {
//        return userId;
//    }
//
//    public void setUserId(String userId) {
//        this.userId = userId;
//    }
}
