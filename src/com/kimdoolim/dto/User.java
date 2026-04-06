package com.kimdoolim.dto;

import java.util.Objects;

public class User {
    private int userId;
    private int schoolId;
    private String id;
    private String password;
    private Permission permission;
    private String name;
    private String phone;
    private int gradeNo;
    private int classNo;
    private boolean isActive;

    public User() { }

    public User(int userId, int schoolId, String id, String password, Permission permission, String name, String phone, int gradeNo, int classNo, boolean isActive) {
        this.userId = userId;
        this.schoolId = schoolId;
        this.id = id;
        this.password = password;
        this.permission = permission;
        this.name = name;
        this.phone = phone;
        this.gradeNo = gradeNo;
        this.classNo = classNo;
        this.isActive = isActive;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(int schoolId) {
        this.schoolId = schoolId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getGradeNo() {
        return gradeNo;
    }

    public void setGradeNo(int gradeNo) {
        this.gradeNo = gradeNo;
    }

    public int getClassNo() {
        return classNo;
    }

    public void setClassNo(int classNo) {
        this.classNo = classNo;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return userId == user.userId && schoolId == user.schoolId && gradeNo == user.gradeNo && classNo == user.classNo && isActive == user.isActive && Objects.equals(id, user.id) && Objects.equals(password, user.password) && permission == user.permission && Objects.equals(name, user.name) && Objects.equals(phone, user.phone);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", schoolId=" + schoolId +
                ", id='" + id + '\'' +
                ", password='" + password + '\'' +
                ", permission=" + permission +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", gradeNo=" + gradeNo +
                ", classNo=" + classNo +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, schoolId, id, password, permission, name, phone, gradeNo, classNo, isActive);
    }
}
