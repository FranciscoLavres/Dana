package com.dana.model;

import com.dana.exception.OperacaoInvalidaException;
import com.dana.exception.VoucherInvalidoException;
import com.dana.util.Formato;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Troca de créditos por um serviço. O código do voucher é único e tem validade.
 * O voluntário pode agendar quando vai usar, cancelar (com estorno) e, depois de usar, avaliar.
 */
public class Resgate extends Entidade {

    private static final long serialVersionUID = 1L;
    public static final int DIAS_VALIDADE = 30;

    private final String codigoVoucher;
    private final Voluntario voluntario;
    private final Servico servico;
    private final BigDecimal custoFinal;
    private final LocalDateTime dataResgate = LocalDateTime.now();
    private final LocalDate validade = LocalDate.now().plusDays(DIAS_VALIDADE);
    private StatusResgate status = StatusResgate.GERADO;
    private LocalDateTime agendadoPara;
    private Avaliacao avaliacao;

    public Resgate(String codigoVoucher, Voluntario voluntario, Servico servico, BigDecimal custoFinal) {
        super(Resgate.class);
        this.codigoVoucher = codigoVoucher;
        this.voluntario = voluntario;
        this.servico = servico;
        this.custoFinal = custoFinal;
    }

    /** Marca o voucher como usado. Usado, cancelado ou expirado não pode ser reutilizado. */
    public void utilizar() throws VoucherInvalidoException {
        switch (getStatus()) {
            case UTILIZADO -> throw new VoucherInvalidoException("Voucher " + codigoVoucher + " já foi utilizado.");
            case CANCELADO -> throw new VoucherInvalidoException("Voucher " + codigoVoucher + " foi cancelado.");
            case EXPIRADO -> throw new VoucherInvalidoException("Voucher " + codigoVoucher + " está expirado.");
            default -> status = StatusResgate.UTILIZADO;
        }
    }

    public void cancelar() throws OperacaoInvalidaException {
        exigirValido("cancelar");
        status = StatusResgate.CANCELADO;
    }

    public void agendar(LocalDateTime dataHora) throws OperacaoInvalidaException {
        exigirValido("agendar");
        if (dataHora == null || !dataHora.isAfter(LocalDateTime.now())) {
            throw new OperacaoInvalidaException("Escolha uma data e um horário futuros.");
        }
        if (dataHora.toLocalDate().isAfter(validade)) {
            throw new OperacaoInvalidaException(
                    "O agendamento precisa ser até " + Formato.data(validade) + " (validade do voucher).");
        }
        agendadoPara = dataHora;
    }

    public void registrarAvaliacao(Avaliacao avaliacao) throws OperacaoInvalidaException {
        if (status != StatusResgate.UTILIZADO) {
            throw new OperacaoInvalidaException("Só é possível avaliar depois de usar o voucher.");
        }
        if (this.avaliacao != null) {
            throw new OperacaoInvalidaException("Você já avaliou este resgate.");
        }
        this.avaliacao = avaliacao;
    }

    private void exigirValido(String acao) throws OperacaoInvalidaException {
        if (getStatus() != StatusResgate.GERADO) {
            throw new OperacaoInvalidaException(
                    "Só é possível " + acao + " vouchers válidos que ainda não foram usados (status atual: "
                            + getStatus() + ").");
        }
    }

    /** A expiração é calculada na hora: um voucher GERADO vencido aparece como EXPIRADO. */
    public StatusResgate getStatus() {
        if (status == StatusResgate.GERADO && LocalDate.now().isAfter(validade)) {
            return StatusResgate.EXPIRADO;
        }
        return status;
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

    public BigDecimal getCustoFinal() {
        return custoFinal;
    }

    public LocalDateTime getDataResgate() {
        return dataResgate;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public LocalDateTime getAgendadoPara() {
        return agendadoPara;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    @Override
    public String toString() {
        return Formato.texto("Voucher %s | %s | %s cred | válido até %s | %s",
                codigoVoucher, servico.getNome(), Formato.creditos(custoFinal), Formato.data(validade), getStatus());
    }
}
