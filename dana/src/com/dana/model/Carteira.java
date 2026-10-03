package com.dana.model;

import com.dana.exception.SaldoInsuficienteException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulamento: o saldo é privado e só muda por creditar() e debitar().
 * Nenhuma outra classe consegue "setar" o saldo direto.
 */
public class Carteira {

    private double saldo;
    private final List<Transacao> extrato = new ArrayList<>();

    public void creditar(double valor, String descricao) {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor a creditar deve ser positivo.");
        }
        saldo = arredondar(saldo + valor);
        extrato.add(new Credito(valor, descricao));
    }

    public void debitar(double valor, String descricao) throws SaldoInsuficienteException {
        if (valor <= 0) {
            throw new IllegalArgumentException("O valor a debitar deve ser positivo.");
        }
        if (valor > saldo) {
            throw new SaldoInsuficienteException(saldo, valor);
        }
        saldo = arredondar(saldo - valor);
        extrato.add(new Debito(valor, descricao));
    }

    public double getSaldo() {
        return saldo;
    }

    /** Devolve uma visão só de leitura, para ninguém alterar o extrato por fora. */
    public List<Transacao> getExtrato() {
        return Collections.unmodifiableList(extrato);
    }

    private double arredondar(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
