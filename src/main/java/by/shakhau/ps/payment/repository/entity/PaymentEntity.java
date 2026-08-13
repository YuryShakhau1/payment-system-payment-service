package by.shakhau.ps.payment.repository.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @Field("order_id")
    private UUID orderId;

    @Field("user_id")
    private UUID userId;

    private PaymentStatus status;

    @Field("created_at")
    private Instant createdAt;

    @Field(name = "payment_amount", targetType = FieldType.DECIMAL128)
    private BigDecimal paymentAmount;
}
