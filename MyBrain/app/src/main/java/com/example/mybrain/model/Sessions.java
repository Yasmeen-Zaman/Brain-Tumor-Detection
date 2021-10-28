package com.example.mybrain.model;

public class Sessions {
    String mriId, mriImage, publisher, result;

    public Sessions() {
    }

    public Sessions(String mriId, String mriImage, String publisher, String result) {
        this.mriId = mriId;
        this.mriImage = mriImage;
        this.publisher = publisher;
        this.result = result;
    }

    public String getMriId() {
        return mriId;
    }

    public void setMriId(String mriId) {
        this.mriId = mriId;
    }

    public String getMriImage() {
        return mriImage;
    }

    public void setMriImage(String mriImage) {
        this.mriImage = mriImage;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
