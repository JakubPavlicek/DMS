package com.dms.integration.controller;

import com.dms.config.AdminUserProperties;
import com.dms.entity.Role;
import com.dms.entity.User;
import com.dms.repository.UserRepository;
import com.dms.util.JwtManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminUserProperties adminUserProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user;

    private final SimpleGrantedAuthority userAuthority = new SimpleGrantedAuthority("ROLE_" + Role.USER.name());
    private final SimpleGrantedAuthority adminAuthority = new SimpleGrantedAuthority("ROLE_" + Role.ADMIN.name());

    @BeforeEach
    void setUp() {
        user = User.builder()
                   .userId("4e2af452-4d8c-45aa-9a13-9811b5c7b999")
                   .name("james")
                   .email("james@gmail.com")
                   .password(passwordEncoder.encode("secret123!"))
                   .role(Role.USER)
                   .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"name": "james",
                        	"email": "james@gmail.com",
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isCreated(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.userId").isNotEmpty(),
               jsonPath("$.name").value("james"),
               jsonPath("$.email").value("james@gmail.com")
           );
    }

    @Test
    void shouldNotCreateUserWhenNameIsNull() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("name: must not be null")
           );
    }

    @Test
    void shouldNotCreateUserWhenEmailIsNull() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james",
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("email: must not be null")
           );
    }

    @Test
    void shouldNotCreateUserWhenPasswordIsNull() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james",
                        	"email": "james@gmail.com"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("password: must not be null")
           );
    }

    @Test
    void shouldNotCreateUserWhenNoDataAreProvided() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{}"))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
           );
    }

    @Test
    void shouldNotCreateUserWhenNameContainsInvalidCharacters() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james!!!",
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value(containsString("name: must match"))
           );
    }

    @Test
    void shouldNotCreateUserWhenUsernameIsInvalid() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "a",
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value(containsString("name"))
           );
    }

    @Test
    void shouldNotCreateUserWhenEmailIsInvalid() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james",
                        	"email": "jamesgmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("email: must be a well-formed email address")
           );
    }

    @Test
    void shouldNotCreateUserWhenPasswordDoesNotContainSpecialCharacter() throws Exception {
        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james",
                        	"email": "james@gmail.com",
                        	"password": "secret123"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value(containsString("password: must match"))
           );
    }

    @Test
    void shouldNotCreateUserWhenEmailIsAlreadyTaken() throws Exception {
        userRepository.save(user);

        mvc.perform(post("/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                            "name": "james",
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isConflict(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON)
           );
    }

    @Test
    void shouldReturnCurrentUser() throws Exception {
        userRepository.save(user);

        mvc.perform(get("/users/me")
               .with(jwt().authorities(userAuthority).jwt(JwtManager.createJwt(user.getEmail()))))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.userId").isNotEmpty(),
               jsonPath("$.name").value("james"),
               jsonPath("$.email").value("james@gmail.com")
           );
    }

    @Test
    void shouldNotReturnCurrentUserWhenUserIsNotFound() throws Exception {
        mvc.perform(get("/users/me")
               .with(jwt().authorities(userAuthority).jwt(JwtManager.createJwt(user.getEmail()))))
           .andExpectAll(
               status().isNotFound(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value(containsString("not found"))
           );
    }

    @Test
    void shouldChangePassword() throws Exception {
        userRepository.save(user);

        mvc.perform(put("/users/password")
               .with(jwt().authorities(adminAuthority).jwt(JwtManager.createJwt(adminUserProperties.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotChangePasswordWhenEmailIsNull() throws Exception {
        mvc.perform(put("/users/password")
               .with(jwt().authorities(adminAuthority).jwt(JwtManager.createJwt(adminUserProperties.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("email: must not be null")
           );
    }

    @Test
    void shouldNotChangePasswordWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(put("/users/password")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpect(status().isUnauthorized());
    }


    @Test
    void shouldNotChangePasswordWhenUserIsNotAuthorized() throws Exception {
        mvc.perform(get("/users/password")
               .with(jwt().authorities(userAuthority).jwt(JwtManager.createJwt(user.getEmail()))))
           .andExpect(status().isForbidden());
    }

    @Test
    void shouldChangeLogLevel() throws Exception {
        mvc.perform(post("/actuator/loggers/com.dms")
               .with(jwt().authorities(adminAuthority).jwt(JwtManager.createJwt(adminUserProperties.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                           "configuredLevel": "INFO"
                        }
                        """))
           .andExpect(status().isNoContent());
    }

    @Test
    void shouldNotChangeLogLevelWhenUserIsNotAuthenticated() throws Exception {
        mvc.perform(post("/actuator/loggers/com.dms")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                           "configuredLevel": "INFO"
                        }
                        """))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldNotChangeLogLevelWhenUserIsNotAuthorized() throws Exception {
        mvc.perform(post("/actuator/loggers/com.dms")
               .with(jwt().authorities(userAuthority).jwt(JwtManager.createJwt(user.getEmail())))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                           "configuredLevel": "INFO"
                        }
                        """))
           .andExpect(status().isForbidden());
    }

}