package io.github.MiguelWainst.transferencia.exception;

import java.util.List;

public record ErroResposta(int status, String mensagem, List<ErroCampo> erroCampo) {
}
