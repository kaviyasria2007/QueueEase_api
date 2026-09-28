
package com.example.controller;

import com.example.model.Token;
import com.example.service.TokenService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
public class TokenController {

    private final TokenService tokenService;

    public TokenController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    // POST - Generate Normal Token
    @PostMapping("/{doctorId}/{patientId}")
    public Token generateToken(
            @PathVariable Long doctorId,
            @PathVariable Long patientId) {

        return tokenService.generateToken(doctorId, patientId);
    }

    // POST - Generate Priority Token
    @PostMapping("/priority/{doctorId}/{patientId}")
    public Token generatePriorityToken(
            @PathVariable Long doctorId,
            @PathVariable Long patientId) {

        return tokenService.generatePriorityToken(doctorId, patientId);
    }

    // GET - Today's Queue
    @GetMapping("/doctor/{doctorId}")
    public List<Token> getTodayQueue(
            @PathVariable Long doctorId) {

        return tokenService.getTodayQueue(doctorId);
    }

    // GET - Current Serving Token
    @GetMapping("/current/{doctorId}")
    public Token getCurrentServing(
            @PathVariable Long doctorId) {

        return tokenService.getCurrentServing(doctorId);
    }

    // POST - Advance Queue
    @PostMapping("/next/{doctorId}")
    public Token advanceQueue(
            @PathVariable Long doctorId) {

        return tokenService.advanceQueue(doctorId);
    }

    // GET - Estimated Waiting Time
    @GetMapping("/wait/{tokenId}")
    public Map<String, Object> getEstimatedWaitTime(
            @PathVariable Long tokenId) {

        return tokenService.getEstimatedWaitTime(tokenId);
    }

    // GET - Daily Token History
    @GetMapping("/history/{doctorId}")
    public List<Token> getDailyHistory(
            @PathVariable Long doctorId) {

        return tokenService.getDailyHistory(doctorId);
    }

    // PUT - Cancel Token
    @PutMapping("/cancel/{tokenId}")
    public Token cancelToken(
            @PathVariable Long tokenId) {

        return tokenService.cancelToken(tokenId);
    }
}