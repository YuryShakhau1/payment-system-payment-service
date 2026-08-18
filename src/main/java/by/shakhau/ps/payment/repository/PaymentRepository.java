package by.shakhau.ps.payment.repository;

import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends MongoRepository<PaymentEntity, UUID> {
    Optional<PaymentEntity> findByUserIdAndId(UUID userId, UUID id);
}
