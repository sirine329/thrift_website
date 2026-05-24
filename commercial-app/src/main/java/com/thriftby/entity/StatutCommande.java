package com.thriftby.entity;

public enum StatutCommande {
    EN_ATTENTE("En attente de paiement"),
    PAYEE("Payée"),
    EXPEDIEE("Expédiée"),
    LIVREE("Livrée"),
    ANNULEE("Annulée"),
    REMBOURSEE("Remboursée");

    private final String libelle;

    StatutCommande(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}
