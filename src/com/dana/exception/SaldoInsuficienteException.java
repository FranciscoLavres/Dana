package com.dana.exception;

import com.dana.util.Formato;

import java.math.BigDecimal;

/** Resgate (ou débito) com saldo menor que o custo. */
public class SaldoInsuficienteException extends DanaException {

    public SaldoInsuficienteException(BigDecimal saldo, BigDecimal custo) {
        super("Saldo insuficiente: você tem " + Formato.creditos(saldo) + " créditos e o custo é "
                + Formato.creditos(custo) + ".");
    }
}
