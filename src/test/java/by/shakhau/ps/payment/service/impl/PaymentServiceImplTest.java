package by.shakhau.ps.payment.service.impl;

import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.mapper.PaymentMapper;
import by.shakhau.ps.payment.service.model.Payment;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentMapper mapper;

    @Mock
    private PaymentRepository repository;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;
    private UUID orderId;
    private Instant from;
    private Instant to;
    private BigDecimal amount;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        from = Instant.now().minusSeconds(3600);
        to = Instant.now();
        amount = BigDecimal.valueOf(1500.00);
    }

    @Test
    void shouldPopulateFieldsAndInsertPaymentWhenCreate() {
        Payment inputPayment = Payment.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .build();

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .orderId(orderId)
                .userId(userId)
                .paymentAmount(amount)
                .build();

        PaymentEntity savedEntity = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .status(PaymentStatus.getBeginStatus())
                .createdAt(Instant.now())
                .paymentAmount(amount)
                .build();

        Payment expectedPayment = Payment.builder()
                .id(savedEntity.getId())
                .orderId(orderId)
                .userId(userId)
                .status(savedEntity.getStatus())
                .createdAt(savedEntity.getCreatedAt())
                .paymentAmount(amount)
                .build();

        when(mapper.toEntity(inputPayment)).thenReturn(paymentEntity);
        when(repository.insert(paymentEntity)).thenReturn(savedEntity);
        when(mapper.toModel(savedEntity)).thenReturn(expectedPayment);

        Payment result = paymentService.create(inputPayment);

        assertNotNull(result);
        assertEquals(expectedPayment.getId(), result.getId());

        assertNotNull(inputPayment.getId());
        assertNotNull(inputPayment.getCreatedAt());
        assertEquals(PaymentStatus.getBeginStatus(), inputPayment.getStatus());

        verify(mapper).toEntity(inputPayment);
        verify(repository).insert(paymentEntity);
        verify(mapper).toModel(savedEntity);
    }

    @Test
    void shouldReturnMappedPaymentsListWhenFindByCriteria() {
        PaymentStatus status = PaymentStatus.getBeginStatus();

        var entity = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .paymentAmount(amount)
                .build();

        Payment model = Payment.builder()
                .id(entity.getId())
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .paymentAmount(amount)
                .build();

        when(mongoTemplate.find(any(Query.class), eq(PaymentEntity.class))).thenReturn(List.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);

        List<Payment> result = paymentService.findByCriteria(userId, orderId, status);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(model, result.get(0));

        verify(mongoTemplate).find(any(Query.class), eq(PaymentEntity.class));
        verify(mapper).toModel(entity);
    }

    @Test
    void shouldReturnUserSumProjectionsListWhenGetUserTotalSum() {
        UserSumProjection projection = new UserSumProjection(BigDecimal.valueOf(3500.00));

        AggregationResults<UserSumProjection> aggregationResults =
                new AggregationResults<>(List.of(projection), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEntity.class), eq(UserSumProjection.class)))
                .thenReturn(aggregationResults);

        List<UserSumProjection> result = paymentService.getUserTotalSum(userId, from, to);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(projection.getTotal(), result.get(0).getTotal());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq(PaymentEntity.class), eq(UserSumProjection.class));
    }

    @Test
    void shouldReturnPagedSliceOfAdminSumProjectionsWhenGetTotalSumForAllUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        AdminSumProjection projection = new AdminSumProjection(userId, BigDecimal.valueOf(5000.00));

        AggregationResults<AdminSumProjection> aggregationResults =
                new AggregationResults<>(List.of(projection), new Document());

        when(mongoTemplate.aggregate(any(Aggregation.class), eq(PaymentEntity.class), eq(AdminSumProjection.class)))
                .thenReturn(aggregationResults);

        Slice<AdminSumProjection> result = paymentService.getTotalSumForAllUsers(from, to, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(projection.getTotal(), result.getContent().get(0).getTotal());
        assertFalse(result.hasNext());

        verify(mongoTemplate).aggregate(any(Aggregation.class), eq(PaymentEntity.class), eq(AdminSumProjection.class));
    }
}
