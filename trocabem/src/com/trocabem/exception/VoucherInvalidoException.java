package com.trocabem.exception;

/** Voucher inexistente, já utilizado ou expirado. */
public class VoucherInvalidoException extends TrocaBemException {

    public VoucherInvalidoException(String mensagem) {
        super(mensagem);
    }
}
