package io.github.MiguelWainst.transferencia.common;

import io.github.MiguelWainst.transferencia.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice()
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResposta handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<ErroCampo> erros = e.getFieldErrors()
                .stream()
                .map(fieldError -> new ErroCampo(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                erros
        );
    }

    @ExceptionHandler(ContaNaoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResposta handleContaNaoEncontradaException(ContaNaoEncontradaException e) {
        return new ErroResposta(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(ContaPropriaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta handleContaPropriaException(ContaPropriaException e) {
        return new ErroResposta(
                HttpStatus.CONFLICT.value(),
                e.getMessage(),
                List.of()
        );
    }

    @ExceptionHandler(SaldoInsuficienteException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResposta handleSaldoInsuficienteException(SaldoInsuficienteException e) {
        return new ErroResposta(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                List.of()
        );
    }



}
