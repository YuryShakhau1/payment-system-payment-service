package by.shakhau.ps.payment.controller;

import by.shakhau.ps.payment.controller.dto.PaymentResponse;
import by.shakhau.ps.payment.controller.filter.AuthenticationFilter.UserPrincipal;
import by.shakhau.ps.payment.controller.mapper.PaymentDtoMapper;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentDtoMapper mapper;
    private final PaymentService service;

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> findPayment(@PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(service.findById(id)));
    }

    @GetMapping(value = "/{id}/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> findCurrentUserPayment(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(mapper.toResponse(service.findByUserIdAndId(principal.getId(), id)));
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PaymentResponse>> findByCriteria(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(service.findByCriteria(
                        from, to, userId, orderId, status, pageable)
                .map(mapper::toResponse));
    }

    @GetMapping(value = "/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<PaymentResponse>> findCurrentUserByCriteria(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        UUID userId = principal.getId();
        return ResponseEntity.ok(service.findByCriteria(
                        from, to, userId, orderId, status, pageable)
                .map(mapper::toResponse));
    }

    @GetMapping(value = "/total-sum/me", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserSumProjection>> totalCurrentUserSum(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(service.getTotalSum(from, to, principal.getId(), status, pageable));
    }

    @GetMapping(value = "/total-sum", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<UserSumProjection>> totalSum(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) PaymentStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(service.getTotalSum(from, to, userId, status, pageable));
    }
}
