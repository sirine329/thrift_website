package com.thriftby.entity;

public enum Etat {
    NEUF("Neuf avec étiquette"),
    COMME_NEUF("Comme neuf"),
    BON("Bon état"),
    ACCEPTABLE("État acceptable"),
    USAGE("Très usagé");

    private final String libelle;

    Etat(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
