package com.prosup.proinsight.domain.enums;

public enum ResponsavelType {
    ACADEMIA("academias"),
    AVALIADOR("avaliadores");

    private final String collectionName;

    ResponsavelType(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
