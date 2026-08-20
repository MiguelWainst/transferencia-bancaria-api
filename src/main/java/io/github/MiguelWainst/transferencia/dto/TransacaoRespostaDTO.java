package io.github.MiguelWainst.transferencia.dto;

import io.github.MiguelWainst.transferencia.entity.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransacaoRespostaDTO(
        UUID id,
        LocalDateTime dataTransacao,
        BigDecimal valor,
        Status status,
        UUID contaOrigemId,
        UUID contaDestinoId
) {
}
