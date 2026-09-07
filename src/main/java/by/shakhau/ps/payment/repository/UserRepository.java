package by.shakhau.ps.payment.repository;

import by.shakhau.ps.payment.repository.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface UserRepository extends MongoRepository<UserEntity, UUID> {
}
