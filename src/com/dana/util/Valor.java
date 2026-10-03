package com.dana.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Créditos são "dinheiro": usamos BigDecimal (e não double) para não acumular erros de arredondamento.
 * Todo valor fica com 2 casas decimais.
 */
public final class Valor {

    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

    private Valor() {
    }

    public static BigDecimal arredondar(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal de(String texto) {
        return arredondar(new BigDecimal(texto));
    }

    public static boolean positivo(BigDecimal valor) {
        return valor != null && valor.signum() > 0;
    }
}
