package com.dana.model;

import java.time.LocalDateTime;

/** Aviso para um usuário (nova candidatura, horas confirmadas, voucher usado, avaliação recebida...). */
public class Notificacao extends Entidade {

    private static final long serialVersionUID = 1L;

    private final Usuario destinatario;
    private final String mensagem;
    private final LocalDateTime data = LocalDateTime.now();
    private boolean lida;

    public Notificacao(Usuario destinatario, String mensagem) {
        super(Notificacao.class);
        this.destinatario = destinatario;
        this.mensagem = mensagem;
    }

    public void marcarComoLida() {
        lida = true;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public String getMensagem() {
        return mensagem;
    }

    public LocalDateTime getData() {
        return data;
    }

    public boolean isLida() {
        return lida;
    }
}
