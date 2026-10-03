package com.dana.relatorio;

import com.dana.model.Candidatura;
import com.dana.model.Instituicao;
import com.dana.model.StatusCandidatura;
import com.dana.model.Vaga;
import com.dana.util.Formato;

import java.util.List;
import java.util.OptionalDouble;

/** Resumo das vagas e das candidaturas de uma instituição. */
public class RelatorioInstituicao extends RelatorioTxt {

    private final Instituicao instituicao;
    private final List<Vaga> vagas;
    private final List<Candidatura> candidaturas;
    private final OptionalDouble media;

    public RelatorioInstituicao(Instituicao instituicao, List<Vaga> vagas, List<Candidatura> candidaturas,
                                OptionalDouble media) {
        this.instituicao = instituicao;
        this.vagas = vagas;
        this.candidaturas = candidaturas;
        this.media = media;
    }

    @Override
    public String nomeArquivoSugerido() {
        return "relatorio-" + paraNomeArquivo(instituicao.getNome()) + ".txt";
    }

    @Override
    protected String titulo() {
        return "Relatório da instituição";
    }

    @Override
    protected void escreverCorpo(StringBuilder texto) {
        campo(texto, "Instituição", instituicao.getNome());
        campo(texto, "CNPJ", instituicao.getDocumentoFormatado());
        campo(texto, "Avaliação média", Formato.media(media));
        double horas = candidaturas.stream()
                .filter(c -> c.getStatus() == StatusCandidatura.CONCLUIDA)
                .mapToDouble(Candidatura::getHorasConfirmadas).sum();
        campo(texto, "Horas confirmadas", Formato.horas(horas));

        texto.append("\nVAGAS (").append(vagas.size()).append(")\n").append(LINHA_SIMPLES).append('\n');
        for (Vaga v : vagas) {
            texto.append(Formato.texto("#%d %s | %s | %s | %d de %d vaga(s) livre(s) | %s",
                    v.getId(), v.getTitulo(), Formato.data(v.getDataAcao()), v.getCategoria().getNome(),
                    v.getVagasDisponiveis(), v.getTotalVagas(), v.getStatus())).append('\n');
        }

        texto.append("\nCANDIDATURAS (").append(candidaturas.size()).append(")\n")
                .append(LINHA_SIMPLES).append('\n');
        for (Candidatura c : candidaturas) {
            String horasTexto = c.getStatus() == StatusCandidatura.CONCLUIDA
                    ? " | " + Formato.horas(c.getHorasConfirmadas()) + " h" : "";
            texto.append(c.getVoluntario().getNome()).append(" -> ").append(c.getVaga().getTitulo())
                    .append(" | ").append(c.getStatus()).append(horasTexto).append('\n');
        }
    }
}
