package com.dana.exception;

/** E-mail ou senha incorretos. */
public class LoginInvalidoException extends DanaException {

    public LoginInvalidoException() {
        super("E-mail ou senha inválidos.");
    }
}
