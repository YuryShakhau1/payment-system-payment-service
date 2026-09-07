package by.shakhau.ps.payment.service.impl;

import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.UserService;
import by.shakhau.ps.payment.service.mapper.PaymentMapper;
import by.shakhau.ps.payment.service.model.Payment;
import by.shakhau.ps.payment.service.model.User;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentMapper mapper;

    @Mock
    private PaymentRepository repository;

    @Mock
    private UserService userService;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private UUID userId;
    private UUID orderId;
    private Instant from;
    private Instant to;
    private BigDecimal amount;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        from = Instant.now().minusSeconds(3600);
        to = Instant.now();
        amount = BigDecimal.valueOf(1500.00);
        user = User.builder()
                .id(userId)
                .email("john_doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .build();
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
    void shouldReturnMappedPaymentsPageWhenFindByCriteria() {
        PaymentStatus status = PaymentStatus.getBeginStatus();
        Pageable pageable = PageRequest.of(0, 10);
        Instant from = Instant.now().minus(1, ChronoUnit.DAYS);
        Instant to = Instant.now();

        var entity = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .paymentAmount(amount)
                .build();

        var model = Payment.builder()
                .id(entity.getId())
                .orderId(orderId)
                .userId(userId)
                .status(status)
                .paymentAmount(amount)
                .build();

        when(mongoTemplate.count(any(Query.class), eq(PaymentEntity.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(PaymentEntity.class))).thenReturn(List.of(entity));
        when(mapper.toModel(entity)).thenReturn(model);
        when(userService.fetchById(userId)).thenReturn(user);

        Page<Payment> result = paymentService.findByCriteria(from, to, userId, orderId, status, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        assertEquals(model, result.getContent().getFirst());

        verify(mongoTemplate).count(any(Query.class), eq(PaymentEntity.class));
        verify(mongoTemplate).find(any(Query.class), eq(PaymentEntity.class));
        verify(mapper).toModel(entity);
    }

    @Test
    void shouldReturnUserSumProjectionsListWhenGetUserTotalSum() {
        Pageable pageable = PageRequest.of(0, 10);
        var projection = new UserSumProjection(userId, null, BigDecimal.valueOf(3500.00));

        Document countDoc = new Document("totalRows", 1);
        AggregationResults<Document> countResults =
                new AggregationResults<>(List.of(countDoc), new Document());

        AggregationResults<UserSumProjection> aggregationResults =
                new AggregationResults<>(List.of(projection), new Document());

        doReturn(countResults)
                .doReturn(aggregationResults)
                .when(mongoTemplate).aggregate(any(Aggregation.class), eq(PaymentEntity.class), any());

        when(userService.fetchById(userId)).thenReturn(user);

        Page<UserSumProjection> result = paymentService.getTotalSum(from, to, userId, PaymentStatus.SUCCESS, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(projection.getTotal(), result.getContent().getFirst().getTotal());
        assertEquals(user, result.getContent().getFirst().getUser());

        verify(mongoTemplate, times(2)).aggregate(any(Aggregation.class), eq(PaymentEntity.class), any());
        verify(userService).fetchById(userId);
    }

    @Test
    void shouldReturnPagedSliceOfAdminSumProjectionsWhenGetTotalSum() {
        Pageable pageable = PageRequest.of(0, 10);
        var projection = new UserSumProjection(userId, null, BigDecimal.valueOf(5000.00));

        Document countDoc = new Document("totalRows", 1);
        AggregationResults<Document> countResults =
                new AggregationResults<>(List.of(countDoc), new Document());

        AggregationResults<UserSumProjection> aggregationResults =
                new AggregationResults<>(List.of(projection), new Document());

        doReturn(countResults)
                .doReturn(aggregationResults)
                .when(mongoTemplate).aggregate(any(Aggregation.class), eq(PaymentEntity.class), any());

        when(userService.fetchById(userId)).thenReturn(user);

        Page<UserSumProjection> result = paymentService.getTotalSum(from, to, null, PaymentStatus.SUCCESS, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(1, result.getContent().size());
        assertEquals(projection.getTotal(), result.getContent().getFirst().getTotal());
        assertEquals(user, result.getContent().getFirst().getUser());

        verify(mongoTemplate, times(2)).aggregate(any(Aggregation.class), eq(PaymentEntity.class), any());
        verify(userService).fetchById(userId);
    }
}
