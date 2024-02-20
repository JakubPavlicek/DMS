package com.dms.integration.controller;

import com.dms.config.SecurityConfig;
import com.dms.controller.UserController;
import com.dms.entity.User;
import com.dms.exception.EmailAlreadyExistsException;
import com.dms.service.UserService;
import com.dms.util.KeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, KeyManager.class})
@AutoConfigureMockMvc
class UserControllerTest {

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .alwaysDo(print())
            .build();

        user = User.builder()
                   .userId("833a814e-da7c-4d64-8bea-57411e2e4f9d")
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();
    }

    @Test
    void shouldCreateUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

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
               jsonPath("$.userId").value("833a814e-da7c-4d64-8bea-57411e2e4f9d"),
               jsonPath("$.name").value("james"),
               jsonPath("$.email").value("james@gmail.com")
           );
    }

    @Test
    void shouldNotCreateUserWhenNameIsNull() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenReturn(user);

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
    void shouldNotCreateUserWhenEmailIsInvalid() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenReturn(user);

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
        when(userService.createUser(any(User.class))).thenThrow(EmailAlreadyExistsException.class);

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
        when(userService.getCurrentUser()).thenReturn(user);

        mvc.perform(get("/users/me")
               .with(jwt()))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.userId").value("833a814e-da7c-4d64-8bea-57411e2e4f9d"),
               jsonPath("$.name").value("james"),
               jsonPath("$.email").value("james@gmail.com")
           );
    }

    @Test
    void shouldChangePassword() throws Exception {
        mvc.perform(put("/users/password")
               .with(httpBasic("admin", "admin123"))
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
               .with(httpBasic("admin", "admin123"))
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
    void shouldNotChangePasswordWhenUnauthenticated() throws Exception {
        mvc.perform(put("/users/password")
               .with(httpBasic("name", "password"))
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                        	"password": "secret123!"
                        }
                        """))
           .andExpect(status().isUnauthorized());
    }

}