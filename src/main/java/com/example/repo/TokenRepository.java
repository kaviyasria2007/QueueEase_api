package com.example.repo;

import com.example.model.Token;
import com.example.model.TokenStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findTopByDoctorIdAndTokenDateOrderByTokenNumberDesc(
            Long doctorId, LocalDate tokenDate);

    List<Token> findByDoctorIdAndTokenDateOrderByTokenNumberAsc(
            Long doctorId, LocalDate tokenDate);

    Optional<Token> findFirstByDoctorIdAndTokenDateAndPriorityTrueAndStatusOrderByCreatedAtAsc(
            Long doctorId, LocalDate tokenDate, TokenStatus status);

    Optional<Token> findFirstByDoctorIdAndTokenDateAndStatusOrderByTokenNumberAsc(
            Long doctorId, LocalDate tokenDate, TokenStatus status);

    Optional<Token> findFirstByDoctorIdAndTokenDateAndStatus(
            Long doctorId, LocalDate tokenDate, TokenStatus status);

    long countByDoctorIdAndTokenDateAndStatus(
            Long doctorId, LocalDate tokenDate, TokenStatus status);

    boolean existsByDoctorIdAndTokenDateAndPriorityTrueAndStatus(
            Long doctorId, LocalDate tokenDate, TokenStatus status);
}