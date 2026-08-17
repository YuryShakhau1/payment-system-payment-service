package by.shakhau.ps.payment.controller;

import by.shakhau.ps.payment.controller.dto.PaymentResponse;
import by.shakhau.ps.payment.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.payment.controller.mapper.PaymentDtoMapper;
import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentDtoMapper mapper;
    private final PaymentService service;

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentResponse>> findByCriteria(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) PaymentStatus status) {
        return ResponseEntity.ok(service.findByCriteria(userId, orderId, status).stream()
                .map(mapper::toResponse)
                .toList());
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<PaymentResponse>> findByCriteria(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) PaymentStatus status) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(service.findByCriteria(userId, orderId, status).stream()
                .map(mapper::toResponse)
                .toList());
    }

    @GetMapping(value = "/total-sum/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserSumProjection>> totalCurrentUserSum(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(service.getUserTotalSum(userId, from, to));
    }

    @GetMapping(value = "/total-sum", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Slice<AdminSumProjection>> totalSum(
            @RequestParam Instant from, @RequestParam Instant to, Pageable pageable) {
        return ResponseEntity.ok(service.getTotalSumForAllUsers(from, to, pageable));
    }
}
