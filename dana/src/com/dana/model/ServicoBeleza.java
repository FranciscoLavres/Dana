package com.dana.model;

/** Salão, barbearia, manicure... Aplica só metade do desconto de nível. */
public class ServicoBeleza extends Servico {

    public ServicoBeleza(String nome, String descricao, double custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public double calcularCusto(NivelVoluntario nivel) {
        return arredondar(getCustoBase() * (1 - nivel.getDesconto() / 2));
    }

    @Override
    public String descreverResgate() {
        return "Agende com antecedência em " + getParceiro().getNome() + " e apresente o voucher: " + getNome();
    }
}
