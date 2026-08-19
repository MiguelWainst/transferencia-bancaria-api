package io.github.MiguelWainst.transferencia.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ContaCriacaoDTO(
        @NotBlank(message = "Campo obrigatório")
        String nome,

        @NotNull(message = "Campo obrigatório")
        @DecimalMin(value = "0.00", message = "Saldo inicial não pode ser negativo")
        @Digits(integer = 10, fraction = 2)
        BigDecimal saldoInicial
) {
}