package com.trocabem.model;

/** Saída de créditos (resgate de serviço). */
public class Debito extends Transacao {

    public Debito(double valor, String descricao) {
        super(valor, descricao);
    }

    @Override
    public String getTipo() {
        return "DEBITO";
    }

    @Override
    public double getValorComSinal() {
        return -getValor();
    }
}
