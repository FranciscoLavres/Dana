package com.dana.persistencia;

import com.dana.model.Avaliacao;
import com.dana.model.Candidatura;
import com.dana.model.Entidade;
import com.dana.model.Notificacao;
import com.dana.model.Resgate;
import com.dana.model.Servico;
import com.dana.model.Usuario;
import com.dana.model.Vaga;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Todos os dados do sistema num único objeto. É ele que o Repositorio grava e lê.
 * As listas são entregues "vivas" para o DanaService, que é quem aplica as regras.
 */
public class BaseDados implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Usuario> usuarios = new ArrayList<>();
    private final List<Vaga> vagas = new ArrayList<>();
    private final List<Candidatura> candidaturas = new ArrayList<>();
    private final List<Servico> servicos = new ArrayList<>();
    private final List<Resgate> resgates = new ArrayList<>();
    private final List<Avaliacao> avaliacoes = new ArrayList<>();
    private final List<Notificacao> notificacoes = new ArrayList<>();
    private boolean dadosDeExemplo;

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Vaga> getVagas() {
        return vagas;
    }

    public List<Candidatura> getCandidaturas() {
        return candidaturas;
    }

    public List<Servico> getServicos() {
        return servicos;
    }

    public List<Resgate> getResgates() {
        return resgates;
    }

    public List<Avaliacao> getAvaliacoes() {
        return avaliacoes;
    }

    public List<Notificacao> getNotificacoes() {
        return notificacoes;
    }

    /** true quando a base começou com os dados de demonstração (contas de teste). */
    public boolean isDadosDeExemplo() {
        return dadosDeExemplo;
    }

    public void setDadosDeExemplo(boolean dadosDeExemplo) {
        this.dadosDeExemplo = dadosDeExemplo;
    }

    /** Usado para continuar a numeração dos ids depois de carregar. */
    public List<Entidade> todasEntidades() {
        List<Entidade> todas = new ArrayList<>();
        todas.addAll(usuarios);
        todas.addAll(vagas);
        todas.addAll(candidaturas);
        todas.addAll(servicos);
        todas.addAll(resgates);
        todas.addAll(avaliacoes);
        todas.addAll(notificacoes);
        return todas;
    }
}
