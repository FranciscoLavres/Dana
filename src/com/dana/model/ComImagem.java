package com.dana.model;

/**
 * Interface de quem pode ter uma foto: a vaga (foto do trabalho) e o serviço (foto do produto).
 * A imagem é guardada como uma referência ao arquivo (o arquivo em si fica na pasta de dados).
 */
public interface ComImagem {

    /** Referência da imagem no armazenamento, ou null se não tiver. */
    String getImagem();

    void setImagem(String referencia);

    /** Único usuário que pode trocar a imagem. */
    Usuario getResponsavel();

    default boolean temImagem() {
        return getImagem() != null;
    }
}
