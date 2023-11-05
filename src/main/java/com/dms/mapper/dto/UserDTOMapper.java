package com.dms.mapper.dto;

import com.dms.dto.UserDTO;
import com.dms.entity.User;

public class UserDTOMapper {

    public static UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                      .username(user.getUsername())
                      .email(user.getEmail())
                      .build();
    }

    public static User mapToUser(UserDTO user) {
        return User.builder()
                   .username(user.getUsername())
                   .email(user.getEmail())
                   .build();
    }

}
