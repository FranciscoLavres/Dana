package com.dana.model;

import com.dana.exception.OperacaoInvalidaException;

import java.time.LocalDateTime;

/**
 * Nota de 1 a 5 (com comentário opcional) que um usuário dá a outro:
 * voluntário -> instituição, instituição -> voluntário e voluntário -> serviço do parceiro.
 */
public class Avaliacao extends Entidade {

    private static final long serialVersionUID = 1L;
    public static final int NOTA_MINIMA = 1;
    public static final int NOTA_MAXIMA = 5;

    private final Usuario autor;
    private final Usuario avaliado;
    private final Servico servico; // só preenchido quando a avaliação é de um serviço
    private final int nota;
    private final String comentario;
    private final LocalDateTime data = LocalDateTime.now();

    public Avaliacao(Usuario autor, Usuario avaliado, Servico servico, int nota, String comentario)
            throws OperacaoInvalidaException {
        super(Avaliacao.class);
        if (nota < NOTA_MINIMA || nota > NOTA_MAXIMA) {
            throw new OperacaoInvalidaException(
                    "A nota deve ser de " + NOTA_MINIMA + " a " + NOTA_MAXIMA + ".");
        }
        this.autor = autor;
        this.avaliado = avaliado;
        this.servico = servico;
        this.nota = nota;
        this.comentario = comentario == null ? "" : comentario.trim();
    }

    public Usuario getAutor() {
        return autor;
    }

    public Usuario getAvaliado() {
        return avaliado;
    }

    public Servico getServico() {
        return servico;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getData() {
        return data;
    }
}
