package com.dana.model;

/** Entrada de créditos (horas confirmadas). */
public class Credito extends Transacao {

    public Credito(double valor, String descricao) {
        super(valor, descricao);
    }

    @Override
    public String getTipo() {
        return "CREDITO";
    }

    @Override
    public double getValorComSinal() {
        return getValor();
    }
}
