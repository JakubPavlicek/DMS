package com.dms.mapper.dto;

import com.dms.dto.UserDTO;
import com.dms.entity.User;

public class UserDTOMapper {

    public static UserDTO map(User user) {
        return UserDTO.builder()
                      .username(user.getUsername())
                      .email(user.getEmail())
                      .build();
    }

}
