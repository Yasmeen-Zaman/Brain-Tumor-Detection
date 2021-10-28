package com.example.mybrain;

public class LoginClass {
    String emailId, password, role, userId;

    public LoginClass() {
    }

    public LoginClass(String userId,String emailId, String password, String role) {
        this.userId = userId;
        this.emailId = emailId;
        this.password = password;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
