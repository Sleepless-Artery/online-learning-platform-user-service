package org.sleepless_artery.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.hateoas.RepresentationModel;

import java.io.Serial;
import java.io.Serializable;


@Getter
@AllArgsConstructor
public class UserResponseDto extends RepresentationModel<UserResponseDto> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String emailAddress;
    private String username;
    private String information;
}
