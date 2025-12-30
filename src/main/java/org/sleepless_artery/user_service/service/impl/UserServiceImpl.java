package org.sleepless_artery.user_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sleepless_artery.user_service.dto.UserRequestDto;
import org.sleepless_artery.user_service.dto.UserResponseDto;
import org.sleepless_artery.user_service.exception.*;
import org.sleepless_artery.user_service.kafka.producer.KafkaProducer;
import org.sleepless_artery.user_service.mapper.UserMapper;
import org.sleepless_artery.user_service.model.User;
import org.sleepless_artery.user_service.repository.UserRepository;
import org.sleepless_artery.user_service.service.EmailChangeService;
import org.sleepless_artery.user_service.service.UserCacheService;
import org.sleepless_artery.user_service.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EmailChangeService emailChangeService;

    private final UserCacheService userCacheService;

    private final KafkaProducer kafkaProducer;

    @Value("${spring.kafka.topic.prefix}")
    private String prefix;

    @Value("${spring.kafka.topic.domain}")
    private String domain;


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users-by-id", key = "#id")
    public UserResponseDto getUserById(Long id) {
        log.info("Getting user with id: {}", id);

        var user = userRepository.findById(id).orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException();
                }
        );

        return userMapper.toUserResponseDto(user);
    }


    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users-by-email", key = "#emailAddress")
    public UserResponseDto getUserByEmailAddress(String emailAddress) {
        log.info("Getting user with email address: {}", emailAddress);

        var user = userRepository.findByEmailAddress(emailAddress).orElseThrow(() -> {
            log.warn("User not found with email address: {}", emailAddress);
            return new UserNotFoundException();
        });

        return userMapper.toUserResponseDto(user);
    }


    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        log.info("Checking if user with id '{}' exists", id);
        if (userCacheService.existsById(id)) {
            return true;
        }
        return userRepository.existsById(id);
    }


    @Override
    @Transactional
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        log.info("Creating user with email address '{}'", userRequestDto.getEmailAddress());

        if (userRepository.findByEmailAddress(userRequestDto.getEmailAddress()).isPresent()) {
            log.warn("User with email address '{}' already exists", userRequestDto.getEmailAddress());
            throw new UserAlreadyExistsException();
        }

        var userResponseDto = userMapper.toUserResponseDto(
                userRepository.save(userMapper.toUser(userRequestDto))
        );

        userCacheService.putUserCache(userResponseDto);

        return userResponseDto;
    }


    @Override
    @Transactional
    @CachePut(value = "users-by-id", key = "#id")
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        log.info("Updating user with id: {}", id);

        var user = userRepository.findById(id).orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException();
                }
        );

        if (!user.getUsername().equals(userRequestDto.getUsername())) {
            user.setUsername(userRequestDto.getUsername());
        }
        if (user.getInformation() == null || !user.getInformation().equals(userRequestDto.getInformation())) {
            user.setInformation(userRequestDto.getInformation());
        }

        if (userRepository.findByEmailAddress(userRequestDto.getEmailAddress()).isPresent() &&
                !user.getEmailAddress().equals(userRequestDto.getEmailAddress())) {
            log.warn("User with email address '{}' already exists", userRequestDto.getEmailAddress());
            throw new UserAlreadyExistsException();
        }

        emailChangeService.requestEmailChange(user.getId(), user.getEmailAddress(), userRequestDto.getEmailAddress());

        var userResponseDto = userMapper.toUserResponseDto(userRepository.save(user));
        userCacheService.putUserCache(userResponseDto);

        return userResponseDto;
    }


    @Override
    @Transactional
    public void deleteUserById(Long id) {
        log.info("Deleting user with id: {}", id);

        var user = userRepository.findById(id).orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException();
                }
        );

        var emailAddress = user.getEmailAddress();
        userRepository.delete(user);

        userCacheService.evictUserCache(id, emailAddress);

        kafkaProducer.send(
                String.format("%s.%s.%s", prefix, domain, "deleted"), id.toString(), emailAddress
        );
    }
}
