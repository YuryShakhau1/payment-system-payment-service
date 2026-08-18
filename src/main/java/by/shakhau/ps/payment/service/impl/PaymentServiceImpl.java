package by.shakhau.ps.payment.service.impl;

import by.shakhau.ps.payment.exception.ResourceNotFoundException;
import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.PaymentService;
import by.shakhau.ps.payment.service.UserService;
import by.shakhau.ps.payment.service.mapper.PaymentMapper;
import by.shakhau.ps.payment.service.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    @Override
    public Payment create(Payment payment) {
        payment.setId(UUID.randomUUID());
        payment.setCreatedAt(Instant.now());
        payment.setStatus(PaymentStatus.getBeginStatus());

        return mapper.toModel(repository.insert(mapper.toEntity(payment)));
    }

    @Override
    public void update(Payment payment) {
        repository.save(mapper.toEntity(payment));
    }

    @Override
    public Payment findById(UUID id) {
        return repository.findById(id)
                .map(mapper::toModel)
                .map(this::fillUser)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public Payment findByUserIdAndId(UUID userId, UUID id) {
        return repository.findByUserIdAndId(userId, id)
                .map(mapper::toModel)
                .map(this::fillUser)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Override
    public Page<Payment> findByCriteria(
            Instant from, Instant to,
            UUID userId, UUID orderId, PaymentStatus status, Pageable pageable) {
        Query query = new Query();
        List<Criteria> criteriaList = new java.util.ArrayList<>();

        if (from != null && to != null) {
            criteriaList.add(Criteria.where("created_at").gte(from).lte(to));
        } else {
            if (from != null) {
                criteriaList.add(Criteria.where("created_at").gte(from));
            }
            if (to != null) {
                criteriaList.add(Criteria.where("created_at").lte(to));
            }
        }

        if (userId != null) {
            criteriaList.add(Criteria.where("user_id").is(userId));
        }
        if (orderId != null) {
            criteriaList.add(Criteria.where("order_id").is(orderId));
        }
        if (status != null) {
            criteriaList.add(Criteria.where("status").is(status));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long total = mongoTemplate.count(query, PaymentEntity.class);
        query.with(pageable);

        List<Payment> payments = mongoTemplate.find(query, PaymentEntity.class)
                .stream()
                .map(mapper::toModel)
                .map(this::fillUser)
                .toList();

        return new PageImpl<>(payments, pageable, total);
    }

    @Override
    public Page<UserSumProjection> getTotalSum(
            Instant from, Instant to, UUID userId, PaymentStatus status, Pageable pageable) {
        Criteria criteria = Criteria.where("createdAt").gte(from).lte(to);
        if (userId != null) {
            criteria.and("userId").is(userId);
        }
        if (status != null) {
            criteria.and("status").is(status);
        }

        var matchStage = Aggregation.match(criteria);
        var groupStage = Aggregation.group("userId")
                .sum("paymentAmount").as("total");

        var countStage = Aggregation.count().as("totalRows");
        Aggregation countAggregation = Aggregation.newAggregation(matchStage, groupStage, countStage);
        AggregationResults<org.bson.Document> countResults = mongoTemplate.aggregate(
                countAggregation, PaymentEntity.class, org.bson.Document.class);

        long totalElements = countResults.getUniqueMappedResult() != null
                ? ((Number) countResults.getUniqueMappedResult().get("totalRows")).longValue()
                : 0L;

        var skipStage = Aggregation.skip(pageable.getOffset());
        var limitStage = Aggregation.limit(pageable.getPageSize());

        Aggregation aggregation = Aggregation.newAggregation(
                matchStage,
                groupStage,
                skipStage,
                limitStage);

        AggregationResults<UserSumProjection> results = mongoTemplate.aggregate(
                aggregation, PaymentEntity.class, UserSumProjection.class);

        List<UserSumProjection> finalContent = results.getMappedResults().stream()
                .map(this::fillUser)
                .toList();

        return new PageImpl<>(finalContent, pageable, totalElements);
    }

    private Payment fillUser(Payment payment) {
        payment.setUser(userService.fetchById(payment.getUserId()));
        return payment;
    }

    private UserSumProjection fillUser(UserSumProjection projection) {
        projection.setUser(userService.fetchById(projection.getId()));
        return projection;
    }
}
