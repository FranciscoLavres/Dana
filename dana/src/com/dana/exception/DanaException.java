package com.dana.exception;

/**
 * Exceção base do sistema. Todas as exceções de regra de negócio herdam dela,
 * então dá para capturar tudo com um único catch (DanaException).
 */
public class DanaException extends Exception {

    public DanaException(String mensagem) {
        super(mensagem);
    }
}
