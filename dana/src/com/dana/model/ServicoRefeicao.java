package com.dana.model;

/** Lanche, refeição, sorvete... Aplica o desconto de nível por inteiro. */
public class ServicoRefeicao extends Servico {

    public ServicoRefeicao(String nome, String descricao, double custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public double calcularCusto(NivelVoluntario nivel) {
        return arredondar(getCustoBase() * (1 - nivel.getDesconto()));
    }

    @Override
    public String descreverResgate() {
        return "Apresente o voucher no balcão de " + getParceiro().getNome() + " para retirar: " + getNome();
    }
}
