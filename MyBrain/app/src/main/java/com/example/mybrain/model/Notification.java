package com.example.mybrain.model;

public class Notification {
    private String userId;
    private String postId;
    private String text;
    private boolean inpost;

    public Notification(String userId, String postId, String text, boolean inpost) {
        this.userId = userId;
        this.postId = postId;
        this.text = text;
        this.inpost = inpost;
    }

    public Notification() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isInpost() {
        return inpost;
    }

    public void setInpost(boolean inpost) {
        this.inpost = inpost;
    }
}
