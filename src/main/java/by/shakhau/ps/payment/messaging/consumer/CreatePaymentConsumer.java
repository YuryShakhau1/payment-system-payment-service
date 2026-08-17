package by.shakhau.ps.payment.messaging.consumer;

import by.shakhau.ps.payment.client.ExternalPaymentClient;
import by.shakhau.ps.payment.client.dto.PaymentRequest;
import by.shakhau.ps.payment.client.dto.PaymentStatus;
import by.shakhau.ps.payment.client.mapper.PaymentCardDtoMapper;
import by.shakhau.ps.payment.messaging.event.CreatePaymentEvent;
import by.shakhau.ps.payment.messaging.event.PaymentFinishedEvent;
import by.shakhau.ps.payment.messaging.producer.PaymentFinishedProducer;
import by.shakhau.ps.payment.service.Encryptor;
import by.shakhau.ps.payment.service.PaymentService;
import by.shakhau.ps.payment.service.model.Payment;
import by.shakhau.ps.payment.service.model.PaymentCard;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static by.shakhau.ps.payment.repository.entity.PaymentStatus.FAILED;
import static by.shakhau.ps.payment.repository.entity.PaymentStatus.SUCCESS;

@Component
@RequiredArgsConstructor
public class CreatePaymentConsumer {

    private static final String TOPIC = "payment.create";

    private final Encryptor encryptor;
    private final PaymentCardDtoMapper paymentCardDtoMapper;
    private final ExternalPaymentClient externalPaymentClient;
    private final PaymentService paymentService;
    private final PaymentFinishedProducer paymentFinishedProducer;

    @KafkaListener(topics = TOPIC, groupId = "payment-service")
    public void consume(CreatePaymentEvent event, Acknowledgment ack) {
        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .paymentAmount(event.getPaymentAmount())
                .build();

        paymentService.create(payment);

        PaymentCard card = event.getCard();
        card.setNumber(encryptor.decrypt(card.getNumber()));
        card.setHolder(encryptor.decrypt(card.getHolder()));

        PaymentStatus status = externalPaymentClient.processPayment(PaymentRequest.builder()
                .card(paymentCardDtoMapper.toDto(encryptor.encrypt(event.getCvv()), event.getCard()))
                .paymentAmount(event.getPaymentAmount())
                .build());

        if ("SUCCESS".equals(status.getStatus())) {
            payment.setStatus(SUCCESS);
        } else {
            payment.setStatus(FAILED);
        }

        paymentFinishedProducer.send(PaymentFinishedEvent.builder()
                        .paymentId(payment.getId())
                        .paymentId(payment.getOrderId())
                        .paymentStatus(status.getStatus())
                .build());

        ack.acknowledge();
    }
}
