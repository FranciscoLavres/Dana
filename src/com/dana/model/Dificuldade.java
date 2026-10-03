package com.dana.model;

import java.math.BigDecimal;

/** Enum com comportamento: cada dificuldade carrega o seu multiplicador. */
public enum Dificuldade {
    LEVE("1.0"),
    MODERADO("1.3"),
    PESADO("1.6");

    private final BigDecimal multiplicador;

    Dificuldade(String multiplicador) {
        this.multiplicador = new BigDecimal(multiplicador);
    }

    public BigDecimal getMultiplicador() {
        return multiplicador;
    }
}
