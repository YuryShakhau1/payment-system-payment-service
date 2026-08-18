package by.shakhau.ps.payment.messaging.consumer;

import by.shakhau.ps.payment.messaging.event.UserUpdatedEvent;
import by.shakhau.ps.payment.messaging.mapper.UserEventMapper;
import by.shakhau.ps.payment.service.UserService;
import by.shakhau.ps.payment.service.model.User;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UpdateUserConsumer {

    private static final String TOPIC = "user.updated";

    private final UserEventMapper userEventMapper;
    private final UserService userService;

    @KafkaListener(topics = TOPIC, groupId = "payment-service")
    public void consume(UserUpdatedEvent event, Acknowledgment ack) {
        User user = userEventMapper.toUser(event);
        userService.save(user);

        ack.acknowledge();
    }
}
