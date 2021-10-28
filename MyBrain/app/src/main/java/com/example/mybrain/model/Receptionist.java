package com.example.mybrain.model;

public class Receptionist {
    String id, username, fullname, email, phone, role, status;
    String image;

    public Receptionist() {
    }

    public Receptionist(String id, String username, String fullname, String email, String phone, String role, String status, String image) {
        this.id =id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.image= image;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
