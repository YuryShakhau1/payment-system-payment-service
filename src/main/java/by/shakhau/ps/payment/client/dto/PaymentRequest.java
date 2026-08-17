package by.shakhau.ps.payment.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
public class PaymentRequest {

    private PaymentCardFull card;
    private BigDecimal paymentAmount;
}
