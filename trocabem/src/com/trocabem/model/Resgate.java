package com.trocabem.model;

import com.trocabem.exception.VoucherInvalidoException;

import java.time.LocalDate;
import java.util.UUID;

/** Troca de créditos por um serviço. O código do voucher é único e tem validade. */
public class Resgate {

    private final String codigoVoucher = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    private final Voluntario voluntario;
    private final Servico servico;
    private final double custoFinal;
    private final LocalDate validade = LocalDate.now().plusDays(30);
    private StatusResgate status = StatusResgate.GERADO;

    public Resgate(Voluntario voluntario, Servico servico, double custoFinal) {
        this.voluntario = voluntario;
        this.servico = servico;
        this.custoFinal = custoFinal;
    }

    /** Marca o voucher como usado. Usado ou expirado não pode ser reutilizado. */
    public void utilizar() throws VoucherInvalidoException {
        if (status == StatusResgate.UTILIZADO) {
            throw new VoucherInvalidoException("Voucher " + codigoVoucher + " já foi utilizado.");
        }
        if (LocalDate.now().isAfter(validade)) {
            status = StatusResgate.EXPIRADO;
        }
        if (status == StatusResgate.EXPIRADO) {
            throw new VoucherInvalidoException("Voucher " + codigoVoucher + " está expirado.");
        }
        status = StatusResgate.UTILIZADO;
    }

    public String getCodigoVoucher() {
        return codigoVoucher;
    }

    public Voluntario getVoluntario() {
        return voluntario;
    }

    public Servico getServico() {
        return servico;
    }

    public double getCustoFinal() {
        return custoFinal;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public StatusResgate getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return String.format("Voucher %s | %s | %.2f cred | válido até %s | %s",
                codigoVoucher, servico.getNome(), custoFinal, validade, status);
    }
}
