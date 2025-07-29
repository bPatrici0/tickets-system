package com.ticket.dto;

public class LoginResponseDTO {
    private String email;
    private String rol;

    public LoginResponseDTO(String email, String rol) {
        this.email = email;
        this.rol = rol;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}