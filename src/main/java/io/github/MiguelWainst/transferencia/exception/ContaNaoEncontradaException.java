package io.github.MiguelWainst.transferencia.exception;

public class ContaNaoEncontradaException extends RuntimeException {
    public ContaNaoEncontradaException(String message) {
        super(message);
    }
    public ContaNaoEncontradaException(){
        super("Não foi possível encontrar a(s) conta(s)");
    }
}
