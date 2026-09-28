
package com.example.service;

import com.example.model.Doctor;
import com.example.model.Patient;
import com.example.model.Token;
import com.example.model.TokenStatus;
import com.example.repo.DoctorRepository;
import com.example.repo.PatientRepository;
import com.example.repo.TokenRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public TokenService(TokenRepository tokenRepository,
                        DoctorRepository doctorRepository,
                        PatientRepository patientRepository) {
        this.tokenRepository = tokenRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    // Generate Normal Token
    public Token generateToken(Long doctorId, Long patientId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found with id: " + doctorId));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found with id: " + patientId));

        if (!patient.isActive()) {
            throw new RuntimeException("Cannot generate token for inactive patient");
        }

        LocalDate today = LocalDate.now();

        int nextTokenNumber = tokenRepository
                .findTopByDoctorIdAndTokenDateOrderByTokenNumberDesc(doctorId, today)
                .map(token -> token.getTokenNumber() + 1)
                .orElse(1);

        Token token = new Token();
        token.setTokenNumber(nextTokenNumber);
        token.setTokenDate(today);
        token.setDoctor(doctor);
        token.setPatient(patient);
        token.setPriority(false);
        token.setStatus(TokenStatus.WAITING);
        token.setCreatedAt(LocalDateTime.now());

        return tokenRepository.save(token);
    }

    // Generate Priority Token
    public Token generatePriorityToken(Long doctorId, Long patientId) {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException("Doctor not found with id: " + doctorId));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException("Patient not found with id: " + patientId));

        if (!patient.isActive()) {
            throw new RuntimeException("Cannot generate token for inactive patient");
        }

        LocalDate today = LocalDate.now();

        boolean priorityWaiting = tokenRepository
                .existsByDoctorIdAndTokenDateAndPriorityTrueAndStatus(
                        doctorId, today, TokenStatus.WAITING);

        boolean priorityServing = tokenRepository
                .existsByDoctorIdAndTokenDateAndPriorityTrueAndStatus(
                        doctorId, today, TokenStatus.SERVING);

        if (priorityWaiting || priorityServing) {
            throw new RuntimeException(
                    "An active priority token already exists for this doctor. " +
                    "Only one priority override is allowed at a time.");
        }

        int nextTokenNumber = tokenRepository
                .findTopByDoctorIdAndTokenDateOrderByTokenNumberDesc(doctorId, today)
                .map(token -> token.getTokenNumber() + 1)
                .orElse(1);

        Token token = new Token();
        token.setTokenNumber(nextTokenNumber);
        token.setTokenDate(today);
        token.setDoctor(doctor);
        token.setPatient(patient);
        token.setPriority(true);
        token.setStatus(TokenStatus.WAITING);
        token.setCreatedAt(LocalDateTime.now());

        return tokenRepository.save(token);
    }

    // Get Today's Queue
    public List<Token> getTodayQueue(Long doctorId) {
        return tokenRepository
                .findByDoctorIdAndTokenDateOrderByTokenNumberAsc(
                        doctorId, LocalDate.now());
    }

    // Get Current Serving Token
    public Token getCurrentServing(Long doctorId) {
        return tokenRepository
                .findFirstByDoctorIdAndTokenDateAndStatus(
                        doctorId, LocalDate.now(), TokenStatus.SERVING)
                .orElse(null);
    }

    // Advance Queue
    public Token advanceQueue(Long doctorId) {

        LocalDate today = LocalDate.now();

        Token current = tokenRepository
                .findFirstByDoctorIdAndTokenDateAndStatus(
                        doctorId, today, TokenStatus.SERVING)
                .orElse(null);

        if (current != null) {
            current.setStatus(TokenStatus.COMPLETED);
            tokenRepository.save(current);
        }

        // Priority Token Gets Served First
        Token next = tokenRepository
                .findFirstByDoctorIdAndTokenDateAndPriorityTrueAndStatusOrderByCreatedAtAsc(
                        doctorId, today, TokenStatus.WAITING)
                .orElse(null);

        // Otherwise Serve Normal Token
        if (next == null) {
            next = tokenRepository
                    .findFirstByDoctorIdAndTokenDateAndStatusOrderByTokenNumberAsc(
                            doctorId, today, TokenStatus.WAITING)
                    .orElse(null);
        }

        if (next != null) {
            next.setStatus(TokenStatus.SERVING);
            tokenRepository.save(next);
        }

        return next;
    }

    // Cancel Token
    public Token cancelToken(Long tokenId) {

    Token token = tokenRepository.findById(tokenId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Token not found"
            ));

    if (token.getStatus() == TokenStatus.CANCELLED) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Token is already cancelled"
        );
    }

    if (token.getStatus() == TokenStatus.SERVING) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot cancel a token that is currently being served"
        );
    }

    if (token.getStatus() == TokenStatus.COMPLETED) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot cancel a completed token"
        );
    }

    token.setStatus(TokenStatus.CANCELLED);

    return tokenRepository.save(token);
}

    // Calculate Estimated Wait Time
    public Map<String, Object> getEstimatedWaitTime(Long tokenId) {

        Token token = tokenRepository.findById(tokenId)
                .orElseThrow(() ->
                        new RuntimeException("Token not found with id: " + tokenId));

        Doctor doctor = token.getDoctor();
        LocalDate tokenDate = token.getTokenDate();
        int avgTime = doctor.getAverageConsultationTime();

        Map<String, Object> result = new HashMap<>();

        result.put("tokenNumber", token.getTokenNumber());
        result.put("status", token.getStatus().name());

        if (token.getStatus() == TokenStatus.SERVING) {
            result.put("estimatedWaitMinutes", 0);
            result.put("message", "This token is currently being served.");
            return result;
        }

        if (token.getStatus() == TokenStatus.COMPLETED) {
            result.put("estimatedWaitMinutes", 0);
            result.put("message", "This token has already been completed.");
            return result;
        }

        if (token.getStatus() == TokenStatus.CANCELLED) {
            result.put("estimatedWaitMinutes", 0);
            result.put("message", "This token has been cancelled.");
            return result;
        }

        // Count Tokens Ahead
        List<Token> allTodayTokens = tokenRepository
                .findByDoctorIdAndTokenDateOrderByTokenNumberAsc(
                        doctor.getId(), tokenDate);

        int tokensAhead = 0;

        boolean currentlyServing = allTodayTokens.stream()
                .anyMatch(t -> t.getStatus() == TokenStatus.SERVING);

        if (currentlyServing) {
            tokensAhead++;
        }

        for (Token t : allTodayTokens) {

            if (t.getId().equals(token.getId())) {
                continue;
            }

            if (t.getStatus() != TokenStatus.WAITING) {
                continue;
            }

            if (t.isPriority() && !token.isPriority()) {
                tokensAhead++;
            } else if (t.isPriority() == token.isPriority()) {

                if (t.getTokenNumber() < token.getTokenNumber()) {
                    tokensAhead++;
                }
            }
        }

        int estimatedWaitMinutes = tokensAhead * avgTime;

        result.put("patientName", token.getPatient().getName());
        result.put("doctorName", doctor.getName());
        result.put("priority", token.isPriority());
        result.put("tokensAhead", tokensAhead);
        result.put("estimatedWaitMinutes", estimatedWaitMinutes);

        return result;
    }

    // Get Daily Token History
    public List<Token> getDailyHistory(Long doctorId) {
        return tokenRepository
                .findByDoctorIdAndTokenDateOrderByTokenNumberAsc(
                        doctorId, LocalDate.now());
    }
}