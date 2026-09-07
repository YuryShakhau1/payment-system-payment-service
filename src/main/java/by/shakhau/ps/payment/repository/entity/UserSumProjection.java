package by.shakhau.ps.payment.repository.entity;

import by.shakhau.ps.payment.service.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSumProjection {

    private UUID id;
    private User user;
    private BigDecimal total;
}
