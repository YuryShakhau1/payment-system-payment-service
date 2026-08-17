package by.shakhau.ps.payment.repository.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum PaymentStatus {

    NEW(0),
    PENDING(1),
    SUCCESS(2),
    FAILED(3),
    REFUNDED(4);

    private final int value;

    private static final Map<Integer, PaymentStatus> STATUSES = Arrays.stream(values())
            .collect(Collectors.toMap(PaymentStatus::getValue, os -> os));

    public static PaymentStatus fromValue(int value) {
        return STATUSES.get(value);
    }

    public static PaymentStatus getBeginStatus() {
        return NEW;
    }
}
