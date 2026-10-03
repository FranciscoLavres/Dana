package com.dana.relatorio;

import com.dana.model.Transacao;
import com.dana.model.Voluntario;
import com.dana.util.Formato;

/** Extrato completo da carteira do voluntário. */
public class RelatorioExtrato extends RelatorioTxt {

    private final Voluntario voluntario;

    public RelatorioExtrato(Voluntario voluntario) {
        this.voluntario = voluntario;
    }

    @Override
    public String nomeArquivoSugerido() {
        return "extrato-" + paraNomeArquivo(voluntario.getNome()) + ".txt";
    }

    @Override
    protected String titulo() {
        return "Extrato de créditos";
    }

    @Override
    protected void escreverCorpo(StringBuilder texto) {
        campo(texto, "Voluntário", voluntario.getNome());
        campo(texto, "CPF", voluntario.getDocumentoFormatado());
        campo(texto, "Nível", voluntario.getNivel());
        campo(texto, "Horas voluntariadas", Formato.horas(voluntario.getHorasTotais()));
        texto.append('\n').append(LINHA_SIMPLES).append('\n');
        if (voluntario.getCarteira().getExtrato().isEmpty()) {
            texto.append("Nenhuma movimentação.\n");
        }
        for (Transacao t : voluntario.getCarteira().getExtrato()) {
            texto.append(t).append('\n');
        }
        texto.append(LINHA_SIMPLES).append('\n');
        campo(texto, "SALDO ATUAL", Formato.creditos(voluntario.getCarteira().getSaldo()) + " créditos");
    }
}
