package io.github.MiguelWainst.transferencia.controller;

import io.github.MiguelWainst.transferencia.dto.ContaCriacaoDTO;
import io.github.MiguelWainst.transferencia.dto.ContaRespostaDTO;
import io.github.MiguelWainst.transferencia.service.ContaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/contas")
@RequiredArgsConstructor
public class ContaController {

    private final ContaService contaService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ContaRespostaDTO criar(@RequestBody @Valid ContaCriacaoDTO dto) {
        return contaService.criar(dto);
    }
}