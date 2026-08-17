package by.shakhau.ps.payment.config;

import by.shakhau.ps.payment.client.ExternalPaymentClient;
import by.shakhau.ps.payment.client.FakeBankClient;
import by.shakhau.ps.payment.client.impl.ExternalPaymentSystemBankClientImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(clients = FakeBankClient.class)
@ConditionalOnProperty(name = "app.fake-bank-url")
public class FeignConditionalConfig {

    @Bean
    @ConditionalOnProperty(name = "app.fake-bank-url")
    public ExternalPaymentClient fakeExternalPaymentClient(
            ObjectProvider<FakeBankClient> fakeBankClientProvider) {
        FakeBankClient fakeBankClient = fakeBankClientProvider.getObject();
        return new ExternalPaymentSystemBankClientImpl(fakeBankClient);
    }
}
