package com.dana.model;

import com.dana.exception.SaldoInsuficienteException;
import com.dana.util.Valor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulamento: o saldo é privado e só muda por creditar() e debitar().
 * Nenhuma outra classe consegue "setar" o saldo direto.
 */
public class Carteira implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal saldo = Valor.ZERO;
    private final List<Transacao> extrato = new ArrayList<>();

    public void creditar(BigDecimal valor, String descricao) {
        exigirPositivo(valor);
        saldo = Valor.arredondar(saldo.add(valor));
        extrato.add(new Credito(Valor.arredondar(valor), descricao, saldo));
    }

    public void debitar(BigDecimal valor, String descricao) throws SaldoInsuficienteException {
        exigirPositivo(valor);
        if (valor.compareTo(saldo) > 0) {
            throw new SaldoInsuficienteException(saldo, valor);
        }
        saldo = Valor.arredondar(saldo.subtract(valor));
        extrato.add(new Debito(Valor.arredondar(valor), descricao, saldo));
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    /** Devolve uma visão só de leitura, para ninguém alterar o extrato por fora. */
    public List<Transacao> getExtrato() {
        return Collections.unmodifiableList(extrato);
    }

    private void exigirPositivo(BigDecimal valor) {
        if (!Valor.positivo(valor)) {
            throw new IllegalArgumentException("O valor deve ser positivo.");
        }
    }
}
