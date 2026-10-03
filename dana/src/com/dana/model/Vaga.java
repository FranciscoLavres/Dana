package com.dana.model;

import com.dana.exception.VagaLotadaException;

/** Vaga de voluntariado publicada por uma instituição. */
public class Vaga {

    private static int proximoId = 1;

    private final int id;
    private final Instituicao instituicao;
    private final String titulo;
    private final String descricao;
    private final double horasPrevistas;
    private final CategoriaAcao categoria;
    private final Dificuldade dificuldade;
    /** Valor por hora "congelado" na criação: mudar a categoria depois não altera esta vaga. */
    private final double valorPorHoraCongelado;
    private int vagasDisponiveis;
    private StatusVaga status = StatusVaga.ABERTA;

    public Vaga(Instituicao instituicao, String titulo, String descricao, double horasPrevistas,
                int totalVagas, CategoriaAcao categoria, Dificuldade dificuldade, double valorPorHoraCongelado) {
        this.id = proximoId++;
        this.instituicao = instituicao;
        this.titulo = titulo;
        this.descricao = descricao;
        this.horasPrevistas = horasPrevistas;
        this.vagasDisponiveis = totalVagas;
        this.categoria = categoria;
        this.dificuldade = dificuldade;
        this.valorPorHoraCongelado = valorPorHoraCongelado;
    }

    public boolean aceitaCandidaturas() {
        return status == StatusVaga.ABERTA && vagasDisponiveis > 0;
    }

    public void ocuparVaga() throws VagaLotadaException {
        if (!aceitaCandidaturas()) {
            throw new VagaLotadaException(titulo);
        }
        vagasDisponiveis--;
    }

    public void liberarVaga() {
        vagasDisponiveis++;
    }

    public void encerrar() {
        status = StatusVaga.ENCERRADA;
    }

    public int getId() {
        return id;
    }

    public Instituicao getInstituicao() {
        return instituicao;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getHorasPrevistas() {
        return horasPrevistas;
    }

    public CategoriaAcao getCategoria() {
        return categoria;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public double getValorPorHoraCongelado() {
        return valorPorHoraCongelado;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public StatusVaga getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("Vaga #%d - %s [%s, %s] %.2f cred/h | %d vaga(s) | %s",
                id, titulo, categoria.getNome(), dificuldade, valorPorHoraCongelado, vagasDisponiveis,
                instituicao.getNome());
    }
}
