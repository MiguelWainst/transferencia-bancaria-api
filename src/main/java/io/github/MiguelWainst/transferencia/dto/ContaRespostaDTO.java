package io.github.MiguelWainst.transferencia.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ContaRespostaDTO(
        UUID id,
        String nome,
        BigDecimal saldo
) {
}