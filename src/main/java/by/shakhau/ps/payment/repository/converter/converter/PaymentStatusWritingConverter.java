package by.shakhau.ps.payment.repository.converter.converter;

import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PaymentStatusWritingConverter implements Converter<PaymentStatus, Integer> {

    @Override
    public Integer convert(PaymentStatus source) {
        return source.getValue();
    }
}
