package by.shakhau.ps.payment.repository.entity;

import java.math.BigDecimal;
import java.util.UUID;

public record AdminSumProjection(UUID id, BigDecimal total) {}
