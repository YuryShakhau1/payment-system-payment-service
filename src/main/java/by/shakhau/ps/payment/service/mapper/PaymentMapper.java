package by.shakhau.ps.payment.service.mapper;

import by.shakhau.ps.payment.repository.entity.PaymentEntity;
import by.shakhau.ps.payment.service.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    PaymentEntity toEntity(Payment payment);
    Payment toModel(PaymentEntity entity);
}
