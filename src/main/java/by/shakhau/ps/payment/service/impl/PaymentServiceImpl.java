package by.shakhau.ps.payment.service.impl;

import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.PaymentService;
import by.shakhau.ps.payment.service.mapper.PaymentMapper;
import by.shakhau.ps.payment.service.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper mapper;
    private final PaymentRepository repository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Payment create(Payment payment) {
        payment.setId(UUID.randomUUID());
        payment.setCreatedAt(Instant.now());
        payment.setStatus(PaymentStatus.getBeginStatus());

        return mapper.toModel(repository.insert(mapper.toEntity(payment)));
    }

    @Override
    public List<Payment> findByCriteria(UUID userId, UUID orderId, PaymentStatus status) {
        Query query = new Query();
        List<Criteria> criteriaList = new java.util.ArrayList<>();

        if (userId != null) {
            criteriaList.add(Criteria.where("userId").is(userId));
        }
        if (orderId != null) {
            criteriaList.add(Criteria.where("orderId").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
        }

        return mongoTemplate.find(query, PaymentEntity.class).stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public List<UserSumProjection> getUserTotalSum(UUID userId, Instant from, Instant to) {
        var matchStage = Aggregation.match(
                Criteria.where("userId").is(userId).and("createdAt").gte(from).lte(to));

        var groupStage = Aggregation.group().sum("paymentAmount").as("total");

        Aggregation aggregation = Aggregation.newAggregation(matchStage, groupStage);

        AggregationResults<UserSumProjection> results = mongoTemplate.aggregate(
                aggregation,
                PaymentEntity.class,
                UserSumProjection.class);

        return results.getMappedResults();
    }

    @Override
    public Slice<AdminSumProjection> getTotalSumForAllUsers(Instant from, Instant to, Pageable pageable) {
        var matchStage = Aggregation.match(Criteria.where("createdAt").gte(from).lte(to));

        var groupStage = Aggregation.group("userId")
                .sum("paymentAmount").as("total");

        long skip = pageable.getOffset();
        long limit = pageable.getPageSize() + 1L;

        var skipStage = Aggregation.skip(skip);
        var limitStage = Aggregation.limit(limit);

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                groupStage,
                skipStage,
                limitStage);

        AggregationResults<AdminSumProjection> results = mongoTemplate.aggregate(
                aggregation, PaymentEntity.class, AdminSumProjection.class);

        List<AdminSumProjection> content = results.getMappedResults();

        boolean hasNext = content.size() > pageable.getPageSize();

        List<AdminSumProjection> finalContent = content.stream()
                .limit(pageable.getPageSize())
                .toList();

        return new SliceImpl<>(finalContent, pageable, hasNext);
    }
}
