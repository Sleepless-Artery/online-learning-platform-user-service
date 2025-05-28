package org.sleepless_artery.user_service.service;

import org.sleepless_artery.user_service.dto.UserRequestDto;
import org.sleepless_artery.user_service.dto.UserResponseDto;


public interface UserService {

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmailAddress(String emailAddress);

    boolean existsById(Long id);

    UserResponseDto createUser(UserRequestDto user);

    UserResponseDto updateUser(Long id, UserRequestDto user);

    void deleteUserById(Long id);
}
