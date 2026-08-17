package by.shakhau.ps.payment.client;

import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(
        name = "fake-bank-client",
        url = "${app.fake-bank-url:}")
public interface FakeBankClient extends BankClient {

    @PostMapping(value = "/fake-bank/payments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    PaymentStatus processPayment(PaymentRequest request);
}
