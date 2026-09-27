package com.trocabem.model;

import com.trocabem.exception.OperacaoInvalidaException;

/** Liga um voluntário a uma vaga e guarda o resultado (horas e créditos) quando concluída. */
public class Candidatura {

    private static int proximoId = 1;

    private final int id;
    private final Voluntario voluntario;
    private final Vaga vaga;
    private StatusCandidatura status = StatusCandidatura.PENDENTE;
    private double horasConfirmadas;
    private double creditosGerados;
    private String detalheCalculo;

    public Candidatura(Voluntario voluntario, Vaga vaga) {
        this.id = proximoId++;
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

    public void concluir(double horas, double creditos, String detalhe) throws OperacaoInvalidaException {
        exigirStatus(StatusCandidatura.APROVADA, "concluir");
        this.horasConfirmadas = horas;
        this.creditosGerados = creditos;
        this.detalheCalculo = detalhe;
        status = StatusCandidatura.CONCLUIDA;
    }

    private void exigirStatus(StatusCandidatura esperado, String acao) throws OperacaoInvalidaException {
        if (status != esperado) {
            throw new OperacaoInvalidaException(
                    "Não é possível " + acao + " uma candidatura com status " + status + ".");
        }
    }

    public int getId() {
        return id;
    }

    public Voluntario getVoluntario() {
        return voluntario;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public StatusCandidatura getStatus() {
        return status;
    }

    public double getHorasConfirmadas() {
        return horasConfirmadas;
    }

    public double getCreditosGerados() {
        return creditosGerados;
    }

    public String getDetalheCalculo() {
        return detalheCalculo;
    }

    @Override
    public String toString() {
        return "Candidatura #" + id + " - " + voluntario.getNome() + " -> " + vaga.getTitulo() + " [" + status + "]";
    }
}
