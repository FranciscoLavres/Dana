package com.dana.exception;

/** O arquivo de dados existe, mas não pôde ser lido (corrompido ou de uma versão antiga do sistema). */
public class DadosCorrompidosException extends PersistenciaException {

    public DadosCorrompidosException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
