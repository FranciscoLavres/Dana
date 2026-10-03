package com.dana.model;

import com.dana.util.Formato;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Movimentação da carteira. Abstrata: só existem Credito e Debito. */
public abstract class Transacao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final BigDecimal valor;
    private final String descricao;
    private final BigDecimal saldoApos;
    private final LocalDateTime data = LocalDateTime.now();

    protected Transacao(BigDecimal valor, String descricao, BigDecimal saldoApos) {
        this.valor = valor;
        this.descricao = descricao;
        this.saldoApos = saldoApos;
    }

    public abstract String getTipo();

    /** Valor com sinal: positivo para crédito, negativo para débito. */
    public abstract BigDecimal getValorComSinal();

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    /** Saldo da carteira logo depois desta movimentação. */
    public BigDecimal getSaldoApos() {
        return saldoApos;
    }

    public LocalDateTime getData() {
        return data;
    }

    @Override
    public String toString() {
        return Formato.texto("%s | %-7s | %+9.2f | saldo %9.2f | %s",
                Formato.dataHora(data), getTipo(), getValorComSinal(), saldoApos, descricao);
    }
}
