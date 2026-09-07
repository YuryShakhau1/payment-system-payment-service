package by.shakhau.ps.payment.client.impl;

import by.shakhau.ps.payment.client.ExternalPaymentClient;
import by.shakhau.ps.payment.client.FakeBankClient;
import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FakeSystemBankClientImpl implements ExternalPaymentClient {

    private final FakeBankClient bankClient;

    @Override
    public PaymentStatus processPayment(PaymentRequest request) {
        return bankClient.processPayment(request);
    }
}
