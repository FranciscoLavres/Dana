package com.dana.exception;

/** Falha ao ler ou gravar arquivos (dados salvos, categorias.txt, imagens, relatórios). */
public class PersistenciaException extends DanaException {

    public PersistenciaException(String mensagem) {
        super(mensagem);
    }

    public PersistenciaException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
