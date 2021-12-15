package com.example.thebrain.datamodels;

public class Comment {
    private String id;
    private String body;
    private String userId;
    private String username;
    private String userImage;
    private String parentId;
    private String createdAt;

    public Comment() {
    }

    public Comment(String id, String body, String userId,String parentId, String createdAt, String username, String userImage) {
        this.id = id;
        this.body = body;
        this.userId = userId;
        this.username = username;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.userImage = userImage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

}
