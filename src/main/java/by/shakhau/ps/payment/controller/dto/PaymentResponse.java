package by.shakhau.ps.payment.controller.dto;

import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PaymentResponse {

    private UUID id;
    private UUID orderId;
    private UUID userId;
    private PaymentStatus status;
    private Instant createdAt;
    private BigDecimal paymentAmount;
}
