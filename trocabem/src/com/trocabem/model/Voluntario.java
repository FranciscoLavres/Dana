package com.trocabem.model;

/** Voluntário: composição com Carteira (ele "tem uma" carteira). */
public class Voluntario extends Usuario {

    private final String cpf;
    private final Carteira carteira = new Carteira();
    private double horasTotais;

    public Voluntario(String nome, String email, String senha, String cpf) {
        super(nome, email, senha);
        this.cpf = cpf;
    }

    @Override
    public String getTipo() {
        return "Voluntario";
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

    public String getCpf() {
        return cpf;
    }

    public Carteira getCarteira() {
        return carteira;
    }

    public double getHorasTotais() {
        return horasTotais;
    }
}
