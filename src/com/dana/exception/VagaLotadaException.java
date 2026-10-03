package com.dana.exception;

/** Candidatura em vaga sem vagas disponíveis (ou encerrada). */
public class VagaLotadaException extends DanaException {

    public VagaLotadaException(String tituloVaga) {
        super("A vaga \"" + tituloVaga + "\" não está aceitando candidaturas.");
    }
}
