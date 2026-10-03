package com.dana.model;

import java.math.BigDecimal;

/** Lanche, refeição, sorvete... Aplica o desconto de nível por inteiro. */
public class ServicoRefeicao extends Servico {

    private static final long serialVersionUID = 1L;

    public ServicoRefeicao(String nome, String descricao, BigDecimal custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public BigDecimal calcularCusto(NivelVoluntario nivel) {
        return custoComDesconto(nivel.getDesconto());
    }

    @Override
    public String descreverResgate() {
        return "Apresente o voucher no balcão de " + getParceiro().getNome() + " para retirar: " + getNome();
    }

    @Override
    public TipoServico getTipoServico() {
        return TipoServico.REFEICAO;
    }
}
