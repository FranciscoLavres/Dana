package com.dana.relatorio;

import com.dana.exception.OperacaoInvalidaException;
import com.dana.model.Candidatura;
import com.dana.model.StatusCandidatura;
import com.dana.model.Vaga;
import com.dana.util.Formato;

/** Comprovante das horas de voluntariado de uma candidatura concluída. */
public class ComprovanteHoras extends RelatorioTxt {

    private final Candidatura candidatura;

    public ComprovanteHoras(Candidatura candidatura) throws OperacaoInvalidaException {
        if (candidatura.getStatus() != StatusCandidatura.CONCLUIDA) {
            throw new OperacaoInvalidaException(
                    "O comprovante só existe depois que a instituição confirma as horas.");
        }
        this.candidatura = candidatura;
    }

    @Override
    public String nomeArquivoSugerido() {
        return "comprovante-horas-" + candidatura.getId() + ".txt";
    }

    @Override
    protected String titulo() {
        return "Comprovante de horas voluntárias";
    }

    @Override
    protected void escreverCorpo(StringBuilder texto) {
        Vaga vaga = candidatura.getVaga();
        texto.append("Declaramos que a pessoa voluntária abaixo participou da ação descrita,\n")
                .append("com as horas confirmadas pela instituição responsável.\n\n");
        campo(texto, "Voluntário", candidatura.getVoluntario().getNome());
        campo(texto, "CPF", candidatura.getVoluntario().getDocumentoFormatado());
        campo(texto, "Instituição", vaga.getInstituicao().getNome());
        campo(texto, "CNPJ", vaga.getInstituicao().getDocumentoFormatado());
        campo(texto, "Ação", vaga.getTitulo());
        campo(texto, "Categoria", vaga.getCategoria().getNome());
        campo(texto, "Data da ação", Formato.data(vaga.getDataAcao()));
        campo(texto, "Horas confirmadas", Formato.horas(candidatura.getHorasConfirmadas()));
        campo(texto, "Créditos gerados", Formato.creditos(candidatura.getCreditosGerados()));
        campo(texto, "Código do comprovante", "DANA-" + candidatura.getId() + "-" + vaga.getId());
        texto.append('\n').append("Cálculo: ").append(candidatura.getDetalheCalculo()).append('\n');
    }
}
