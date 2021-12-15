package com.example.thebrain.datamodels;

public class Session {
    String key, name, mriImage, date, tag, permission;

    public Session(String key, String name, String mriImage, String date, String tag, String permission) {
        this.key = key;
        this.name = name;
        this.date = date;
        this.tag = tag;
        this.mriImage = mriImage;
        this.permission = permission;
    }

    public Session() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMriImage() {
        return mriImage;
    }

    public void setMriImage(String mriImage) {
        this.mriImage = mriImage;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String Date) {
        this.date = Date;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

}
