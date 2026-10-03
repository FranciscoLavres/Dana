package com.dana.model;

import java.math.BigDecimal;

/** Entrada de créditos (horas confirmadas ou estorno de resgate). */
public class Credito extends Transacao {

    private static final long serialVersionUID = 1L;

    public Credito(BigDecimal valor, String descricao, BigDecimal saldoApos) {
        super(valor, descricao, saldoApos);
    }

    @Override
    public String getTipo() {
        return "CREDITO";
    }

    @Override
    public BigDecimal getValorComSinal() {
        return getValor();
    }
}
