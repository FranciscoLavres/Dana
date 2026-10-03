package com.dana.util;

/** Validação e formatação de CPF e CNPJ (cálculo dos dígitos verificadores). */
public final class Documento {

    private static final int[] PESOS_CNPJ_1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    private static final int[] PESOS_CNPJ_2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

    private Documento() {
    }

    /** Remove pontos, traços e barras. */
    public static String digitos(String documento) {
        return documento == null ? "" : documento.replaceAll("\\D", "");
    }

    public static boolean cpfValido(String cpf) {
        String d = digitos(cpf);
        if (d.length() != 11 || todosIguais(d)) {
            return false;
        }
        return digitoCpf(d, 9) == d.charAt(9) - '0' && digitoCpf(d, 10) == d.charAt(10) - '0';
    }

    public static boolean cnpjValido(String cnpj) {
        String d = digitos(cnpj);
        if (d.length() != 14 || todosIguais(d)) {
            return false;
        }
        return digitoCnpj(d, PESOS_CNPJ_1) == d.charAt(12) - '0' && digitoCnpj(d, PESOS_CNPJ_2) == d.charAt(13) - '0';
    }

    public static String formatarCpf(String cpf) {
        String d = digitos(cpf);
        return d.length() != 11 ? cpf
                : d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    public static String formatarCnpj(String cnpj) {
        String d = digitos(cnpj);
        return d.length() != 14 ? cnpj
                : d.substring(0, 2) + "." + d.substring(2, 5) + "." + d.substring(5, 8) + "/" + d.substring(8, 12)
                + "-" + d.substring(12);
    }

    /** Dígito do CPF considerando os "tamanho" primeiros números (pesos decrescentes a partir de tamanho+1). */
    private static int digitoCpf(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = soma * 10 % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digitoCnpj(String d, int[] pesos) {
        int soma = 0;
        for (int i = 0; i < pesos.length; i++) {
            soma += (d.charAt(i) - '0') * pesos[i];
        }
        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosIguais(String d) {
        return d.chars().allMatch(c -> c == d.charAt(0));
    }
}
