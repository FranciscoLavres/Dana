package com.trocabem.service;

import com.trocabem.model.CategoriaAcao;
import com.trocabem.model.Dificuldade;
import com.trocabem.model.NivelVoluntario;
import com.trocabem.model.Vaga;

import java.util.Locale;

/**
 * Único lugar onde o valor dos créditos é calculado (regra centralizada).
 *
 * valorHoraVaga = min(base(categoria) x mult(dificuldade), teto)      -> congelado na vaga
 * creditos      = horas x min(valorHoraVaga x bonus(nivel), teto)
 */
public class CalculadoraCreditos {

    public static final double TETO_POR_HORA = 40.0;

    /** Valor por hora da vaga, sem o bônus de nível (que depende de quem trabalha). */
    public double calcularValorHoraVaga(CategoriaAcao categoria, Dificuldade dificuldade) {
        double valor = categoria.getValorBaseHora() * dificuldade.getMultiplicador();
        return arredondar(Math.min(valor, TETO_POR_HORA));
    }

    public double calcularCreditos(Vaga vaga, double horas, NivelVoluntario nivel) {
        double valorHora = Math.min(vaga.getValorPorHoraCongelado() * nivel.getBonus(), TETO_POR_HORA);
        return arredondar(horas * valorHora);
    }

    /** Texto explicando a conta (vai para o comprovante de horas depois). */
    public String detalharCalculo(Vaga vaga, double horas, NivelVoluntario nivel) {
        double valorHora = Math.min(vaga.getValorPorHoraCongelado() * nivel.getBonus(), TETO_POR_HORA);
        return String.format(Locale.forLanguageTag("pt-BR"),
                "%.2f h x %.2f cred/h (base %.2f x %s %.1f x nível %s %.2f%s) = %.2f créditos",
                horas, valorHora,
                vaga.getCategoria().getValorBaseHora(),
                vaga.getDificuldade(), vaga.getDificuldade().getMultiplicador(),
                nivel, nivel.getBonus(),
                (vaga.getValorPorHoraCongelado() * nivel.getBonus() > TETO_POR_HORA ? ", limitado pelo teto" : ""),
                calcularCreditos(vaga, horas, nivel));
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
