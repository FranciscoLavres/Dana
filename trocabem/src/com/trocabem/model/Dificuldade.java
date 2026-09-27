package com.trocabem.model;

/** Enum com comportamento: cada dificuldade carrega o seu multiplicador. */
public enum Dificuldade {
    LEVE(1.0),
    MODERADO(1.3),
    PESADO(1.6);

    private final double multiplicador;

    Dificuldade(double multiplicador) {
        this.multiplicador = multiplicador;
    }

    public double getMultiplicador() {
        return multiplicador;
    }
}
