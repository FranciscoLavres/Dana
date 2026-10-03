package com.dana.relatorio;

import com.dana.exception.PersistenciaException;
import com.dana.util.Formato;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

/**
 * Padrão Template Method: gerar() monta sempre o mesmo cabeçalho e rodapé,
 * e cada subclasse só escreve o corpo (escreverCorpo).
 */
public abstract class RelatorioTxt {

    private static final String LINHA_DUPLA = "=".repeat(64);
    protected static final String LINHA_SIMPLES = "-".repeat(64);

    public final String gerar() {
        StringBuilder texto = new StringBuilder();
        texto.append(LINHA_DUPLA).append('\n');
        texto.append("DĀNA - ").append(titulo().toUpperCase(Formato.LOCALE)).append('\n');
        texto.append("Gerado em ").append(Formato.dataHora(LocalDateTime.now())).append('\n');
        texto.append(LINHA_DUPLA).append("\n\n");
        escreverCorpo(texto);
        texto.append('\n').append(LINHA_DUPLA).append('\n');
        texto.append("Dāna - faça o bem e troque por benefícios\n");
        return texto.toString();
    }

    public final void salvar(Path destino) throws PersistenciaException {
        try {
            Files.writeString(destino, gerar(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenciaException("Não foi possível gravar o arquivo " + destino + ".", e);
        }
    }

    /** Nome sugerido na janela "Salvar como". */
    public abstract String nomeArquivoSugerido();

    protected abstract String titulo();

    protected abstract void escreverCorpo(StringBuilder texto);

    /** Linha no formato "Rótulo:      valor". */
    protected static void campo(StringBuilder texto, String rotulo, Object valor) {
        texto.append(Formato.texto("%-24s %s", rotulo + ":", valor)).append('\n');
    }

    /** Deixa o texto seguro para nome de arquivo. */
    protected static String paraNomeArquivo(String texto) {
        return texto.replaceAll("[^\\p{L}\\p{N}]+", "-").replaceAll("(^-|-$)", "").toLowerCase(Formato.LOCALE);
    }
}
