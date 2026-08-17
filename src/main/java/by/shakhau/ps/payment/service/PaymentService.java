package by.shakhau.ps.payment.service;

import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.model.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentService {

    Payment create(Payment payment);
    List<Payment> findByCriteria(UUID userId, UUID orderId, PaymentStatus status);
    List<UserSumProjection> getUserTotalSum(UUID userId, Instant from, Instant to);
    Slice<AdminSumProjection> getTotalSumForAllUsers(Instant from, Instant to, Pageable pageable);
}
