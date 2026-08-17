package by.shakhau.ps.payment.client;

import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;

public interface ExternalPaymentClient {

    PaymentStatus processPayment(PaymentRequest request);
}
