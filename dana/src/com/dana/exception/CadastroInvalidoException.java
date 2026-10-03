package com.dana.exception;

/** Dados de cadastro inválidos (campo vazio, e-mail já usado etc.). */
public class CadastroInvalidoException extends DanaException {

    public CadastroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
