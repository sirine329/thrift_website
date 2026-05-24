package com.thriftby.entity;

public enum ModePaiement {

    ESPECE_EN_LIVRAISON("Espèce en livraison"),
    EN_LIGNE("Paiement en ligne");

    private final String libelle;

    ModePaiement(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
