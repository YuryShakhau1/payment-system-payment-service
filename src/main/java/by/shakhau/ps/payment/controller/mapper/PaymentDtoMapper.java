package by.shakhau.ps.payment.controller.mapper;

import by.shakhau.ps.payment.controller.dto.PaymentResponse;
import by.shakhau.ps.payment.service.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentDtoMapper {

    PaymentResponse toResponse(Payment payment);
}
