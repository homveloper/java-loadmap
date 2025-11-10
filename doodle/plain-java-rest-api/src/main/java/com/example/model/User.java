package com.example.model;

/**
 * User 모델 클래스
 *
 * 사용자 정보를 표현하는 도메인 모델
 */
public class User {
    private Long id;
    private String name;
    private String email;

    // 기본 생성자
    public User() {
    }

    // 전체 필드 생성자
    public User(Long id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
