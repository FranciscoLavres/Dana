package com.dana.model;

import java.math.BigDecimal;

/** Saída de créditos (resgate de serviço). */
public class Debito extends Transacao {

    private static final long serialVersionUID = 1L;

    public Debito(BigDecimal valor, String descricao, BigDecimal saldoApos) {
        super(valor, descricao, saldoApos);
    }

    @Override
    public String getTipo() {
        return "DEBITO";
    }

    @Override
    public BigDecimal getValorComSinal() {
        return getValor().negate();
    }
}
