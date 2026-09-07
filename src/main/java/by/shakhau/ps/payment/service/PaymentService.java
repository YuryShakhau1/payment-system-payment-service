package by.shakhau.ps.payment.service;

import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface PaymentService {

    Payment create(Payment payment);
    void update(Payment payment);
    Payment findById(UUID id);
    Payment findByUserIdAndId(UUID userId, UUID id);
    Page<Payment> findByCriteria(
            Instant from, Instant to,
            UUID userId, UUID orderId, PaymentStatus status, Pageable pageable);
    Page<UserSumProjection> getTotalSum(
            Instant from, Instant to, UUID userId, PaymentStatus status, Pageable pageable);
}
