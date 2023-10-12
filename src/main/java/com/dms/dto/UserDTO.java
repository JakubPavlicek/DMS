package com.dms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long userId;

    @NotBlank(message = "Username is mandatory")
    @Size(max = 40, message = "Username must not exceed 40 characters")
    private String username;

    @NotBlank(message = "Email is mandatory")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Email(message = "Invalid email format")
    private String email;

}
