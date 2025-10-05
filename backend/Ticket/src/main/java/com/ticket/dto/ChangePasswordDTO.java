package com.ticket.dto;

public class ChangePasswordDTO {
    private String email;
    private String nuevaPassword;

    public ChangePasswordDTO() {}

    public ChangePasswordDTO(String email, String nuevaPassword) {
        this.email = email;
        this.nuevaPassword = nuevaPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}