package org.sleepless_artery.user_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.sleepless_artery.user_service.dto.UserRequestDto;
import org.sleepless_artery.user_service.dto.UserResponseDto;
import org.sleepless_artery.user_service.service.UserService;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@Validated
@RefreshScope
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> getUserById(@PathVariable Long id) {
        UserResponseDto userResponseDto = userService.getUserById(id);
        return ResponseEntity.ok(convertToEntityModel(userResponseDto));
    }

    @GetMapping
    public ResponseEntity<EntityModel<UserResponseDto>> getUserByEmailAddress(
            @RequestParam @NotBlank String emailAddress
    ) {
        UserResponseDto userResponseDto = userService.getUserByEmailAddress(emailAddress);
        return ResponseEntity.ok(convertToEntityModel(userResponseDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<UserResponseDto>> updateUser(
            @PathVariable Long id, @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto userResponseDto = userService.updateUser(id, userRequestDto);
        return new ResponseEntity<>(convertToEntityModel(userResponseDto), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    private EntityModel<UserResponseDto> convertToEntityModel(UserResponseDto userResponseDto) {
        EntityModel<UserResponseDto> resources = EntityModel.of(userResponseDto);

        resources.add(
                linkTo(methodOn(UserController.class)
                        .getUserById(userResponseDto.getId()))
                        .withSelfRel(),
                linkTo(methodOn(UserController.class)
                        .getUserByEmailAddress(userResponseDto.getEmailAddress()))
                        .withRel("by-email-address"),
                linkTo(methodOn(UserController.class)
                        .updateUser(userResponseDto.getId(), null))
                        .withRel("update"),
                linkTo(methodOn(UserController.class)
                        .deleteUser(userResponseDto.getId()))
                        .withRel("delete")
        );
        return resources;
    }
}
