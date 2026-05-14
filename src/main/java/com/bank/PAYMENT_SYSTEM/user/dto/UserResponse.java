package com.bank.PAYMENT_SYSTEM.user.dto;





import com.bank.PAYMENT_SYSTEM.common.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID id;

    private String fullName;

    private String email;

    private String phoneNumber;

    private Role role;

    private Boolean enabled;

    private LocalDateTime createdAt;

    // Constructor

    public UserResponse() {
    }

    public UserResponse(UUID id,
                        String fullName,
                        String email,
                        String phoneNumber,
                        Role role,
                        Boolean enabled,
                        LocalDateTime createdAt) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}