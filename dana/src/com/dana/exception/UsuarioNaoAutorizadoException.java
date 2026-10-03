package com.dana.exception;

/** Ação de um perfil sobre um recurso que não é dele. */
public class UsuarioNaoAutorizadoException extends DanaException {

    public UsuarioNaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
