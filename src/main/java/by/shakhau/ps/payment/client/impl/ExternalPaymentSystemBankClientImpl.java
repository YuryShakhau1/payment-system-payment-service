package by.shakhau.ps.payment.client.impl;

import by.shakhau.ps.payment.client.BankClient;
import by.shakhau.ps.payment.client.ExternalPaymentClient;
import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExternalPaymentSystemBankClientImpl implements ExternalPaymentClient {

    private final BankClient bankClient;

    @Override
    public PaymentStatus processPayment(PaymentRequest request) {
        return bankClient.processPayment(request);
    }
}
