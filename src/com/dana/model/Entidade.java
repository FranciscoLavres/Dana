package com.dana.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Superclasse de tudo que tem id (usuário, vaga, candidatura...).
 * Cada "família" (Usuario, Vaga...) tem sua própria sequência de ids, e duas entidades
 * são iguais quando são da mesma família e têm o mesmo id (equals/hashCode).
 * Serializable: é o que permite gravar os objetos em arquivo.
 */
public abstract class Entidade implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Map<String, Integer> CONTADORES = new HashMap<>();

    private final String familia;
    private final int id;

    protected Entidade(Class<? extends Entidade> familia) {
        this.familia = familia.getSimpleName();
        this.id = proximoId(this.familia);
    }

    private static synchronized int proximoId(String familia) {
        return CONTADORES.merge(familia, 1, Integer::sum);
    }

    /** Depois de carregar do arquivo, faz os próximos ids continuarem de onde pararam. */
    public static synchronized void sincronizarContadores(Collection<? extends Entidade> entidades) {
        for (Entidade e : entidades) {
            CONTADORES.merge(e.familia, e.id, Math::max);
        }
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object outro) {
        return outro instanceof Entidade e && familia.equals(e.familia) && id == e.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(familia, id);
    }
}
