package com.trocabem.service;

import com.trocabem.model.Parceiro;
import com.trocabem.model.Servico;
import com.trocabem.model.ServicoBemEstar;
import com.trocabem.model.ServicoBeleza;
import com.trocabem.model.ServicoRefeicao;
import com.trocabem.model.TipoServico;

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
