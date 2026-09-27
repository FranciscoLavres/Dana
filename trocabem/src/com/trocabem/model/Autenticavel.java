package com.trocabem.model;

/** Interface implementada por quem pode fazer login. */
public interface Autenticavel {

    boolean autenticar(String senha);
}
