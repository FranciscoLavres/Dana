package com.dana.model;

/**
 * Nível do voluntário, definido pelas horas acumuladas.
 * bonus = multiplicador nos créditos ganhos; desconto = desconto nos resgates.
 */
public enum NivelVoluntario {
    BRONZE(0, 1.00, 0.00),
    PRATA(20, 1.05, 0.05),
    OURO(50, 1.10, 0.10),
    DIAMANTE(100, 1.20, 0.15);

    private final int horasMinimas;
    private final double bonus;
    private final double desconto;

    NivelVoluntario(int horasMinimas, double bonus, double desconto) {
        this.horasMinimas = horasMinimas;
        this.bonus = bonus;
        this.desconto = desconto;
    }

    public int getHorasMinimas() {
        return horasMinimas;
    }

    public double getBonus() {
        return bonus;
    }

    public double getDesconto() {
        return desconto;
    }

    /** Descobre o nível a partir do total de horas (percorre do maior para o menor). */
    public static NivelVoluntario porHoras(double horas) {
        NivelVoluntario[] niveis = values();
        for (int i = niveis.length - 1; i >= 0; i--) {
            if (horas >= niveis[i].horasMinimas) {
                return niveis[i];
            }
        }
        return BRONZE;
    }
}
