package com.dana.model;

/** Spa, massagem, ingresso de cinema... Preço fixo, sem desconto de nível. */
public class ServicoBemEstar extends Servico {

    public ServicoBemEstar(String nome, String descricao, double custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public double calcularCusto(NivelVoluntario nivel) {
        return arredondar(getCustoBase());
    }

    @Override
    public String descreverResgate() {
        return "Voucher válido em " + getParceiro().getNome() + " (sujeito a disponibilidade): " + getNome();
    }
}
