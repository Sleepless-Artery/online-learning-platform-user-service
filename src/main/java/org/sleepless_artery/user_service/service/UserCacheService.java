package org.sleepless_artery.user_service.service;

import org.sleepless_artery.user_service.dto.UserResponseDto;


public interface UserCacheService {

    void putUserCache(UserResponseDto userResponseDto);

    void evictUserCache(Long userId, String emailAddress);

    boolean existsById(Long userId);
}
