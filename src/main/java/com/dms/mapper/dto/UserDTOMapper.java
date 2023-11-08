package com.dms.mapper.dto;

import com.dms.dto.UserDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.User;

public class UserDTOMapper {

    public static UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                      .userId(user.getUserId())
                      .name(user.getName())
                      .email(user.getEmail())
                      .password("*******")
                      .build();
    }

    public static User mapToUser(UserDTO user) {
        return User.builder()
                   .userId(user.getUserId())
                   .name(user.getName())
                   .email(user.getEmail())
                   .password(user.getPassword())
                   .build();
    }

    public static User mapToUser(UserRegisterDTO userRegister) {
        return User.builder()
                   .name(userRegister.getName())
                   .email(userRegister.getEmail())
                   .password(userRegister.getPassword())
                   .build();
    }

}
