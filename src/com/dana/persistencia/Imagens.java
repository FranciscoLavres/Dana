package com.dana.persistencia;

import com.dana.exception.OperacaoInvalidaException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

/** Regras para aceitar uma imagem (formato e tamanho), usadas pelos dois repositórios. */
final class Imagens {

    static final Set<String> EXTENSOES = Set.of("png", "jpg", "jpeg", "gif", "bmp");
    static final long TAMANHO_MAXIMO = 5L * 1024 * 1024; // 5 MB

    private Imagens() {
    }

    static String validar(Path origem) throws OperacaoInvalidaException {
        if (origem == null || !Files.isRegularFile(origem)) {
            throw new OperacaoInvalidaException("Arquivo de imagem não encontrado.");
        }
        String extensao = extensao(origem);
        if (!EXTENSOES.contains(extensao)) {
            throw new OperacaoInvalidaException("Formato de imagem não aceito. Use PNG, JPG, GIF ou BMP.");
        }
        try {
            if (Files.size(origem) > TAMANHO_MAXIMO) {
                throw new OperacaoInvalidaException("A imagem deve ter no máximo 5 MB.");
            }
        } catch (IOException e) {
            throw new OperacaoInvalidaException("Não foi possível ler a imagem escolhida.");
        }
        return extensao;
    }

    private static String extensao(Path arquivo) {
        String nome = arquivo.getFileName().toString();
        int ponto = nome.lastIndexOf('.');
        return ponto < 0 ? "" : nome.substring(ponto + 1).toLowerCase(Locale.ROOT);
    }
}
