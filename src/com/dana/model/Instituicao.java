package com.dana.model;

import com.dana.exception.CadastroInvalidoException;
import com.dana.util.Documento;

/** ONG, abrigo, escola etc. que publica vagas. */
public class Instituicao extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String cnpj;
    private final String descricao;

    public Instituicao(String nome, String email, String senha, String cnpj, String descricao)
            throws CadastroInvalidoException {
        super(nome, email, senha);
        if (!Documento.cnpjValido(cnpj)) {
            throw new CadastroInvalidoException("CNPJ inválido.");
        }
        this.cnpj = Documento.digitos(cnpj);
        this.descricao = descricao == null ? "" : descricao.trim();
    }

    @Override
    public String getTipo() {
        return "Instituição";
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

    public String getDescricao() {
        return descricao;
    }
}
