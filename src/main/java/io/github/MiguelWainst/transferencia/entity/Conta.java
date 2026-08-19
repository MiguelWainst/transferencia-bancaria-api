package io.github.MiguelWainst.transferencia.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "contas")
@Getter
@Setter
@NoArgsConstructor
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "saldo")
    private BigDecimal saldo;
}
