package com.dms.mapper.dto;

import com.dms.dto.UserDTO;
import com.dms.dto.UserRegisterDTO;
import com.dms.entity.User;

/**
 * The {@code UserDTOMapper} class is responsible for mapping between {@link User} entities and their corresponding DTOs ({@link UserDTO} and {@link UserRegisterDTO}).
 */
public class UserDTOMapper {

    /** Private constructor to prevent instantiation of this utility class. */
    private UserDTOMapper() {
    }

    /**
     * Maps a {@link User} entity to a {@link UserDTO} DTO.
     *
     * @param user the {@link User} entity to map
     * @return the mapped {@link UserDTO} DTO
     */
    public static UserDTO mapToUserDTO(User user) {
        return UserDTO.builder()
                      .userId(user.getUserId())
                      .name(user.getName())
                      .email(user.getEmail())
                      .role(user.getRole().name())
                      .build();
    }

    /**
     * Maps a {@link UserDTO} DTO to a {@link User} entity.
     *
     * @param user the {@link UserDTO} DTO to map
     * @return the mapped {@link User} entity
     */
    public static User mapToUser(UserDTO user) {
        return User.builder()
                   .userId(user.getUserId())
                   .name(user.getName())
                   .email(user.getEmail())
                   .build();
    }

    /**
     * Maps a {@link UserRegisterDTO} DTO to a {@link User} entity.
     *
     * @param userRegister the {@link UserRegisterDTO} DTO to map
     * @return the mapped {@link User} entity
     */
    public static User mapToUser(UserRegisterDTO userRegister) {
        return User.builder()
                   .name(userRegister.getName())
                   .email(userRegister.getEmail())
                   .password(userRegister.getPassword())
                   .build();
    }

}
