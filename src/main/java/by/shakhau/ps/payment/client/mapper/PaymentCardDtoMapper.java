package by.shakhau.ps.payment.client.mapper;

import by.shakhau.ps.payment.client.dto.PaymentCardFull;
import by.shakhau.ps.payment.service.model.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentCardDtoMapper {

    PaymentCardFull toDto(String cvv, PaymentCard card);
}
