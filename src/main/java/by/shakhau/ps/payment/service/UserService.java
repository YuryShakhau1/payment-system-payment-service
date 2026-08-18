package by.shakhau.ps.payment.service;

import by.shakhau.ps.payment.service.model.User;

import java.util.UUID;

public interface UserService {

    User fetchById(UUID id);
    void save(User user);
}
