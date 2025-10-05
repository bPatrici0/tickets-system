package com.ticket.dto;

public class PasswordResetRequestDTO {
    private Long usuarioId;
    private String email;

    public PasswordResetRequestDTO() {}

    public PasswordResetRequestDTO(Long usuarioId, String email) {
        this.usuarioId = usuarioId;
        this.email = email;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}