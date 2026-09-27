package com.trocabem.exception;

/** Operação que não faz sentido no estado atual (ex.: confirmar horas de candidatura não aprovada). */
public class OperacaoInvalidaException extends TrocaBemException {

    public OperacaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
