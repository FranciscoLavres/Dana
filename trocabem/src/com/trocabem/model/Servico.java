package com.trocabem.model;

/**
 * Serviço que o parceiro oferece em troca de créditos.
 * Abstrata: cada tipo (refeição, beleza, bem-estar) calcula o custo e descreve o resgate do seu jeito.
 */
public abstract class Servico {

    private static int proximoId = 1;

    private final int id;
    private final String nome;
    private final String descricao;
    private final double custoBase;
    private final Parceiro parceiro;
    private boolean ativo = true;

    protected Servico(String nome, String descricao, double custoBase, Parceiro parceiro) {
        this.id = proximoId++;
        this.nome = nome;
        this.descricao = descricao;
        this.custoBase = custoBase;
        this.parceiro = parceiro;
    }

    /** Polimorfismo: o custo final depende do tipo de serviço e do nível do voluntário. */
    public abstract double calcularCusto(NivelVoluntario nivel);

    /** Texto que aparece no voucher. */
    public abstract String descreverResgate();

    /** Arredonda para 2 casas, usado pelas subclasses. */
    protected double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public double getCustoBase() {
        return custoBase;
    }

    public Parceiro getParceiro() {
        return parceiro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return String.format("Servico #%d - %s (%s) base %.2f cred | %s", id, nome, getClass().getSimpleName(),
                custoBase, parceiro.getNome());
    }
}
