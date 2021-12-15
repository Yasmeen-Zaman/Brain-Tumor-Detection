package com.example.thebrain.datamodels;

public class Patient {
    String id, username, firstname, lastname, cnic, gender, age, city, street, email, phone, role, status, doctor_reference_id;
    String image, password, province;

    public Patient() {
    }

    public Patient(String id, String username, String firstname, String lastname, String cnic, String gender, String age, String city, String street, String email, String phone, String role, String status, String image, String doctor_reference_id, String password, String province) {
        this.id =id;
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.cnic = cnic;
        this.gender = gender;
        this.age = age;
        this.city = city;
        this.doctor_reference_id = doctor_reference_id;
        this.street = street;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.image= image;
        this.password = password;
        this.province = province;
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

    public String getDoctor_reference_id() {
        return doctor_reference_id;
    }

    public void setDoctor_reference_id(String doctor_reference_id) {
        this.doctor_reference_id = doctor_reference_id;
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

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

}
