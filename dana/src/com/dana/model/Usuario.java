package com.dana.model;

import com.dana.util.SenhaUtil;

/**
 * Classe abstrata: não existe "usuário genérico", só Voluntario, Instituicao ou Parceiro.
 * Guarda o que é comum aos três (dados de acesso) e força cada filho a dizer seu tipo.
 */
public abstract class Usuario implements Autenticavel {

    private static int proximoId = 1;

    private final int id;
    private final String nome;
    private final String email;
    private final String senhaHash; // nunca guardamos a senha pura
    private boolean ativo = true;

    protected Usuario(String nome, String email, String senha) {
        this.id = proximoId++;
        this.nome = nome;
        this.email = email.toLowerCase();
        this.senhaHash = SenhaUtil.hash(senha);
    }

    @Override
    public boolean autenticar(String senha) {
        return ativo && senhaHash.equals(SenhaUtil.hash(senha));
    }

    /** Cada subclasse informa o seu tipo (polimorfismo). */
    public abstract String getTipo();

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return getTipo() + " #" + id + " - " + nome;
    }
}
