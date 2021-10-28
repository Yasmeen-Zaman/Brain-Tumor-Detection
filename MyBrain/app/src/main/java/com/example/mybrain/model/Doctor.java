package com.example.mybrain.model;

public class Doctor {
    String id, reference_id, username, fullname, qualification, specialization, email, phone, role, status;
    String image;

    public Doctor() {
    }

    public Doctor(String id, String reference_id, String username, String fullname, String qualification, String specialization, String email, String phone, String role, String status, String profileUri) {
        this.id = id;
        this.reference_id = reference_id;
        this.username = username;
        this.fullname = fullname;
        this.qualification = qualification;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.image = profileUri;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference_id() {
        return reference_id;
    }

    public void setReference_id(String reference_id) {
        this.reference_id = reference_id;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
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

    public String getImage() {
        return image;
    }

}
