package by.shakhau.ps.payment.integration;

import by.shakhau.ps.payment.controller.filter.AuthenticationFilter;
import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PaymentControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;

    @Container
    private static final MongoDBContainer mongoDB = new MongoDBContainer("mongo:8.0");

    static {
        mongoDB.start();
    }

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDB::getReplicaSetUrl);
    }

    private UUID userId;
    private UUID sessionId;
    private UUID orderId;
    private AuthenticationFilter.UserPrincipal userPrincipal;
    private Instant now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sessionId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        userPrincipal = new AuthenticationFilter.UserPrincipal(userId, sessionId);
        now = Instant.now();

        paymentRepository.deleteAll();
    }

    @Test
    void shouldReturnPaymentsListWhenFindByCriteria() throws Exception {
        var payment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderId(orderId)
                .status(PaymentStatus.getBeginStatus())
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(500.00))
                .build();
        paymentRepository.save(payment);

        mockMvc.perform(get("/payments")
                        .param("userId", userId.toString())
                        .param("orderId", orderId.toString())
                        .param("status", PaymentStatus.getBeginStatus().name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].orderId").value(orderId.toString()))
                .andExpect(jsonPath("$[0].status").value(PaymentStatus.getBeginStatus().name()));
    }

    @Test
    void shouldReturnCurrentUsersPaymentsWhenFindByCriteriaMe() throws Exception {
        PaymentEntity payment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderId(orderId)
                .status(PaymentStatus.getBeginStatus())
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(1200.00))
                .build();

        PaymentEntity otherPayment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .status(PaymentStatus.getBeginStatus())
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(300.00))
                .build();

        paymentRepository.saveAll(java.util.List.of(payment, otherPayment));

        mockMvc.perform(get("/payments/me")
                        .param("status", PaymentStatus.getBeginStatus().name())
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].paymentAmount").value(1200.00))
                .andExpect(jsonPath("$[1].paymentAmount").value(300.00));
    }

    @Test
    void shouldReturnAggregatedUserTotalSumWhenTotalCurrentUserSum() throws Exception {
        Instant from = now.minusSeconds(3600);
        Instant to = now.plusSeconds(3600);

        PaymentEntity firstPayment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(100.50))
                .build();

        PaymentEntity secondPayment = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(200.50))
                .build();

        paymentRepository.saveAll(List.of(firstPayment, secondPayment));

        mockMvc.perform(get("/payments/total-sum/me")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .with(SecurityMockMvcRequestPostProcessors.user(userPrincipal))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].total").value(301.00));
    }

    @Test
    void shouldReturnPagedSliceOfAdminSumProjectionsWhenTotalSum() throws Exception {
        Instant from = now.minusSeconds(3600);
        Instant to = now.plusSeconds(3600);
        UUID anotherUserId = UUID.randomUUID();

        PaymentEntity payment1 = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(1000.00))
                .build();

        PaymentEntity payment2 = PaymentEntity.builder()
                .id(UUID.randomUUID())
                .userId(anotherUserId)
                .createdAt(now)
                .paymentAmount(BigDecimal.valueOf(2000.00))
                .build();

        paymentRepository.saveAll(List.of(payment1, payment2));

        mockMvc.perform(get("/payments/total-sum")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.last").value(false));
    }
}
