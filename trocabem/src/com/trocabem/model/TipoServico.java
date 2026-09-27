package com.trocabem.model;

/** Tipos de serviço que o parceiro pode cadastrar (usado na tela e na ServicoFactory). */
public enum TipoServico {
    REFEICAO("Refeição / lanche"),
    BELEZA("Beleza (salão, barbearia, manicure)"),
    BEM_ESTAR("Bem-estar / lazer (spa, cinema)");

    private final String rotulo;

    TipoServico(String rotulo) {
        this.rotulo = rotulo;
    }

    @Override
    public String toString() {
        return rotulo; // é o que aparece no ChoiceBox
    }
}
