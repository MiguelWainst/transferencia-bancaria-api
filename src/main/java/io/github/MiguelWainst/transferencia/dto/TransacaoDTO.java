package io.github.MiguelWainst.transferencia.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record TransacaoDTO(
        @NotNull(message = "Campo obrigatório")
        UUID contaOrigemId,
        @NotNull(message = "Campo obrigatório")
        UUID contaDestinoId,
        @NotNull(message = "Campo obrigatório")
        @DecimalMin(value = "0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal valor
) {
    @AssertTrue(message = "Não é permitido fazer uma transferênica para a própria conta")
    private boolean isIdDiferente() {
        return contaDestinoId.compareTo(contaOrigemId) != 0;
    }
}
