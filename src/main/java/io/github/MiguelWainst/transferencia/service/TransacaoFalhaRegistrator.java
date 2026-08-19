package io.github.MiguelWainst.transferencia.service;

import io.github.MiguelWainst.transferencia.entity.Conta;
import io.github.MiguelWainst.transferencia.entity.Status;
import io.github.MiguelWainst.transferencia.entity.Transacao;
import io.github.MiguelWainst.transferencia.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TransacaoFalhaRegistrator {

    private final TransacaoRepository transacaoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarErroTransacao(BigDecimal valor, Conta contaOrigem, Conta contaDestino) {
        transacaoRepository.save(new Transacao(null, LocalDateTime.now(), valor, Status.FALHOU, contaOrigem, contaDestino));
    }
}
