package io.github.MiguelWainst.transferencia.exception;

public class ContaPropriaException extends RuntimeException {
    public ContaPropriaException(String message) {
        super(message);
    }
    public ContaPropriaException() {
        super("Não é permitido transferir dinheiro para a própria conta!");
    }
}
