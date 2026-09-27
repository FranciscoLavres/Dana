package com.trocabem.model;

/** Empresa (lanchonete, cinema, spa...) que oferece serviços em troca de créditos. */
public class Parceiro extends Usuario {

    private final String cnpj;
    private final String tipoNegocio;

    public Parceiro(String nome, String email, String senha, String cnpj, String tipoNegocio) {
        super(nome, email, senha);
        this.cnpj = cnpj;
        this.tipoNegocio = tipoNegocio;
    }

    @Override
    public String getTipo() {
        return "Parceiro";
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }
}
