package org.sleepless_artery.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.sleepless_artery.user_service.dto.UserResponseDto;
import org.sleepless_artery.user_service.service.UserCacheService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheManager cacheManager;


    @Override
    public void putUserCache(UserResponseDto userResponseDto) {
        Cache usersByIdCache = cacheManager.getCache("users-by-id");
        Cache usersByEmailCache = cacheManager.getCache("users-by-email");
        if (usersByIdCache != null) {
            usersByIdCache.put(userResponseDto.getId(), userResponseDto);
        }
        if (usersByEmailCache != null) {
            usersByEmailCache.put(userResponseDto.getEmailAddress(), userResponseDto);
        }
    }


    @Override
    public void evictUserCache(Long userId, String emailAddress) {
        Cache userIdCache = cacheManager.getCache("users-by-id");
        Cache emailAddressCache = cacheManager.getCache("users-by-email");
        if (userIdCache != null) {
            userIdCache.evict(userId);
        }
        if (emailAddressCache != null) {
            emailAddressCache.evict(emailAddress);
        }
    }


    @Override
    public boolean existsById(Long userId) {
        Cache userIdCache = cacheManager.getCache("users-by-id");
        if (userIdCache != null) {
            return userIdCache.get(userId) != null;
        }
        return false;
    }
}
