package io.github.MiguelWainst.transferencia.service;

import io.github.MiguelWainst.transferencia.dto.ContaCriacaoDTO;
import io.github.MiguelWainst.transferencia.dto.ContaRespostaDTO;
import io.github.MiguelWainst.transferencia.entity.Conta;
import io.github.MiguelWainst.transferencia.repository.ContaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaRespostaDTO criar(ContaCriacaoDTO dto) {
        Conta conta = new Conta();
        conta.setNome(dto.nome());
        conta.setSaldo(dto.saldoInicial());
        conta = contaRepository.save(conta);
        return new ContaRespostaDTO(conta.getId(), conta.getNome(), conta.getSaldo());
    }
}