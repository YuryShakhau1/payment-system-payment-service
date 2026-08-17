package by.shakhau.ps.payment.client.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PaymentCardFull {

    private String number;
    private String holder;
    private LocalDate expirationDate;
    private String cvv;
}
