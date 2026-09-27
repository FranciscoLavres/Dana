package com.trocabem.exception;

/** E-mail ou senha incorretos. */
public class LoginInvalidoException extends TrocaBemException {

    public LoginInvalidoException() {
        super("E-mail ou senha inválidos.");
    }
}
