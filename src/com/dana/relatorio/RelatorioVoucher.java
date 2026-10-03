package com.dana.relatorio;

import com.dana.model.Resgate;
import com.dana.util.Formato;

/** Voucher para imprimir ou guardar no celular. */
public class RelatorioVoucher extends RelatorioTxt {

    private final Resgate resgate;

    public RelatorioVoucher(Resgate resgate) {
        this.resgate = resgate;
    }

    @Override
    public String nomeArquivoSugerido() {
        return "voucher-" + resgate.getCodigoVoucher() + ".txt";
    }

    @Override
    protected String titulo() {
        return "Voucher";
    }

    @Override
    protected void escreverCorpo(StringBuilder texto) {
        campo(texto, "CÓDIGO", resgate.getCodigoVoucher());
        campo(texto, "Serviço", resgate.getServico().getNome());
        campo(texto, "Parceiro", resgate.getServico().getParceiro().getNome());
        campo(texto, "Voluntário", resgate.getVoluntario().getNome());
        campo(texto, "Custo", Formato.creditos(resgate.getCustoFinal()) + " créditos");
        campo(texto, "Resgatado em", Formato.dataHora(resgate.getDataResgate()));
        campo(texto, "Válido até", Formato.data(resgate.getValidade()));
        campo(texto, "Agendado para", Formato.dataHora(resgate.getAgendadoPara()));
        campo(texto, "Situação", resgate.getStatus());
        texto.append('\n').append(resgate.getServico().descreverResgate()).append('\n');
    }
}
