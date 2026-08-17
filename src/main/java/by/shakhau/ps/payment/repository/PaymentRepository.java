package by.shakhau.ps.payment.repository;

import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends MongoRepository<PaymentEntity, UUID> {

    @Query("{ $or: [ " +
            "  :userId != null ? { 'user_id': :userId } : { '_id': null }, " +
            "  :orderId != null ? { 'order_id': :orderId } : { '_id': null }, " +
            "  :status != null ? { 'status': :status } : { '_id': null } " +
            "] }")
    List<PaymentEntity> findByCriteria(UUID userId, UUID orderId, PaymentStatus status);

    @Aggregation(pipeline = {
            "{ $match: { 'user_id': :userId, 'created_at': { $gte: :from, $lte: :to } } }",
            "{ $group: { _id: null, total: { $sum: '$payment_amount' } } }"})
    List<UserSumProjection> getUserTotalSum(UUID userId, Instant from, Instant to);

    @Aggregation(pipeline = {
            "{ $match: { 'created_at': { $gte: :from, $lte: :to } } }",
            "{ $group: { _id: '$user_id', total: { $sum: '$payment_amount' } } }"})
    Slice<AdminSumProjection> getTotalSumForAllUsers(Instant from, Instant to, Pageable pageable);
}
