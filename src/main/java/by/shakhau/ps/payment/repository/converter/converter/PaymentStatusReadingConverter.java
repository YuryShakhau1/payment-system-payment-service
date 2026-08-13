package by.shakhau.ps.payment.repository.converter.converter;


import by.shakhau.ps.payment.repository.entity.PaymentStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class PaymentStatusReadingConverter implements Converter<Integer, PaymentStatus> {

    @Override
    public PaymentStatus convert(Integer source) {
        return PaymentStatus.fromValue(source);
    }
}
