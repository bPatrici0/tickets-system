package com.ticket.dto;

public class PasswordResetResponseDTO {
    private String mensaje;
    private String email;
    private Boolean passwordResetRequired;

    public PasswordResetResponseDTO() {}

    public PasswordResetResponseDTO(String mensaje, String email, Boolean passwordResetRequired) {
        this.mensaje = mensaje;
        this.email = email;
        this.passwordResetRequired = passwordResetRequired;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getPasswordResetRequired() {
        return passwordResetRequired;
    }

    public void setPasswordResetRequired(Boolean passwordResetRequired) {
        this.passwordResetRequired = passwordResetRequired;
    }
}