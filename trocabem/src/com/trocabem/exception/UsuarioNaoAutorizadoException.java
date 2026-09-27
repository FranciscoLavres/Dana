package com.trocabem.exception;

/** Ação de um perfil sobre um recurso que não é dele. */
public class UsuarioNaoAutorizadoException extends TrocaBemException {

    public UsuarioNaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
