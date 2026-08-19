package io.github.MiguelWainst.transferencia.controller;

import io.github.MiguelWainst.transferencia.dto.TransacaoDTO;
import io.github.MiguelWainst.transferencia.service.TransacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transacoes")
@RequiredArgsConstructor
public class TransacaoController {

    private final TransacaoService transacaoService;

    @PostMapping()
    @ResponseStatus(HttpStatus.OK)
    public void transferir(@RequestBody @Valid TransacaoDTO dto) {
        transacaoService.transferir(dto);
    }
}
