package com.dana.model;

/** Categoria de ação voluntária com o valor-base por hora (futuramente lida do categorias.txt). */
public class CategoriaAcao {

    private final String nome;
    private final double valorBaseHora;

    public CategoriaAcao(String nome, double valorBaseHora) {
        this.nome = nome;
        this.valorBaseHora = valorBaseHora;
    }

    public String getNome() {
        return nome;
    }

    public double getValorBaseHora() {
        return valorBaseHora;
    }

    @Override
    public String toString() {
        return nome + " (base " + valorBaseHora + "/h)";
    }
}
