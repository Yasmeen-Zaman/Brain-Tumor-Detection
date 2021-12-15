package com.example.thebrain.datamodels;

public class Doctor {

    String id, doctor_reference_id, username, firstname, lastname, cnic, gender, city, street, age, qualification, specialization, email, phone, role, status;
    String image;

    public Doctor() {
    }

    public Doctor(String id, String doctor_reference_id, String username, String firstname, String lastname, String age, String cnic, String city, String street, String gender, String qualification, String specialization, String email, String phone, String role, String status, String profileUri) {
        this.id = id;
        this.doctor_reference_id = doctor_reference_id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.cnic = cnic;
        this.gender = gender;
        this.city = city;
        this.street = street;
        this.age = age;
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

    public String getDoctor_reference_id() {
        return doctor_reference_id;
    }

    public void setDoctor_reference_id(String doctor_reference_id) {
        this.doctor_reference_id = doctor_reference_id;
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

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getCnic() {
        return cnic;
    }

    public void setCnic(String cnic) {
        this.cnic = cnic;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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
