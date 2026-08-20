package io.github.MiguelWainst.transferencia.repository;

import io.github.MiguelWainst.transferencia.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {

    @Query("SELECT t FROM Transacao t " +
            "JOIN FETCH t.contaOrigem " +
            "JOIN FETCH t.contaDestino " +
            "WHERE t.contaOrigem.id = :contaId OR t.contaDestino.id = :contaId " +
            "ORDER BY t.dataTransacao DESC")
    List<Transacao> findByContaId(@Param("contaId") UUID contaId);
}
