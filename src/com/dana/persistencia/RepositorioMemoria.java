package com.dana.persistencia;

import com.dana.exception.DanaException;
import com.dana.model.CategoriaAcao;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Repositório que não grava nada em disco: usado nos testes. Começa sempre com os dados de exemplo. */
public class RepositorioMemoria implements Repositorio {

    private BaseDados dados;

    @Override
    public BaseDados carregar() {
        return dados;
    }

    @Override
    public void salvar(BaseDados dados) {
        this.dados = dados;
    }

    @Override
    public void arquivarDados() {
        dados = null;
    }

    @Override
    public List<CategoriaAcao> carregarCategorias() {
        return Categorias.padrao();
    }

    /** Em memória a imagem não é copiada: a referência é o próprio caminho do arquivo. */
    @Override
    public String guardarImagem(Path origem) throws DanaException {
        Imagens.validar(origem);
        return origem.toAbsolutePath().toString();
    }

    @Override
    public void apagarImagem(String referencia) {
        // nada a apagar: o arquivo original não é do sistema
    }

    @Override
    public Path localizarImagem(String referencia) {
        if (referencia == null) {
            return null;
        }
        Path arquivo = Path.of(referencia);
        return Files.isRegularFile(arquivo) ? arquivo : null;
    }
}
