package com.dms.integration.controller;

import com.dms.entity.User;
import com.dms.service.UserService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                   .name("james")
                   .email("james@gmail.com")
                   .password("secret123!")
                   .build();
    }

    @Test
    void shouldReturnToken() throws Exception {
        userService.createUser(user);

        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                            "password": "secret123!"
                        }
                        """))
           .andExpectAll(
               status().isOk(),
               content().contentType(MediaType.APPLICATION_JSON),
               jsonPath("$.token").isNotEmpty()
           );
    }

    @Test
    void shouldNotReturnTokenWhenCredentialsDoesNotMatch() throws Exception {
        userService.createUser(user);

        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
                        	"email": "james@gmail.com",
                            "password": "secret12345!"
                        }
                        """))
           .andExpectAll(
               status().isBadRequest(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.detail").value("Bad credentials")
           );
    }

    @Test
    void shouldNotReturnTokenWhenEmailIsNotProvided() throws Exception {
        mvc.perform(post("/oauth2/token")
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
    void shouldNotReturnTokenWhenPasswordIsNotProvided() throws Exception {
        mvc.perform(post("/oauth2/token")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                        {
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
    void shouldNotReturnTokenWhenRequestLoginDoesNotContainAnyData() throws Exception {
        mvc.perform(post("/oauth2/token"))
           .andExpectAll(
               status().isUnsupportedMediaType(),
               content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
               jsonPath("$.context_info.messages[0]").value("Request must contain data")
           );
    }

}