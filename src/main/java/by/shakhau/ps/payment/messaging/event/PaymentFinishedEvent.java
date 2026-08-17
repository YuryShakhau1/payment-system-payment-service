package by.shakhau.ps.payment.messaging.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Getter
@Setter
public class PaymentFinishedEvent {

    private UUID paymentId;
    private UUID orderId;
    private String paymentStatus;
}
