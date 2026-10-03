package com.dana.relatorio;

import com.dana.model.Parceiro;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.StatusResgate;
import com.dana.util.Formato;

import java.util.List;
import java.util.OptionalDouble;

/** Serviços e resgates (vouchers) de um parceiro. */
public class RelatorioParceiro extends RelatorioTxt {

    private final Parceiro parceiro;
    private final List<Servico> servicos;
    private final List<Resgate> resgates;
    private final OptionalDouble media;

    public RelatorioParceiro(Parceiro parceiro, List<Servico> servicos, List<Resgate> resgates,
                             OptionalDouble media) {
        this.parceiro = parceiro;
        this.servicos = servicos;
        this.resgates = resgates;
        this.media = media;
    }

    @Override
    public String nomeArquivoSugerido() {
        return "relatorio-" + paraNomeArquivo(parceiro.getNome()) + ".txt";
    }

    @Override
    protected String titulo() {
        return "Relatório do parceiro";
    }

    @Override
    protected void escreverCorpo(StringBuilder texto) {
        campo(texto, "Parceiro", parceiro.getNome());
        campo(texto, "CNPJ", parceiro.getDocumentoFormatado());
        campo(texto, "Avaliação média", Formato.media(media));
        long usados = resgates.stream().filter(r -> r.getStatus() == StatusResgate.UTILIZADO).count();
        campo(texto, "Vouchers emitidos", resgates.size());
        campo(texto, "Vouchers utilizados", usados);

        texto.append("\nSERVIÇOS (").append(servicos.size()).append(")\n").append(LINHA_SIMPLES).append('\n');
        for (Servico s : servicos) {
            texto.append(Formato.texto("#%d %s | %s | %s créditos | %s", s.getId(), s.getNome(),
                    s.getTipoServico(), Formato.creditos(s.getCustoBase()), s.isAtivo() ? "ativo" : "inativo"))
                    .append('\n');
        }

        texto.append("\nRESGATES (").append(resgates.size()).append(")\n").append(LINHA_SIMPLES).append('\n');
        for (Resgate r : resgates) {
            texto.append(Formato.texto("%s | %s | %s | %s | %s", r.getCodigoVoucher(), r.getServico().getNome(),
                    r.getVoluntario().getNome(), Formato.dataHora(r.getDataResgate()), r.getStatus()))
                    .append('\n');
        }
    }
}
