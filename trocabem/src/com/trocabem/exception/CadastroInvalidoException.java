package com.trocabem.exception;

/** Dados de cadastro inválidos (campo vazio, e-mail já usado etc.). */
public class CadastroInvalidoException extends TrocaBemException {

    public CadastroInvalidoException(String mensagem) {
        super(mensagem);
    }
}
