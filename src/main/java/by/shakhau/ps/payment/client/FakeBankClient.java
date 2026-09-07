package by.shakhau.ps.payment.client;

import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.web.bind.annotation.PostMapping;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "fake-bank-client", url = "${app.fake-bank-url:localhost:8085}")
public interface FakeBankClient {

    @Retryable(
            retryFor = { FeignException.class },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 2000,
                    multiplier = 2.0
            )
    )
    @PostMapping(value = "/fake-bank/payments", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    PaymentStatus processPayment(PaymentRequest request);
}
