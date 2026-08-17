package by.shakhau.ps.payment.messaging.producer;

import by.shakhau.ps.payment.messaging.event.PaymentFinishedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentFinishedProducer {

    private static final String TOPIC = "payment.finished";
    private final KafkaTemplate<String, PaymentFinishedEvent> template;

    public void send(PaymentFinishedEvent event) {
        try {
            template.send(TOPIC, event.getPaymentId().toString(), event).get();
        } catch (Exception e) {
            throw new KafkaException(e.getMessage(), e);
        }
    }
}
