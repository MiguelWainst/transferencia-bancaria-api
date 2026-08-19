package io.github.MiguelWainst.transferencia.service;

import io.github.MiguelWainst.transferencia.dto.TransacaoDTO;
import io.github.MiguelWainst.transferencia.entity.Conta;
import io.github.MiguelWainst.transferencia.repository.ContaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransacaoServiceConcorrenciaTest {

    @Autowired
    private TransacaoService transacaoService;

    @Autowired
    private ContaRepository contaRepository;

    private Conta contaOrigem;
    private Conta contaDestino;

    @BeforeEach
    void criarContasDeTeste() {
        contaOrigem = new Conta();
        contaOrigem.setNome("Origem Teste Concorrencia");
        contaOrigem.setSaldo(new BigDecimal("100.00"));
        contaOrigem = contaRepository.save(contaOrigem);

        contaDestino = new Conta();
        contaDestino.setNome("Destino Teste Concorrencia");
        contaDestino.setSaldo(BigDecimal.ZERO);
        contaDestino = contaRepository.save(contaDestino);
    }

    @AfterEach
    void limparContasDeTeste() {
        contaRepository.delete(contaOrigem);
        contaRepository.delete(contaDestino);
    }

    @Test
    void apenasUmaTransferenciaDeveTerSucessoSobConcorrencia() throws InterruptedException {
        int numeroDeThreads = 50;
        ExecutorService executor = Executors.newFixedThreadPool(numeroDeThreads);
        CountDownLatch largada = new CountDownLatch(1);
        CountDownLatch chegada = new CountDownLatch(numeroDeThreads);
        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);

        TransacaoDTO dto = new TransacaoDTO(contaOrigem.getId(), contaDestino.getId(), new BigDecimal("100.00"));

        for (int i = 0; i < numeroDeThreads; i++) {
            executor.submit(() -> {
                try {
                    largada.await();
                    transacaoService.transferir(dto);
                    sucessos.incrementAndGet();
                } catch (Exception e) {
                    falhas.incrementAndGet();
                } finally {
                    chegada.countDown();
                }
            });
        }

        largada.countDown();
        boolean todasTerminaram = chegada.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(todasTerminaram, "Alguma thread não terminou dentro do tempo limite");

        Conta origemAtualizada = contaRepository.findById(contaOrigem.getId()).orElseThrow();
        Conta destinoAtualizada = contaRepository.findById(contaDestino.getId()).orElseThrow();

        assertEquals(1, sucessos.get());
        assertEquals(numeroDeThreads - 1, falhas.get());
        assertEquals(0, origemAtualizada.getSaldo().compareTo(BigDecimal.ZERO));
        assertEquals(0, destinoAtualizada.getSaldo().compareTo(new BigDecimal("100.00")));
    }
}