package com.dana.model;

import com.dana.util.Formato;
import com.dana.util.Valor;

import java.math.BigDecimal;

/**
 * Serviço que o parceiro oferece em troca de créditos (pode ter uma foto do produto).
 * Abstrata: cada tipo (refeição, beleza, bem-estar) calcula o custo e descreve o resgate do seu jeito.
 */
public abstract class Servico extends Entidade implements ComImagem {

    private static final long serialVersionUID = 1L;

    private final String nome;
    private final String descricao;
    private final BigDecimal custoBase;
    private final Parceiro parceiro;
    private boolean ativo = true;
    private String imagem;

    protected Servico(String nome, String descricao, BigDecimal custoBase, Parceiro parceiro) {
        super(Servico.class);
        this.nome = nome;
        this.descricao = descricao;
        this.custoBase = Valor.arredondar(custoBase);
        this.parceiro = parceiro;
    }

    /** Polimorfismo: o custo final depende do tipo de serviço e do nível do voluntário. */
    public abstract BigDecimal calcularCusto(NivelVoluntario nivel);

    /** Texto que aparece no voucher. */
    public abstract String descreverResgate();

    public abstract TipoServico getTipoServico();

    /** custoBase x (1 - desconto), arredondado. Usado pelas subclasses. */
    protected BigDecimal custoComDesconto(BigDecimal desconto) {
        return Valor.arredondar(custoBase.multiply(BigDecimal.ONE.subtract(desconto)));
    }

    @Override
    public String getImagem() {
        return imagem;
    }

    @Override
    public void setImagem(String referencia) {
        this.imagem = referencia;
    }

    @Override
    public Usuario getResponsavel() {
        return parceiro;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getCustoBase() {
        return custoBase;
    }

    public Parceiro getParceiro() {
        return parceiro;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return Formato.texto("Servico #%d - %s (%s) base %s cred | %s", getId(), nome, getTipoServico(),
                Formato.creditos(custoBase), parceiro.getNome());
    }
}
