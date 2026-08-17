package by.shakhau.ps.payment.service.impl;

import by.shakhau.ps.payment.repository.PaymentRepository;
import by.shakhau.ps.payment.repository.entity.AdminSumProjection;
import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import by.shakhau.ps.payment.repository.entity.UserSumProjection;
import by.shakhau.ps.payment.service.PaymentService;
import by.shakhau.ps.payment.service.mapper.PaymentMapper;
import by.shakhau.ps.payment.service.model.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentMapper mapper;
    private final PaymentRepository repository;

    @Override
    public Payment create(Payment payment) {
        payment.setId(UUID.randomUUID());
        payment.setCreatedAt(Instant.now());
        payment.setStatus(PaymentStatus.getBeginStatus());

        return mapper.toModel(repository.insert(mapper.toEntity(payment)));
    }

    @Override
    public List<Payment> findByCriteria(UUID userId, UUID orderId, PaymentStatus status) {
        return repository.findByCriteria(userId, orderId, status).stream()
                .map(mapper::toModel)
                .toList();
    }

    @Override
    public List<UserSumProjection> getUserTotalSum(UUID userId, Instant from, Instant to) {
        return repository.getUserTotalSum(userId, from, to);
    }

    @Override
    public List<AdminSumProjection> getTotalSumForAllUsers(Instant from, Instant to) {
        return repository.getTotalSumForAllUsers(from, to);
    }
}
