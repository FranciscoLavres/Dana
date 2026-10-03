package com.dana.model;

import java.math.BigDecimal;

/** Spa, massagem, ingresso de cinema... Preço fixo, sem desconto de nível. */
public class ServicoBemEstar extends Servico {

    private static final long serialVersionUID = 1L;

    public ServicoBemEstar(String nome, String descricao, BigDecimal custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public BigDecimal calcularCusto(NivelVoluntario nivel) {
        return getCustoBase();
    }

    @Override
    public String descreverResgate() {
        return "Voucher válido em " + getParceiro().getNome() + " (sujeito a disponibilidade): " + getNome();
    }

    @Override
    public TipoServico getTipoServico() {
        return TipoServico.BEM_ESTAR;
    }
}
