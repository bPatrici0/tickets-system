package com.ticket.entity;

public enum EstadoTicket {
    ABIERTO("ABIERTO"),
    EN_PROGRESO("EN_PROGRESO"),
    RESUELTO("RESUELTO");

    private final String valor;

    EstadoTicket(String valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return this.valor;
    }
}