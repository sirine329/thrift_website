package com.thriftby.entity;

public enum StatutItem {
    DISPONIBLE("Disponible"),
    RESERVE("Réservé"),
    VENDU("Vendu"),
    RETIRE("Retiré");

    private final String libelle;

    StatutItem(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
