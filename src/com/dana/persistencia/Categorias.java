package com.dana.persistencia;

import com.dana.exception.PersistenciaException;
import com.dana.model.CategoriaAcao;
import com.dana.util.Valor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Categorias de ação no formato texto do categorias.txt: uma por linha, "Nome;valor por hora".
 * Linhas vazias e linhas que começam com # são ignoradas.
 */
final class Categorias {

    static final String PADRAO = String.join("\n",
            "# Categorias de ação voluntária: nome;valor-base de créditos por hora",
            "# Pode editar este arquivo. As mudanças valem para as vagas publicadas depois.",
            "Apoio administrativo;10",
            "Educação e reforço;15",
            "Cuidado e saúde;18",
            "Manutenção e obras;20",
            "Alimentação;14",
            "Eventos e campanhas;12",
            "Habilidade especializada;25",
            "");

    private Categorias() {
    }

    static List<CategoriaAcao> ler(List<String> linhas, String origem) throws PersistenciaException {
        List<CategoriaAcao> categorias = new ArrayList<>();
        for (int i = 0; i < linhas.size(); i++) {
            String linha = linhas.get(i).strip();
            if (linha.isEmpty() || linha.startsWith("#")) {
                continue;
            }
            String[] partes = linha.split(";");
            String erro = origem + ", linha " + (i + 1) + ": ";
            if (partes.length != 2 || partes[0].isBlank()) {
                throw new PersistenciaException(erro + "use o formato Nome;valor.");
            }
            try {
                BigDecimal valor = Valor.de(partes[1].strip().replace(',', '.'));
                if (valor.signum() <= 0) {
                    throw new PersistenciaException(erro + "o valor deve ser maior que zero.");
                }
                categorias.add(new CategoriaAcao(partes[0].strip(), valor));
            } catch (NumberFormatException e) {
                throw new PersistenciaException(erro + "\"" + partes[1].strip() + "\" não é um número.");
            }
        }
        if (categorias.isEmpty()) {
            throw new PersistenciaException(origem + " não tem nenhuma categoria.");
        }
        return categorias;
    }

    static List<CategoriaAcao> padrao() {
        try {
            return ler(List.of(PADRAO.split("\n")), "padrão");
        } catch (PersistenciaException e) {
            throw new IllegalStateException(e);
        }
    }
}
