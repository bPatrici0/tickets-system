package com.ticket.dto;

public class LoginResponseDTO {
    private String email;
    private String rol;
    private Boolean passwordResetRequired;
    private String nombre;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String email, String rol, Boolean passwordResetRequired, String nombre) {
        this.email = email;
        this.rol = rol;
        this.passwordResetRequired = passwordResetRequired;
        this.nombre = nombre;
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

    public Boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(Boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}