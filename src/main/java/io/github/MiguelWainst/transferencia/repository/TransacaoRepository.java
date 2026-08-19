package io.github.MiguelWainst.transferencia.repository;

import io.github.MiguelWainst.transferencia.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransacaoRepository extends JpaRepository<Transacao, UUID> {
}
