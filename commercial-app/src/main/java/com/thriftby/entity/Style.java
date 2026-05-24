package com.thriftby.entity;

public enum Style {
    CASUAL("Casual"),
    VINTAGE("Vintage"),
    STREETWEAR("Streetwear"),
    FORMEL("Formel"),
    SPORT("Sport"),
    BOHEME("Bohème"),
    CHIC("Chic");

    private final String libelle;

    Style(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
