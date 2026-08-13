package by.shakhau.ps.payment.config;

import by.shakhau.ps.payment.repository.converter.converter.PaymentStatusReadingConverter;
import by.shakhau.ps.payment.repository.converter.converter.PaymentStatusWritingConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new PaymentStatusWritingConverter(),
                new PaymentStatusReadingConverter()));
    }
}
