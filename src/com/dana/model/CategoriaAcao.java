package com.dana.model;

import com.dana.util.Formato;

import java.io.Serializable;
import java.math.BigDecimal;

/** Categoria de ação voluntária com o valor-base por hora (lida do arquivo categorias.txt). */
public class CategoriaAcao implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String nome;
    private final BigDecimal valorBaseHora;

    public CategoriaAcao(String nome, BigDecimal valorBaseHora) {
        this.nome = nome;
        this.valorBaseHora = valorBaseHora;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorBaseHora() {
        return valorBaseHora;
    }

    @Override
    public String toString() {
        return nome + " (base " + Formato.creditos(valorBaseHora) + "/h)"; // é o que aparece no ChoiceBox
    }
}
