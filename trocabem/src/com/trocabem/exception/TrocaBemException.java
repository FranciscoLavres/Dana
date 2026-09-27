package com.trocabem.exception;

/**
 * Exceção base do sistema. Todas as exceções de regra de negócio herdam dela,
 * então dá para capturar tudo com um único catch (TrocaBemException).
 */
public class TrocaBemException extends Exception {

    public TrocaBemException(String mensagem) {
        super(mensagem);
    }
}
