package com.dms.unit.mapper.dto;

import com.dms.dto.UserDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.mapper.dto.UserDTOMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserDTOMapperTest {

    @Test
    void shouldReturnUserDTO() {
        User user = User.builder()
                        .userId("195d2cf3-e3fd-4e15-80dd-15e43e63b25b")
                        .name("james")
                        .email("james@gmail.com")
                        .role(Role.USER)
                        .build();

        UserDTO userDTO = UserDTOMapper.mapToUserDTO(user);

        assertThat(userDTO.getUserId()).isEqualTo(user.getUserId());
        assertThat(userDTO.getName()).isEqualTo(user.getName());
        assertThat(userDTO.getEmail()).isEqualTo(user.getEmail());
        assertThat(userDTO.getRole()).isEqualTo(user.getRole().name());
    }

    @Test
    void shouldReturnUserWhenUserDTOIsValid() {
        UserDTO userDTO = UserDTO.builder()
                                 .userId("195d2cf3-e3fd-4e15-80dd-15e43e63b25b")
                                 .name("james")
                                 .email("james@gmail.com")
                                 .build();

        User user = UserDTOMapper.mapToUser(userDTO);

        assertThat(user.getUserId()).isEqualTo(userDTO.getUserId());
        assertThat(user.getName()).isEqualTo(userDTO.getName());
        assertThat(user.getEmail()).isEqualTo(userDTO.getEmail());
    }

    @Test
    void shouldReturnUserWhenUserRegisterDTOIsValid() {
        UserRegisterDTO userRegisterDTO = UserRegisterDTO.builder()
                                                         .name("james")
                                                         .email("james@gmail.com")
                                                         .password("secret123!")
                                                         .build();

        User user = UserDTOMapper.mapToUser(userRegisterDTO);

        assertThat(user.getName()).isEqualTo(userRegisterDTO.getName());
        assertThat(user.getEmail()).isEqualTo(userRegisterDTO.getEmail());
        assertThat(user.getPassword()).isEqualTo(userRegisterDTO.getPassword());
    }

}