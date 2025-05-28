package org.sleepless_artery.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sleepless_artery.user_service.exception.EmailAddressAlreadyExistsException;
import org.sleepless_artery.user_service.exception.UserNotFoundException;
import org.sleepless_artery.user_service.grpc.client.EmailVerificationServiceGrpcClient;
import org.sleepless_artery.user_service.mapper.UserMapper;
import org.sleepless_artery.user_service.model.User;
import org.sleepless_artery.user_service.repository.UserRepository;
import org.sleepless_artery.user_service.service.EmailChangeService;
import org.sleepless_artery.user_service.service.UserCacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailChangeServiceImpl implements EmailChangeService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserCacheService userCacheService;

    private final RedisTemplate<String, String> redisTemplate;
    private final EmailVerificationServiceGrpcClient grpcClient;


    @Override
    @Transactional
    public void requestEmailChange(Long userId, String oldEmailAddress, String newEmailAddress)
            throws EmailAddressAlreadyExistsException
    {
        log.info("Starting email change process");

        if (oldEmailAddress != null && oldEmailAddress.equals(newEmailAddress)) {
            log.info("Email address was not changed");
            return;
        }

        if (!grpcClient.changeEmailAddress(oldEmailAddress, newEmailAddress)) {
            log.warn("Email address '{}' is already occupied", newEmailAddress);
            throw new EmailAddressAlreadyExistsException();
        }

        Boolean reserved = redisTemplate.opsForValue().setIfAbsent(
                "email_change_request:" + newEmailAddress,
                userId.toString(),
                Duration.ofMinutes(60)
        );

        if (Boolean.FALSE.equals(reserved)) {
            log.warn("Account with email address '{}' is awaiting confirmation", newEmailAddress);
            throw new EmailAddressAlreadyExistsException();
        }
    }


    @Override
    @Transactional
    public void confirmEmailAddressChange(String oldEmailAddress, String newEmailAddress) {
        log.info("Changing email address to '{}'", newEmailAddress);

        User user = userRepository.findByEmailAddress(oldEmailAddress).orElseThrow(() -> {
            log.warn("User not found with email address: {}", oldEmailAddress);
            return new UserNotFoundException();
        });

        user.setEmailAddress(newEmailAddress);
        userRepository.save(user);

        userCacheService.evictUserCache(user.getId(), oldEmailAddress);
        userCacheService.putUserCache(userMapper.toUserResponseDto(user));
    }
}
