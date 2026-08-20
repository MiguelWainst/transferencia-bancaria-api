package io.github.MiguelWainst.transferencia.controller;

import io.github.MiguelWainst.transferencia.dto.ContaCriacaoDTO;
import io.github.MiguelWainst.transferencia.dto.ContaRespostaDTO;
import io.github.MiguelWainst.transferencia.dto.TransacaoRespostaDTO;
import io.github.MiguelWainst.transferencia.service.ContaService;
import io.github.MiguelWainst.transferencia.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;
    private final TransacaoService transacaoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaRespostaDTO criar(@RequestBody @Valid ContaCriacaoDTO dto) {
        return contaService.criar(dto);
    }

    @GetMapping("/{id}")
    public ContaRespostaDTO buscarPorId(@PathVariable UUID id) {
        return contaService.buscarPorId(id);
    }

    @GetMapping("/{id}/transacoes")
    public List<TransacaoRespostaDTO> buscarHistorico(@PathVariable UUID id) {
        return transacaoService.buscarHistorico(id);
    }
}