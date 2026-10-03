package com.dana.exception;

/** Voucher inexistente, já utilizado ou expirado. */
public class VoucherInvalidoException extends DanaException {

    public VoucherInvalidoException(String mensagem) {
        super(mensagem);
    }
}
