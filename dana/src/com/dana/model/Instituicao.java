package com.dana.model;

/** ONG, abrigo, escola etc. que publica vagas. */
public class Instituicao extends Usuario {

    private final String cnpj;
    private final String descricao;

    public Instituicao(String nome, String email, String senha, String cnpj, String descricao) {
        super(nome, email, senha);
        this.cnpj = cnpj;
        this.descricao = descricao;
    }

    @Override
    public String getTipo() {
        return "Instituicao";
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getDescricao() {
        return descricao;
    }
}
