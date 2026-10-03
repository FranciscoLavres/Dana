package com.dana.exception;

/** Resgate (ou débito) com saldo menor que o custo. */
public class SaldoInsuficienteException extends DanaException {

    public SaldoInsuficienteException(double saldo, double custo) {
        super(String.format("Saldo insuficiente: você tem %.2f créditos e o custo é %.2f.", saldo, custo));
    }
}
