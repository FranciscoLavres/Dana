package com.dana.model;

import java.math.BigDecimal;

/** Salão, barbearia, manicure... Aplica só metade do desconto de nível. */
public class ServicoBeleza extends Servico {

    private static final long serialVersionUID = 1L;
    private static final BigDecimal DOIS = BigDecimal.valueOf(2);

    public ServicoBeleza(String nome, String descricao, BigDecimal custoBase, Parceiro parceiro) {
        super(nome, descricao, custoBase, parceiro);
    }

    @Override
    public BigDecimal calcularCusto(NivelVoluntario nivel) {
        return custoComDesconto(nivel.getDesconto().divide(DOIS));
    }

    @Override
    public String descreverResgate() {
        return "Agende com antecedência em " + getParceiro().getNome() + " e apresente o voucher: " + getNome();
    }

    @Override
    public TipoServico getTipoServico() {
        return TipoServico.BELEZA;
    }
}
