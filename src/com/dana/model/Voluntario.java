package com.dana.model;

import com.dana.exception.CadastroInvalidoException;
import com.dana.util.Documento;

/** Voluntário: composição com Carteira (ele "tem uma" carteira). */
public class Voluntario extends Usuario {

    private static final long serialVersionUID = 1L;

    private final String cpf;
    private final Carteira carteira = new Carteira();
    private double horasTotais;

    public Voluntario(String nome, String email, String senha, String cpf) throws CadastroInvalidoException {
        super(nome, email, senha);
        if (!Documento.cpfValido(cpf)) {
            throw new CadastroInvalidoException("CPF inválido.");
        }
        this.cpf = Documento.digitos(cpf);
    }

    @Override
    public String getTipo() {
        return "Voluntário";
    }

    @Override
    public String getDocumento() {
        return cpf;
    }

    @Override
    public String getNomeDocumento() {
        return "CPF";
    }

    @Override
    public String getDocumentoFormatado() {
        return Documento.formatarCpf(cpf);
    }

    /** O nível não é guardado: é sempre calculado a partir das horas. */
    public NivelVoluntario getNivel() {
        return NivelVoluntario.porHoras(horasTotais);
    }

    public void registrarHoras(double horas) {
        if (horas <= 0) {
            throw new IllegalArgumentException("Horas devem ser positivas.");
        }
        horasTotais += horas;
    }

    public Carteira getCarteira() {
        return carteira;
    }

    public double getHorasTotais() {
        return horasTotais;
    }
}
