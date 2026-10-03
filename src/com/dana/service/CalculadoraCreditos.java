package com.dana.service;

import com.dana.model.CategoriaAcao;
import com.dana.model.Dificuldade;
import com.dana.model.NivelVoluntario;
import com.dana.model.Vaga;
import com.dana.util.Formato;
import com.dana.util.Valor;

import java.math.BigDecimal;

/**
 * Único lugar onde o valor dos créditos é calculado (regra centralizada).
 *
 * valorHoraVaga = min(base(categoria) x mult(dificuldade), teto)      -> congelado na vaga
 * creditos      = horas x min(valorHoraVaga x bonus(nivel), teto)
 *
 * As horas confirmadas não podem passar de horasPrevistas x FATOR_MAXIMO_HORAS.
 */
public class CalculadoraCreditos {

    public static final BigDecimal TETO_POR_HORA = Valor.de("40");
    public static final double FATOR_MAXIMO_HORAS = 1.5;

    /** Valor por hora da vaga, sem o bônus de nível (que depende de quem trabalha). */
    public BigDecimal calcularValorHoraVaga(CategoriaAcao categoria, Dificuldade dificuldade) {
        BigDecimal valor = categoria.getValorBaseHora().multiply(dificuldade.getMultiplicador());
        return Valor.arredondar(valor.min(TETO_POR_HORA));
    }

    public BigDecimal valorHoraComBonus(Vaga vaga, NivelVoluntario nivel) {
        BigDecimal valor = vaga.getValorPorHoraCongelado().multiply(nivel.getBonus());
        return Valor.arredondar(valor.min(TETO_POR_HORA));
    }

    public BigDecimal calcularCreditos(Vaga vaga, double horas, NivelVoluntario nivel) {
        return Valor.arredondar(valorHoraComBonus(vaga, nivel).multiply(BigDecimal.valueOf(horas)));
    }

    /** Limite de horas que a instituição pode confirmar numa vaga. */
    public double horasMaximas(Vaga vaga) {
        return vaga.getHorasPrevistas() * FATOR_MAXIMO_HORAS;
    }

    /** Texto explicando a conta (vai para o comprovante de horas). */
    public String detalharCalculo(Vaga vaga, double horas, NivelVoluntario nivel) {
        boolean limitado = vaga.getValorPorHoraCongelado().multiply(nivel.getBonus()).compareTo(TETO_POR_HORA) > 0;
        return Formato.texto("%s h x %s cred/h (base %s x %s %.1f x nível %s %.2f%s) = %s créditos",
                Formato.horas(horas), Formato.creditos(valorHoraComBonus(vaga, nivel)),
                Formato.creditos(vaga.getCategoria().getValorBaseHora()),
                vaga.getDificuldade(), vaga.getDificuldade().getMultiplicador(),
                nivel, nivel.getBonus(),
                limitado ? ", limitado pelo teto" : "",
                Formato.creditos(calcularCreditos(vaga, horas, nivel)));
    }
}
