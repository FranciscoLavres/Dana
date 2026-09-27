package com.trocabem.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Movimentação da carteira. Abstrata: só existem Credito e Debito. */
public abstract class Transacao {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final double valor;
    private final String descricao;
    private final LocalDateTime data = LocalDateTime.now();

    protected Transacao(double valor, String descricao) {
        this.valor = valor;
        this.descricao = descricao;
    }

    public abstract String getTipo();

    /** Valor com sinal: positivo para crédito, negativo para débito. */
    public abstract double getValorComSinal();

    public double getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("%s | %-7s | %+9.2f | %s", data.format(FORMATO), getTipo(), getValorComSinal(), descricao);
    }
}
