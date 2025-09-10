package org.sleepless_artery.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.sleepless_artery.user_service.dto.UserResponseDto;
import org.sleepless_artery.user_service.service.UserCacheService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserCacheServiceImpl implements UserCacheService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, Object> redisTemplate;


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
        Cache usersByIdCache = cacheManager.getCache("users-by-id");
        Cache usersByEmailCache = cacheManager.getCache("users-by-email");
        if (usersByIdCache != null) {
            redisTemplate.delete("users-by-id" + "::" + userId);
        }
        if (usersByEmailCache != null) {
            redisTemplate.delete("users-by-email" + "::" + emailAddress);
        }
    }


    @Override
    public boolean existsById(Long userId) {
        Cache userIdCache = cacheManager.getCache("users-by-id");
        if (userIdCache != null) {
            return redisTemplate.hasKey("users-by-id" + "::" + userId);
        }
        return false;
    }
}
