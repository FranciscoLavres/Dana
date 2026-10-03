package com.dana.service;

import com.dana.model.Parceiro;
import com.dana.model.Servico;
import com.dana.model.ServicoBemEstar;
import com.dana.model.ServicoBeleza;
import com.dana.model.ServicoRefeicao;
import com.dana.model.TipoServico;

/** Padrão Factory: quem chama só diz o tipo; a fábrica decide qual subclasse instanciar. */
public final class ServicoFactory {

    private ServicoFactory() {
    }

    public static Servico criar(TipoServico tipo, String nome, String descricao, double custo, Parceiro parceiro) {
        switch (tipo) {
            case REFEICAO:
                return new ServicoRefeicao(nome, descricao, custo, parceiro);
            case BELEZA:
                return new ServicoBeleza(nome, descricao, custo, parceiro);
            case BEM_ESTAR:
                return new ServicoBemEstar(nome, descricao, custo, parceiro);
            default:
                throw new IllegalArgumentException("Tipo de serviço desconhecido: " + tipo);
        }
    }
}
