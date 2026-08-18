package by.shakhau.ps.payment.service.mapper;

import by.shakhau.ps.payment.repository.entity.UserEntity;
import by.shakhau.ps.payment.service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserEntity toEntity(User user);
    User toModel(UserEntity user);
}
