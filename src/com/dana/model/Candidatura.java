package com.dana.model;

import com.dana.exception.OperacaoInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Liga um voluntário a uma vaga e guarda o resultado (horas e créditos) quando concluída.
 * Depois de concluída, cada lado pode avaliar o outro uma única vez.
 */
public class Candidatura extends Entidade {

    private static final long serialVersionUID = 1L;

    private final Voluntario voluntario;
    private final Vaga vaga;
    private final LocalDateTime data = LocalDateTime.now();
    private StatusCandidatura status = StatusCandidatura.PENDENTE;
    private double horasConfirmadas;
    private BigDecimal creditosGerados;
    private String detalheCalculo;
    private Avaliacao avaliacaoDoVoluntario;   // voluntário avalia a instituição
    private Avaliacao avaliacaoDaInstituicao;  // instituição avalia o voluntário

    public Candidatura(Voluntario voluntario, Vaga vaga) {
        super(Candidatura.class);
        this.voluntario = voluntario;
        this.vaga = vaga;
    }

    public void aprovar() throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.PENDENTE, "aprovar");
        status = StatusCandidatura.APROVADA;
    }

    public void recusar() throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.PENDENTE, "recusar");
        status = StatusCandidatura.RECUSADA;
    }

    public void concluir(double horas, BigDecimal creditos, String detalhe) throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.APROVADA, "concluir");
        this.horasConfirmadas = horas;
        this.creditosGerados = creditos;
        this.detalheCalculo = detalhe;
        status = StatusCandidatura.CONCLUIDA;
    }

    public void registrarAvaliacaoDoVoluntario(Avaliacao avaliacao) throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.CONCLUIDA, "avaliar");
        if (avaliacaoDoVoluntario != null) {
            throw new OperacaoInvalidaException("Você já avaliou esta participação.");
        }
        avaliacaoDoVoluntario = avaliacao;
    }

    public void registrarAvaliacaoDaInstituicao(Avaliacao avaliacao) throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.CONCLUIDA, "avaliar");
        if (avaliacaoDaInstituicao != null) {
            throw new OperacaoInvalidaException("Este voluntário já foi avaliado nesta vaga.");
        }
        avaliacaoDaInstituicao = avaliacao;
    }

    private void exigirStatus(StatusCandidatura esperado, String acao) throws OperacaoInvalidaException {
        if (status != esperado) {
            throw new OperacaoInvalidaException(
                    "Não é possível " + acao + " uma candidatura com status " + status + ".");
        }
    }

    public Voluntario getVoluntario() {
        return voluntario;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public LocalDateTime getData() {
        return data;
    }

    public StatusCandidatura getStatus() {
        return status;
    }

    public double getHorasConfirmadas() {
        return horasConfirmadas;
    }

    public BigDecimal getCreditosGerados() {
        return creditosGerados;
    }

    public String getDetalheCalculo() {
        return detalheCalculo;
    }

    public Avaliacao getAvaliacaoDoVoluntario() {
        return avaliacaoDoVoluntario;
    }

    public Avaliacao getAvaliacaoDaInstituicao() {
        return avaliacaoDaInstituicao;
    }

    @Override
    public String toString() {
        return "Candidatura #" + getId() + " - " + voluntario.getNome() + " -> " + vaga.getTitulo() + " [" + status + "]";
    }
}
