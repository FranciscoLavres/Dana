package com.dana.model;

import com.dana.exception.CadastroInvalidoException;
import com.dana.util.Documento;

/** Empresa (lanchonete, cinema, spa...) que oferece serviços em troca de créditos. */
public class Parceiro extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String cnpj;
    private final String tipoNegocio;

    public Parceiro(String nome, String email, String senha, String cnpj, String tipoNegocio)
            throws CadastroInvalidoException {
        super(nome, email, senha);
        if (!Documento.cnpjValido(cnpj)) {
            throw new CadastroInvalidoException("CNPJ inválido.");
        }
        if (tipoNegocio == null || tipoNegocio.isBlank()) {
            throw new CadastroInvalidoException("Informe o tipo de negócio.");
        }
        this.cnpj = Documento.digitos(cnpj);
        this.tipoNegocio = tipoNegocio.trim();
    }

    @Override
    public String getTipo() {
        return "Parceiro";
    }

    @Override
    public String getDocumento() {
        return cnpj;
    }

    @Override
    public String getNomeDocumento() {
        return "CNPJ";
    }

    @Override
    public String getDocumentoFormatado() {
        return Documento.formatarCnpj(cnpj);
    }

    public String getTipoNegocio() {
        return tipoNegocio;
    }
}
