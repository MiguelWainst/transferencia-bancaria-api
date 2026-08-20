package io.github.MiguelWainst.transferencia.service;

import io.github.MiguelWainst.transferencia.dto.TransacaoDTO;
import io.github.MiguelWainst.transferencia.dto.TransacaoRespostaDTO;
import io.github.MiguelWainst.transferencia.entity.Conta;
import io.github.MiguelWainst.transferencia.entity.Status;
import io.github.MiguelWainst.transferencia.entity.Transacao;
import io.github.MiguelWainst.transferencia.exception.ContaNaoEncontradaException;
import io.github.MiguelWainst.transferencia.exception.ContaPropriaException;
import io.github.MiguelWainst.transferencia.exception.SaldoInsuficienteException;
import io.github.MiguelWainst.transferencia.repository.ContaRepository;
import io.github.MiguelWainst.transferencia.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoFalhaRegistrator falhaRegistrator;

    @Transactional
    public void transferir(TransacaoDTO dto) {
        if (dto.contaDestinoId().equals(dto.contaOrigemId())) throw new ContaPropriaException();
        Conta contaDestino;
        Conta contaOrigem;
        if (dto.contaDestinoId().compareTo(dto.contaOrigemId()) < 0) {
            contaDestino = buscarComLock(dto.contaDestinoId());
            contaOrigem = buscarComLock(dto.contaOrigemId());
        } else {
            contaOrigem = buscarComLock(dto.contaOrigemId());
            contaDestino = buscarComLock(dto.contaDestinoId());
        }
        try{
            boolean temSaldo = contaOrigem.getSaldo().compareTo(dto.valor()) >= 0;
            if (!temSaldo) {
                throw new SaldoInsuficienteException("Saldo insuficiente!");
            }
            contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(dto.valor()));
            contaDestino.setSaldo(contaDestino.getSaldo().add(dto.valor()));
            transacaoRepository.save(new Transacao(null, LocalDateTime.now(), dto.valor(), Status.CONCLUIDO, contaOrigem, contaDestino));
        } catch (SaldoInsuficienteException e) {
            falhaRegistrator.registrarErroTransacao(dto.valor(), contaOrigem, contaDestino);
            throw e;
        }
    }

    private Conta buscarComLock(UUID id) {
        return contaRepository.findByIdWithLock(id)
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
    }

    public List<TransacaoRespostaDTO> buscarHistorico(UUID contaId) {
        if (!contaRepository.existsById(contaId)) {
            throw new ContaNaoEncontradaException("Conta não encontrada");
        }
        return transacaoRepository.findByContaId(contaId).stream()
                .map(t -> new TransacaoRespostaDTO(
                        t.getId(),
                        t.getDataTransacao(),
                        t.getValor(),
                        t.getStatus(),
                        t.getContaOrigem().getId(),
                        t.getContaDestino().getId()
                ))
                .toList();
    }
}
