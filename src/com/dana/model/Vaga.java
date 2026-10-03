package com.dana.model;

import com.dana.exception.OperacaoInvalidaException;
import com.dana.exception.VagaLotadaException;
import com.dana.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Vaga de voluntariado publicada por uma instituição (pode ter uma foto do trabalho). */
public class Vaga extends Entidade implements ComImagem {

    private static final long serialVersionUID = 1L;

    private final Instituicao instituicao;
    private final String titulo;
    private final String descricao;
    private final double horasPrevistas;
    private final int totalVagas;
    private final CategoriaAcao categoria;
    private final Dificuldade dificuldade;
    /** Valor por hora "congelado" na criação: mudar a categoria depois não altera esta vaga. */
    private final BigDecimal valorPorHoraCongelado;
    private final LocalDate dataAcao;
    private int vagasDisponiveis;
    private StatusVaga status = StatusVaga.ABERTA;
    private String imagem;

    public Vaga(Instituicao instituicao, String titulo, String descricao, double horasPrevistas, int totalVagas,
                CategoriaAcao categoria, Dificuldade dificuldade, BigDecimal valorPorHoraCongelado,
                LocalDate dataAcao) {
        super(Vaga.class);
        this.instituicao = instituicao;
        this.titulo = titulo;
        this.descricao = descricao;
        this.horasPrevistas = horasPrevistas;
        this.totalVagas = totalVagas;
        this.vagasDisponiveis = totalVagas;
        this.categoria = categoria;
        this.dificuldade = dificuldade;
        this.valorPorHoraCongelado = valorPorHoraCongelado;
        this.dataAcao = dataAcao;
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
        if (vagasDisponiveis < totalVagas) {
            vagasDisponiveis++;
        }
    }

    public void encerrar() throws OperacaoInvalidaException {
        if (status == StatusVaga.ENCERRADA) {
            throw new OperacaoInvalidaException("Esta vaga já está encerrada.");
        }
        status = StatusVaga.ENCERRADA;
    }

    @Override
    public String getImagem() {
        return imagem;
    }

    @Override
    public void setImagem(String referencia) {
        this.imagem = referencia;
    }

    @Override
    public Usuario getResponsavel() {
        return instituicao;
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

    public int getTotalVagas() {
        return totalVagas;
    }

    public CategoriaAcao getCategoria() {
        return categoria;
    }

    public Dificuldade getDificuldade() {
        return dificuldade;
    }

    public BigDecimal getValorPorHoraCongelado() {
        return valorPorHoraCongelado;
    }

    public LocalDate getDataAcao() {
        return dataAcao;
    }

    public int getVagasDisponiveis() {
        return vagasDisponiveis;
    }

    public StatusVaga getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return Formato.texto("Vaga #%d - %s [%s, %s] %s cred/h | %d vaga(s) | %s | %s",
                getId(), titulo, categoria.getNome(), dificuldade, Formato.creditos(valorPorHoraCongelado),
                vagasDisponiveis, Formato.data(dataAcao), instituicao.getNome());
    }
}
