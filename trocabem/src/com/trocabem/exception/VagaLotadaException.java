package com.trocabem.exception;

/** Candidatura em vaga sem vagas disponíveis (ou encerrada). */
public class VagaLotadaException extends TrocaBemException {

    public VagaLotadaException(String tituloVaga) {
        super("A vaga \"" + tituloVaga + "\" não está aceitando candidaturas.");
    }
}
