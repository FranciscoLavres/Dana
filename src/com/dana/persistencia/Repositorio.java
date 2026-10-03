package com.dana.persistencia;

import com.dana.exception.DanaException;
import com.dana.exception.PersistenciaException;
import com.dana.model.CategoriaAcao;

import java.nio.file.Path;
import java.util.List;

/**
 * Onde os dados ficam guardados. O DanaService só conhece esta interface,
 * então dá para trocar arquivo por memória (testes) ou, no futuro, por um banco, sem mexer nas regras.
 */
public interface Repositorio {

    /** Devolve os dados salvos, ou null se ainda não existe nada salvo (primeira execução). */
    BaseDados carregar() throws PersistenciaException;

    void salvar(BaseDados dados) throws PersistenciaException;

    /** Guarda uma cópia de segurança dos dados atuais e deixa o repositório vazio. */
    void arquivarDados() throws PersistenciaException;

    List<CategoriaAcao> carregarCategorias() throws PersistenciaException;

    /** Copia a imagem para o armazenamento e devolve a referência que fica guardada no objeto. */
    String guardarImagem(Path origem) throws DanaException;

    void apagarImagem(String referencia);

    /** Caminho do arquivo de uma imagem guardada, ou null se não existir. */
    Path localizarImagem(String referencia);
}
