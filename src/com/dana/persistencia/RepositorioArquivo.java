package com.dana.persistencia;

import com.dana.exception.DadosCorrompidosException;
import com.dana.exception.DanaException;
import com.dana.exception.PersistenciaException;
import com.dana.model.CategoriaAcao;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Guarda tudo numa pasta:
 * <pre>
 *   dana.dat        todos os objetos (serialização do Java)
 *   categorias.txt  categorias de ação (texto, pode ser editado)
 *   imagens/        fotos das vagas e dos serviços
 * </pre>
 */
public class RepositorioArquivo implements Repositorio {

    private static final String ARQUIVO_DADOS = "dana.dat";
    private static final String ARQUIVO_CATEGORIAS = "categorias.txt";
    private static final String PASTA_IMAGENS = "imagens";

    private final Path pasta;

    public RepositorioArquivo(Path pasta) {
        this.pasta = pasta.toAbsolutePath();
    }

    public Path getPasta() {
        return pasta;
    }

    @Override
    public BaseDados carregar() throws PersistenciaException {
        Path arquivo = pasta.resolve(ARQUIVO_DADOS);
        if (!Files.exists(arquivo)) {
            return null;
        }
        try (InputStream entrada = Files.newInputStream(arquivo);
             ObjectInputStream objetos = new ObjectInputStream(entrada)) {
            return (BaseDados) objetos.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new DadosCorrompidosException("Não foi possível ler os dados salvos em " + arquivo
                    + ".\nO arquivo pode estar corrompido ou ser de uma versão anterior do sistema.", e);
        }
    }

    /** Grava num arquivo temporário e depois troca, para nunca deixar o arquivo pela metade. */
    @Override
    public void salvar(BaseDados dados) throws PersistenciaException {
        Path arquivo = pasta.resolve(ARQUIVO_DADOS);
        Path temporario = pasta.resolve(ARQUIVO_DADOS + ".tmp");
        try {
            Files.createDirectories(pasta);
            try (OutputStream saida = Files.newOutputStream(temporario);
                 ObjectOutputStream objetos = new ObjectOutputStream(saida)) {
                objetos.writeObject(dados);
            }
            try {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível salvar os dados em " + arquivo + ".", e);
        }
    }

    @Override
    public void arquivarDados() throws PersistenciaException {
        Path arquivo = pasta.resolve(ARQUIVO_DADOS);
        String carimbo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        try {
            if (Files.exists(arquivo)) {
                Files.move(arquivo, pasta.resolve("dana-" + carimbo + ".bak"));
            }
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível fazer a cópia de segurança de " + arquivo + ".", e);
        }
    }

    /** Lê o categorias.txt. Na primeira vez, cria o arquivo com as categorias padrão. */
    @Override
    public List<CategoriaAcao> carregarCategorias() throws PersistenciaException {
        Path arquivo = pasta.resolve(ARQUIVO_CATEGORIAS);
        try {
            if (!Files.exists(arquivo)) {
                Files.createDirectories(pasta);
                Files.writeString(arquivo, Categorias.PADRAO, StandardCharsets.UTF_8);
            }
            return Categorias.ler(Files.readAllLines(arquivo, StandardCharsets.UTF_8), arquivo.toString());
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível ler " + arquivo + ".", e);
        }
    }

    @Override
    public String guardarImagem(Path origem) throws DanaException {
        String extensao = Imagens.validar(origem);
        String nome = UUID.randomUUID() + "." + extensao;
        try {
            Path destino = pasta.resolve(PASTA_IMAGENS);
            Files.createDirectories(destino);
            Files.copy(origem, destino.resolve(nome));
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível copiar a imagem.", e);
        }
        return PASTA_IMAGENS + "/" + nome;
    }

    @Override
    public void apagarImagem(String referencia) {
        Path arquivo = localizarImagem(referencia);
        if (arquivo != null) {
            try {
                Files.deleteIfExists(arquivo);
            } catch (IOException e) {
                // não impede a operação: no máximo sobra um arquivo de imagem sem uso
            }
        }
    }

    @Override
    public Path localizarImagem(String referencia) {
        if (referencia == null) {
            return null;
        }
        Path arquivo = pasta.resolve(referencia).normalize();
        return arquivo.startsWith(pasta) && Files.isRegularFile(arquivo) ? arquivo : null;
    }
}
